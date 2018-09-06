(ns tumtum.auth
  (:require [buddy.auth :as auth]
            [buddy.hashers :as hash]
            [tumtum.db :refer [conn] :as db]
            [taoensso.timbre :as log])
  (:import [org.postgresql.util PSQLException]))

(defn add-auth-user [user password]
  (try
    (db/add-user! conn user (hash/encrypt password))
    (catch Exception e
      (log/error e))))

(defn auth-user [user password]
  (if-let [user* (db/find-user conn user)]
    (if (hash/check password (:password user*))
      true
      false)))
