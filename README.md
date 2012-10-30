# netty-ring-adapter

netty-ring-adapter is a ring server built with [Netty](https://netty.io/). netty-ring-adapter is designed to be a drop in ring
adapter that should work just like reference ring adapter.

[![Build Status](https://secure.travis-ci.org/aesterline/netty-ring-adapter.png)](http://travis-ci.org/aesterline/netty-ring-adapter)

## Installation

`netty-ring-adapter` is available as a Maven artifact from
[Clojars](http://clojars.org/netty-ring-adapter):

```clojure
[netty-ring-adapter "0.2.4"]
```

Previous versions available as

```clojure
[netty-ring-adapter "0.2.3"]
[netty-ring-adapter "0.2.2"]
[netty-ring-adapter "0.2.1"]
[netty-ring-adapter "0.2.0"]
[netty-ring-adapter "0.1.0"]
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
{ :port 8080        ;; The port in which the server will be listening for requests
  :zero-copy true } ;; Should the server send file response bodies with Netty's FileRegion functionality
```

Using `:zero-copy` may not work in all cases depending on your operating system and JVM version. Please see
[FileRegion](http://static.netty.io/3.5/api/org/jboss/netty/channel/FileRegion.html) for more information.

## Development

To run the tests:

    $ lein2 deps
    $ lein2 test


## License

Distributed under the Eclipse Public License, the same as Clojure. <http://opensource.org/licenses/eclipse-1.0.php>
