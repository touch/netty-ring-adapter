(ns netty.ring.request-test
  (:use clojure.test)
  (:require [netty.ring.request :as request])
  (:import [org.jboss.netty.handler.codec.http HttpMethod]))

(deftest should-understand-all-methods
  (is (= :put (request/method HttpMethod/PUT)))
  (is (= :options (request/method HttpMethod/OPTIONS)))
  (is (= :cool (request/method (HttpMethod. "COOL")))))

(deftest should-parse-url-into-parts
  (is (= ["/help" nil] (request/url "/help")))
  (is (= ["/go" "you=me"] (request/url "/go?you=me")))
  (is (= ["/fun" "I=me&you=your"] (request/url "/fun?I=me&you=your"))))
