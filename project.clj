(defproject phoenixbot "1.0.19"
  :description "CN-ATG Workflow tools"
  :url "http://github.com/Ziplist/phoenixbot"
  :min-lein-version "2.0.0"
  :license {:name "Owned by CN"
            :url "http://www.condenast.com"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [environ               "1.0.0"]
                 [uswitch/lambada "0.1.0"]
                 [org.clojure/data.json "0.2.6"]
                 [com.fasterxml.jackson.core/jackson-core "2.6.3"]
                 [amazonica "0.3.39" ]
                 [clj-http "2.0.0"]
                 [hipchat-clj "0.1.3"]
                 [cheshire "5.4.0"]
                 [tentacles "0.3.0" :exclusions [org.clojure/tools.reader]]
                ]

  :repositories [["cnds" {:url "s3p://clojars.cnds.io/cnds/releases" :sign-releases false}]]

  :target-path "target/"

  :release-tasks [["vcs" "assert-committed"]
                  ["clean"]
                  ["deps"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "push"]
                  ["uberjar"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["vcs" "commit"]
                  ["vcs" "push"]
                  ]

  :profiles {:uberjar {:aot :all}}

  :plugins [
            [lein-environ "1.0.0"]
            [s3-wagon-private "1.1.2" :exclusions [commons-codec commons-logging]]
          ]
  )
