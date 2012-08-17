(ns netty.ring.adapter-test
  (:use clojure.test
        netty.ring.adapter
        compojure.core)
  (:require [clj-http.client :as client]
            [compojure.route :as route]))

(deftest simple
  (is (= (:body (client/get "http://localhost:8080/")) "Hello World")))

(defroutes tests
  (GET "/" [] "Hello World")
  (route/not-found "Unknown"))

(defn server-fixture [f]
  (let [shutdown (start-server tests {:port 8080})]
    (f)
    (shutdown)))

(use-fixtures :each server-fixture)
