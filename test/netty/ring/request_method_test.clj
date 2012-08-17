(ns netty.ring.request-method-test
  (:use clojure.test)
  (:require [netty.ring.request :as request])
  (:import [org.jboss.netty.handler.codec.http HttpMethod]))

(deftest should-understand-all-methods
  (is (= :put (request/method HttpMethod/PUT)))
  (is (= :options (request/method HttpMethod/OPTIONS)))
  (is (= :cool (request/method (HttpMethod. "COOL")))))
