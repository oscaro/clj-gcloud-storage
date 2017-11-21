(ns com.oscaro.clj-gcloud.storage-test
  (:require [clojure.test :refer :all]
            [com.oscaro.clj-gcloud.storage :refer :all]
            [clojure.tools.logging :as log])
  (:import (java.util.concurrent TimeUnit)
           (com.google.cloud.storage Storage)
           (com.google.cloud.storage.testing RemoteStorageHelper)))

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

(use-fixtures
  :once
  (fn [f]
    (binding [*storage* (init {:project-id  "oscaro-cloud"
                               :credentials "test-resources/service-account.json"})]
      (before-tests)
      (f)
      (after-tests))))

(deftest bucket-is-initialized
  (is (not (nil? (get-bucket *storage* bucket)))))
