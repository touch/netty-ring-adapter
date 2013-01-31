# netty-ring-adapter

netty-ring-adapter is a ring server built with [Netty](https://netty.io/). netty-ring-adapter is designed to be a drop in ring
adapter that should work just like reference ring adapter.

[![Build Status](https://secure.travis-ci.org/aesterline/netty-ring-adapter.png)](http://travis-ci.org/aesterline/netty-ring-adapter)

## Installation

`netty-ring-adapter` is available as a Maven artifact from
[Clojars](http://clojars.org/netty-ring-adapter):

```clojure
[netty-ring-adapter "0.4.2"]
```

Previous versions available as

```clojure
[netty-ring-adapter "0.4.0"]
[netty-ring-adapter "0.3.2"]
[netty-ring-adapter "0.3.0"]
[netty-ring-adapter "0.2.5"]
```

## Usage

```clj
(use 'netty.ring.adapter)

(defn handler [req]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "Hello world from Netty"})

(def shutdown (start-server handler {:port 8080}))

;; If you want to stop the server, just invoke the function returned from the `start-server` function.
(shutdown)
```

The server currently supports the following options when starting the server.

```clj
{ :port 8080                        ;; The port in which the server will be listening for requests
  :zero-copy true                   ;; Should the server send file response bodies with Netty's FileRegion functionality
  :channel-options                  ;; Channel options passed to the ServerBootstrap.setOptions
    { "child.tcpNoDelay" true}
  :max-http-chunk-length 1048576    ;; The maximum length of the aggregated content
  :number-of-handler-threads 16     ;; The number of threads that will be used to handle requests.
                                    ;; These threads are used to allow the handler function to work without blocking an I/O
                                    ;; worker thread.
  :max-channel-memory-size 1048576  ;; the maximum total size of the queued events per channel
  :max-total-memory-size 1048576    ;; the maximum total size of the queued events
  :debug :slf4j }                   ;; turns on debugging using the slf4j as a logging framework.
                                    ;; debugging options include (:commons :jboss :log4j :slf4j :jdk)
```

Using `:zero-copy` may not work in all cases depending on your operating system and JVM version. Please see
[FileRegion](http://static.netty.io/3.5/api/org/jboss/netty/channel/FileRegion.html) for more information.

## Development

To run the tests:

    $ lein deps
    $ lein test

## TODO

* ~~Support HTTP Keep-Alive? Not sure if this already works, but it should.~~
* Add metrics. Seems like it would be good to add JMX metrics to the various queues and requests/responses.
* Create a lein plugin for the netty-ring-adapter.
* SSL support


## License

Distributed under the Eclipse Public License, the same as Clojure. <http://opensource.org/licenses/eclipse-1.0.php>
