(ns tumtum.utils
  (:require [aero.core :refer [read-config]]
            [mount.core :refer [defstate] :as mount]
            [taoensso.timbre :as log]
            [clojure.spec.alpha :as s]
            [ragtime.jdbc :as jdbc]
            [ragtime.repl :as repl]))

(defn load-config [profile]
  (when (s/valid? #{:prod :test} profile)
    (log/info "read config" profile)
    (-> (read-config "config.edn" {:profile profile})
        (dissoc :secrets))))

(defstate config
  :start (load-config (mount/args)))

;; all your's migrations in database is always up-to-date
(defstate migrations
  :start (repl/migrate
          {:datastore  (jdbc/sql-database @config)
           :migrations (jdbc/load-resources "migrations")}))
