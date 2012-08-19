(ns netty.ring.request-test
  (:use clojure.test)
  (:require [netty.ring.request :as request])
  (:import [org.jboss.netty.handler.codec.http HttpMethod DefaultHttpRequest HttpVersion HttpMethod HttpHeaders HttpHeaders$Names]))

(declare netty-request add-header add-host-header add-content-length add-content-type add-character-encoding)

(deftest method
  (is (= :put (request/method HttpMethod/PUT)))
  (is (= :options (request/method HttpMethod/OPTIONS)))
  (is (= :cool (request/method (HttpMethod. "COOL")))))

(deftest uri-query-string
  (is (= ["/help" nil] (request/url "/help")))
  (is (= ["/go" "you=me"] (request/url "/go?you=me")))
  (is (= ["/fun" "I=me&you=your"] (request/url "/fun?I=me&you=your"))))

(deftest hostname
  (is (nil? (request/hostname (netty-request))))
  (is (= "localhost" (request/hostname (doto (netty-request) (add-host-header "localhost:8080")))))
  (is (= "good.me.com" (request/hostname (doto (netty-request) (add-host-header "good.me.com"))))))

(deftest content-length
  (is (nil? (request/content-length (netty-request))))
  (is (= 10 (request/content-length (doto (netty-request) (add-content-length 10))))))

(deftest scheme
  (is (= :http (request/scheme (netty-request))))
  (is (= :http (request/scheme (doto (netty-request) (add-header "X-Scheme" "http")))))
  (is (= :https (request/scheme (doto (netty-request) (add-header "X-Scheme" "https"))))))

(deftest content-type
  (is (nil? (request/content-type (netty-request))))
  (is (= "application/json" (request/content-type (doto (netty-request) (add-content-type "application/json")))))
  (is (= "multipart/mixed" (request/content-type (doto (netty-request) (add-content-type "multipart/mixed; boundary=frontier"))))))

(deftest character-encoding
  (is (nil? (request/character-encoding (netty-request))))
  (is (= "utf8" (request/character-encoding (doto (netty-request) (add-character-encoding "utf8"))))))

(deftest headers
  (is (empty? (request/headers (netty-request))))
  (is (= {"host" "localhost:8080"} (request/headers (doto (netty-request) (add-host-header "localhost:8080"))))))

(defn netty-request
  ([] (netty-request "/default"))
  ([uri] (netty-request HttpMethod/GET uri))
  ([method uri] (DefaultHttpRequest. HttpVersion/HTTP_1_1 method uri)))

(defn add-character-encoding [request encoding]
  (add-header request HttpHeaders$Names/CONTENT_ENCODING encoding))

(defn add-content-type [request content-type]
  (add-header request HttpHeaders$Names/CONTENT_TYPE content-type))

(defn add-content-length [request length]
  (HttpHeaders/setContentLength request (long length)))

(defn add-host-header [request host]
  (add-header request HttpHeaders$Names/HOST host))

(defn add-header [request name value]
  (.addHeader request name value))
