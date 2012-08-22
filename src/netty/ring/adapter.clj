(ns netty.ring.adapter
  (:require [clojure.string :as s]
            [netty.ring.request :as request]
            [netty.ring.response :as response])
  (:import [java.util.concurrent Executors]
           [java.net InetSocketAddress]
           org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory
           [org.jboss.netty.bootstrap ServerBootstrap]
           [org.jboss.netty.handler.stream ChunkedWriteHandler]
           [org.jboss.netty.channel
            ChannelHandlerContext
            Channels
            SimpleChannelUpstreamHandler
            ChannelPipelineFactory]
           [org.jboss.netty.handler.codec.http
            HttpRequestDecoder
            HttpResponseEncoder
            HttpChunkAggregator]))

(defn- create-handler [handler]
  (proxy [SimpleChannelUpstreamHandler] []
    (messageReceived [context event]
      (-> (request/create-ring-request context (.getMessage event))
        handler
        (response/write-ring-response context)))
    (exceptionCaught [context evt]
      (-> evt .getChannel .close))))

(defn- create-pipeline [handler]
  (doto (Channels/pipeline)
    (.addLast "decoder" (HttpRequestDecoder.))
    (.addLast "chunkedAggregator" (HttpChunkAggregator. 1048576))
    (.addLast "encoder" (HttpResponseEncoder.))
    (.addLast "chunkedWriter" (ChunkedWriteHandler.))
    (.addLast "handler" (create-handler handler))))

(defn- pipeline-factory [handler]
  (reify ChannelPipelineFactory
    (getPipeline [this] (create-pipeline handler))))

(defn- create-bootstrap [channel-factory pipeline]
  (let [bootstrap (ServerBootstrap. channel-factory)]
    (doto bootstrap
      (.setPipelineFactory pipeline)
      (.setOption "child.tcpNoDelay" true)
      (.setOption "child.keepAlive" true))))

(defn start-server [handler options]
  (let [channel-factory (NioServerSocketChannelFactory.)
        pipeline (pipeline-factory handler)
        bootstrap (create-bootstrap channel-factory pipeline)
        bind-address (InetSocketAddress. (options :port 8080))
        channel (.bind bootstrap bind-address)]
    (fn []
      (.close channel)
      (.releaseExternalResources channel-factory))))