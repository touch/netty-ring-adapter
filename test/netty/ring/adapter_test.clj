(ns netty.ring.adapter-test
  (:use clojure.test
        netty.ring.adapter)
  (:require [clj-http.client :as client]))

(defn handler [request]
  {:status 200
   :body "Hello World"})

(defn server-fixture [f]
  (let [shutdown (start-server handler {:port 8080})]
    (f)
    (shutdown)))

(use-fixtures :each server-fixture)

(deftest simple
  (is (= (:body (client/get "http://localhost:8080/")) "Hello World")))
