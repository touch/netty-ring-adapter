(ns netty.ring.adapter
  (:require [netty.ring.request :as request]
            [netty.ring.response :as response]
            [netty.ring.writers :as writers])
  (:import [java.util.concurrent Executors]
           [java.net InetSocketAddress]
           [org.jboss.netty.channel.socket.nio NioServerSocketChannelFactory]
           [org.jboss.netty.bootstrap ServerBootstrap]
           [org.jboss.netty.handler.stream ChunkedWriteHandler]
           [org.jboss.netty.handler.execution ExecutionHandler OrderedMemoryAwareThreadPoolExecutor]
           [org.jboss.netty.channel
            ChannelHandlerContext
            Channels
            SimpleChannelUpstreamHandler
            ChannelPipelineFactory
            ExceptionEvent
            MessageEvent
            Channel]
           [org.jboss.netty.handler.codec.http
            HttpRequestDecoder
            HttpResponseEncoder
            HttpChunkAggregator]))

(def default-options
  {:port 8080
   :zero-copy false
   :channel-options {"child.tcpNoDelay" true "child.keepAlive" true "reuseAddress" true}
   :max-http-chunk-length 1048576
   :number-of-handler-threads 16
   :max-channel-memory-size 1048576
   :max-total-memory-size 1048576})

(defn- create-handler-factory [handler zero-copy]
  #(proxy [SimpleChannelUpstreamHandler] []
     (messageReceived [context ^MessageEvent event]
       (binding [writers/*zero-copy* zero-copy]
         (->> (.getMessage event)
           (request/create-ring-request context)
           handler
           (response/write-ring-response context))))
     (exceptionCaught [context ^ExceptionEvent evt]
       (-> evt .getChannel .close))))

(defn- pipeline-factory [handler execution-handler options]
  (reify ChannelPipelineFactory
    (getPipeline [this]
      (doto (Channels/pipeline)
        (.addLast "decoder" (HttpRequestDecoder.))
        (.addLast "chunkedAggregator" (HttpChunkAggregator. (:max-http-chunk-length options)))
        (.addLast "encoder" (HttpResponseEncoder.))
        (.addLast "chunkedWriter" (ChunkedWriteHandler.))
        (.addLast "execution" execution-handler)
        (.addLast "handler" (handler))))))

(defn- create-bootstrap [channel-factory pipeline options]
  (doto (ServerBootstrap. channel-factory)
    (.setPipelineFactory pipeline)
    (.setOptions options)))

(defn- create-execution-handler [options]
  (let [{:keys [number-of-handler-threads max-channel-memory-size max-total-memory-size]} options
        executor (OrderedMemoryAwareThreadPoolExecutor. number-of-handler-threads max-channel-memory-size max-total-memory-size)]
    (ExecutionHandler. executor)))

(defn start-server
  ([handler] (start-server handler {}))
  ([handler user-options]
    (let [options (merge default-options user-options)
          channel-factory (NioServerSocketChannelFactory.)
          handler (create-handler-factory handler (:zero-copy options))
          execution-handler (create-execution-handler options)
          pipeline (pipeline-factory handler execution-handler options)
          ^ServerBootstrap bootstrap (create-bootstrap channel-factory pipeline (:channel-options options))
          bind-address (InetSocketAddress. (:port options))
          ^Channel channel (.bind bootstrap bind-address)]
      (fn []
        (.close channel)
        (.releaseExternalResources channel-factory)
        (.releaseExternalResources execution-handler)))))