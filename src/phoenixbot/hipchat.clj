(ns phoenixbot.hipchat
  (:require
    [clojure.data.json :as json]
    [phoenixbot.config :as config]
    [phoenixbot.pivotal :as pivotal]
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
