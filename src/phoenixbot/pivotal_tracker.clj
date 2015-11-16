(ns phoenixbot.pivotal-tracker
  (:require
    [phoenixbot.config :as config]
    [clojure.walk]
    [clojure.java.io :as io]
    [clojure.data.json :as json]
    [clj-http.lite.client :as client]
    ))
(comment
 (get-story 107407794)
 (add-labels 107407794 #{"test-label"})
  )

(defn pivotal-stories-in-commit-range
  [commits]
  (set (filter identity (flatten (map (fn [commit] (map  (fn [res] (get res 4)) ;;  Pull the issue # directly
                                                            (re-seq #"\[((Delivers|Fixes)( )+)?#([0-9]+)\]" (clojure.string/join (:message (:commit commit))))
                                                           )) commits)))))

(defn- url
  [& rest]
  (apply str (concat ["https://www.pivotaltracker.com/services/v5"] rest)))

(defn get-story
  [story-id]
  (clojure.walk/keywordize-keys
    (json/read-str
      (:body
        (clojure.walk/keywordize-keys
          (client/get
            (url "/stories/" story-id ) {:headers {"X-TrackerToken" config/pivotal-tracker-token}}))))))

(defn add-labels
  [story-id labels]
  (let [story (get-story story-id)
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
