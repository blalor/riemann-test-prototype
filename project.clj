(defproject riemann-test-prototype "0.0.1-SNAPSHOT"
    :description "prototype project to demonstrate test-driven Riemann config"
    :maintainer {:email "blalor@bravo5.org"}
    :dependencies [
        [org.clojure/clojure "1.6.0"]
        [riemann "0.2.9"]
    ]
    
    :profiles {
        :dev {
            ;; "lein test-refresh" helps make iterative testing blazing fast
            :plugins [[com.jakemccrary/lein-test-refresh "0.7.0"]]
            :test-refresh {:notify-command ["terminal-notifier" "-title" "Tests" "-message"]} 
        }
    }
    
    :aot [rtp.bin]
    :main rtp.bin
)
