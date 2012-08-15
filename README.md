# netty-ring-adapter

netty-ring-adapter is a ring server built with [Netty](https://netty.io/). netty-ring-adapter is designed to be a drop in ring
adapter that should work just like reference ring adapter.

[![Build Status](https://secure.travis-ci.org/aesterline/netty-ring-adapter.png)](http://travis-ci.org/aesterline/netty-ring-adapter)

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

## Development

To run the tests:

    $ lein2 deps
    $ lein2 test


## License

The use and distribution terms for this software are covered by the
Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
which can be found in the file epl-v10.html at the root of this distribution.
By using this software in any fashion, you are agreeing to be bound by
the terms of this license.
You must not remove this notice, or any other, from this software.

Copyright © 2012 Adam Esterline. All rights reserved.

Distributed under the Eclipse Public License, the same as Clojure.
