(ns docraider.indexer
  (:require [clojure.java.io :as io]
            [docraider.lucene :as lucene]))

(defn is-file? [^java.io.File f] (.isFile f))

(defn find-files
  "Returns a lazy-seq of all the files in the given directory"
  [dir] (filter is-file? (file-seq (io/as-file dir))))

(defn split-extension [^java.io.File f]
  (let [directory (.getParent f)
        filename (.getName f)
        split (re-matches #"(^.*)\.([^\.]*$)" filename)
        [name extension] (if (nil? split) [filename nil] (rest split))]
    {:root (.getCanonicalPath (io/file directory name)) :extension extension}))

(defn to-document-record [[root document-data]]
  (let [extensions (set (map :extension document-data))]
    {:path root
     :content (if (extensions "txt") (str root ".txt") nil)
     :metadata (if (extensions "meta") (str root ".meta") nil)
     :files (map #(str root (if (nil? %) "" (str "." %))) (disj extensions "txt" "meta"))}))

(defn documents-from [files]
  (let [split-files (map split-extension files)
        raw-docs (group-by :root split-files)]
    (map to-document-record raw-docs)))

(defn relative-path [file-path base-path]
  (let [file-uri (.toURI (io/as-file file-path))
        base-uri (.toURI (io/as-file base-path))]
    (-> (.relativize base-uri file-uri) .getPath)))

(defn index-files [target-dir index-dir]
  (let [full-target-dir (.getCanonicalPath (io/as-file target-dir))
        full-index-dir (.getCanonicalPath (io/as-file index-dir))
        files (filter #(not (-> % .getCanonicalPath (.startsWith full-index-dir))) (find-files full-target-dir))
        docs (map #(assoc % :path (relative-path (:path %) full-target-dir)
                            :files (map (fn [x] (relative-path x full-target-dir)) (:files %))) (documents-from files))]
    (with-open [index-writer (lucene/create-index-writer full-index-dir)]
      (doseq [doc docs] (lucene/index-document index-writer doc)))
    docs))