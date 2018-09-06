(ns user.migrator
  (:require [ragtime.jdbc :as jdbc]
            [ragtime.repl :as repl]
            [tumtum.utils :as u]
            [mount.core :as mount]))

(defn run-config [profile]
  (-> (mount/only #{#'u/config})
      (mount/with-args (keyword profile))
      (mount/start)))

(defn load-config []
  {:datastore  (jdbc/sql-database u/config)
   :migrations (jdbc/load-resources "migrations")})

(defn create-migration [description]
  (let [path (format "resources/migrations/%s.edn" description)
        template {:up [""] :down [""]}]
    (spit path template)))

(defn migrate [profile]
  (run-config profile)
  (repl/migrate (load-config)))

(defn rollback [profile]
  (run-config profile)
  (repl/rollback (load-config)))

