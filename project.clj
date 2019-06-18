(defproject rest-api "0.1.0-SNAPSHOT"
  :description "Peakon code challenge"

  ; :url "http://example.com/FIXME"
  ; :license {:name "Eclipse Public License"
  ;           :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.10.1"]

                 ;; web serving
                 [org.clojure/data.json "0.2.6"]
                 [http-kit "2.3.0"]
                 [compojure "1.6.1"]
                 [ring/ring-defaults "0.3.2"]

                 ;; test support
                 [ring/ring-mock "0.4.0"]
                 [eftest "0.5.8"]
                 [digest "1.4.9"]]

  :resource-paths ["resources" "target/resources"]
  :plugins [[lein-eftest "0.5.8"]]

  :main ^:skip-aot rest-api.core
  :target-path "target/%s"

  :profiles {:uberjar {:aot :all
                       :uberjar-name "peakon-cc.jar"}})
