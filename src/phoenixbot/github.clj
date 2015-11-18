(ns phoenixbot.github
 (:require
    [clojure.data.json :as json]
    [clj-http.lite.client :as client]
    [phoenixbot.config :as config]
    [phoenixbot.pivotal-tracker :as pivotal]
    [phoenixbot.aws :as aws]
    ))

(def github-host "https://api.github.com")

(comment
  (get-commits "Ziplist" "sindicati-web" "master" 10)
  )

(defn get-commits
  [org-name repo-name sha limit]
  (clojure.walk/keywordize-keys
    (json/read-str
      (:body
        (clojure.walk/keywordize-keys
          (client/get (str github-host "/repos/" org-name "/" repo-name "/commits")
                               {:headers {"Authorization" (str "token " config/github-auth)
                                          "Accept"  "application/vnd.github.v3+json"}
                                :query-params {"page" 1 "per_page" limit}
                               }))))))

(defn index-of-release
  [release-version commits]
  (let [ release-commit-indexes (into {}
                                      (keep-indexed #(if-let [ver (re-matches #"Version (\d+\.\d+\.\d+)" (:message (:commit %2)))]
                                                        [(get ver 1) %1]
                                                        nil) commits))]
    (println release-commit-indexes)
    (println release-version)
    (get release-commit-indexes release-version)))

(defn get-commits-in-this-release
  "It uses knowledge of the release string in lein release, to look for the version that is currently running.
  It then walks down the history till it finds the next release commit message.
  The commits between those two are the ones that are released in this release."
  [application release-version]
  (let [
        {org-name :org
         repo-name :repo
         branch-name :branch :as repo-map}  (get config/application-repository-map application)
        ]

    (if repo-map
      (let [
             commits (get-commits org-name repo-name branch-name 500)
             debug (println "Commits: " (pr-str commits))
             debug (println "Commits: " (count commits))
             ;; Now find the currently deployed commit's index
             deployed-commit-index (index-of-release release-version commits)

             ;; Now we see which commits in the last 500 are releases based on their commit messages "Version #.#.#"
             release-commit-indexes (keep-indexed #(if (re-matches #"Version (\d+\.\d+\.\d+)" (:message (:commit %2))) %1 nil) commits)
             ;; Find the previous commit
             next-commit-index (some (fn [index] (when (> index deployed-commit-index) index)) release-commit-indexes)
            ]
        (if deployed-commit-index
          (subvec (vec commits) deployed-commit-index next-commit-index)
          (do
            (println "Could not find release in github history? " release-version)
            [])))
        (do
          (println "Could not find configured repository map")
          []))))
(comment
  (get-commits-on-staging-not-prod "sindicati-spugna")
  )

(defn get-commits-on-staging-not-prod
  "It uses knowledge of the release string in lein release, to look for the version that is currently running.
  It then walks down the history till it finds the next release commit message.
  The commits between those two are the ones that are released in this release."
  [application]
  (try
    (let [
        {org-name :org
         repo-name :repo
         branch-name :branch
         stag-env :stag
         prod-env :prod
         :as repo-map}  (get config/application-repository-map application)
        ]

    (if repo-map
      (let [commits (get-commits org-name repo-name branch-name 500)
            index-of-prod-release (index-of-release (aws/get-current-application-version application prod-env) commits)
            index-of-stag-release (index-of-release (aws/get-current-application-version application stag-env) commits)
        ]

          (subvec (vec commits) index-of-stag-release index-of-prod-release))
      (println "Could not find repo-map for application: " application)))
    (catch Exception e (do
                         (println "Failed to find commits on staging / prod?")
                         []))))
