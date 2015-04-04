;; entrypoint
(ns rtp.bin
    (:gen-class :name rtp.bin)
    (:require
        [riemann.bin]
        [riemann.logging]
        [riemann.time]
        [riemann.config]
        [clojure.tools.logging :refer :all]
    )
)

(defn- init
    "sets up Riemann config"
    []
    (prn "initializing")
    (riemann.config/streams
        prn)
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
