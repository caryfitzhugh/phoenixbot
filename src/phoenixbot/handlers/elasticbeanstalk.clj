(ns phoenixbot.handlers.elasticbeanstalk
  (:require [uswitch.lambada.core :refer [deflambdafn]]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [phoenixbot.aws :as aws]
            [phoenixbot.config :as config]
            [phoenixbot.github :as github]
            [phoenixbot.hipchat :as hipchat]
            [phoenixbot.pivotal-tracker :as pivotal]
            ))

(comment
  (def event {"Records" [{"EventSource" "aws:sns", "EventVersion" "1.0", "EventSubscriptionArn" "arn:aws:sns:us-east-1:774902671593:phoenixbot-elastic-beanstalk:2417f25a-0dc3-4eed-b4bf-e6c00662eb1c", "Sns" {"SigningCertUrl" "https://sns.us-east-1.amazonaws.com/SimpleNotificationService-bb750dd426d95ee9390147a5624348ee.pem", "UnsubscribeUrl" "https://sns.us-east-1.amazonaws.com/?Action=Unsubscribe&SubscriptionArn=arn:aws:sns:us-east-1:774902671593:phoenixbot-elastic-beanstalk:2417f25a-0dc3-4eed-b4bf-e6c00662eb1c", "MessageId" "72ec0c31-1185-51ff-b394-42f1092acdf1", "Type" "Notification", "MessageAttributes" {}, "Signature" "Ru5PpYMIl7IMbGOIDuyU4qHeX9xE75DMdIUMRhuyPvIbNO9ATjzVqC1KhjLsIoWYXMBX+rWbpga6iO8rQrx7Uf/eMNutpAPUguGYMnEtQnN6IEmFwECXzLA2KdFKBR/eDNKcER2b3mq/Jlv6P2IB455Xbmz0KcKZze5B4DVZSXFbJuHG0fvTzh9t1GvTqJ/junBZieNDWg0MlALnn/ei5Kh5OPKMvYYwWwuv51Gg7EoCD7JAXq4qpCsbvuWF4VgVXv0ZJGGd9h8iYlI/tNDMrs1pm3ufc4w5wITx9uW4jqVGK1uyB9jmmdviBHW75sAxHKMYtJwGvoyuk3SGbLh7CA==", "Subject" "AWS Elastic Beanstalk Notification - New application version was deployed to running EC2 instances", "Message" "Timestamp: Fri Nov 13 16:56:45 UTC 2015\nMessage: New application version was deployed to running EC2 instances.\n\nEnvironment: sindicati-w-stag\nApplication: sindicati-web\n\nEnvironment URL: http://sindicati-w-stag.elasticbeanstalk.com\nRequestId: 79144ffd-8a27-11e5-b947-b77766ad55bf\nNotificationProcessId: 23f88f8d-854a-4f19-90de-b4b6b0e6c2af", "TopicArn" "arn:aws:sns:us-east-1:774902671593:phoenixbot-elastic-beanstalk", "Timestamp" "2015-11-13T16:57:19.537Z", "SignatureVersion" "1"}}]})
  (def event {"Records" [{"EventSource" "aws:sns", "EventVersion" "1.0", "EventSubscriptionArn" "arn:aws:sns:us-east-1:774902671593:phoenixbot-elastic-beanstalk:2417f25a-0dc3-4eed-b4bf-e6c00662eb1c", "Sns" {"SigningCertUrl" "https://sns.us-east-1.amazonaws.com/SimpleNotificationService-bb750dd426d95ee9390147a5624348ee.pem", "UnsubscribeUrl" "https://sns.us-east-1.amazonaws.com/?Action=Unsubscribe&SubscriptionArn=arn:aws:sns:us-east-1:774902671593:phoenixbot-elastic-beanstalk:2417f25a-0dc3-4eed-b4bf-e6c00662eb1c", "MessageId" "7da3131f-8903-5b1e-bf0d-5da4990849b5", "Type" "Notification", "MessageAttributes" {}, "Signature" "cMN6hRz4XPUztJ3YVPn+RJwy2SzTp42Hje+qbDu5n0KrXOLWbdXwlP6VJ4R8z5Ebf0dLi/d+rPzqx0QXUQ59VA6nVThest9qVUyNkIwQQmGOjdmDO15Tn27U3PS1y02PPOWKrPle/Lw6NKerd+KR2+3zjzLA+g+TFOm5fCZ7a8adlXdgIErMag4Dek4QiBUmTq+DYu1dlSCH1Rpdhh0o7caMjbKWlva1WfbLStmvc7AapDC/9PB4JdsNI3RFj+j5nU36ukyvYZ1nxDysaYQHLYHDdnuiV2WTVai7XKT0tsS9dq6YRZNf91mL6i5Mv9kWsZmcRP9RYAxfcv9efy3Ssw==", "Subject" "AWS Elastic Beanstalk Notification - New application version was deployed to running EC2 instances", "Message" "Timestamp: Mon Nov 09 21:18:09 UTC 2015\nMessage: New application version was deployed to running EC2 instances.\n\nEnvironment: dowser-c-stag\nApplication: dowser-categorizer\n\nEnvironment URL: http://null\nRequestId: 39d6a2ca-8727-11e5-ae80-5d482c947646\nNotificationProcessId: cc6e6271-5bd0-4e9d-90ec-31282b8b237a", "TopicArn" "arn:aws:sns:us-east-1:774902671593:phoenixbot-elastic-beanstalk", "Timestamp" "2015-11-09T21:19:07.489Z", "SignatureVersion" "1"}}]})
  (def event {"Records" [{"EventSource" "aws:sns", "EventVersion" "1.0", "EventSubscriptionArn" "arn:aws:sns:us-east-1:774902671593:phoenixbot-elastic-beanstalk:2417f25a-0dc3-4eed-b4bf-e6c00662eb1c", "Sns" {"SigningCertUrl" "https://sns.us-east-1.amazonaws.com/SimpleNotificationService-bb750dd426d95ee9390147a5624348ee.pem", "UnsubscribeUrl" "https://sns.us-east-1.amazonaws.com/?Action=Unsubscribe&SubscriptionArn=arn:aws:sns:us-east-1:774902671593:phoenixbot-elastic-beanstalk:2417f25a-0dc3-4eed-b4bf-e6c00662eb1c", "MessageId" "52034243-b522-5108-af81-0cf9615e3c73", "Type" "Notification", "MessageAttributes" {}, "Signature" "dns9n8KsyRoVAiOxcRf4pUKzN1CLpbUyNsiPUz6hhwBYFw9/8dTBf02JjLQf+p0zkUQ2bkhF8/hgvzvnHliN6Rz9Z2IoRtmncgvS1dwNYLFKsvVtm4S5/68ynQ3IQ22ovINkJrgw7lT5l3KAy9N2dsMPOkXhCKfI+eUMgLkrEXqzaUtnMGYHbb0yh1w6tet9A7Fb5VNQbpRmFq1cW9PkkP0SvRoP6GhG07asyV26ixDa2Bue1TzyApNAJMi5t7xPHukTad+ckGf6IeLnMnu2MGZSdp30Qj2c0E+zj9KjAXG+QXC2RHk4jdbOycdRaGYno/OsX+RnxRzHpB8c/iU/Jg==", "Subject" "AWS Elastic Beanstalk Notification - New application version was deployed to running EC2 instances", "Message" "Timestamp: Mon Nov 09 17:04:49 UTC 2015\nMessage: New application version was deployed to running EC2 instances.\n\nEnvironment: sindicati-p-stag\nApplication: sindicati-publish\n\nEnvironment URL: http://null\nRequestId: eb29929e-8703-11e5-8498-037d9460e29b\nNotificationProcessId: 147ed550-4c5c-4d61-80a4-30858c236c8a", "TopicArn" "arn:aws:sns:us-east-1:774902671593:phoenixbot-elastic-beanstalk", "Timestamp" "2015-11-09T17:05:00.766Z", "SignatureVersion" "1"}}]} )
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
  (def msg  "Bumped version of validations\n[Fixes #107700366]")
  (re-seq #"\[((Delivers|Fixes)( )+)?#([0-9]+)\]" (clojure.string/join msg))
  (handle-event event)
  (def application-version "1.0.22-20151105111006")
  (handle-event {"Records" [ {"Sns" {"Message" "FOO"}}]})
  (aws/get-current-application-version application environment)
  (handle-new-deployment "auth-service" "auth-s-stag")

  (save-previous-deployed-info "test-env" {:version "1.2.1" :ref "ddeddeddefasefasefasefasef4"})
  (get-previous-deployed-info "test-env")
  (get-previous-deployed-info "test-env2")
  )

;;  We need to do a few things
;;
(defn handle-new-deployment
  [application environment]
      (if-let [ release-version (aws/get-current-application-version application environment)]
        (let [ commits-in-this-release (github/get-commits-in-this-release application release-version)
               pivotal-stories (pivotal/pivotal-stories-in-commit-range commits-in-this-release)
               labels-to-apply (get config/labels environment)
              ]

          (println "Release version: " release-version)
          (println "Commits in this release " (count commits-in-this-release)
                   (clojure.string/join ", " (map :sha (map :tree (map :commit commits-in-this-release)))))
          (println "Pivotal-stories in this release:" pivotal-stories)
          (println "Labels to apply: " labels-to-apply)

          ;; label in pivotal (on-prod, etc)
          (pivotal/apply-label-to-stories pivotal-stories labels-to-apply)
          (pivotal/deliver-stories pivotal-stories)
          (pivotal/comment-on-stories pivotal-stories (str "Deployed this story to " environment " inside " release-version))

          ;; Comment in hipchat
          (hipchat/report-deployment application environment release-version pivotal-stories)

          ;; Comment on status btwn the staging deploy and the prod
          (if (some #(= environment %) (map :stag (vals config/application-repository-map)))
            (let [commit-diff (github/get-commits-on-staging-not-prod application)
                  pivotal-task-ids (pivotal/pivotal-stories-in-commit-range commit-diff)
                  pivotal-stories (map pivotal/get-story pivotal-task-ids)]
              (hipchat/report-staging-state application pivotal-stories)))

          true)
        (println "Could not find app version")
        ))

(defn parse-new-deployment
  [message]
  (let [new-deploy  (re-find #"(?ms)New application version was deployed" message)
        application (get (re-find #"(?ms)Application: ([a-zA-Z0-9-]+)" message) 1)
        environment (get (re-find #"(?ms)Environment: ([a-zA-Z0-9-]+)" message) 1)
       ]
    (println "New deployment: " new-deploy)
    (println "App: " application )
    (println "Env: " environment)

    (if (and new-deploy environment application)
        (handle-new-deployment application environment)
        (println "Not new deployment message..."))))

(defn ignore-message
  [message]
  (println "Ignoring message... " message)
  {:status "ok"})

(defn handle-event
  [event]
  (println "Processing: "  (pr-str event))
  (println "")

  (if-let [message (get-in event ["Records" 0 "Sns" "Message"])]
    (parse-new-deployment message)))

(deflambdafn phoenixbot.handlers.elasticbeanstalk.OnEventHandler
    [in out ctx]
      (let [event (json/read (io/reader in))
                    res (handle-event event)]
            (json/write res (io/writer out))))
