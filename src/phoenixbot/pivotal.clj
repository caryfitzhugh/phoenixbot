(ns phoenixbot.pivotal
  (:require
    [uswitch.lambada.core :refer [deflambdafn]]
    [phoenixbot.config :as config]
    [clojure.walk]
    [clojure.java.io :as io]
    [clojure.data.json :as json]
    [clj-http.client :as client]
    ))
(comment
 (get-story 107407794)
 (add-labels 107407794 #{"test-label"})
  )

(defn- url
  [& rest]
  (apply str (concat ["https://www.pivotaltracker.com/services/v5"] rest)))

(defn get-story
  [story-id]
  (clojure.walk/keywordize-keys
    (json/read-str
      (:body
        (clojure.walk/keywordize-keys
          (client/get  (url "/stories/" story-id ) {:headers {"X-TrackerToken" config/pivotal-tracker-token}}))))))

(defn add-labels
  [story-id labels]
  (let [story (get-story story-id)
        ;;[{:id 12974898, :project_id 1243524, :kind label, :name stag-publish, :created_at 2015-10-06T20:27:38Z, :updated_at 2015-10-06T20:27:38Z}]
        existing-labels (set (map :name (:labels story)))
        new-labels (into existing-labels labels)
        json-payload (json/write-str {"labels" (map #(hash-map :name %1) new-labels)})
        ]
    (clojure.walk/keywordize-keys
      (json/read-str
        (:body
          (clojure.walk/keywordize-keys
            (client/put  (url "/stories/" story-id )
                        {:headers {"X-TrackerToken" config/pivotal-tracker-token}
                         :content-type :json
                         :body json-payload
                        })))))))
(defn deliver-story
  [story-id]
  (let [ json-payload (json/write-str {"current_state" "delivered"})]
    (clojure.walk/keywordize-keys
      (json/read-str
        (:body
          (clojure.walk/keywordize-keys
            (client/put  (url "/stories/" story-id )
                        {:headers {"X-TrackerToken" config/pivotal-tracker-token}
                         :content-type :json
                         :body json-payload
                        })))))))

(defn deliver-stories
  [pivotal-stories]
  (doseq [story-id pivotal-stories]
    (deliver-story story-id)))
(defn comment-on-story
  [story-id comment-text]

  )

(defn comment-on-stories
  [pivotal-stories comment-text]
  (doseq [story-id pivotal-stories]
    (comment-on-story story-id comment-text)))

(defn apply-label-to-stories
  [pivotal-stories labels]
  (println "Labelling stories: " pivotal-stories labels)
  (doseq [story-id pivotal-stories]
    (add-labels story-id labels)))
(comment
  (def event {"message" "Jeff Margolis finished this bug", "project" {"kind" "project", "id" 1243524, "name" "CN Digital Solutions"}, "project_version" 8035, "performed_by" {"kind" "person", "id" 453077, "name" "Jeff Margolis", "initials" "JM"}, "changes" [{"kind" "label", "change_type" "update", "id" 11871070, "original_values" {"counts" {"number_of_zero_point_stories_by_state" {"started" 1, "delivered" 0, "accepted" 108, "kind" "counts_by_story_state", "planned" 0, "unscheduled" 2, "unstarted" 2, "rejected" 0, "finished" 1}, "sum_of_story_estimates_by_state" {"started" 3, "delivered" 0, "accepted" 36, "kind" "counts_by_story_state", "planned" 0, "unscheduled" 0, "unstarted" 0, "rejected" 0, "finished" 8}, "number_of_stories_by_state" {"started" 3, "delivered" 0, "accepted" 131, "kind" "counts_by_story_state", "planned" 0, "unscheduled" 2, "unstarted" 2, "rejected" 0, "finished" 2}, "kind" "story_counts"}}, "new_values" {"counts" {"number_of_zero_point_stories_by_state" {"started" 0, "delivered" 0, "accepted" 108, "kind" "counts_by_story_state", "planned" 0, "unscheduled" 2, "unstarted" 2, "rejected" 0, "finished" 2}, "sum_of_story_estimates_by_state" {"started" 3, "delivered" 0, "accepted" 36, "kind" "counts_by_story_state", "planned" 0, "unscheduled" 0, "unstarted" 0, "rejected" 0, "finished" 8}, "number_of_stories_by_state" {"started" 2, "delivered" 0, "accepted" 131, "kind" "counts_by_story_state", "planned" 0, "unscheduled" 2, "unstarted" 2, "rejected" 0, "finished" 3}, "kind" "story_counts"}}, "name" "scraper"} {"kind" "story", "change_type" "update", "id" 107757274, "original_values" {"current_state" "started", "updated_at" 1447105976000}, "new_values" {"current_state" "finished", "updated_at" 1447105979000}, "name" "Bon Appetit scraper - Body text partially scraping", "story_type" "bug"}], "kind" "story_update_activity", "primary_resources" [{"kind" "story", "id" 107757274, "name" "Bon Appetit scraper - Body text partially scraping", "story_type" "bug", "url" "https://www.pivotaltracker.com/story/show/107757274"}], "highlight" "finished", "guid" "1243524_8035", "occurred_at" 1447105979000})

  )
(defn handle-story-change-event
  [event]

  (println "Story change event: " (pr-str event))
    {:status "ok"}
  )

(deflambdafn phoenixbot.pivotal.OnStoryChange
    [in out ctx]
      (let [event (json/read (io/reader in))
                    res (handle-story-change-event event)]
            (json/write res (io/writer out))))
