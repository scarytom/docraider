(ns docraider.searcher
  (:require [clojure.java.io :as io]))

(defn search [index-dir term]
  (let [dir (org.apache.lucene.store.FSDirectory/open (io/as-file index-dir))]
    (with-open [reader (org.apache.lucene.index.DirectoryReader/open dir)]
      (let [searcher (org.apache.lucene.search.IndexSearcher. reader)
            version org.apache.lucene.util.Version/LUCENE_41
            analyser (org.apache.lucene.analysis.standard.StandardAnalyzer. version)
            query-parser (org.apache.lucene.queryparser.classic.QueryParser. version "contents" analyser)
            query (.parse query-parser term)
            results (.search searcher query nil 100)
            hits (.scoreDocs results)]
        (doseq [hit hits] (println (.get (.doc searcher (.doc hit)) "path")))))))