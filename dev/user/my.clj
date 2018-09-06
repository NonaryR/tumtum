(ns user.my
  (:require [clojure.tools.namespace.repl :refer [refresh]]
            [mount.core :as mount]))

(defn start []
  (mount/start-with-args :prod))

(defn stop []
  (mount/stop))

(defn go []
  (start)
  :started)

(defn reset []
  (stop)
  (refresh :after 'user.my/go)
  :ok)

