(ns tumtum.events
  (:require [re-frame.core :as re-frame :refer [reg-event-db]]
            [tumtum.db :as db]
            [day8.re-frame.tracing :refer-macros [fn-traced]]
            [pneumatic-tubes.core :as tubes]))

(defn on-receive [event-v]
  (.log js/console "received from server:" (str event-v))
  (re-frame/dispatch event-v))

(defn on-disconnect []
  (.log js/console "Connection with server lost.")
  (re-frame/dispatch [:backend-connected false]))

(defn on-connect []
  (re-frame/dispatch [:backend-connected true]))

(defn on-connect-failed [code]
  (.log js/console "Connection attemt failed. code: " code))

(def host (.-host js/location))

(def tube (tubes/tube (str "ws://" host "/chat") on-receive on-connect on-disconnect on-connect-failed))

(def send-to-server (re-frame/after (fn [_ v] (tubes/dispatch tube v))))

(reg-event-db
 :initialize-db
 (fn-traced [_ _]
   db/default-db))

(reg-event-db
 :exit-chat-room
 (fn-traced [_ _]
   (tubes/destroy! tube)
   db/default-db))

(reg-event-db
 :register-new-user
 (fn-traced [db [_ user password]]
            (tubes/create! tube {:user user
                                 :password password
                                 :act "reg"})
            db))

(reg-event-db
 :confirm-authorization
 (fn-traced [db [_ name]]
            (-> db
                (assoc :authorized? true)
                (assoc :name name))))

(reg-event-db
 :authorize-in-chat
 (fn-traced [db [_ user password]]
            (tubes/create! tube {:user user
                                 :password password
                                 :act "auth"})
            db))

(reg-event-db
 :error-login
 (fn-traced [db [_ user]]
            (assoc-in db [:chat-room :error-login] true)))

(reg-event-db
 :user-exists
 (fn-traced [db [_ user]]
            (assoc-in db [:chat-room :user-exists] true)))

(reg-event-db
 :create-chat
 (fn-traced [db _]
            (assoc-in db [:chat-room :new-chat?] true)))

(reg-event-db
 :enter-chat-room
 send-to-server
 (fn-traced [db [_ name room]] db))

(reg-event-db
 :update-room
 (fn-traced [db [_ room]]
            (assoc-in db [:chat-room :name] room)))

(reg-event-db
 :choose-chat
 (fn-traced [db [_ user chats]]
            (assoc-in db [:chat-room :all-chats] chats)))

(reg-event-db
 :post-message
 send-to-server
 (fn-traced [db [_ message]] db))

(reg-event-db
 :users-online-changed
 (fn-traced [db [_ names]]
   (assoc-in db [:chat-room :users] (-> names distinct sort vec))))

(reg-event-db
 :clean-messages
 (fn-traced [db _]
            (assoc-in db [:chat-room :messages] {})))

(reg-event-db
 :new-messages
 (fn-traced [db [_ messages]]
   (update-in db [:chat-room :messages] into (map (fn [m] [(:id m) m]) messages))))

(reg-event-db
 :backend-connected
 (fn-traced [db [_ state]]
            (assoc db :backend-connected state)))
