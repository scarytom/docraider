(defproject docraider "0.1.0-SNAPSHOT"
  :description "A catalogue for your documents"
  :url "http://github.com/netmelody/docraider"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [ring/ring-core "1.3.0"]
                 [ring/ring-jetty-adapter "1.3.0"]
                 [compojure "1.1.8"]
                 [cheshire "5.3.1"]
                 [org.apache.lucene/lucene-core "4.9.0"]
                 [org.apache.lucene/lucene-queryparser "4.9.0"]
                 [org.apache.lucene/lucene-analyzers-common "4.9.0"]]
  :plugins [[lein-ring "0.8.11"]]
  :ring {:handler docraider.handler/app}
  :profiles
  {:dev {:dependencies [[ring-mock "0.1.5"]]}})
