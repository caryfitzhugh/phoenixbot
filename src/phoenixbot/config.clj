(ns phoenixbot.config
  (:require [environ.core :refer [env]]))


(def github-auth
  ;; This belongs to zipbot.
  (env :oauth-token))

(def pivotal-tracker-token (env :pivotal-tracker-token))
(def hipchat-token (env :hipchat-token))
(def hipchat-room-id (env :hipchat-room-id))

(def labels
  {"sindicati-publish" {
                        "sindicati-p-stag" ["publish-stag"]
                        "sindicati-p-prod" ["publish-prod"]
                       }
   })

(def application-repository-map
  {"sindicati-publish" {:org "Ziplist"
                        :repo "sindicati-publish"
                        :branch "master"}
   })
