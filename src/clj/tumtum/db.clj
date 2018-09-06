(ns tumtum.db
  (:require [clojure.java.jdbc :as jdbc]
            [honeysql.core :as sql]
            [honeysql.helpers :as h]
            [mount.core :as mount :refer [defstate]]
            [taoensso.timbre :as log]
            [tumtum.utils :as u])
  (:import com.mchange.v2.c3p0.ComboPooledDataSource))

(defn- connect-db [spec]
  (log/info "connect to db")
  {:datasource
   (doto (ComboPooledDataSource.)
     (.setDriverClass (:classname spec))
     (.setJdbcUrl (str "jdbc:" (:subprotocol spec) ":" (:subname spec) "?sslmode=" (:sslmode spec)))
     (.setUser (:user spec))
     (.setPassword (:password spec))
     (.setMaxIdleTimeExcessConnections (* 30 60))
     (.setMaxIdleTime (* 3 60 60)))})

(defstate conn
  :start (connect-db u/config)
  :stop  (.close (:datasource conn)))

(defn find-user [conn user]
  (let [q {:select [:author :password]
           :from [:authors]
           :where [:= :author user]}]
    (first (jdbc/query conn (sql/format q)))))

(defn fetch-chat-messages [conn chat-name]
  (let [q {:select [:*] :from [:chat-messages]
           :where [:= :chat chat-name]}
        messages (jdbc/query conn (sql/format q))]
    (sort-by :date_at messages)))

(defn all-chats [conn]
  (let [q {:select [:chat] :from [:chat-messages]}]
    (->> (sql/format q)
         (jdbc/query conn)
         (keep :chat)
         (distinct))))

;; TODO macros and miltimethods

(defn add-user! [conn & values]
  (let [exec (-> (h/insert-into :authors)
                 (h/columns :author :password)
                 (h/values [(into [] values)])
                 (sql/format))]
    (jdbc/execute! conn exec)))

(defn add-message! [conn & values]
  (let [exec (-> (h/insert-into :chat-messages)
                 (h/columns :chat :author :message)
                 (h/values [(into [] values)])
                 (sql/format))]
        (jdbc/execute! conn exec)))
