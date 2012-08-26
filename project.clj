(defproject netty-ring-adapter "0.2.1"
  :description "Ring server built with Netty (https://netty.io/)"
  :url "http://github.com/aesterline/netty-ring-adapter"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [io.netty/netty "3.5.5.Final"]]
  :profiles {:dev {:dependencies [[clj-http "0.5.2"]
                                  [compojure "1.1.1"]]}})
