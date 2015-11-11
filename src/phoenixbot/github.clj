(ns phoenixbot.github
 (:require
    [clojure.data.json :as json]
    [clj-http.lite.client :as client]
    [phoenixbot.config :as config]
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
      (let [
             commits (get-commits org-name repo-name branch-name 500)
             ;; Now we see which commits in the last 100 are releases based on their commit messages "Version #.#.#"
             release-commit-indexes (keep-indexed #(if (re-matches #"Version (\d+\.\d+\.\d+)" (:message (:commit %2))) %1 nil) commits)
             ;; Now find the currently deployed commit's index
             v (println release-version)
             v (println "Commits" (:message (:commit (first commits))))
             deployed-commit-index (first (keep-indexed (fn [index commit] (if (re-matches (re-pattern (str "Version " release-version)) (:message (:commit commit))) index nil)) commits))
            ]
        (if deployed-commit-index
          (let [
                 ;; Find the next release's index (just the next higher index than the deployed one)
                 next-commit-index (some (fn [index] (when (> index deployed-commit-index) index)) release-commit-indexes)]
                ;; Now we have all the commits in this release!
              (subvec (vec commits) deployed-commit-index next-commit-index))
          (do
            (println "Could not find release in github history? " release-version)
            [])))
        (do
          (println "Could not find configured repository map")
          []))))
