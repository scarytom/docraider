(ns docraider.searcher
  (:require [clojure.java.io :as io]
            [docraider.lucene :as lucene]))

(defn search [index-dir term]
  (with-open [index-reader (lucene/create-index-reader index-dir)]
    (let [results (lucene/search-index index-reader term)]
      (doseq [result results] (println (.get result "path"))))))