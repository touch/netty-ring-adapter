(ns netty.ring.response
  (:require [netty.ring.writers :as w])
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

(defn write-ring-response [^ChannelHandlerContext context ring-response]
  (let [status (HttpResponseStatus/valueOf (ring-response :status 200))
        response (DefaultHttpResponse. HttpVersion/HTTP_1_1 status)]
    (set-headers response (:headers ring-response))
    (w/write (:body ring-response) response (.getChannel context))))
