(ns rest-api.core
  (:require [rest-api.app :refer [start-app stop-app]])
  (:gen-class))


(defn -main
  "Start server"
  [& args]
  (start-app))
