(ns docraider.handler
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.util.response :as resp]))

(defroutes app-routes
  (GET "/" [] (resp/redirect "/index.html"))
  (fn [req] (if-let [resp ((route/resources "/") req)] (resp/charset resp "UTF-8")))
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))
