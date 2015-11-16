(ns phoenixbot.config
  (:require [environ.core :refer [env]]))


(def ^:const github-auth
  ;; This belongs to zipbot.
  (env :github-oauth-token))

(def ^:const ddb-environments (env :ddb-environments))

(def ^:const pivotal-tracker-token (env :pivotal-tracker-token))
(def ^:const hipchat-token (env :hipchat-token))
(def ^:const hipchat-room-id (env :hipchat-room-id))
(def ^:const labels
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

(def ^:const environment-labels
  (apply hash-map
         (flatten (map (fn [[k v]]
                           (map #(vector % k) v)) labels))))

(def ^:const application-repository-map
  {"sindicati-publish" {:org "Ziplist" :repo "sindicati-publish" :branch "master"
                        :stag "sindicati-p-stag" :prod "sindicati-p-prod"}
   "sindicati-spugna" {:org "Ziplist" :repo "sindicati-spugna" :branch "master"
                        :stag "sindicati-spugna-stag" :prod "sindicati-spugna-prod"}
   "auth-service" {:org "Ziplist" :repo "cnds-auth" :branch "master"
                   :stag "auth-s-stag" :prod "auth-s-prod"}
   "butterfly-service" {:org "Ziplist" :repo "butterfly-service" :branch "master"
                        :prod "butterfly-s-prod" :stag "no-stag-butterfly-service"}
   "dowser-categorizer" {:org "Ziplist" :repo "dowser-categorizer" :branch "master"
                        :stag "dowser-c-stag" :prod "dowser-c-prod" }
   "dowser-service" {:org "Ziplist" :repo "dowser-service" :branch "master"
                    :stag "dowser-s-stag" :prod "dowser-s-prod" }
   "recirculati-service" {:org "Ziplist" :repo "recirculati" :branch "master"
                         :stag "recirculati-s-stag" :prod "recirculati-s-prod" }
   "sindicati-service" {:org "Ziplist" :repo "sindicapi" :branch "master"
                       :stag "sindicati-s-stag" :prod "sindicati-s-prod" }
   "sindicati-web" {:org "Ziplist" :repo "sindicati-web" :branch "master"
                    :stag "sindicati-w-stag" :prod "sindicati-w-prod"}
  })

(def ^:const env-application-map
  (apply hash-map
         (flatten (map (fn [[k v]]
                           [(:stag v) k
                            (:prod v) k]) application-repository-map))))
