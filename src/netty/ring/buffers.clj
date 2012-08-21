(ns netty.ring.buffers
  (:import [org.jboss.netty.buffer ChannelBuffers ChannelBuffer]
           [java.nio.charset Charset]
           [clojure.lang ISeq]))

(def charset (Charset/defaultCharset))

(defn buffer->string [^ChannelBuffer buffer]
  (.toString buffer charset ))

(defprotocol ToChannelBuffer
  "Provides the most effecient ChannelBuffer for the object"
  (to-buffer [this]))

(extend-type String
  ToChannelBuffer
  (to-buffer [this]
    (ChannelBuffers/copiedBuffer this charset)))

(extend-type ISeq
  ToChannelBuffer
  (to-buffer [this]
    (to-buffer (apply str this))))

