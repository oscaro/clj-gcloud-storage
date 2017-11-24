(ns com.oscaro.clj-gcloud.storage-test
  (:require [clojure.test :refer :all]
            [clojure.edn :as edn]
            [com.oscaro.clj-gcloud.storage :refer :all]
            [clojure.tools.logging :as log])
  (:import (java.util.concurrent TimeUnit)
           (com.google.cloud.storage Storage Blob)
           (com.google.cloud.storage.testing RemoteStorageHelper)
           (java.io File)))

; Constants
(def ^String bucket (RemoteStorageHelper/generateBucketName))

(def ^:dynamic ^Storage *storage* nil)

(defn before-tests []
  (let [info (bucket-info bucket {})]
    (get-or-create-bucket *storage* info)))

(defn after-tests []
  (when *storage*
    (do
      (log/info "Deleting bucket:" bucket)
      (when (not (RemoteStorageHelper/forceDelete *storage* bucket 5 TimeUnit/SECONDS))
        (log/warn "Deletion of bucket" bucket "timed out, bucket is not empty")))))

(defn- create-temp-file [suffix]
  (let [file (File/createTempFile "tmp" suffix)]
    (.deleteOnExit file)
    file))

(use-fixtures
  :once
  (fn [f]
    (binding [*storage* (init (assoc (edn/read-string (slurp "test-resources/test-config.edn"))
                                     :credentials "test-resources/service-account.json"))]
      (before-tests)
      (f)
      (after-tests))))

(deftest bucket-is-initialized
  (is (not (nil? (get-bucket *storage* bucket)))))

(deftest copy-dump-to-storage-test

  (testing "It should copy a file to storage matching a certain path"
    (let [^File tmp (create-temp-file ".json")
          dest-uri  (str "gs://" bucket "/tmp.json")
          data      "{\"test\":\"data\"}"]
      (spit tmp data)
      (copy-file-to-storage *storage* tmp dest-uri)
      (let [[^Blob blob] (ls *storage* dest-uri)]
        (is (= (count data) (.getSize blob)))
        (is (delete-blob *storage* (.getBlobId blob))))
      (.delete tmp))))
