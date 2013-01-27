(ns netty.ring.writers
  (:require [clojure.java.io :as io])
  (:import [org.jboss.netty.buffer ChannelBuffers ChannelBuffer]
           [org.jboss.netty.channel Channel ChannelFutureListener ChannelFuture DefaultFileRegion]
           [org.jboss.netty.handler.codec.http HttpResponse HttpHeaders HttpHeaders$Names]
           [org.jboss.netty.handler.stream ChunkedStream ChunkedFile]
           [java.io InputStream File RandomAccessFile]
           [java.net URLConnection]
           [java.nio.charset Charset]
           [clojure.lang ISeq]))

(def charset (Charset/defaultCharset))

(defn- keep-alive? [^HttpResponse response]
  (= "keep-alive" (HttpHeaders/getHeader response "connection")))

(defn- add-close-listener [^ChannelFuture future ^HttpResponse response]
  (when-not (keep-alive? response)
    (.addListener future ChannelFutureListener/CLOSE)))

(defn- add-close-stream-listener [^ChannelFuture future ^InputStream stream]
  (let [listener (reify ChannelFutureListener (operationComplete [_ _] (.close stream)))]
    (.addListener future listener)))

(defn- write-response [^HttpResponse response ^Channel channel]
  (-> (.write channel response)
    (add-close-listener response)))

(defprotocol ResponseWriter
  "Provides the best way to write a response for the give ring response body"
  (write [body ^HttpResponse response ^Channel channel]))

(extend-type String
  ResponseWriter
  (write [body ^HttpResponse response ^Channel channel]
    (let [buffer (ChannelBuffers/copiedBuffer body charset)]
      (HttpHeaders/setContentLength response (.readableBytes buffer))
      (.setContent response buffer)
      (write-response response channel))))

(extend-type ISeq
  ResponseWriter
  (write [body ^HttpResponse response ^Channel channel]
    (write (apply str body) response channel)))

(extend-type InputStream
  ResponseWriter
  (write [body ^HttpResponse response ^Channel channel]
    (.write channel response)
    (doto (.write channel (ChunkedStream. body))
      (add-close-listener response)
      (add-close-stream-listener body))))

(def ^:dynamic *zero-copy* false)

(defn file-body [file]
  (let [random-access-file (RandomAccessFile. file "r")]
    (if *zero-copy*
      (DefaultFileRegion. (.getChannel random-access-file) 0 (.length file) true)
      (ChunkedFile. random-access-file))))

(extend-type File
  ResponseWriter
  (write [body ^HttpResponse response ^Channel channel]
    (let [response-body (file-body body)
          content-type (URLConnection/guessContentTypeFromName (.getName body))]
      (.setHeader response HttpHeaders$Names/CONTENT_TYPE content-type)
      (.setHeader response "Zero-Copy" *zero-copy*)
      (HttpHeaders/setContentLength response (.length body))

      (.write channel response)
      (add-close-listener (.write channel response-body) response))))

(extend-type nil
  ResponseWriter
  (write [body ^HttpResponse response ^Channel channel]
    (write-response response channel)))
