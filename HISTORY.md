## 0.4.5 (2013-04-12)
* Updated Netty to 3.6.5-Final

## 0.4.4 (2013-03-20)
* Fixed a bug with file content type detection
* No longer trying to detect file content types.

## 0.4.3 (2013-03-09)
* Updated Netty to 3.6.3-Final
* Updated org.clojure/tools.logging to 0.2.6
* Started testing with the final release of Clojure 1.5

## 0.4.2 (2013-01-30)
* Fixed a defect that caused the server to consume CPU cycles when a client closes the socket before a response is sent.

## 0.4.1 (2013-01-28)
* Added error logging when exceptions are thrown during processing.

## 0.4.0 (2013-01-27)
* Added support for HTTP keep-alive

## 0.3.2 (2013-01-25)
* Added the ability to turn on debug logging (Netty logging).
* Debugging frameworks supported (:commons :jboss :log4j :slf4j :jdk)

## 0.3.0 (2013-01-24)
* Updated Netty to 3.6.2.Final
* All handler processing is now done in a different thread from the IO worker threads.
* Added many more options that can help configure the behavior of the adapter

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
```

## 0.2.5 (2013-01-02)
* Updated Netty to 3.6.0.Final

## 0.2.4 (2012-10-29)
* Updated Netty to 3.5.9.Final

## 0.2.3 (2012-09-18)
* Updated license name

## 0.2.2 (2012-09-12)
* Updated Netty to 3.5.7.Final

## 0.2.1 (2012-08-25)

* Updated Netty to 3.5.5.Final
* Removed some reflection warnings
* Fixed bug that would cause the server to not respond if the response :body was null

## 0.2.0 (2012-08-22)

* netty-ring-adapter now supporting all the required [Ring SPEC](https://github.com/ring-clojure/ring/blob/master/SPEC) body types

## 0.1.0 (2012-08-19)

* First numbered release
* Currently only supports string response :body type
