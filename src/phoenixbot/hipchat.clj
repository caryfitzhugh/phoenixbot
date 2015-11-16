(ns phoenixbot.hipchat
  (:require
    [clojure.data.json :as json]
    [phoenixbot.config :as config]
    [phoenixbot.pivotal-tracker :as pivotal]
    [clj-http.lite.client :as client]
   ))

(comment
  (report-deployment "sindicapi-publish" "sindicati-publish-stag" "v1.2.3" [103936498])
  )
(def hipchat-host "https://api.hipchat.com")

(defn report-deployment-msg
  [app env ver stories]
  (str "<b>" app "</b><br/>"
       env " <br/>"
       ver " <br/> "
      (if (not (empty? stories))
        (str "Fixes: <ul> "
            (clojure.string/join
              (map (fn [story-id]
                      (let [story (pivotal/get-story story-id)]
                        (str "<li><a href='" (:url story) "'>"
                             (:name story) "</a></li>"))) stories)) "</ul>"))))
(defn report-deployment
  [application environment application-version pivotal-stories]
  (let [ options {:headers {"Authorization" (str "Bearer " config/hipchat-token)
                           }
                 :content-type :json
                 :body (json/write-str {:message_format "html"
                                        :message (report-deployment-msg application environment application-version pivotal-stories)
                                        })
                 }]
    (client/post (str hipchat-host "/v2/room/" config/hipchat-room-id "/notification") options)))

(defn report-staging-color
  [stories]
  (if (= (count stories) 0)
    "gray"

    (if (apply = (concat ["accepted"] (map :current_state stories)))
      "green"
      "yellow")))

(defn report-staging-state-msg
  [app stories]
    (let [accepted (filter #(= "accepted" (:current_state %)) stories)
          not-accepted (remove #(= "accepted" (:curent_state %)) stories)
          total (count stories)]

      (str "<b>" app "</b><br/>"
           (count accepted) " / " total " Accepted"
           "<br/>"
           "<b> Awaiting Acceptance </b></br>"
            (str ": <ul> "
              (clojure.string/join
                (map (fn [story]
                    (str "<li><a href='" (:url story) "'>"
                         (:name story) "</a></li>"))) not-accepted)
            "</ul>")
           "<b> Accepted </b></br>"
            (str ": <ul> "
              (clojure.string/join
                (map (fn [story]
                    (str "<li><a href='" (:url story) "'>"
                         (:name story) "</a></li>"))) accepted)
            "</ul>"))))

(defn report-staging-state
  [application stories]
  (if (not (empty? stories))
    (let [ alert-color (report-staging-color stories)
        options {:headers {"Authorization" (str "Bearer " config/hipchat-token) }
                 :content-type :json
                 :body (json/write-str {:message_format "html"
                                        :message (report-staging-state-msg application stories)
                                        :color alert-color
                                        })
                 }]
    (client/post (str hipchat-host "/v2/room/" config/hipchat-room-id "/notification") options))))
