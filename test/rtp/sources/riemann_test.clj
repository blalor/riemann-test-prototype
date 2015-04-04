(ns rtp.sources.riemann-test
    (:require
        ;; the library we're testing
        [rtp.sources.riemann :as riemann]
        
        ;; supporting libraries and functions
        [rtp.fixtures   :refer [*captured-events* capture-event]]
        [riemann.common :refer [event]]
        [riemann.test   :refer [inject!]]
        [clojure.test   :refer [deftest testing use-fixtures is]]
    )
)

;; these fixtures do most of what's provided by riemann.test/deftest and the
;; setup performed by "riemann test /my/config.clj", but do not support "tap"
;; and "io".
(use-fixtures :once rtp.fixtures/once)
(use-fixtures :each rtp.fixtures/each)

; {:host "Crazy-Harry.local", :service "riemann streams rate", :state nil, :description nil, :metric 4.563966211587358, :tags ["riemann"], :time 1428146742203/1000, :ttl 20}
(deftest rewrites-streams-rate
    ;; riemann.test/inject! optionally takes a sequence of streams as its first
    ;; argument; we'll exploit that to focus on our code under test.
    (inject!
        [ (riemann/init capture-event) ]
        [
            {:host "localhost",
             :service "riemann streams rate",
             :state nil,
             :description nil,
             :metric 0.6,
             :tags ["riemann"],
             :time 142814362667/100,
             :ttl 20}
        ]
    )
    
    ;; rtp.fixtures/capture-event does what it says on the label, and stores the
    ;; events it receives in the dynamically-bound vector rtp.fixtures/*captured-events*
    (let [
        event (first @*captured-events*)
    ]
        ;; now we can just make our assertions!
        
        ;; service was rewritten
        (is (= "streams.rate" (:service event)))
        
        ;; everything else was passed through unchanged
        (is (= "localhost"      (:host event)))
        (is (nil?               (:state event)))
        (is (nil?               (:description event)))
        (is (= 0.6              (:metric event)))
        (is (= ["riemann"]      (:tags event)))
        (is (= 142814362667/100 (:time event)))
        (is (= 20               (:ttl event)))
    )
)

; {:host "Crazy-Harry.local", :service "riemann netty event-executor threads active", :state "ok", :description nil, :metric 4, :tags nil, :time 1428146742203/1000, :ttl 20}
(deftest rewrites-netty-threads-active
    (inject!
        [ (riemann/init capture-event) ]
        [
            
            {:host "localhost",
             :service "riemann netty event-executor threads active",
             :state nil,
             :description nil,
             :metric 0.6,
             :tags ["riemann"],
             :time 142814362667/100,
             :ttl 20}
        ]
    )
    
    (let [
        event (first @*captured-events*)
    ]
        (is (= "netty.event-executor.threads.active" (:service event)))
    )
)

; {:host "Crazy-Harry.local", :service "riemann server udp 0.0.0.0:5555 in rate", :state "ok", :description nil, :metric 0.0, :tags nil, :time 357036685551/250, :ttl 20}
(deftest rewrites-udp-server-rate
    (inject!
        [ (riemann/init capture-event) ]
        [
            
            {:host "localhost",
             :service "riemann server udp 0.0.0.0:5555 in rate",
             :state nil,
             :description nil,
             :metric 0.6,
             :tags ["riemann"],
             :time 142814362667/100,
             :ttl 20}
        ]
    )
    
    (let [
        event (first @*captured-events*)
    ]
        (is (= "server.udp.in.rate" (:service event)))
    )
)

(deftest drops-events-it-doesn't-understand
    (inject!
        [ (riemann/init capture-event) ]
        [
            (event {:host "localhost",
                    :service "bagels eaten",
                    :metric 3,
                    :tags ["carbs"],
                    :time 142814362667,
                    :ttl 172800})
        ]
    )
    
    (is (= 0 (count @*captured-events*)))
)

;; in case I get bored:
; {:host "Crazy-Harry.local", :service "riemann streams latency 0.0",                       :state nil,  :description nil, :metric 0.260191,            :tags ["riemann"], :time 1428146742203/1000, :ttl 20}
; {:host "Crazy-Harry.local", :service "riemann streams latency 0.5",                       :state nil,  :description nil, :metric 0.44662599999999997, :tags ["riemann"], :time 1428146742203/1000, :ttl 20}
; {:host "Crazy-Harry.local", :service "riemann streams latency 0.95",                      :state nil,  :description nil, :metric 0.886211,            :tags ["riemann"], :time 1428146742203/1000, :ttl 20}
; {:host "Crazy-Harry.local", :service "riemann streams latency 0.99",                      :state nil,  :description nil, :metric 24.135277,           :tags ["riemann"], :time 1428146742203/1000, :ttl 20}
; {:host "Crazy-Harry.local", :service "riemann streams latency 0.999",                     :state nil,  :description nil, :metric 24.135277,           :tags ["riemann"], :time 1428146742203/1000, :ttl 20}
; {:host "Crazy-Harry.local", :service "riemann server tcp 0.0.0.0:5555 conns",             :state "ok", :description nil, :metric 1,                   :tags nil,         :time 357036685551/250,   :ttl 20}
; {:host "Crazy-Harry.local", :service "riemann server tcp 0.0.0.0:5555 in rate",           :state "ok", :description nil, :metric 0.0,                 :tags nil,         :time 357036685551/250,   :ttl 20}
; {:host "Crazy-Harry.local", :service "riemann server tcp 0.0.0.0:5555 in latency 0.0",    :state "ok", :description nil, :metric nil,                 :tags nil,         :time 357036685551/250,   :ttl 20}
; {:host "Crazy-Harry.local", :service "riemann server tcp 0.0.0.0:5555 in latency 0.5",    :state "ok", :description nil, :metric nil,                 :tags nil,         :time 357036685551/250,   :ttl 20}
; {:host "Crazy-Harry.local", :service "riemann server tcp 0.0.0.0:5555 in latency 0.95",   :state "ok", :description nil, :metric nil,                 :tags nil,         :time 357036685551/250,   :ttl 20}
; {:host "Crazy-Harry.local", :service "riemann server tcp 0.0.0.0:5555 in latency 0.99",   :state "ok", :description nil, :metric nil,                 :tags nil,         :time 357036685551/250,   :ttl 20}
; {:host "Crazy-Harry.local", :service "riemann server tcp 0.0.0.0:5555 in latency 0.999",  :state "ok", :description nil, :metric nil,                 :tags nil,         :time 357036685551/250,   :ttl 20}
; {:host "Crazy-Harry.local", :service "riemann server udp 0.0.0.0:5555 in latency 0.0",    :state "ok", :description nil, :metric nil,                 :tags nil,         :time 357036685551/250,   :ttl 20}
; {:host "Crazy-Harry.local", :service "riemann server udp 0.0.0.0:5555 in latency 0.5",    :state "ok", :description nil, :metric nil,                 :tags nil,         :time 357036685551/250,   :ttl 20}
; {:host "Crazy-Harry.local", :service "riemann server udp 0.0.0.0:5555 in latency 0.95",   :state "ok", :description nil, :metric nil,                 :tags nil,         :time 357036685551/250,   :ttl 20}
; {:host "Crazy-Harry.local", :service "riemann server udp 0.0.0.0:5555 in latency 0.99",   :state "ok", :description nil, :metric nil,                 :tags nil,         :time 357036685551/250,   :ttl 20}
; {:host "Crazy-Harry.local", :service "riemann server udp 0.0.0.0:5555 in latency 0.999",  :state "ok", :description nil, :metric nil,                 :tags nil,         :time 357036685551/250,   :ttl 20}
; {:host "Crazy-Harry.local", :service "riemann server ws 0.0.0.0:5556 conns",              :state "ok", :description nil, :metric 0,                   :tags nil,         :time 285629348441/200,   :ttl 20}
; {:host "Crazy-Harry.local", :service "riemann server ws 0.0.0.0:5556 out rate",           :state "ok", :description nil, :metric 0.0,                 :tags nil,         :time 285629348441/200,   :ttl 20}
; {:host "Crazy-Harry.local", :service "riemann server ws 0.0.0.0:5556 in rate",            :state "ok", :description nil, :metric 0.0,                 :tags nil,         :time 285629348441/200,   :ttl 20}
; {:host "Crazy-Harry.local", :service "riemann server ws 0.0.0.0:5556 out latency 0.0",    :state "ok", :description nil, :metric nil,                 :tags nil,         :time 285629348441/200,   :ttl 20}
; {:host "Crazy-Harry.local", :service "riemann server ws 0.0.0.0:5556 out latency 0.5",    :state "ok", :description nil, :metric nil,                 :tags nil,         :time 285629348441/200,   :ttl 20}
; {:host "Crazy-Harry.local", :service "riemann server ws 0.0.0.0:5556 out latency 0.95",   :state "ok", :description nil, :metric nil,                 :tags nil,         :time 285629348441/200,   :ttl 20}
; {:host "Crazy-Harry.local", :service "riemann server ws 0.0.0.0:5556 out latency 0.99",   :state "ok", :description nil, :metric nil,                 :tags nil,         :time 285629348441/200,   :ttl 20}
; {:host "Crazy-Harry.local", :service "riemann server ws 0.0.0.0:5556 out latency 0.999",  :state "ok", :description nil, :metric nil,                 :tags nil,         :time 285629348441/200,   :ttl 20}
; {:host "Crazy-Harry.local", :service "riemann server ws 0.0.0.0:5556 in latency 0.0",     :state "ok", :description nil, :metric nil,                 :tags nil,         :time 285629348441/200,   :ttl 20}
; {:host "Crazy-Harry.local", :service "riemann server ws 0.0.0.0:5556 in latency 0.5",     :state "ok", :description nil, :metric nil,                 :tags nil,         :time 285629348441/200,   :ttl 20}
; {:host "Crazy-Harry.local", :service "riemann server ws 0.0.0.0:5556 in latency 0.95",    :state "ok", :description nil, :metric nil,                 :tags nil,         :time 285629348441/200,   :ttl 20}
; {:host "Crazy-Harry.local", :service "riemann server ws 0.0.0.0:5556 in latency 0.99",    :state "ok", :description nil, :metric nil,                 :tags nil,         :time 285629348441/200,   :ttl 20}
; {:host "Crazy-Harry.local", :service "riemann server ws 0.0.0.0:5556 in latency 0.999",   :state "ok", :description nil, :metric nil,                 :tags nil,         :time 285629348441/200,   :ttl 20}
; {:host "Crazy-Harry.local", :service "riemann server sse 0.0.0.0:5557 conns",             :state "ok", :description nil, :metric 0,                   :tags nil,         :time 285629348441/200,   :ttl 20}
; {:host "Crazy-Harry.local", :service "riemann server sse 0.0.0.0:5557 out rate",          :state "ok", :description nil, :metric 0.0,                 :tags nil,         :time 285629348441/200,   :ttl 20}
; {:host "Crazy-Harry.local", :service "riemann server sse 0.0.0.0:5557 in rate",           :state "ok", :description nil, :metric 0.0,                 :tags nil,         :time 285629348441/200,   :ttl 20}
; {:host "Crazy-Harry.local", :service "riemann server sse 0.0.0.0:5557 out latency 0.0",   :state "ok", :description nil, :metric nil,                 :tags nil,         :time 285629348441/200,   :ttl 20}
; {:host "Crazy-Harry.local", :service "riemann server sse 0.0.0.0:5557 out latency 0.5",   :state "ok", :description nil, :metric nil,                 :tags nil,         :time 285629348441/200,   :ttl 20}
; {:host "Crazy-Harry.local", :service "riemann server sse 0.0.0.0:5557 out latency 0.95",  :state "ok", :description nil, :metric nil,                 :tags nil,         :time 285629348441/200,   :ttl 20}
; {:host "Crazy-Harry.local", :service "riemann server sse 0.0.0.0:5557 out latency 0.99",  :state "ok", :description nil, :metric nil,                 :tags nil,         :time 285629348441/200,   :ttl 20}
; {:host "Crazy-Harry.local", :service "riemann server sse 0.0.0.0:5557 out latency 0.999", :state "ok", :description nil, :metric nil,                 :tags nil,         :time 285629348441/200,   :ttl 20}
; {:host "Crazy-Harry.local", :service "riemann server sse 0.0.0.0:5557 in latency 0.0",    :state "ok", :description nil, :metric nil,                 :tags nil,         :time 285629348441/200,   :ttl 20}
; {:host "Crazy-Harry.local", :service "riemann server sse 0.0.0.0:5557 in latency 0.5",    :state "ok", :description nil, :metric nil,                 :tags nil,         :time 285629348441/200,   :ttl 20}
; {:host "Crazy-Harry.local", :service "riemann server sse 0.0.0.0:5557 in latency 0.95",   :state "ok", :description nil, :metric nil,                 :tags nil,         :time 285629348441/200,   :ttl 20}
; {:host "Crazy-Harry.local", :service "riemann server sse 0.0.0.0:5557 in latency 0.99",   :state "ok", :description nil, :metric nil,                 :tags nil,         :time 285629348441/200,   :ttl 20}
; {:host "Crazy-Harry.local", :service "riemann server sse 0.0.0.0:5557 in latency 0.999",  :state "ok", :description nil, :metric nil,                 :tags nil,         :time 285629348441/200,   :ttl 20}
