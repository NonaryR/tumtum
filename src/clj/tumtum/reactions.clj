(ns tumtum.reactions
  (:require [taoensso.timbre :as log]
            [pneumatic-tubes.core :refer [transmitter dispatch find-tubes] :as tubes]
            [tumtum.db :as db :refer [conn]]
            [clojure.core.async :refer [chan <!! thread]]))

(def tx (transmitter #(log/info "Dispatching " %2 "to" %1)))

(def dispatch-to (partial dispatch tx))

(defn users-in-room [chat-room-name]
  (fn [tube] (= chat-room-name (:chat-room-name tube))))

(defn- to-chats [tube user]
  (dispatch-to tube [:choose-chat user (db/all-chats conn)]))

(defn- confirm-authorization [tube name]
  (dispatch-to tube [:confirm-authorization name]))

(defn confirm-user [tube user]
  (confirm-authorization tube user)
  (to-chats tube user))

(defn error-login-user [tube user]
  (dispatch-to tube [:error-login user]))

(defn user-exists [tube user]
  (dispatch-to tube [:user-exists user]))

(defn tube-with-room [tube room]
  (let [user {:user (:user tube)}
        data (assoc user :chat-room-name room)
        tube-id (tubes/update-tube-data! (:tube/id tube) data)]
    (tubes/get-tube tube-id)))

(defn update-room [tube room-name]
  (dispatch-to tube [:update-room room-name]))

(defn push-users-online [room-name]
  (let [all-in-room (users-in-room room-name)
        names-in-room (map :user (find-tubes all-in-room))]
    (dispatch-to all-in-room [:users-online-changed names-in-room])))

(defn push-clean-messages [tube]
  (dispatch-to tube [:clean-messages]))

(defn on-transaction [conn {:keys [chat author message tube]}]
  (db/add-message! conn chat author message)
  (dispatch-to (users-in-room chat) [:new-messages (db/fetch-chat-messages conn chat)]))

(def incoming-events (chan))

(defn react-on-transaction [conn]
  (log/info "started incoming transactions channel")
  (thread
    (while true
      (try
        (when-let [transaction (<!! incoming-events)]
          (on-transaction conn transaction))
        (catch Exception e
          (log/error e "Error processing to db"))))))
