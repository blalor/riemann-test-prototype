;; entrypoint
(ns rtp.bin
    (:gen-class :name rtp.bin)
    (:require
        [riemann.bin]
        [riemann.logging]
        [riemann.time]
        [riemann.config]
        [clojure.tools.logging :refer :all]
        
        [rtp.sources.riemann :as riemann]
    )
)

(defn- init
    "sets up Riemann config"
    []
    
    ;; set up servers, which generate their own metrics
    (riemann.config/tcp-server)
    (riemann.config/udp-server)
    (riemann.config/ws-server)
    (riemann.config/sse-server)
    (riemann.config/repl-server)
    
    ;; connect the streams
    (riemann.config/streams
        (riemann/init prn)
    )
)

(defn -main
    "start Riemann"
    []
    
    (when (nil? (System/getProperty "log4j.configuration"))
        (riemann.logging/init))

    (try
        (info "PID" (riemann.bin/pid))
        (riemann.time/start!)
        (init)
        (riemann.config/apply!)
        nil
        (catch Exception e
            (error e "Couldn't start"))
    )
)
