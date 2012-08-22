(ns netty.ring.adapter-test
  (:refer-clojure :exclude [get])
  (:use clojure.test
        netty.ring.adapter
        compojure.core)
  (:require [clj-http.client :as client]
            [compojure.route :as route]
            [clojure.java.io :as io]))

(def ^:const server "http://localhost:8080")
(declare get post put make-request)

(deftest simple
  (is (= (get "/") "Hello World")))

(deftest request-method
  (is (= (get "/method") "get"))
  (is (= (post "/method") "post"))
  (is (= (put "/method") "put")))

(deftest uri
  (is (= (get "/uri/me") "/uri/me"))
  (is (= (get "/uri/you?help=me") "/uri/you")))

(deftest query-string
  (is (= (get "/query?you=me") "you=me"))
  (is (= (get "/query?me=you&you=I") "me=you&you=I")))

(deftest server-name
  (is (= (get "/serverName") "localhost")))

(deftest server-port
  (is (= (get "/port") "8080")))

(deftest remote-address
  (is (= (get "/remoteAddress") "127.0.0.1")))

(deftest scheme
  (is (= (get "/scheme") "http"))
  (is (= (make-request :get "/scheme" {:headers {"X-Scheme" "https"}}) "https")))

(deftest content-type
  (is (= (make-request :get "/contentType" {:content-type :json}) "application/json"))
  (is (= (make-request :get "/contentType" {:content-type :html}) "application/html")))

(deftest chracter-encoding
  (is (= (:body (client/post (str server "/characterEncoding") {:headers {"Content-Encoding" "UTF-8"}}) "UTF-8"))))

(deftest headers
  (is (= (get "/headers") "localhost:8080")))

(deftest response-headers
  (is (= (:headers (client/get (str server "/responseHeaders/single")) {"foo" "bar"})))
  (is (= (:headers (client/get (str server "/responseHeaders/multiple"))) {"foo" ["bar" "baz"]})))

(deftest response-body-types
  (is (= "agoodresponse" (get "/ISeqResponse")))
  (is (= "afineresponse" (get "/InputStreamResponse")))
  (println "file body")
  (println "zero copy file body"))

(deftest bad-responses
  (println "empty response")
  (println "exception thrown during handle"))

(defn header-handler [request]
  (if (.contains (:uri request) "single")
    {:status 200 :headers {"foo" "bar"}}
    {:status 200 :headers {"foo" ["bar" "baz"]}}))

(defroutes test-routes
  (GET "/" [] "Hello World")
  (ANY "/method" [] #(name (:request-method %)))
  (GET "/uri/*" [] #(:uri %))
  (GET "/query" [] #(:query-string %))
  (GET "/serverName" [] #(:server-name %))
  (GET "/port" [] #(str (:server-port %)))
  (GET "/remoteAddress" [] #(:remote-addr %))
  (GET "/scheme" [] #(name (:scheme %)))
  (GET "/contentType" [] #(:content-type %))
  (POST "/characterEncoding" [] #(:character-encoding %))
  (GET "/headers" [] #((:headers %) "host"))
  (GET "/responseHeaders/*" [] header-handler)
  (GET "/ISeqResponse" [] {:status 200 :body '("a" "good" "response")})
  (GET "/InputStreamResponse" [] {:status 200 :body (io/input-stream (.getBytes "afineresponse"))})
  (route/not-found "Unknown"))

(defn server-fixture [f]
  (let [shutdown (start-server test-routes {:port 8080})]
    (f)
    (shutdown)))

(use-fixtures :each server-fixture)

(defn make-request [method path options]
  (:body (client/request (merge {:method method :url (str server path)} options))))

(defn request [f]
  #(:body (f (str server %))))

(def get (request client/get))
(def post (request client/post))
(def put (request client/put))
