(ns docraider.indexer
  (:require [clojure.java.io :as io]))

(defn is-file? [^java.io.File f] (.isFile f))

(defn find-files
  "Returns a lazy-seq of all the files in the given directory"
  [dir] (filter is-file? (file-seq (io/as-file dir))))

(defn split-extension
  "Returns a two-element vector containing the canonical path of the given
   file (including its name but not its extension) and its extension.
   
   (split-extension (as-file \"/foo/bar.baz\"))
   [\"/foo/bar\" \"baz\"]"
  [^java.io.File f]
  (let [directory (.getParent f)
        filename (.getName f)
        split (re-matches #"(^.*)\.([^\.]*$)" filename)
        [name extension] (if (nil? split) [filename nil] (rest split))]
    [(.getCanonicalPath (io/file directory name)) extension]))

(defn to-document-record [[name extension-data]]
  (let [extensions (set (map last extension-data))]
    {:path name
     :content (if (extensions "txt") (str name ".txt") nil)
     :metadata (if (extensions "meta") (str name ".meta") nil)
     :files (map #(str name (if (nil? %) "" (str "." %))) (disj extensions "txt" "meta"))}))

(defn documents-from [files]
  (let [split-files (map split-extension files)
        raw-docs (group-by first split-files)]
    (map to-document-record raw-docs)))

(defn relative-path [file-path base-path]
  (let [file-uri (.toURI (io/as-file file-path))
        base-uri (.toURI (io/as-file base-path))]
    (-> (.relativize base-uri file-uri) .getPath)))

(defn create-index-writer [index-dir]
  (let [dir (org.apache.lucene.store.FSDirectory/open (io/as-file index-dir))
        version org.apache.lucene.util.Version/LUCENE_41
        analyser (org.apache.lucene.analysis.standard.StandardAnalyzer. version)
        config (org.apache.lucene.index.IndexWriterConfig. version analyser)]
    (.setOpenMode config org.apache.lucene.index.IndexWriterConfig$OpenMode/CREATE)
    (org.apache.lucene.index.IndexWriter. dir config)))

(defn index-document [index-writer document-record]
  (let [document (org.apache.lucene.document.Document.)]
    (.add document (org.apache.lucene.document.StringField. "path" (:path document-record) org.apache.lucene.document.Field$Store/YES))
    (.add document (org.apache.lucene.document.TextField. "contents" (io/reader (:content document-record))))
    (.addDocument index-writer document)))

(defn index-files [target-dir index-dir]
  (let [full-target-dir (.getCanonicalPath (io/as-file target-dir))
        full-index-dir (.getCanonicalPath (io/as-file index-dir))
        files (filter #(not (-> % .getCanonicalPath (.startsWith full-index-dir))) (find-files full-target-dir))
        docs (map #(assoc % :path (relative-path (:path %) full-target-dir)
                            :files (map (fn [x] (relative-path x full-target-dir)) (:files %))) (documents-from files))
        index-writer (create-index-writer full-index-dir)]
    (doseq [doc docs] (index-document index-writer doc))
    (.close index-writer)
    docs))