(defproject tumtum "0.1.1"
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.10.339"]
                 [org.clojure/core.async "0.4.474"]
                 [reagent "0.7.0"]
                 [re-frame "0.10.5"]
                 [mount "0.1.13"]
                 [aero "1.1.3"]
                 [ragtime "0.7.2"]
                 [honeysql "0.9.3"]
                 [com.taoensso/timbre "4.10.0"]
                 [org.clojure/java.jdbc "0.7.7"]
                 [org.postgresql/postgresql "42.2.4"]
                 [clojure.jdbc/clojure.jdbc-c3p0 "0.3.3"]
                 [pneumatic-tubes "0.3.0"]
                 [buddy/buddy-auth "2.1.0"]
                 [buddy/buddy-hashers "1.3.0"]
                 [compojure "1.6.1"]
                 [ring "1.7.0-RC2"]
                 [ring/ring-defaults "0.3.2"]
                 [http-kit "2.3.0"]]

  :plugins [[lein-cljsbuild "1.1.7"]
            [lein-cljfmt "0.6.0"]
            [lein-ancient "0.6.15"]]

  :min-lein-version "2.5.3"

  :main tumtum.core

  :source-paths ["src/clj" "dev"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :figwheel {:css-dirs ["resources/public/css"]}

  :repl-options {:timeout 600000
                 :nrepl-middleware [cider.piggieback/wrap-cljs-repl]}

  :profiles
  {:repl {:dependencies [[org.clojure/tools.namespace "0.2.11"]]
          :repl-options {:init-ns user.my}
          :injections [(require 'clojure.tools.namespace.repl)
                       (require 'user.my)]}
   :dev
   {:dependencies [[binaryage/devtools "0.9.10"]
                   [day8.re-frame/re-frame-10x "0.3.3"]
                   [day8.re-frame/tracing "0.5.1"]
                   [re-frisk "0.5.3"]
                   [cider/piggieback "0.3.5"]
                   [figwheel-sidecar "0.5.16"]]

    :plugins      [[lein-figwheel "0.5.16"]
                   [com.jakemccrary/lein-test-refresh "0.23.0"]]}
   :prod {:dependencies [[day8.re-frame/tracing-stubs "0.5.1"]]}
   :uberjar {:prep-tasks ["compile" ["cljsbuild" "once" "min"]]
             :aot :all}}

  :aliases {"migrate" ["run" "-m" "user.migrator/migrate"]
            "rollback" ["run" "-m" "user.migrator/rollback"]
            "create-migration" ["run" "-m" "user.migrator/create-migration"]}

  :cljsbuild
  {:builds
   [{:id           "dev"
     :source-paths ["src/cljs" "dev" "src/clj"]
     :figwheel     {:on-jsload "tumtum.core/mount-root"}
     :compiler     {:main                 tumtum.core
                    :output-to            "resources/public/js/compiled/app.js"
                    :output-dir           "resources/public/js/compiled/out"
                    :asset-path           "js/compiled/out"
                    :source-map-timestamp true
                    :preloads             [devtools.preload
                                           day8.re-frame-10x.preload
                                           re-frisk.preload]
                    :closure-defines      {"re_frame.trace.trace_enabled_QMARK_" true
                                           "day8.re_frame.tracing.trace_enabled_QMARK_" true}
                    :external-config      {:devtools/config {:features-to-install :all}}}}

    {:id           "min"
     :source-paths ["src/cljs" "src/clj"]
     :compiler     {:main            tumtum.core
                    :output-to       "resources/public/js/compiled/app.js"
                    :optimizations   :advanced
                    :closure-defines {goog.DEBUG false}
                    :pretty-print    false}}]})
