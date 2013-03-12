(ns netty.ring.buffer
  (:require [clojure.java.io :as io])
  (:import [org.jboss.netty.buffer ChannelBuffer ChannelBuffers ChannelBufferInputStream]
           [java.io File]))

(defprotocol Buffer
  (fill [buffer ^ChannelBuffer channel-buffer])
  (stream [buffer]))

(defn- create-file-buffer [^ChannelBuffer buffer ^ChannelBuffer channel-buffer]
  (let [file (File/createTempFile "netty" "buffer")
        buffers (into-array ChannelBuffer [buffer channel-buffer])
        composite-buffer-channel (ChannelBuffers/wrappedBuffer buffers)]
    (.deleteOnExit file)
    (with-open [buffer-stream (ChannelBufferInputStream. composite-buffer-channel)]
      (io/copy buffer-stream file))
    file))

(defn- create-in-memory-buffer [^Integer max-in-memory-size]
  (ChannelBuffers/dynamicBuffer max-in-memory-size))

(defn- full? [^ChannelBuffer buffer ^ChannelBuffer channel-buffer]
  (let [capacity (.capacity buffer)
        current-size (.readableBytes buffer)
        buffer-size (.readableBytes channel-buffer)]
    (> (+ buffer-size current-size) capacity)))

(extend-type ChannelBuffer
  Buffer
    (fill [buffer ^ChannelBuffer channel-buffer]
      (if-not (full? buffer channel-buffer)
        (doto buffer (.writeBytes channel-buffer))
        (create-file-buffer buffer channel-buffer)))
    (stream [buffer]
      (ChannelBufferInputStream. buffer)))

(extend-type File
  Buffer
    (fill [file-buffer ^ChannelBuffer channel-buffer]
      (with-open [output-stream (io/output-stream file-buffer)]
        (.getBytes channel-buffer 0 output-stream (.readableBytes channel-buffer)))
      file-buffer)
    (stream [file-buffer]
      (io/input-stream file-buffer)))

(defn create-buffer [max-in-memory-size]
  (atom {:max-in-memory-size max-in-memory-size
         :buffer nil}))

(defn write [buffer channel-buffer]
  (-> (or (:buffer @buffer) (create-in-memory-buffer (:max-in-memory-size @buffer)))
      (fill channel-buffer)
      (->> (swap! buffer assoc :buffer)))
  buffer)

(defn input-stream [buffer]
  (stream (:buffer @buffer)))