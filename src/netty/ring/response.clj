(ns netty.ring.response
  (:import [org.jboss.netty.handler.codec.http
            HttpResponseStatus
            HttpVersion
            HttpHeaders
            HttpResponse
            DefaultHttpResponse]
           [org.jboss.netty.buffer ChannelBuffers ChannelBufferOutputStream]
           [org.jboss.netty.channel
            ChannelHandlerContext
            ChannelFutureListener]))

(defn- write-response [^ChannelHandlerContext context ^HttpResponse response]
  (let [channel (.getChannel context)]
    (doto (.write channel response)
      (.addListener ChannelFutureListener/CLOSE))))

(defn write-ring-response [^ChannelHandlerContext context ring-response]
  (let [status (HttpResponseStatus/valueOf (ring-response :status 200))
        response (DefaultHttpResponse. HttpVersion/HTTP_1_1 status)
        buffer (ChannelBuffers/dynamicBuffer 100)
        output (ChannelBufferOutputStream. buffer)]
    (.writeBytes output (:body ring-response))
    (.setContent response buffer)
    (write-response context response)))
