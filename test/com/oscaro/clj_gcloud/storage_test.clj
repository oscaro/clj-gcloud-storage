(ns com.oscaro.clj-gcloud.storage-test
  (:require [clojure.test :refer :all]
            [com.oscaro.clj-gcloud.storage :refer :all]
            [com.oscaro.clj-gcloud.coerce :refer [->clj]]
            [clojure.java.io :as io])
  (:import (com.google.cloud.storage Storage Blob)
           (com.google.cloud.storage.testing RemoteStorageHelper)
           (com.google.cloud.storage.contrib.nio.testing LocalStorageHelper)
           (java.io File)))

;; Constants
(def ^String bucket (RemoteStorageHelper/generateBucketName))

(def ^:dynamic ^Storage *storage* nil)

(defn- create-temp-file [suffix]
  (let [file (File/createTempFile "tmp" suffix)]
    (.deleteOnExit file)
    file))

(use-fixtures
  :once
  (fn [f]
    (binding [*storage* (.getService (LocalStorageHelper/getOptions))]
      (f))))

(deftest copy-dump-to-storage-test
  (testing "It should copy a file to storage matching a certain path"
    (let [^File tmp (create-temp-file ".json")
          dest-uri  (str "gs://" bucket "/tmp.json")
          data      "{\"test\":\"data\"}"]
      (spit tmp data)
      (copy-file-to-storage *storage* tmp dest-uri)
      (let [^Blob blob (first (ls *storage* dest-uri))]
        (is (= {:blob-id      {:bucket bucket :name "tmp.json"}
                :content-type "application/json"
                :name         "tmp.json"}
               (->clj blob)))
        (is (= data (-> (read-channel blob)
                        (->input-stream)
                        (io/reader)
                        (slurp))))
        (is (delete-blob *storage* (.getBlobId blob)))
        (is (= 0 (count (ls *storage* dest-uri)))))
      (.delete tmp))))

