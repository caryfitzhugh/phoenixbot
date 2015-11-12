(defproject phoenixbot "1.0.46"
  :description "CN-ATG Workflow tools"
  :url "http://github.com/Ziplist/phoenixbot"
  :min-lein-version "2.0.0"
  :license {:name "Owned by CN"
            :url "http://www.condenast.com"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [environ               "1.0.0"]
                 [uswitch/lambada "0.1.0"]
                 [org.clojure/data.json "0.2.6"]
                 [amazonica "0.3.39" ]
                 [clj-http-lite "0.3.0"]
                ]

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

  :aot :all

  :plugins [
            [lein-environ "1.0.0"]
            ]
  )
