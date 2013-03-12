(ns netty.ring.buffer-test
  (:use clojure.test)
  (:require [netty.ring.buffer :as buffer])
  (:import [org.jboss.netty.buffer ChannelBuffers ChannelBufferOutputStream]))

(declare to-channel-buffer write)

(deftest in-memory
  (let [in-memory-buffer (buffer/create-buffer 100)]
    (write in-memory-buffer "Hello")

    (is (= 100 (:max-in-memory-size @in-memory-buffer)))
    (is (= 5 (.readableBytes (:buffer @in-memory-buffer))))
    (is (= "Hello" (slurp (buffer/input-stream in-memory-buffer))))))

(deftest multiple-in-memory-writes
  (let [in-memory-buffer (buffer/create-buffer 1000)]
    (write in-memory-buffer "Hello ")
    (write in-memory-buffer "World")

    (is (= 11 (.readableBytes (:buffer @in-memory-buffer))))
    (is (= "Hello World" (slurp (buffer/input-stream in-memory-buffer))))))

(deftest file-backed
  (let [file-buffer (buffer/create-buffer 10)]
    (write file-buffer "I hope this works!")

    (is (= true (.isFile (:buffer @file-buffer))))
    (is (= "I hope this works!" (slurp (buffer/input-stream file-buffer))))))

(deftest multiple-writes-to-memory-and-file
  (let [file-buffer (buffer/create-buffer 30)]
    (write file-buffer "Hello ")
    (write file-buffer "World ")
    (write file-buffer "I should be writing to a file")

    (is (= true (.isFile (:buffer @file-buffer))))
    (is (= "Hello World I should be writing to a file" (slurp (buffer/input-stream file-buffer))))))

(defn write [buffer msg]
  (buffer/write buffer (to-channel-buffer msg)))

(defn to-channel-buffer [msg]
  (let [buffer (ChannelBuffers/dynamicBuffer)]
    (with-open [input-stream (ChannelBufferOutputStream. buffer)]
      (spit input-stream msg)
      buffer)))