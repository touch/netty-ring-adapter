(ns netty.ring.request
  (:require [clojure.string :as s])
  (:import [org.jboss.netty.channel ChannelHandlerContext]
           [org.jboss.netty.handler.codec.http HttpMethod HttpRequest HttpHeaders$Names]))

(def method-mapping
  {HttpMethod/GET :get
   HttpMethod/POST :post
   HttpMethod/PUT :put
   HttpMethod/TRACE :trace
   HttpMethod/PATCH :patch
   HttpMethod/OPTIONS :options
   HttpMethod/DELETE :delete
   HttpMethod/HEAD :head
   HttpMethod/CONNECT :connect})

(defn method [^HttpMethod method]
  (if-let [method-keyword (method-mapping method)]
    method-keyword
    (-> method (.getName) (s/lower-case) (keyword))))

(defn url [request-uri]
  (let [regex #"([^?]+)[?]?([^?]+)?"
        [match uri query] (re-find regex request-uri)]
    [uri query]))

(defn hostname [^HttpRequest request]
  (when-let [host (.getHeader request HttpHeaders$Names/HOST)]
    (aget (.split host ":") 0)))

(defn local-address [^ChannelHandlerContext context]
  (-> context .getChannel .getLocalAddress .getHostName))

(defn server-name [^ChannelHandlerContext ctx ^HttpRequest request]
  (if-let [host (hostname request)]
    host
    (local-address ctx)))

