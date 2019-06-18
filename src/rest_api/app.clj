(ns rest-api.app
  (:require [compojure.core :refer [defroutes GET POST DELETE ANY context]]
            [compojure.route :refer [files not-found]]
            [compojure.response :as response]
            [ring.util.response :refer [content-type]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [clojure.java.io :as io]
            [clojure.data.json :as json]
            [org.httpkit.server :refer [run-server]]
            [rest-api.db :refer [search-employees]]))


(defn show-landing-page [req]
  (io/resource "public/index.html"))


;(defn dump-db [req]
;  (-> (response/render db-raw req)
;      (content-type "application/json")))


(defn search-employee [req]
  "HTTP handler function for searching employee by name or email.
  Input search term could be 'cleaned' to conform to only know valid input, but that is
  left as an exercise :-)"
  (let []                                                   ;req-params (:req :params)]
    ;search-term (-> req :params :search-term)]
    (-> (response/render (json/write-str (search-employees (req :params))) req)
        (content-type "application/json"))))



(defroutes app-routes
           (GET "/" [] show-landing-page)
           (GET "/v1/employee/" [] search-employee)
           (context "/v1/employee/:search-string" []
                      (GET "/" {} search-employee))
                    ;(POST "/" [] search-employee)
           ;(GET "/raw-db" [] dump-db)
           (files "/static/" {:root "resources/public/"})   ;; static file url prefix /static, in `public` folder
           (not-found "<p>Page not found.</p>"))            ;; all other, return 404


;; App configuration. Keys are:
;;   :port - runs at port 8080 unless otherwise specified
;;   :server - running server function from http-kit (set automatically)
(defonce app-config (atom {:port (or (System/getenv "HOST_PORT") 8080)}))

(defn app-started? []
  (not (nil? (:server @app-config))))

(defn stop-app []
  (let [server (:server @app-config)]
    (when-not (nil? server)
      ;; graceful shutdown: wait 100ms for existing requests to be finished
      (server :timeout 100)
      (swap! app-config assoc :server nil))))


(defn start-app []
  (let [app-port (:port @app-config)]
    (if-not (:server @app-config)
      (do
        (println "Starting server on port" app-port
                 (swap! app-config assoc :server (run-server (wrap-defaults #'app-routes site-defaults) {:port app-port}))))
      (println "Server already started (running on port" app-port ")"))))