(ns phoenixbot.config
  (:require [environ.core :refer [env]]))


(def github-auth
  ;; This belongs to zipbot.
  {:oauth-token (env :github-oauth-token)})

(def pivotal-tracker-token (env :pivotal-tracker-token))
(def hipchat-token (env :hipchat-token))
(def hipchat-room-id (env :hipchat-room-id))

(def labels
  {
    "sindicati-p-stag" ["publish-stag"]
    "sindicati-p-prod" ["publish-prod"]
    "sindicati-spugna-stag" ["spugna-stag"]
    "sindicati-spugna-prod" ["spugna-prod"]
    "auth-s-prod" ["auth-prod"]
    "auth-s-stag" ["auth-stag"]
    "butterfly-s-prod" ["butterfly-prod"]
    "dowser-c-prod" ["dowser-cat-prod"]
    "dowser-c-stag" ["dowser-cat-stag"]
    "dowser-s-prod" ["dowser-serv-prod"]
    "dowser-s-stag" ["dowser-serv-stag"]
    "recirculati-s-prod" ["recirc-prod"]
    "recirculati-s-stag" ["recirc-stag"]
    "sindicati-s-prod" ["api-prod"]
    "sindicati-s-stag" ["api-stag"]
    "sindicati-w-prod" ["web-prod"]
    "sindicati-w-stag" ["web-stag"]
    })

(def application-repository-map
  {"sindicati-publish" {:org "Ziplist"
                        :repo "sindicati-publish"
                        :branch "master"}
   "sindicati-spugna" {:org "Ziplist"
                       :repo "sindicati-spugna"
                       :branch "master"}
   "auth-service" {:org "Ziplist"
                   :repo "cnds-auth"
                   :branch "master"}
   "butterfly-service" {:org "Ziplist"
                        :repo "butterfly-service"
                        :branch "master"}
   "dowser-categorizer" {:org "Ziplist" :repo "dowser-categorizer" :branch "master"}
   "dowser-service" {:org "Ziplist" :repo "dowser-service" :branch "master"}
   "recirculati-service" {:org "Ziplist" :repo "recirculati" :branch "master"}
   "sindicati-service" {:org "Ziplist" :repo "sindicapi" :branch "master"}
   "sindicati-web" {:org "Ziplist" :repo "sindicati-web" :branch "master"}

   })
