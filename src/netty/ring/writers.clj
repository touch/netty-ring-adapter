(ns netty.ring.writers
  (:require [clojure.java.io :as io])
  (:import [org.jboss.netty.buffer ChannelBuffers ChannelBuffer]
           [org.jboss.netty.channel Channel ChannelFutureListener ChannelFuture]
           [org.jboss.netty.handler.codec.http HttpResponse]
           [org.jboss.netty.handler.stream ChunkedStream]
           [java.io InputStream]
           [java.nio.charset Charset]
           [clojure.lang ISeq]))

(def charset (Charset/defaultCharset))

(defn- add-close-listener [^ChannelFuture future]
  (.addListener future ChannelFutureListener/CLOSE))

(defn- add-close-stream-listener [^ChannelFuture future ^InputStream stream]
  (let [listener (reify ChannelFutureListener (operationComplete [_ _] (.close stream)))]
    (.addListener future listener)))

(defprotocol ResponseWriter
  "Provides the best way to write a response for the give ring response body"
  (write [body ^HttpResponse response ^Channel channel]))

(extend-type String
  ResponseWriter
  (write [body ^HttpResponse response ^Channel channel]
    (let [buffer (ChannelBuffers/copiedBuffer body charset)]
      (.setContent response buffer)
      (doto (.write channel response)
        add-close-listener))))

(extend-type ISeq
  ResponseWriter
  (write [body ^HttpResponse response ^Channel channel]
    (write (apply str body) response channel)))

(extend-type InputStream
  ResponseWriter
  (write [body ^HttpResponse response ^Channel channel]
    (.write channel response)
    (doto (.write channel (ChunkedStream. body))
      add-close-listener
      (add-close-stream-listener body))))