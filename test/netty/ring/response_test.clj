(ns netty.ring.response-test
  (:import [org.jboss.netty.handler.codec.http
            DefaultHttpResponse
            HttpResponseStatus
            HttpVersion])
  (:use [clojure.test])
  (:require [netty.ring.response :as response]))

(declare netty-response)

(deftest headers
  (let [response (netty-response)]
    (response/set-headers response {"foo" "bar" "baz" ["help" "me"]})
    (is (= "bar" (.getHeader response "foo")))
    (is (= ["help" "me"] (.getHeaders response "baz")))))

(defn netty-response []
  (DefaultHttpResponse. HttpVersion/HTTP_1_1 HttpResponseStatus/OK))