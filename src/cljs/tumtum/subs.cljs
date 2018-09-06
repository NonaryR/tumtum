(ns tumtum.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
 :name
 (fn [db]
   (:name db)))

(reg-sub
 :backend-connected
 (fn [db]
   (:backend-connected db)))

(reg-sub
 :authorized?
 (fn [db]
   (:authorized? db)))

(reg-sub
 :chat-room/error-login
 (fn [db]
   (get-in db [:chat-room :error-login])))

(reg-sub
 :chat-room/user-exists
 (fn [db]
   (get-in db [:chat-room :user-exists])))

(reg-sub
 :chat-room/name
 (fn [db]
   (get-in db [:chat-room :name])))

(reg-sub
 :chat-room/new-chat?
 (fn [db]
   (get-in db [:chat-room :new-chat?])))

(reg-sub
 :chat-room/users
 (fn [db]
   (get-in db [:chat-room :users])))

(reg-sub
 :chat-room/all-chats
 (fn [db]
   (get-in db [:chat-room :all-chats])))

(reg-sub
 :chat-room/messages
 (fn [db]
   (sort-by :id (vals (get-in db [:chat-room :messages])))))

