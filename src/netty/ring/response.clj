(ns netty.ring.response
  (:require [netty.ring.buffers :as b])
  (:import [org.jboss.netty.handler.codec.http
            HttpResponseStatus
            HttpVersion
            HttpHeaders
            HttpResponse
            DefaultHttpResponse]
           [org.jboss.netty.channel
            ChannelHandlerContext
            ChannelFutureListener]))

(defn set-headers [^HttpResponse response headers]
  (doseq [[key values] headers]
    (.setHeader response key values)))

(defn- write-response [^ChannelHandlerContext context ^HttpResponse response]
  (let [channel (.getChannel context)]
    (doto (.write channel response)
      (.addListener ChannelFutureListener/CLOSE))))

(defn write-ring-response [^ChannelHandlerContext context ring-response]
  (let [status (HttpResponseStatus/valueOf (ring-response :status 200))
        response (DefaultHttpResponse. HttpVersion/HTTP_1_1 status)
        buffer (b/to-buffer (:body ring-response))]
    (set-headers response (:headers ring-response))
    (.setContent response buffer)
    (write-response context response)))
