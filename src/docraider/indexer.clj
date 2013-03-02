(ns docraider.indexer
  (:require [clojure.java.io :as io]
            [clojure.algo.generic.functor :as f]))

(defn is-file? [^java.io.File f] (.isFile f))

(defn find-files
  "Return a lazy-seq of all the files in the given directory"
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


(defn documents-from [files]
  (let [split-files (map split-extension files)
        raw-docs (group-by first split-files)]
    (f/fmap #(map last %) raw-docs)))

(defn index [target-dir index-dir]
  (let [docs (documents-from (scan-files target-dir))]
    docs))