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

(def default-charset (Charset/forName "UTF-8"))

(def charset-pattern
  "Regex to extract the charset from a content-type header.
   See: http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.7"
  #";\s*charset=\"?([^\s;\"]+)\"?")

(defn ^Charset get-charset
  "Extracts the charset from the content-type header, if present.
   Returns nil if the charset cannot be discovered."
  [^HttpResponse response]
  (if-let [content-type (.getHeader response "Content-Type")]
    (if-let [[_ charset] (re-find charset-pattern content-type)]
      (Charset/forName charset))))

(defn- keep-alive? [^HttpResponse response]
  (= "keep-alive" (HttpHeaders/getHeader response "connection")))

(defn- add-close-listener [^ChannelFuture future ^HttpResponse response]
  (if (keep-alive? response)
    (.addListener future ChannelFutureListener/CLOSE_ON_FAILURE)
    (.addListener future ChannelFutureListener/CLOSE)))

(defn- add-close-stream-listener [^ChannelFuture future ^InputStream stream]
  (let [listener (reify ChannelFutureListener (operationComplete [_ _] (.close stream)))]
    (.addListener future listener)
    (.addListener future ChannelFutureListener/CLOSE)))

(defn- write-response [^HttpResponse response ^Channel channel]
  (-> (.write channel response)
    (add-close-listener response)))

(defprotocol ResponseWriter
  "Provides the best way to write a response for the give ring response body"
  (write [body ^HttpResponse response ^Channel channel]))

(extend-type String
  ResponseWriter
  (write [body ^HttpResponse response ^Channel channel]
    (let [charset (or (get-charset response) default-charset)
          buffer (ChannelBuffers/copiedBuffer body charset)]
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
    (-> (.write channel (ChunkedStream. body))
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
    (let [response-body (file-body body)]
      (.setHeader response "Zero-Copy" *zero-copy*)
      (HttpHeaders/setContentLength response (.length body))

      (.write channel response)
      (add-close-listener (.write channel response-body) response))))

(extend-type nil
  ResponseWriter
  (write [body ^HttpResponse response ^Channel channel]
    (HttpHeaders/setContentLength response 0)
    (write-response response channel)))
