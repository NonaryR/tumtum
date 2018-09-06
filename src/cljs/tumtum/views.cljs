(ns tumtum.views
  (:require [reagent.ratom :refer [atom]]
            [re-frame.core :refer [dispatch subscribe]]))

(defn auth-or-reg-view []
  (let [user (atom "")
        password (atom "")
        error-login? (subscribe [:chat-room/error-login])
        user-exists? (subscribe [:chat-room/user-exists])]
    (fn []
      (let [enter-disabled? (or (empty? @user) (empty? @password))]
        [:div.container
         [:div.form-signin
          [:h2.form-signin-heading "Авторизация в чат"]
          [:input.form-control {:type "text" :placeholder "Ваш никнейм" :value @user
                                :on-change #(reset! user (-> % .-target .-value))}]
          [:input.form-control {:type "password" :placeholder "Ваш пароль" :value @password
                                :on-change #(reset! password (-> % .-target .-value))}]
          [:button.btn.btn-lg.btn-primary.btn-block
           {:class    (when enter-disabled? "disabled" )
            :on-click #(dispatch [:authorize-in-chat @user @password])} "Логин"]
          [:button.btn.btn-lg.btn-success.btn-block
           {:class    (when enter-disabled? "disabled" )
            :on-click #(dispatch [:register-new-user @user @password])} "Регистрация"]
          (if @error-login?
            [:alert.alert-danger [:strong "Неверный логин/пароль"]])
          (if @user-exists?
            [:alert.alert-danger [:strong "Пользователь с таким именем уже существует. Попробуйте новое :)"]])]]))))

(defn create-chat [name]
  (let [chat-room (atom "")]
    (fn []
      [:div.container
       [:div.form-signin
        [:h2.form-signin-heading "Создать чат"]
        [:input.form-control {:type "text" :placeholder "Имя чата"
                              :value @chat-room
                              :on-change #(reset! chat-room (-> % .-target .-value))}]
        [:button.btn.btn-lg.btn-primary.btn-block
         {:on-click #(dispatch [:enter-chat-room name @chat-room])} "Создать"]]])))

(defn button-chat [name chat-room]
  [:button.btn.btn-lg.btn-primary.btn-block
   {:on-click #(dispatch [:enter-chat-room name chat-room])} chat-room])

(defn new-button-chat []
  [:button.btn.btn-lg.btn-link
   {:on-click #(dispatch [:create-chat])} "+ создать чат"])

(defn grid-chats [name chats]
  (let [buttons (partial button-chat name)
        create-button (new-button-chat)
        new-chat? (subscribe [:chat-room/new-chat?])]
    (fn []
      (if @new-chat?
        [create-chat name]
        [:div.container
         (vec (cons :div.form-signin (vec (conj (mapv buttons chats) create-button))))]))))



(defn message-input-inner [{:keys [value enabled? on-change on-submit]}]
  [:div.input-group
   [:input.form-control {:type "text" :placeholder "Введите сообщение..."
                         :value value
                         :on-change on-change}]
   [:span.input-group-btn
    [:button.btn.btn-info {:type "button"
                           :class (when (or (empty? value) (not enabled?)) "disabled")
                           :on-click on-submit} "Отправить"]]])

(defn message-input []
  (let [text (atom "")
        backend-connected? (subscribe [:backend-connected])]
    (fn []
      [message-input-inner {:value     @text
                            :enabled?  @backend-connected?
                            :on-change #(reset! text (-> % .-target .-value))
                            :on-submit #(do
                                          (dispatch [:post-message @text])
                                          (reset! text ""))}])))

(defn chat-view [chat-name users messages]
  [:div.container
   [:div.row
    [:h3.text-center chat-name] [:br] [:br]
    [:div.col-md-8
     [:div.panel.panel-info
      [:div.panel-heading "История чата"]
      [:div.panel-body]
      [:ul.media-list
       (for [{id :id at :date_at author :author message :message} messages]
         ^{:key id}
         [:li.media
          [:div.media-body
           [:div.media
            [:div.pull-left
             [:img.media-object.img-circle.user-icon {:src "img/unknown.png"}]]
            [:div.media-body
             message
             [:br]
             [:small.text-muted author (str " at " at)]]]]])]]
     [:div.panel-footer
      [message-input]]]
    [:div.col-md-4
     [:div.panel.panel-primary
      [:div.panel-heading "Пользователи онлайн"]
      [:div.panel-body
       [:ul.media-list
        (for [user users]
          ^{:key user}
          [:li.media
           [:div.media-body
            [:div.media
             [:div.pull-left
              [:img.media-object.img-circle.user-icon {:src "img/unknown.png"}]]
             [:div.media-body
              [:h5 user]]]]])]]]
     [:button.btn.btn-primary {:on-click #(dispatch [:exit-chat-room])} "Выход"]]]])

(defn chats-grid [room-name users-online messages user chats]
  (fn []
    (if @room-name
      [chat-view @room-name @users-online @messages]
      [grid-chats @user @chats])))

(defn main-panel []
  (let [auth (subscribe [:authorized?])
        user (subscribe [:name])
        chats (subscribe [:chat-room/all-chats])
        room-name (subscribe [:chat-room/name])
        users-online (subscribe [:chat-room/users])
        messages (subscribe [:chat-room/messages])]
    (fn []
      (if (and @auth @user @chats)
        [chats-grid room-name users-online messages user chats]
        [auth-or-reg-view]))))
