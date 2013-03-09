(ns docraider.test.handler
  (:use clojure.test
        ring.mock.request  
        docraider.handler))

(deftest test-app
  (testing "redirects to index.html"
    (let [response (app (request :get "/"))]
      (is (= (:status response) 302))
      (is (= (get (:headers response) "Location") "/index.html"))))
  
  (testing "not-found route"
    (let [response (app (request :get "/invalid"))]
      (is (= (:status response) 404))))
  
    (testing "serves index.html"
    (let [response (app (request :get "/index.html"))]
      (is (= (:status response) 200)))))