(ns phoenixbot.hipchat
  (:require [hipchat.core :as hc]
            [phoenixbot.config :as config]
            [phoenixbot.pivotal :as pivotal]
            ))
(comment
  )

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
  (hc/set-auth-token! config/hipchat-token)
  (hc/send-message-to-room config/hipchat-room-id (report-deployment-msg application environment application-version pivotal-stories)
                                   :message_format "html"
                                   :color "red"
                                   :nofity true))
