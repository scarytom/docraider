(ns docraider.app
  (:use ring.adapter.jetty)
  (:require [docraider.handler :as handler])
  (:gen-class))

(defn -main [& args]
  (let [port (try (Integer/parseInt (first args)) (catch Exception e 3000))]
    (run-jetty handler/app {:port port})))
