(ns phoenixbot.handlers.pivotal
  (:require
    [uswitch.lambada.core :refer [deflambdafn]]
    [clojure.data.json :as json]
    [phoenixbot.config :as config]
    [clojure.java.io :as io]
    [phoenixbot.github :as github]
    [phoenixbot.hipchat :as hipchat]
    [phoenixbot.pivotal-tracker :as pivotal]
    ))

(comment
  (def event {"message" "Jeff Margolis finished this bug", "project" {"kind" "project", "id" 1243524, "name" "CN Digital Solutions"}, "project_version" 8035, "performed_by" {"kind" "person", "id" 453077, "name" "Jeff Margolis", "initials" "JM"}, "changes" [{"kind" "label", "change_type" "update", "id" 11871070, "original_values" {"counts" {"number_of_zero_point_stories_by_state" {"started" 1, "delivered" 0, "accepted" 108, "kind" "counts_by_story_state", "planned" 0, "unscheduled" 2, "unstarted" 2, "rejected" 0, "finished" 1}, "sum_of_story_estimates_by_state" {"started" 3, "delivered" 0, "accepted" 36, "kind" "counts_by_story_state", "planned" 0, "unscheduled" 0, "unstarted" 0, "rejected" 0, "finished" 8}, "number_of_stories_by_state" {"started" 3, "delivered" 0, "accepted" 131, "kind" "counts_by_story_state", "planned" 0, "unscheduled" 2, "unstarted" 2, "rejected" 0, "finished" 2}, "kind" "story_counts"}}, "new_values" {"counts" {"number_of_zero_point_stories_by_state" {"started" 0, "delivered" 0, "accepted" 108, "kind" "counts_by_story_state", "planned" 0, "unscheduled" 2, "unstarted" 2, "rejected" 0, "finished" 2}, "sum_of_story_estimates_by_state" {"started" 3, "delivered" 0, "accepted" 36, "kind" "counts_by_story_state", "planned" 0, "unscheduled" 0, "unstarted" 0, "rejected" 0, "finished" 8}, "number_of_stories_by_state" {"started" 2, "delivered" 0, "accepted" 131, "kind" "counts_by_story_state", "planned" 0, "unscheduled" 2, "unstarted" 2, "rejected" 0, "finished" 3}, "kind" "story_counts"}}, "name" "scraper"} {"kind" "story", "change_type" "update", "id" 107757274, "original_values" {"current_state" "started", "updated_at" 1447105976000}, "new_values" {"current_state" "finished", "updated_at" 1447105979000}, "name" "Bon Appetit scraper - Body text partially scraping", "story_type" "bug"}], "kind" "story_update_activity", "primary_resources" [{"kind" "story", "id" 107757274, "name" "Bon Appetit scraper - Body text partially scraping", "story_type" "bug", "url" "https://www.pivotaltracker.com/story/show/107757274"}], "highlight" "finished", "guid" "1243524_8035", "occurred_at" 1447105979000})
  (handle-story-change-event {"message" "Cary Fitzhugh accepted this chore", "project" {"kind" "project", "id" 1243524, "name" "CN Digital Solutions"}, "project_version" 8140, "performed_by" {"kind" "person", "id" 120053, "name" "Cary Fitzhugh", "initials" "CF"}, "changes" [{"kind" "story", "change_type" "update", "id" 107923330, "original_values" {"current_state" "started", "accepted_at" nil, "updated_at" 1447258611000, "before_id" 98744630, "after_id" 107741682}, "new_values" {"current_state" "accepted", "accepted_at" 1447258614000, "updated_at" 1447258614000, "before_id" 106971928, "after_id" 107920676}, "name" "test ", "story_type" "chore"}], "kind" "story_update_activity", "primary_resources" [{"kind" "story", "id" 107923330, "name" "test ", "story_type" "chore", "url" "https://www.pivotaltracker.com/story/show/107923330"}], "highlight" "accepted", "guid" "1243524_8140", "occurred_at" 1447258614000})

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
  (def application "sindicati-publish")
  (def environment "sindicati-p-stag")
  )

(defn handle-accepted-stories
  [accepted-story-changes event]
  (let [story-ids (map #(get % "id") accepted-story-changes)
        stories (map pivotal/get-story story-ids)
        labels  (flatten (filter identity (map :labels stories)))
        label-names (flatten (filter identity (map :name labels)))
        environments (set (map #(get config/environment-labels %) label-names))
        ]
      (println "Environments affected: " environments)
      (println "Accepted stories: " (pr-str accepted-story-changes))
      (doseq [environment environments]
        (if (some #(= environment %) (map :stag (vals config/application-repository-map)))
          (let [application (get config/env-application-map environment)
                commit-diff (github/get-commits-on-staging-not-prod application)
                pivotal-task-ids (pivotal/pivotal-stories-in-commit-range commit-diff)
                pivotal-stories (map pivotal/get-story pivotal-task-ids)]
            (hipchat/report-staging-state application pivotal-stories))))

    ))

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

(deflambdafn phoenixbot.handlers.pivotal.OnStoryChange
    [in out ctx]
      (let [event (json/read (io/reader in))
                    res (handle-story-change-event event)]
            (json/write res (io/writer out))))
