(ns netty.ring.buffers-test
  (:require [netty.ring.buffers :as b])
  (:use clojure.test))

(deftest string-to-buffer
  (let [s "this is a string"
        buffer (b/to-buffer s)]
    (is (= s (b/buffer->string buffer)))))

(deftest seq-to-buffer
  (let [s '("this" "is" "a" "string")
        buffer (b/to-buffer s)]
    (is (= (apply str s) (b/buffer->string buffer)))))


