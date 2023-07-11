(ns clj-gcloud.storage-test
  (:require
   [clj-gcloud.coerce :refer [->clj]]
   [clj-gcloud.storage :refer [copy-file-to-storage read-channel ->input-stream delete-blob ls]]
   [clojure.java.io :as io]
   [clojure.test :refer [deftest is use-fixtures testing]])
  (:import
   (com.google.cloud.storage Storage Blob)
   (com.google.cloud.storage.contrib.nio.testing LocalStorageHelper)
   (com.google.cloud.storage.testing RemoteStorageHelper)
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
        (let [coerced-blob (->clj blob)]
          (is (= {:blob-id      {:bucket bucket :name "tmp.json"}
                  :content-type "application/json"
                  :name         "tmp.json"
                  :content-encoding "UTF-8"}
                 (select-keys coerced-blob [:blob-id :content-type :name :content-encoding])))
          (is (= #{:content-language
                   :content-disposition
                   :name
                   :update-time
                   :blob-id
                   :cache-control
                   :create-time
                   :content-type
                   :content-encoding
                   :delete-time}
                 (set (keys coerced-blob)))))
        (is (= data (-> (read-channel blob)
                        (->input-stream)
                        (io/reader)
                        (slurp))))
        (is (delete-blob *storage* (.getBlobId blob)))
        (is (= 0 (count (ls *storage* dest-uri)))))
      (.delete tmp))))

