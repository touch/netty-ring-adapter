(ns netty.ring.request-test
  (:use clojure.test)
  (:require [netty.ring.request :as request])
  (:import [org.jboss.netty.handler.codec.http HttpMethod DefaultHttpRequest HttpVersion HttpMethod HttpHeaders$Names]))

(declare netty-request add-header add-host-header)

(deftest should-understand-all-methods
  (is (= :put (request/method HttpMethod/PUT)))
  (is (= :options (request/method HttpMethod/OPTIONS)))
  (is (= :cool (request/method (HttpMethod. "COOL")))))

(deftest should-parse-url-into-parts
  (is (= ["/help" nil] (request/url "/help")))
  (is (= ["/go" "you=me"] (request/url "/go?you=me")))
  (is (= ["/fun" "I=me&you=your"] (request/url "/fun?I=me&you=your"))))

(deftest should-be-able-to-calculate-host-name
  (is (nil? (request/hostname (netty-request))))
  (is (= "localhost" (request/hostname (doto (netty-request) (add-host-header "localhost:8080")))))
  (is (= "good.me.com" (request/hostname (doto (netty-request) (add-host-header "good.me.com"))))))

(defn netty-request
  ([] (netty-request "/default"))
  ([uri] (netty-request HttpMethod/GET uri))
  ([method uri] (DefaultHttpRequest. HttpVersion/HTTP_1_1 method uri)))

(defn add-host-header [request host]
  (add-header request HttpHeaders$Names/HOST host))

(defn add-header [^DefaultHttpRequest request name value]
  (.addHeader request name value))
