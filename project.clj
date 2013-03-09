(defproject netty-ring-adapter "0.4.3"
  :description "Ring server built with Netty (https://netty.io/)"
  :url "http://github.com/RallySoftware/netty-ring-adapter"
  :license {:name "Eclipse Public License 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[io.netty/netty "3.6.3.Final"]
                 [org.clojure/tools.logging "0.2.6"]]
  :profiles {:shared {:dependencies [[clj-http "0.6.5"] [compojure "1.1.5"]]}
             :dev [:shared {:dependencies [[org.clojure/clojure "1.4.0"]]}]
             :1.5 [:shared {:dependencies [[org.clojure/clojure "1.5.0"]]}]}
  :jvm-opts ["-Djava.util.logging.config.file=logging.properties"])
