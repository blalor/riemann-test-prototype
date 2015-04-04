;; stream to rewrite Riemann's internal instrumentation metrics as something a
;; little more friendly to time-series backends.
(ns rtp.sources.riemann
    (:require
        [riemann.streams :refer [sdo smap where]]
    )
)

(defn- rewrite
    "rewrites internally-generated Riemann instrumentation metrics so they're
    less human-readable and more friendly to Graphite"
    
    [child-stream]
    
    ;; {:host "Crazy-Harry.local",
    ;;  :service "riemann streams latency 0.999",
    ;;  :state nil,
    ;;  :description nil,
    ;;  :metric 24.135277,
    ;;  :tags ["riemann"],
    ;;  :time 1428146742203/1000,
    ;;  :ttl 20}
    ;; will have the service rewritten as "streams.latency.0.999"
    
    (smap
        (fn [event]
            (let [
                service (:service event)
                
                ;; if pattern matches then …
                new-service (cond
                    ;; drop address
                    ;; riemann server tcp 0.0.0.0:5555 in rate → server.tcp.in.rate
                    (not (nil? (re-find #"^riemann server" service)))
                        (clojure.string/join "."
                            (keep-indexed (fn [idx item] (if (not (= 2 idx)) item))
                                (rest (clojure.string/split service #" "))
                            )
                        )

                    ;; riemann netty execution-handler queue size → netty.execution-handler.queue.size
                    ;; riemann streams latency 0.0 → streams.latency.0.0
                    (not (nil? (re-find #"^riemann (netty|streams)" service)))
                        (clojure.string/join "." (rest (clojure.string/split service #" ")))
                    
                    :else service ;; default if no match
                )
            ]
                (assoc event :service new-service
                             :tags (distinct (concat ["riemann"] (:tags event))))
            )
        )

        child-stream
    )
)

(defn init
    [& children]

    (let [
        dispatch (apply sdo children)
    ]
        (where (service #"^riemann ")
            (rewrite dispatch)
        )
    )
)
