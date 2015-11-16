(ns phoenixbot.aws
  (:require [uswitch.lambada.core :refer [deflambdafn]]
            [clojure.data.json :as json]
            [amazonica.aws.elasticbeanstalk :as eb]
            [clojure.edn :as edn]))

(defn get-current-application-version
  [application environment-name]
  (let [environments (:environments (eb/describe-environments))
        environment (first (filter (fn [env] (= environment-name (:environment-name env))) environments))
        v-label (:version-label environment)]
    (if v-label
      (get (re-matches #"(\d+\.\d+\.\d+)-(\d+)" v-label) 1)
      nil)))
