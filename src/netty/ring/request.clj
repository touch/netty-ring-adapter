(ns netty.ring.request
  (:require [clojure.string :as s])
  (:import [org.jboss.netty.handler.codec.http HttpMethod HttpRequest]))

(def method-mapping
  { HttpMethod/GET :get
    HttpMethod/POST :post
    HttpMethod/PUT :put
    HttpMethod/TRACE :trace
    HttpMethod/PATCH :patch
    HttpMethod/OPTIONS :options
    HttpMethod/DELETE :delete
    HttpMethod/HEAD :head
    HttpMethod/CONNECT :connect })

(defn method [^HttpMethod method]
  (if-let [method-keyword (method-mapping method)]
    method-keyword
    (-> method (.getName) (s/lower-case) (keyword))))

(defn url [^HttpRequest request]
  (let [regex #"([^?]+)[?]?([^?]+)?"
        request-uri (.getUri request)
        [match uri query] (re-find regex request-uri)]
    [uri query]))

