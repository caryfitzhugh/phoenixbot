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
  (def accepted-chore {"message" "Cary Fitzhugh accepted this chore", "project" {"kind" "project", "id" 1243524, "name" "CN Digital Solutions"}, "project_version" 8140, "performed_by" {"kind" "person", "id" 120053, "name" "Cary Fitzhugh", "initials" "CF"}, "changes" [{"kind" "story", "change_type" "update", "id" 107923330, "original_values" {"current_state" "started", "accepted_at" nil, "updated_at" 1447258611000, "before_id" 98744630, "after_id" 107741682}, "new_values" {"current_state" "accepted", "accepted_at" 1447258614000, "updated_at" 1447258614000, "before_id" 106971928, "after_id" 107920676}, "name" "test ", "story_type" "chore"}], "kind" "story_update_activity", "primary_resources" [{"kind" "story", "id" 107923330, "name" "test ", "story_type" "chore", "url" "https://www.pivotaltracker.com/story/show/107923330"}], "highlight" "accepted", "guid" "1243524_8140", "occurred_at" 1447258614000})
  (def accepted-feature {"message" "Cary Fitzhugh accepted this feature", "project" {"kind" "project", "id" 1243524, "name" "CN Digital Solutions"}, "project_version" 8130, "performed_by" {"kind" "person", "id" 120053, "name" "Cary Fitzhugh", "initials" "CF"}, "changes" [{"kind" "story", "change_type" "update",
                                                                                                                                                                                                                                                                                 "id" 107923330,
                                                                                                                                                                                                                                                                                 "original_values" {"current_state" "delivered", "accepted_at" nil, "updated_at" 1447258579000, "before_id" 98744630, "after_id" 107741682},
                                                                                                                                                                                                                                                                                 "new_values" {"current_state" "accepted", "accepted_at" 1447258581000, "updated_at" 1447258581000, "before_id" 106971928, "after_id" 107920676},
                                                                                                                                                                                                                                                                                 "name" "test ",
                                                                                                                                                                                                                                                                                 "story_type" "feature"}], "kind" "story_update_activity", "primary_resources" [{"kind" "story", "id" 107923330, "name" "test ", "story_type" "feature", "url" "https://www.pivotaltracker.com/story/show/107923330"}], "highlight" "accepted", "guid" "1243524_8130", "occurred_at" 1447258582000})
  (def delivered-feature {"message" "Cary Fitzhugh delivered this feature", "project" {"kind" "project", "id" 1243524, "name" "CN Digital Solutions"}, "project_version" 8129, "performed_by" {"kind" "person", "id" 120053, "name" "Cary Fitzhugh", "initials" "CF"}, "changes" [{"kind" "story", "change_type" "update", "id" 107923330, "original_values" {"current_state" "finished", "updated_at" 1447258576000}, "new_values" {"current_state" "delivered", "updated_at" 1447258579000}, "name" "test ", "story_type" "feature"}], "kind" "story_update_activity", "primary_resources" [{"kind" "story", "id" 107923330, "name" "test ", "story_type" "feature", "url" "https://www.pivotaltracker.com/story/show/107923330"}], "highlight" "delivered", "guid" "1243524_8129", "occurred_at" 1447258579000})
  (def finished-bug {"message" "Cary Fitzhugh finished this bug", "project" {"kind" "project", "id" 1243524, "name" "CN Digital Solutions"}, "project_version" 8144, "performed_by" {"kind" "person", "id" 120053, "name" "Cary Fitzhugh", "initials" "CF"}, "changes" [{"kind" "story", "change_type" "update", "id" 107923330, "original_values" {"current_state" "started", "updated_at" 1447258895000}, "new_values" {"current_state" "finished", "updated_at" 1447258900000}, "name" "test ", "story_type" "bug"}], "kind" "story_update_activity", "primary_resources" [{"kind" "story", "id" 107923330, "name" "test ", "story_type" "bug", "url" "https://www.pivotaltracker.com/story/show/107923330"}], "highlight" "finished", "guid" "1243524_8144", "occurred_at" 1447258901000})

  (def accepted {"message" "Cary Fitzhugh accepted this feature", "project" {"kind" "project", "id" 1243524, "name" "CN Digital Solutions"}, "project_version" 8130, "performed_by" {"kind" "person", "id" 120053, "name" "Cary Fitzhugh", "initials" "CF"}, "changes" [{"kind" "story", "change_type" "update", "id" 107923330, "original_values" {"current_state" "delivered", "accepted_at" nil, "updated_at" 1447258579000, "before_id" 98744630, "after_id" 107741682}, "new_values" {"current_state" "accepted", "accepted_at" 1447258581000, "updated_at" 1447258581000, "before_id" 106971928, "after_id" 107920676}, "name" "test ", "story_type" "feature"}], "kind" "story_update_activity", "primary_resources" [{"kind" "story", "id" 107923330, "name" "test ", "story_type" "feature", "url" "https://www.pivotaltracker.com/story/show/107923330"}], "highlight" "accepted", "guid" "1243524_8130", "occurred_at" 1447258582000})
  (def accepted-chore {"message" "Cary Fitzhugh accepted this chore", "project" {"kind" "project", "id" 1243524, "name" "CN Digital Solutions"}, "project_version" 8136, "performed_by" {"kind" "person", "id" 120053, "name" "Cary Fitzhugh", "initials" "CF"}, "changes" [{"kind" "story", "change_type" "update", "id" 107923330, "original_values" {"current_state" "delivered", "accepted_at" nil, "updated_at" 1447258598000, "before_id" 98744630, "after_id" 107741682}, "new_values" {"current_state" "accepted", "accepted_at" 1447258601000, "updated_at" 1447258601000, "before_id" 106971928, "after_id" 107920676}, "name" "test ", "story_type" "chore"}], "kind" "story_update_activity", "primary_resources" [{"kind" "story", "id" 107923330, "name" "test ", "story_type" "chore", "url" "https://www.pivotaltracker.com/story/show/107923330"}], "highlight" "accepted", "guid" "1243524_8136", "occurred_at" 1447258601000})
  (def label-change-event {"message" "Cary Fitzhugh edited this bug", "project" {"kind" "project", "id" 1243524, "name" "CN Digital Solutions"},
                           "project_version" 8125, "performed_by" {"kind" "person", "id" 120053, "name" "Cary Fitzhugh", "initials" "CF"},
                           "changes" [{"kind" "label", "change_type" "update", "id" 13296620, "original_values" {"counts" {"number_of_zero_point_stories_by_state" {"started" 0, "delivered" 0, "accepted" 0, "kind" "counts_by_story_state", "planned" 0, "unscheduled" 0, "unstarted" 0, "rejected" 0, "finished" 0}, "sum_of_story_estimates_by_state" {"started" 0, "delivered" 0, "accepted" 0, "kind" "counts_by_story_state", "planned" 0, "unscheduled" 0, "unstarted" 0, "rejected" 0, "finished" 0}, "number_of_stories_by_state" {"started" 0, "delivered" 0, "accepted" 0, "kind" "counts_by_story_state", "planned" 0, "unscheduled" 0, "unstarted" 0, "rejected" 0, "finished" 0}, "kind" "story_counts"}},
                                                                                              "new_values" {"counts" {"number_of_zero_point_stories_by_state" {"started" 0, "delivered" 0, "accepted" 1, "kind" "counts_by_story_state", "planned" 0, "unscheduled" 0, "unstarted" 0, "rejected" 0, "finished" 0}, "sum_of_story_estimates_by_state" {"started" 0, "delivered" 0, "accepted" 0, "kind" "counts_by_story_state", "planned" 0, "unscheduled" 0, "unstarted" 0, "rejected" 0, "finished" 0},
                                                                                                                      "number_of_stories_by_state" {"started" 0, "delivered" 0, "accepted" 1, "kind" "counts_by_story_state", "planned" 0, "unscheduled" 0, "unstarted" 0, "rejected" 0, "finished" 0}, "kind" "story_counts"}},
                                                                                              "name" "publish-prod"}
                                      {"kind" "story", "change_type" "update", "id" 107407794, "original_values" {"label_ids" [13259304], "updated_at" 1447258090000, "labels" ["publish-stag"]}, "new_values" {"label_ids" [13296620 13259304], "updated_at" 1447258104000, "labels" ["publish-prod" "publish-stag"]}, "name" "Flipboard Feed - AD slide body text issue", "story_type" "bug"}],
                           "kind" "story_update_activity",
                           "primary_resources" [{"kind" "story", "id" 107407794, "name" "Flipboard Feed - AD slide body text issue", "story_type" "bug", "url" "https://www.pivotaltracker.com/story/show/107407794"}],
                           "highlight" "edited",
                           "guid" "1243524_8125",
                           "occurred_at" 1447258104000})
  (handle-story-change-event accepted-chore)
  )
(defn handle-accepted-stories
  [accepted-story-changes event]
  (println "Accepted stories: " (pr-str accepted-story-changes))
  )

(defn handle-finished-stories
  [finished-story-changes event]
  (println "Finished stories: " (pr-str finished-story-changes))
  )

(defn handle-story-change-event
  [event]
  ;; We want to hande a few situations
  ;; *  When a story is accepted, we want to see if we can push to prod with what is on staging.
  ;; *  When a story is finished, we want to alert that we should release to staging
  (let [changes (get event "changes")
        story-changes (filter (fn [ch] (and (= "update" (get ch "change_type"))
                                            (= "story"  (get ch "kind")))) changes)
        accepted-story-changes (filter (fn [ch]
                                  (and (not (= "accepted" (get-in ch ["original_values" "current_state"])))
                                            (= "accepted" (get-in ch ["new_values" "current_state"])))) story-changes)
        finished-story-changes (filter (fn [ch]
                                  (and (not (= "finished" (get-in ch ["original_values" "current_state"])))
                                            (= "finished" (get-in ch ["new_values" "current_state"])))) story-changes)]
  (if (not (empty? accepted-story-changes))
    (handle-accepted-stories accepted-story-changes event))
  (if (not (empty? finished-story-changes))
    (handle-finished-stories finished-story-changes event))
  {:status "ok"}
  ))

(deflambdafn phoenixbot.pivotal.OnStoryChange
    [in out ctx]
      (let [event (json/read (io/reader in))
                    res (handle-story-change-event event)]
            (json/write res (io/writer out))))
