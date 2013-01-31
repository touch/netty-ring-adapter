(ns netty.ring.adapter
  (:require [netty.ring.request :as request]
            [netty.ring.response :as response]
            [netty.ring.writers :as writers]
            [clojure.tools.logging :as log])
  (:import [java.util.concurrent Executors]
           [java.net InetSocketAddress]
           [org.jboss.netty.channel.socket.nio NioServerSocketChannelFactory]
           [org.jboss.netty.bootstrap ServerBootstrap]
           [org.jboss.netty.handler.stream ChunkedWriteHandler]
           [org.jboss.netty.handler.execution ExecutionHandler OrderedMemoryAwareThreadPoolExecutor]
           [org.jboss.netty.handler.logging LoggingHandler]
           [org.jboss.netty.logging InternalLoggerFactory]
           [org.jboss.netty.channel ChannelHandlerContext Channels SimpleChannelUpstreamHandler ChannelPipelineFactory]
           [org.jboss.netty.handler.codec.http HttpRequestDecoder HttpResponseEncoder HttpChunkAggregator HttpHeaders]))

(def default-options
  {:port 8080
   :zero-copy false
   :channel-options {"child.tcpNoDelay" true "child.keepAlive" true "reuseAddress" true}
   :max-http-chunk-length 1048576
   :number-of-handler-threads 16
   :max-channel-memory-size 1048576
   :max-total-memory-size 1048576})

(defn- add-keep-alive [http-request ring-response]
  (if (HttpHeaders/isKeepAlive http-request)
    (assoc-in ring-response [:headers "connection"] "keep-alive")
    ring-response))

(defn- create-handler-factory [handler zero-copy]
  #(proxy [SimpleChannelUpstreamHandler] []
     (messageReceived [context event]
       (binding [writers/*zero-copy* zero-copy]
         (let [http-request (.getMessage event)]
           (->> http-request
             (request/create-ring-request context)
             handler
             (add-keep-alive http-request)
             (response/write-ring-response context)))))
     (exceptionCaught [context evt]
       (log/error (.getCause evt) "Error occurred in I/O thread")
       (when (-> context .getChannel .isOpen)
         (response/write-ring-response context {:status 500})))))

(defn- create-logging-factory [debug-type]
  (case debug-type
    :commons (org.jboss.netty.logging.CommonsLoggerFactory.)
    :jboss (org.jboss.netty.logging.JBossLoggerFactory.)
    :log4j (org.jboss.netty.logging.Log4JLoggerFactory.)
    :slf4j (org.jboss.netty.logging.Slf4JLoggerFactory.)
    (org.jboss.netty.logging.JdkLoggerFactory.)))

(defn- pipeline-factory [handler execution-handler options]
  (reify ChannelPipelineFactory
    (getPipeline [this]
      (let [pipeline (Channels/pipeline)]
        (when (:debug options) (.addLast pipeline "logger" (LoggingHandler.)))

        (doto pipeline
          (.addLast "decoder" (HttpRequestDecoder.))
          (.addLast "chunkedAggregator" (HttpChunkAggregator. (:max-http-chunk-length options)))
          (.addLast "encoder" (HttpResponseEncoder.))
          (.addLast "chunkedWriter" (ChunkedWriteHandler.))
          (.addLast "execution" execution-handler)
          (.addLast "handler" (handler)))))))

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
    (when (:debug user-options)
      (InternalLoggerFactory/setDefaultFactory (create-logging-factory (:debug user-options))))

    (let [options (merge default-options user-options)
          channel-factory (NioServerSocketChannelFactory.)
          handler (create-handler-factory handler (:zero-copy options))
          execution-handler (create-execution-handler options)
          pipeline (pipeline-factory handler execution-handler options)
          bootstrap (create-bootstrap channel-factory pipeline (:channel-options options))
          bind-address (InetSocketAddress. (:port options))
          channel (.bind bootstrap bind-address)]
      (fn []
        (.close channel)
        (.releaseExternalResources channel-factory)
        (.releaseExternalResources execution-handler)))))