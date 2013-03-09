(ns docraider.test.indexer
  (:use clojure.test
        docraider.indexer)
  (:require [clojure.java.io :as io]))

(defn- make-temp-dir []
  (let [directory (java.io.File/createTempFile "docraider-test" "")]
    (.delete directory)
    (.mkdir directory)
    directory))

(defn- make-temp-filesystem
  ([structure]
    (let [base (make-temp-dir)]
      (make-temp-filesystem base structure)
      base))
  ([base structure]
    (doall (map (fn [[key value]]
                  (if (map? value)
                    (do (let [dir (io/file base key)]
                          (.mkdir dir)
                          (make-temp-filesystem dir value)))
                    (do (let [file (io/file base key)]
                          (.createNewFile file)
                          (with-open [wrtr (io/writer file)]
                            (.write wrtr value))))))
                structure))))

(deftest test-indexer
  (let [base (make-temp-filesystem {"foo.pdf" ""
                                    "foo.txt" "foo-content"
                                    "bar" {"whizz.pdf" "", "whizz.txt" "bang",
                                           "whee.pdf" "", "whee.txt" "whap"}})]

    (testing "find-files"
      (let [result (find-files base)]
        (is (= result (seq [(io/file base "bar/whee.pdf")
                            (io/file base "bar/whee.txt")
                            (io/file base "bar/whizz.pdf")
                            (io/file base "bar/whizz.txt")
                            (io/file base "foo.pdf")
                            (io/file base "foo.txt")])))))
    
    (testing "split-extension"
      (is (= (split-extension (io/file base "bar/whee.pdf")) {:root (-> (io/file base "bar/whee") .toString) :extension "pdf"}))
      (is (= (split-extension (io/file base "bar/whee."))    {:root (-> (io/file base "bar/whee") .toString) :extension ""}))
      (is (= (split-extension (io/file base "bar/whee"))     {:root (-> (io/file base "bar/whee") .toString) :extension nil})))
    
    (testing "to-document-record"
      (let [result (to-document-record ["/tmp/foo"
                                        [{:extension nil} {:extension ""} {:extension "txt"} {:extension "pdf"} {:extension "meta"}]])]
        (is (= result {:path "/tmp/foo"
                       :content "/tmp/foo.txt"
                       :metadata "/tmp/foo.meta"
                       :files (seq ["/tmp/foo" "/tmp/foo." "/tmp/foo.pdf"])}))))
    
    (testing "documents-from"
      (let [result (documents-from (find-files base))]
        (is (= (map :path result) (seq [(-> (io/file base "bar/whee") .toString)
                                        (-> (io/file base "bar/whizz") .toString)
                                        (-> (io/file base "foo") .toString)])))))
    
    (testing "relative-path"
      (is (= (relative-path (io/file base "foo/bar/../baz") base) "foo/baz"))
      (is (= (relative-path (io/file base "bar") base) "bar/")))
    
    
    ))


