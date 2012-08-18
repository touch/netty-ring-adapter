(ns netty.ring.adapter
  (:require [clojure.string :as s]
            [netty.ring.request :as request])
  (:import [java.util.concurrent Executors]
           [java.net InetSocketAddress]
           org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory
           [org.jboss.netty.bootstrap ServerBootstrap]
           [org.jboss.netty.buffer ChannelBufferInputStream ChannelBuffers ChannelBufferOutputStream]
           [org.jboss.netty.channel
            ChannelHandlerContext
            Channels
            SimpleChannelUpstreamHandler
            ChannelFutureListener
            ChannelPipelineFactory]
           [org.jboss.netty.handler.codec.http
            HttpRequestDecoder
            HttpResponseEncoder
            HttpChunkAggregator
            DefaultHttpResponse
            HttpResponseStatus
            HttpVersion
            HttpHeaders
            HttpResponse
            HttpRequest
            HttpMethod]))


(defn- create-ring-request [^ChannelHandlerContext context ^HttpRequest http-request]
  (let [[uri query] (request/url (.getUri http-request))]
    {:body (ChannelBufferInputStream. (.getContent http-request))
     :uri uri
     :query-string query
     :request-method (request/method (.getMethod http-request))
     :server-name (request/server-name context http-request)}))

(defn- write-response [context response]
  (let [channel (.getChannel context)]
    (doto (.write channel response)
      (.addListener ChannelFutureListener/CLOSE))))

(defn- write-ring-response [context ring-response]
  (let [status (HttpResponseStatus/valueOf (ring-response :status 200))
        response (DefaultHttpResponse. HttpVersion/HTTP_1_1 status)
        buffer (ChannelBuffers/dynamicBuffer 100)
        output (ChannelBufferOutputStream. buffer)]
    (.writeBytes output (:body ring-response))
    (.setContent response buffer)
    (write-response context response)))

(defn- create-handler [handler]
  (proxy [SimpleChannelUpstreamHandler] []
    (messageReceived [context event]
      (let [ring-request (create-ring-request context (.getMessage event))
            ring-response (handler ring-request)]
        (when ring-response
          (write-ring-response context ring-response))))
    (exceptionCaught [ctx evt]
      (-> evt .getChannel .close))))

(defn- create-pipeline [handler]
  (doto (Channels/pipeline)
    (.addLast "decoder" (HttpRequestDecoder.))
    (.addLast "chunked" (HttpChunkAggregator. 1048576))
    (.addLast "encoder" (HttpResponseEncoder.))
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