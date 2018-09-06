(ns tumtum.utils
  (:require [aero.core :refer [read-config]]
            [mount.core :refer [defstate] :as mount]
            [taoensso.timbre :as log]
            [clojure.spec.alpha :as s]))

(defn load-config [profile]
  (when (s/valid? #{:prod :test} profile)
    (log/info "read config" profile)
    (-> (read-config "config.edn" {:profile profile})
        (dissoc :secrets))))

(defstate config
  :start (load-config (mount/args)))
