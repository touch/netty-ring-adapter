(ns netty.ring.adapter-test
  (:use clojure.test
        netty.ring.adapter
        compojure.core)
  (:require [clj-http.client :as client]
            [compojure.route :as route]))

(deftest simple
  (is (= (:body (client/get "http://localhost:8080/")) "Hello World")))

(deftest request-method
  (is (= (:body (client/get "http://localhost:8080/method")) "get"))
  (is (= (:body (client/post "http://localhost:8080/method")) "post"))
  (is (= (:body (client/put "http://localhost:8080/method")) "put")))

(defroutes tests
  (GET "/" [] "Hello World")
  (ANY "/method" [] #(name (:request-method %)))
  (route/not-found "Unknown"))

(defn server-fixture [f]
  (let [shutdown (start-server tests {:port 8080})]
    (f)
    (shutdown)))

(use-fixtures :each server-fixture)
