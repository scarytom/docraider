(defproject docraider "0.1.0-SNAPSHOT"
  :description "A catalogue for your documents"
  :url "http://github.com/netmelody/docraider"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [compojure "1.1.5"]
                 [cheshire "5.0.2"]
                 [org.apache.lucene/lucene-core "4.1.0"]
                 [org.apache.lucene/lucene-queryparser "4.1.0"]
                 [org.apache.lucene/lucene-analyzers-common "4.1.0"]]
  :plugins [[lein-ring "0.8.2"]]
  :ring {:handler docraider.handler/app}
  :profiles
  {:dev {:dependencies [[ring-mock "0.1.3"]]}})
