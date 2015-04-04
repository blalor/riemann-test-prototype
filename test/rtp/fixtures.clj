(ns rtp.fixtures
    (:require
        [riemann.test]
        [riemann.config]
        [riemann.time]
    )
)

(def ^:dynamic *captured-events*
  "A vector of captured events." nil)

(defn capture-event [e]
    (swap! *captured-events* conj e)
)

(defn once
    "(use-fixtures :once rtp.fixtures/once)"
    [f]
    
    (binding [
        ;; from riemann.bin/-main
        riemann.test/*streams* (:streams @riemann.config/next-core)
        
        ;; from riemann.test/with-test-env
        ;; we're not using taps, but riemann.test/inject! is
        riemann.test/*taps* (atom {})
    ]
        (f)
    )
)

(defn each
     "(use-fixtures :each rtp.fixtures/each)"
   [f]
    
    (binding [
        *captured-events* (atom [])
    ]
        ;; from riemann.test/deftest
        (riemann.time.controlled/with-controlled-time!
            (riemann.time.controlled/reset-time!)
            
            (f)
        )
    )
)
