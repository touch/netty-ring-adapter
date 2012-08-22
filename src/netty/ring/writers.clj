(ns netty.ring.writers
  (:require [clojure.java.io :as io])
  (:import [org.jboss.netty.buffer ChannelBuffers ChannelBuffer]
           [org.jboss.netty.channel Channel ChannelFutureListener]
           [org.jboss.netty.handler.codec.http HttpResponse]
           [java.io InputStream]
           [java.nio.charset Charset]
           [clojure.lang ISeq]))

(def charset (Charset/defaultCharset))

(defprotocol ResponseWriter
  "Provides the best way to write a response for the give ring response body"
  (write [body ^HttpResponse response ^Channel channel]))

(extend-type String
  ResponseWriter
  (write [body ^HttpResponse response ^Channel channel]
    (let [buffer (ChannelBuffers/copiedBuffer body charset)]
      (.setContent response buffer)
      (doto (.write channel response)
        (.addListener ChannelFutureListener/CLOSE)))))

(extend-type ISeq
  ResponseWriter
  (write [body ^HttpResponse response ^Channel channel]
    (write (apply str body) response channel)))