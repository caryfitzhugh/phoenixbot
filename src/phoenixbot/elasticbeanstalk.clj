(ns phoenixbot.elasticbeanstalk
  (:require [uswitch.lambada.core :refer [deflambdafn]]
            [clojure.data.json :as json]
            [amazonica.aws.elasticbeanstalk :as eb]
            [clojure.java.io :as io]
            [phoenixbot.config :as config]
            [phoenixbot.hipchat :as hc]
            [tentacles.repos :as repos]
            [phoenixbot.pivotal :as pivotal]
            ))

(comment
  (def event {"Records" [
                   {"EventSource" "aws:sns", "EventVersion" "1.0", "EventSubscriptionArn" "arn:aws:sns:us-east-1:774902671593:phoenixbot-elastic-beanstalk:2417f25a-0dc3-4eed-b4bf-e6c00662eb1c",
                    "Sns" {"SigningCertUrl" "https://sns.us-east-1.amazonaws.com/SimpleNotificationService-bb750dd426d95ee9390147a5624348ee.pem",
                           "UnsubscribeUrl" "https://sns.us-east-1.amazonaws.com/?Action=Unsubscribe&SubscriptionArn=arn:aws:sns:us-east-1:774902671593:phoenixbot-elastic-beanstalk:2417f25a-0dc3-4eed-b4bf-e6c00662eb1c", "MessageId" "570f2c36-7eed-533b-8332-6283fc6b79f0",
                           "Type" "Notification", "MessageAttributes" {},
                           "Signature" "fLot/zI0FQLPGazAKqtgDiaR9z26AzObsrqPAEwn9EJr3qkEjvexKq5wn07CfCsOg92i7Habu4EZSLQf+mJ0qomtAPQy4z6Nafu0GbGp9ZQrsjyJKTcVNhxVqcSAkcJLJR3WSSXaKrBQ1OlbP96bE2ZIPDunrNY1sWL75+cX6T+lqUF3cMDCoBJvnEgu7XW5SMT/iDwwm1x4N9RB2+bqRKLw4bmV5yLoDlheeJaUl8rUZ7xTXP7kEh3/8nGMNgiL6j2KQ/9KhUp+eKOVujfHd6RePcgbedS6vLhYTjGqv1xvRv48SgNPs5kERDWiyBYVm+5kw0n15HdaT4a18YO7zA==",
                           "Subject" "AWS Elastic Beanstalk Notification - New application version was deployed to running EC2 instances",
                           "Message" "Timestamp: Thu Nov 05 16:13:30 UTC 2015\nMessage: New application version was deployed to running EC2 instances.\n\nEnvironment: sindicati-p-stag\nApplication: sindicati-publish\n\nEnvironment URL: http://null\nRequestId: 132897e7-83d8-11e5-b867-9947d83e109f\nNotificationProcessId: 56cab754-7f04-48e3-889a-990977e062d1",
                           "TopicArn" "arn:aws:sns:us-east-1:774902671593:phoenixbot-elastic-beanstalk", "Timestamp" "2015-11-05T16:13:58.689Z",
                           "SignatureVersion" "1"}}]})
  (def commit {:sha "de71be3bd5d8ce1591ecdb2fd252d7874b11c34a",
               :commit {
                        :author {:name "script-deployer", :email "script-deployer@me.com", :date "2015-11-05T16:13:34Z"},
                        :committer {:name "script-deployer", :email "script-deployer@me.com", :date "2015-11-05T16:13:34Z"},
                        :message "Version 1.0.23-SNAPSHOT",
                        :tree {:sha "c00ae14bcf518e6450be9ee52338418e1393142f", :url "https://api.github.com/repos/Ziplist/sindicati-publish/git/trees/c00ae14bcf518e6450be9ee52338418e1393142f"},
                        :url "https://api.github.com/repos/Ziplist/sindicati-publish/git/commits/de71be3bd5d8ce1591ecdb2fd252d7874b11c34a",
                        :comment_count 0
                      },
               :url "https://api.github.com/repos/Ziplist/sindicati-publish/commits/de71be3bd5d8ce1591ecdb2fd252d7874b11c34a",
               :html_url "https://github.com/Ziplist/sindicati-publish/commit/de71be3bd5d8ce1591ecdb2fd252d7874b11c34a",
               :comments_url "https://api.github.com/repos/Ziplist/sindicati-publish/commits/de71be3bd5d8ce1591ecdb2fd252d7874b11c34a/comments",
               })

  (def application-version "1.0.22-20151105111006")
  (handle-event event)
  (handle-event {"Records" [ {"Sns" {"Message" "FOO"}}]})
  (get-current-application-version application environment)
  )

;; If there is a new deployment.
;;  We need to do a few things
;;
(defn get-current-application-version
  [application environment]
  (let [environments (:environments (eb/describe-environments :application-name application ))
        environment (first (filter (fn [env] (= environment (:environment-name env))) environments))]
    (:version-label environment)))

(defn get-commits-in-this-release
  "It uses knowledge of the release string in lein release, to look for the version that is currently running.
  It then walks down the history till it finds the next release commit message.
  The commits between those two are the ones that are released in this release."

  [application release-version]
  (let [
        {org-name :org
         repo-name :repo
         branch-name :branch :as repo-map}  (get config/application-repository-map application)]
    (if repo-map
      (let [ repo (repos/specific-repo org-name repo-name config/github-auth)
             commits (repos/commits org-name repo-name (merge {:sha branch-name :per-page 100} config/github-auth))
             ;; Now we see which commits in the last 100 are releases (based on their commit messages "Version #.#.#"
             release-commit-indexes (keep-indexed #(if (re-matches #"Version (\d+\.\d+\.\d+)" (:message (:commit %2))) %1 nil) commits)
             ;; Now find the currently deployed commit's index
             deployed-commit-index (first (keep-indexed (fn [index commit] (if (re-matches (re-pattern (str "Version " release-version)) (:message (:commit commit))) index nil)) commits))
             ;; Find the next release's index (just the next higher index than the deployed one)
             next-commit-index (some (fn [index] (when (> index deployed-commit-index) index)) release-commit-indexes)
             ;; Now we have all the commits in this release!
            ]
        (subvec (vec commits) deployed-commit-index next-commit-index))
       [])))

(defn handle-new-deployment
  [message]
  (let [new-deploy  (re-find #"(?ms)New application version was deployed" message)
        application (get (re-find #"(?ms)Application: ([a-zA-Z0-9-]+)" message) 1)
        environment (get (re-find #"(?ms)Environment: ([a-zA-Z0-9-]+)" message) 1)
       ]
    (if (and new-deploy environment application)
      (if-let [ application-version (get-current-application-version application environment)]
        (let [ release-version (get (re-matches #"(\d+\.\d+\.\d+)-(\d+)" application-version) 1)
               commits-in-this-release (get-commits-in-this-release application release-version)
               pivotal-stories (set (filter identity (flatten (map (fn [commit] (map  (fn [res] (get res 3)) ;;  Pull the issue # directly
                                                                        (re-seq #"\[((Fixes|Delivers)\w*)?#([0-9]+)\]" (:message (:commit commit)))
                                                                       )) commits-in-this-release))))
               labels-to-apply (get-in config/labels [application environment])
              ]
          (println "Application version: " application-version)
          (println "Commits in this release " (count commits-in-this-release))
          (println "Pivotal-stories in this release:" pivotal-stories)
          (println "Labels to apply: " labels-to-apply)

          (pivotal/apply-label-to-stories pivotal-stories labels-to-apply)
          (pivotal/deliver-stories pivotal-stories)
          (pivotal/comment-on-stories pivotal-stories (str "Deployed this story to " environment " inside " release-version))

          ;; Comment in hipchat
          (hc/report-deployment application environment application-version pivotal-stories)

          ;; label in github (on-prod, etc)


          true)))))

(defn ignore-message
  [message]
  (println "Ignoring message... " message))

(defn handle-event
  [event]
  (println "Processing: "  (pr-str event))
  (println "")
  (println "")
  (if-let [message (get-in event ["Records" 0 "Sns" "Message"])]
      (or
        (handle-new-deployment message)
        ;; ....
        (ignore-message message)
        ))
    {:status "ok"})

(deflambdafn phoenixbot.elasticbeanstalk.OnEventHandler
    [in out ctx]
      (let [event (json/read (io/reader in))
                    res (handle-event event)]
            (json/write res (io/writer out))))
