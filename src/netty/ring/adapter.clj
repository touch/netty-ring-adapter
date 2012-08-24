(ns netty.ring.adapter
  (:require [netty.ring.request :as request]
            [netty.ring.response :as response]
            [netty.ring.writers :as writers])
  (:import [java.util.concurrent Executors]
           [java.net InetSocketAddress]
           [org.jboss.netty.channel.socket.nio NioServerSocketChannelFactory]
           [org.jboss.netty.bootstrap ServerBootstrap]
           [org.jboss.netty.handler.stream ChunkedWriteHandler]
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

(defn- create-handler-factory [handler options]
  #(proxy [SimpleChannelUpstreamHandler] []
     (messageReceived [context ^MessageEvent event]
       (binding [writers/*zero-copy* (options :zero-copy false)]
         (->> (.getMessage event)
           (request/create-ring-request context)
           handler
           (response/write-ring-response context))))
     (exceptionCaught [context ^ExceptionEvent evt]
       (-> evt .getChannel .close))))

(defn- pipeline-factory [handler]
  (reify ChannelPipelineFactory
    (getPipeline [this]
      (doto (Channels/pipeline)
        (.addLast "decoder" (HttpRequestDecoder.))
        (.addLast "chunkedAggregator" (HttpChunkAggregator. 1048576))
        (.addLast "encoder" (HttpResponseEncoder.))
        (.addLast "chunkedWriter" (ChunkedWriteHandler.))
        (.addLast "handler" (handler))))))

(defn- create-bootstrap [channel-factory pipeline]
  (doto (ServerBootstrap. channel-factory)
    (.setPipelineFactory pipeline)
    (.setOption "child.tcpNoDelay" true)
    (.setOption "child.keepAlive" true)))

(defn start-server [handler options]
  (let [channel-factory (NioServerSocketChannelFactory.)
        pipeline (pipeline-factory (create-handler-factory handler options))
        ^ServerBootstrap bootstrap (create-bootstrap channel-factory pipeline)
        bind-address (InetSocketAddress. (options :port 8080))
        ^Channel channel (.bind bootstrap bind-address)]
    (fn []
      (.close channel)
      (.releaseExternalResources channel-factory))))