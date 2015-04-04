PoC embedded [Riemann](http://riemann.io/) instance with testable config

Running tests against Riemann config is slow.  It's not Riemann's fault, it's the JVM's.  The goal of this sample project is to demonstrate how you can treat your Riemann config as you would a standard lein project, allowing you to test your config with `lein test` and therefore also use your choice of plugins (such as [prism](https://github.com/aphyr/prism/) or [lein-test-refresh](https://github.com/jakemcc/lein-test-refresh)) to speed up the iterative testing process.

The `riemann.test/tap` and `riemann.test/io` macros are **not** supported.  They require a dynamic variable (`riemann.test/*testing*`) to be set to `true` at the time the code under test is _loaded_.  A major goal of this PoC is to find a way to make these macros work within the `lein test` framework, *or* to find an alternative way of structuring Riemann code so they aren't required.  The initial focus will be on the latter.

The best way to understand the flow of tests in here is to take a look at the `rewrites-streams-rate` test in [`test/rtp/sources/riemann_test.clj`](test/rtp/sources/riemann_test.clj), which I've annotated.

## running

For dev purposes:

    lein run

For production purposes (guessing, here; haven't done this, yet)

    lein uberjar
    java -jar target/riemann-test-prototype-0.0.1-SNAPSHOT-standalone.jar

## notes

### reloading is disabled

`require`d code doesn't appear to get reloaded by Riemann on a `SIGHUP`. `rtp.bin/-main` copies the important bits of `riemann.bin/-main` in order to load the config and get the core running, but all reloading magic is left behind.

## disclaimers

* I'm a Clojure newbie
* I'm a Leiningen newbie
* My coding style is not idiomatic Clojure or lisp, but I don't have to count closing parens as often
* There are probably many things in here that run counter to aphyr's intentions
* I don't even know if this works for me, yet
