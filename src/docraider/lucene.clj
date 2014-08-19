(ns docraider.lucene
  (:require [clojure.java.io :as io]))

(defn create-index-writer [index-dir]
  (let [dir (org.apache.lucene.store.FSDirectory/open (io/as-file index-dir))
        version org.apache.lucene.util.Version/LUCENE_4_9
        analyser (org.apache.lucene.analysis.standard.StandardAnalyzer. version)
        config (org.apache.lucene.index.IndexWriterConfig. version analyser)]
    (.setOpenMode config org.apache.lucene.index.IndexWriterConfig$OpenMode/CREATE)
    (org.apache.lucene.index.IndexWriter. dir config)))

(defn index-document [index-writer document-record]
  (let [document (org.apache.lucene.document.Document.)]
    (.add document (org.apache.lucene.document.StringField. "path" (:path document-record) org.apache.lucene.document.Field$Store/YES))
    (.add document (org.apache.lucene.document.TextField. "contents" (io/reader (:content document-record))))
    (.addDocument index-writer document)))

(defn create-index-reader [index-dir]
  (let [dir (org.apache.lucene.store.FSDirectory/open (io/as-file index-dir))]
    (org.apache.lucene.index.DirectoryReader/open dir)))

(defn search-index [index-reader term]
  (let [searcher (org.apache.lucene.search.IndexSearcher. index-reader)
        version org.apache.lucene.util.Version/LUCENE_4_9
        analyser (org.apache.lucene.analysis.standard.StandardAnalyzer. version)
        query-parser (org.apache.lucene.queryparser.classic.QueryParser. version "contents" analyser)
        query (.parse query-parser term)
        results (.search searcher query nil 100)
        hits (.scoreDocs results)]
    (doall (map #(.doc searcher (.doc %)) hits))))
