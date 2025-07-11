(ns clj-gcloud.storage
  (:require
   [clj-gcloud
    [coerce :refer [create-clj-coerce ->clj page->seq]]
    [common :refer [build-service array-type]]]
   [clojure.java.io :as io]
   [clojure.string :as str]
   [clojure.walk :refer [stringify-keys keywordize-keys]]
   [clojure.tools.logging :as log])
  (:import
   (com.google.cloud WriteChannel)
   (com.google.cloud.storage BucketInfo Blob BlobId BlobInfo BucketInfo$Builder Storage StorageOptions
                             Storage$BucketGetOption Storage$BucketTargetOption
                             Storage$BucketSourceOption BlobInfo$Builder Storage$CopyRequest
                             Storage$BlobTargetOption Storage$BlobWriteOption
                             Blob$BlobSourceOption Storage$BlobListOption CopyWriter)
   (com.google.common.io ByteStreams)
   (java.io InputStream OutputStream FileInputStream File)
   (java.nio.channels Channels ReadableByteChannel WritableByteChannel)
   (java.nio.file Paths)))

(create-clj-coerce BucketInfo [:name :location :storage-class])
(create-clj-coerce Blob [:blob-id :name :content-type
                         :cache-control :create-time :update-time :delete-time
                         :content-disposition :content-encoding :content-language])
(create-clj-coerce BlobId [:bucket :name])
(create-clj-coerce BlobInfo [:blob-id :cache-control :create-time :update-time :delete-time
                             :content-disposition :content-encoding :content-language :content-type
                             :metadata])

(defn bucket-info
  [bucket-name options]
  (let [opt-map {:index-page         (fn [^BucketInfo$Builder b v] (.setIndexPage b v))
                 :location           (fn [^BucketInfo$Builder b v] (.setLocation b v))
                 :not-found-page     (fn [^BucketInfo$Builder b v] (.setNotFoundPage b v))
                 :storage-class      (fn [^BucketInfo$Builder b v] (.setStorageClass b v))
                 :versioning-enabled (fn [^BucketInfo$Builder b v] (.setVersioningEnabled b (boolean v)))}
        builder (.toBuilder (BucketInfo/of bucket-name))]
    (doseq [[k v] options
            :let [set-fn (get opt-map k)]
            :when set-fn]
      (set-fn builder v))
    (.build builder)))

(defn init
  ^Storage
  ([]
   (init {}))
  ([options]
   (let [builder (StorageOptions/newBuilder)]
     (build-service builder options))))

;; BUCKETS
(defn get-bucket
  [^Storage storage ^String bucket-name]
  (let [^#=(array-type Storage$BucketGetOption) no-options (into-array Storage$BucketGetOption [])]
    (.get storage bucket-name no-options)))

(defn create-bucket
  [^Storage storage ^BucketInfo bucket-info]
  (let [^#=(array-type Storage$BucketTargetOption) no-options (into-array Storage$BucketTargetOption [])]
    (.create storage bucket-info no-options)))

(defn get-or-create-bucket
  "Fetches or creates a bucket if it doesn't exist."
  [^Storage storage ^BucketInfo info]
  (let [bucket-name (.getName info)
        bucket (get-bucket storage bucket-name)]
    (or bucket
        (do
          (log/info "Creating new bucket:" bucket-name)
          (create-bucket storage info)))))

(defn delete-bucket
  [^Storage storage ^String bucket-name]
  (let [^#=(array-type Storage$BucketSourceOption) no-opts (into-array Storage$BucketSourceOption [])]
    (.delete storage bucket-name no-opts)))

;; BLOBS
(declare ->blob-id)

(defn read-gs-uri
  [gs-uri]
  (let [[scheme host path] (str/split gs-uri #"[/]+" 3)]
    (if (= "gs:" scheme)
      (->blob-id host path)
      (throw (ex-info "Invalid scheme" {:input gs-uri})))))

(defn ->blob-id
  ([bucket name]
   (BlobId/of bucket name))
  ([gs-uri-or-blob-id]
   (if (instance? BlobId gs-uri-or-blob-id)
     gs-uri-or-blob-id
     (read-gs-uri gs-uri-or-blob-id))))

(defn ->metadata  [m]
  (some-> m (stringify-keys) (update-vals name)))

(defn blob-info
  [blob-id options]
  (let [opt-map {:cache-control       (fn [^BlobInfo$Builder b v] (.setCacheControl b v))
                 :content-disposition (fn [^BlobInfo$Builder b v] (.setContentDisposition b v))
                 :content-encoding    (fn [^BlobInfo$Builder b v] (.setContentEncoding b v))
                 :content-language    (fn [^BlobInfo$Builder b v] (.setContentLanguage b v))
                 :content-type        (fn [^BlobInfo$Builder b v] (.setContentType b v))
                 :metadata            (fn [^BlobInfo$Builder b v] (.setMetadata b (->metadata v)))}
        builder (BlobInfo/newBuilder blob-id)]
    (doseq [[k v] options
            :let [set-fn (get opt-map k)]
            :when set-fn]
      (set-fn builder v))
    (.build builder)))

(defn copy
  ([storage source-blob-id target-blob-id]
   (copy storage source-blob-id target-blob-id nil))
  ([^Storage storage ^BlobId source-blob-id ^BlobId target-blob-id options]
   (let [cr (if (:with-precondition options)
              ;; Copy blobs within cloud storage using preconditions recommended in Google Cloud Storage guides
              ;; See: https://cloud.google.com/storage/docs/copying-renaming-moving-objects#storage-copy-object-java
              (let [precondition (if-let [target (.get storage target-blob-id)]
                                   (Storage$BlobTargetOption/generationMatch (.getGeneration target))
                                   (Storage$BlobTargetOption/doesNotExist))
                    ^Iterable target-opts (into-array Storage$BlobTargetOption [precondition])]
                (-> (Storage$CopyRequest/newBuilder)
                    (.setSource source-blob-id)
                    (.setTarget target-blob-id target-opts)
                    .build))
              (Storage$CopyRequest/of source-blob-id target-blob-id))]
     (-> (.copy storage cr)
         (.getResult)))))

(defn create-blob
  [^Storage storage ^BlobInfo blob-info]
  (let [^#=(array-type Storage$BlobTargetOption) no-options (into-array Storage$BlobTargetOption [])]
    (.create storage blob-info no-options)))

(defn create-blob-writer
  ^WriteChannel
  [^Storage storage ^BlobInfo blob-info]
  (let [^#=(array-type Storage$BlobWriteOption) no-opts (into-array Storage$BlobWriteOption [])]
    (.writer storage blob-info no-opts)))

(defn get-blob
  ([^Storage storage ^BlobId blob-id]
   (.get storage blob-id)))

(defn delete-blob
  ([^Storage storage ^BlobId blob-id]
   (.delete storage blob-id)))

(defn copy-blob
  ([^Blob source-blob ^BlobId target-blob-id]
   (let [^#=(array-type Blob$BlobSourceOption) no-opts (into-array Blob$BlobSourceOption [])
         ^CopyWriter writer                            (.copyTo source-blob target-blob-id no-opts)]
     (.getResult writer))))

(defn ->blob-list-options
  [options]
  (let [opt-map {:current-directory (fn [_] (Storage$BlobListOption/currentDirectory))
                 :match-glob        (fn [v] (Storage$BlobListOption/matchGlob v))
                 :page-size         (fn [v] (Storage$BlobListOption/pageSize (long v)))
                 :page-token        (fn [v] (Storage$BlobListOption/pageToken v))
                 :prefix            (fn [v] (Storage$BlobListOption/prefix v))
                 :versions          (fn [v] (Storage$BlobListOption/versions (boolean v)))}]
    (->> (for [[k v] options
               :let [opt-fn (get opt-map k)]
               :when opt-fn]
           (opt-fn v))
         (into-array Storage$BlobListOption))))

(defn list-blobs
  [^Storage storage ^String bucket-name options]
  (.list storage bucket-name (->blob-list-options options)))

(defn- split-bucket-path
  [gs-uri]
  (let [{:keys [bucket name]} (->clj (read-gs-uri gs-uri))]
    [bucket name]))

(defn ls
  "List files.
  Usage:
     (ls storage gs-uri [options])
     (ls storage bucket path [options])"
  ([^Storage storage gs-uri]
   (let [[bucket path] (split-bucket-path gs-uri)]
     (ls storage bucket path)))
  ([^Storage storage bucket path]
   (if (map? path)
     (let [options path
           [bucket path] (split-bucket-path bucket)]
       (ls storage bucket path options))
     (ls storage bucket path {})))
  ([^Storage storage bucket path options]
   (page->seq (list-blobs storage bucket (merge options
                                                (if (clojure.string/blank? path)
                                                  {} {:prefix path}))))))

;;
;; Utility IO functions
;;

(defn read-channel
  [^Blob blob]
  (.reader blob (into-array Blob$BlobSourceOption [])))

(defn write-channel
  [^Blob blob]
  (.writer blob (into-array Storage$BlobWriteOption [])))

(defn ->input-stream
  [^ReadableByteChannel channel]
  (Channels/newInputStream channel))

(defn ->output-stream
  [^WritableByteChannel channel]
  (Channels/newOutputStream channel))

(defn stream->file
  "Writes an input stream to disk."
  [^InputStream input-stream local-path]
  (io/copy input-stream (io/file local-path)))

(defn file->stream
  "Writes an local file to stream."
  [local-path ^OutputStream output-stream]
  (io/copy (io/file local-path) output-stream))

;;
;; Convenience functions
;;

(defn copy-file-to-storage
  "Convenience function for copying a local file to a blob to storage.
  By default the type is JSON encoded in UTF-8"
  ([storage src dest-uri]
   (copy-file-to-storage storage src dest-uri
                         :content-type "application/json"
                         :content-encoding "UTF-8"))
  ([^Storage storage ^File src dest-gs-uri & options]
   (let [opts (apply array-map options)
         info (-> dest-gs-uri ->blob-id (blob-info opts))]
     (with-open [from (.getChannel (FileInputStream. src))
                 to   (create-blob-writer storage info)]
       (ByteStreams/copy from to)))))

;; Blob source option : if required add them
(defn download-file-from-storage
  "Downloads a storage file to a local one.
   Usage :
   (download-file-from-storage storage-client 'gs://mybucket/myfolder/.../myfile' 'mylocalfile')"
  [^Storage storage source-gs-uri dest-local-path & _options]
  (let [^Blob blob (->> source-gs-uri ->blob-id (get-blob storage))
        path (Paths/get dest-local-path (make-array String 0))
        ;; this resolves reflection but it's pretty ugly:
        ;; ^"[Lcom.google.cloud.storage.Blob$BlobSourceOption;" opts (into-array Blob$BlobSourceOption [])
        ;; for now skip it, since we pass no options anyway
        ]
    (.downloadTo blob path)))

(defn set-metadata!
  "Set custom metadata to an existing blob in a bucket

   CAUTION: If no `metadata` arg specified, the field will be nulled and all
   the custom metadata will be removed from the Blob

   Usage:

   ;; Clean Metadata
   (set-metadata! storage-client \"gs://foo/bar/file.clj\")

   ;; Attach metadata
   (set-metadata! storage-client \"gs://foo/bar/file.clj\" {:instance \"instance-foobar-1-cloud-vm\")"
  {:added "0.240-2.0"}
  ([^Storage storage source-gs-uri]
   (set-metadata! storage source-gs-uri nil))
  ([^Storage storage source-gs-uri metadata]
   (when-let [^Blob blob (->> source-gs-uri ->blob-id (get-blob storage))]
     ;; Set a generation-match precondition to avoid potential race
     ;; conditions and data corruptions.
     ;;
     ;; NOTE: The request to upload returns a 412 error if
     ;; the object's generation number does not match your precondition.
     (let [pre-cond (Storage$BlobTargetOption/generationMatch)]
       (some->
        (.toBuilder blob)
        (.setMetadata (->metadata metadata))
        (.build)
        (.update (into-array Storage$BlobTargetOption [pre-cond])))

       true))))

(defn get-metadata
  "Retrieve the custom metadata associated to the current
   google storage blob.

   Options: `:keywordize?` (default: `false`) - keywordize return"
  {:added "0.240-2.0"}
  [^Storage storage source-gs-uri & {:keys [keywordize?] :or {keywordize? false}}]
  (when-let [^Blob blob (->> source-gs-uri ->blob-id (get-blob storage))]
    (when-let [metadata? (.getMetadata blob)]
      (cond->> (into {} metadata?)
        keywordize? keywordize-keys))))
