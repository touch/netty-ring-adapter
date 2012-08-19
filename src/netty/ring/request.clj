(ns netty.ring.request
  (:require [clojure.string :as s])
  (:import [org.jboss.netty.buffer ChannelBufferInputStream]
           [org.jboss.netty.channel ChannelHandlerContext]
           [org.jboss.netty.handler.codec.http HttpMethod HttpRequest HttpHeaders HttpHeaders$Names]))

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
  (-> context .getChannel .getLocalAddress))

(defn server-name [^ChannelHandlerContext context ^HttpRequest request]
  (if-let [host (hostname request)]
    host
    (.getHostName (local-address context))))

(defn remote-address [^ChannelHandlerContext context]
  (-> context
    .getChannel
    .getRemoteAddress
    .getAddress
    .getHostAddress))

(defn scheme [^HttpRequest request]
  (let [scheme (HttpHeaders/getHeader request "X-Scheme" "http")]
    (keyword scheme)))

(defn create-ring-request [^ChannelHandlerContext context ^HttpRequest http-request]
  (let [[uri query] (url (.getUri http-request))]
    {:body (ChannelBufferInputStream. (.getContent http-request))
     :uri uri
     :query-string query
     :request-method (method (.getMethod http-request))
     :server-name (server-name context http-request)
     :server-port (.getPort (local-address context))
     :remote-addr (remote-address context)
     :scheme (scheme http-request)}))


