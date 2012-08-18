(ns netty.ring.adapter-test
  (:refer-clojure :exclude [get])
  (:use clojure.test
        netty.ring.adapter
        compojure.core)
  (:require [clj-http.client :as client]
            [compojure.route :as route]))

(declare get post put)

(deftest simple
  (is (= (get "/") "Hello World")))

(deftest request-method
  (is (= (get "/method") "get"))
  (is (= (post "/method") "post"))
  (is (= (put "/method") "put")))

(deftest uri
  (is (= (get "/uri/me") "/uri/me"))
  (is (= (get "/uri/you?help=me") "/uri/you")))

(deftest query-string
  (is (= (get "/query?you=me") "you=me"))
  (is (= (get "/query?me=you&you=I") "me=you&you=I")))

(defroutes test-routes
  (GET "/" [] "Hello World")
  (ANY "/method" [] #(name (:request-method %)))
  (GET "/uri/*" [] #(:uri %))
  (GET "/query" [] #(:query-string %))
  (route/not-found "Unknown"))

(defn server-fixture [f]
  (let [shutdown (start-server test-routes {:port 8080})]
    (f)
    (shutdown)))

(use-fixtures :each server-fixture)

(def ^:const server "http://localhost:8080")

(defn request [f]
  #(:body (f (str server %))))

(def get (request client/get))
(def post (request client/post))
(def put (request client/put))
