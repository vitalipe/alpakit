(defproject alpakit "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.7.1"
  :jvm-opts ~(concat
                 ;; Normal JVM opts to pass in
                 ["-Xmx40g"])
                 ;; Java 9+ recognition, adding --add-modules. Java versions before 9
                 ;; had a different version syntax where they contained '.' delimiters,
                 ;; from Java 9 onwards it has a simple versioning scheme based on one
                 ;; number.

  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/clojurescript "1.10.520"]
                 [devcards "0.2.6"]

                 [reagent "0.8.1"]
                 [garden "1.3.6"]
                 [org.clojars.mmb90/cljs-cache "0.1.4"]]


  :plugins [[lein-figwheel "0.5.18"]
            [lein-cljsbuild "1.1.7" :exclusions [org.clojure/clojure]]]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]
  :source-paths ["src"]

  :cljsbuild {
              :builds [{:id "cards"
                        :source-paths ["src"]
                        :figwheel { :devcards true  ;; <- note this
                                   ;; :open-urls will pop open your application
                                   ;; in the default browser once Figwheel has
                                   ;; started and complied your application.
                                   ;; Comment this out once it no longer serves you.
                                   :open-urls ["http://localhost:3449/"]}
                        :compiler { :main       "cards.core"
                                    :asset-path "js/compiled/devcards_out"
                                    :output-to  "resources/public/js/compiled/alpakit_devcards.js"
                                    :output-dir "resources/public/js/compiled/devcards_out"
                                    :source-map-timestamp true

                                    :install-deps true
                                    :npm-deps {}}}

                       {:id "cards-prod"
                        :source-paths ["src"]
                        :compiler {:main       "cards.core"
                                   :asset-path "js/compiled/out"
                                   :output-to  "resources/public/js/compiled/cards.js"
                                   :optimizations :advanced}}]}

  :figwheel { :css-dirs ["resources/public/css"]}
  :profiles {:dev {:dependencies [[binaryage/devtools "0.9.10"]
                                  [figwheel-sidecar "0.5.18"]
                                  [com.cemerick/piggieback "0.2.2"]]
                   ;; need to add dev source path here to get user.clj loaded
                   :source-paths ["src" "dev"]
                   ;; for CIDER
                   ;; :plugins [[cider/cider-nrepl "0.12.0"]]
                   :repl-options {; for nREPL dev you really need to limit output
                                  :init (set! *print-length* 50)
                                  :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}})
