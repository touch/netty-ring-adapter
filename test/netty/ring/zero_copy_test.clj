(ns netty.ring.zero-copy-test
  (:use clojure.test
        netty.ring.adapter
        compojure.core)
  (:require [clj-http.client :as client]
            [compojure.route :as route]
            [clojure.java.io :as io]))

(deftest zero-copy-file-body
  (let [response (client/get "http://localhost:8080/FileResponse")]
    (is (= (slurp "./test/netty/ring/response.txt") (:body response)))
    (is (= "true" (get-in response [:headers "zero-copy"])))))

(defroutes test-routes
  (GET "/FileResponse" [] {:status 200 :body (io/file "./test/netty/ring/response.txt")})
  (route/not-found "Unknown"))

(defn server-fixture [f]
  (let [shutdown (start-server test-routes {:port 8080 :zero-copy true})]
    (f)
    (shutdown)))

(use-fixtures :each server-fixture)
