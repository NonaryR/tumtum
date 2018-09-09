(ns tumtum.core
  (:require [taoensso.timbre :as log]
            [compojure.core :refer [GET defroutes routes]]
            [compojure.route :refer [resources not-found]]
            [ring.util.response :refer [file-response]]
            [ring.middleware.defaults :refer [wrap-defaults]]
            [ring.middleware.resource :refer [wrap-resource]]
            [pneumatic-tubes.core :refer [receiver wrap-handlers]]
            [pneumatic-tubes.httpkit :refer [websocket-handler]]
            [tumtum.db :refer [conn]]
            [tumtum.reactions :as r]
            [tumtum.auth :as auth]
            [mount.core :as mount :refer [defstate]]
            [tumtum.utils :refer [config]]
            [clojure.core.async :refer [>!!]]
            [org.httpkit.server :refer [run-server]])
  (:gen-class))

(declare app)

(defn db-transaction
  "Middleware to apply a DB transaction."
  [handler]
  (fn [tube event-v]
    (let [[tube m] (handler tube conn event-v)]
      (when m
        (log/debug "Passed message for writing to db" tube m)
        (>!! r/incoming-events m))
      tube)))

(defn debug-middleware
  "Middleware to log incoming events"
  [handler]
  (fn [tube event-v]
    (log/debug "Received event" event-v "from" tube)
    (handler tube event-v)))

(def handlers
  {:tube/on-create
   (fn [tube _ _]
     (let [user-name (:user tube)
           password (:password tube)
           auth? (= "auth" (:act tube))]
       (if auth?
         (if (auth/auth-user user-name password)
           (r/confirm-user tube user-name)
           (r/error-login-user tube user-name))
         (do
           (if (auth/add-auth-user user-name password)
             (r/confirm-user tube user-name)
             (r/user-exists tube user-name))))
       [tube {:chat "system" :author "system"
              :message (format "%s joined app" user-name) :tube tube}]))

   :tube/on-destroy
   (fn [tube _ _]
     (let [room-name (:chat-room-name tube)
           user-name (:user tube)]
       (r/push-users-online room-name)
       [tube {:chat room-name :author "system"
              :message (format "%s lefted room %s" user-name room-name) :tube tube}]))

   :enter-chat-room
   (fn [tube _ [_ user-name room-name]]
     (let [tube* (r/tube-with-room tube room-name)]
       (r/push-users-online room-name)
       (r/push-clean-messages tube)
       (r/update-room tube* room-name)
       [tube* {:chat room-name :author user-name
               :message (format "%s joined room %s" user-name room-name) :tube tube*}]))

   :post-message
   (fn [tube _ [_ message]]
     (let [room-name (:chat-room-name tube)
           user-name (:user tube)]
       [tube {:chat room-name :author user-name
              :message message :tube tube}]))})

(def rx (receiver
          (wrap-handlers
           handlers
           db-transaction
           debug-middleware)))

(defroutes handler
  (GET "/" [] (file-response "index.html" {:root "resources/public"}))
  (GET "/chat" [user password act] (websocket-handler rx {:user user :password password :act act}))
  (resources "/")
  (not-found "Not Found"))

(def server
  (-> handler
      (wrap-defaults {:params {:urlencoded true
                               :keywordize true}})
      (wrap-resource "/")))

(defn at-shutdown
  [f]
  (-> (Runtime/getRuntime)
      (.addShutdownHook (Thread. (bound-fn []
                                   (log/info "Shutdown!")
                                   (f))))))


(defn start-server [port]
  (log/info "Starting server on port" port)
  (when-let [server (run-server #'server {:port port :join? false})]
    server))

(defn stop-server []
  (log/info "Stopping server")
  (app :timeout 100))

(defstate app
  :start (start-server (:app-port config))
  :stop  (stop-server))

(defstate reactions
  :start (r/react-on-transaction conn))

(defn -main [& args]
  (let [system (keyword (first args))]
    (mount/start-with-args system)
    (log/info "Starting app!!")
    (at-shutdown #(do (mount/stop)))
    (while true
      (Thread/sleep 100))))



