# clj-gcloud-storage

Clojure wrapper for [google-cloud-storage][] Java client.

## Usage

```clojure
[com.oscaro/clj-gcloud-storage "0.71-1.2"]
```

### Initializing a client

As any other Google cloud service, Storage requires the user to be authentified.
Thus, you must create a client you will then pass to every operation :

There are several ways to authenticate, two major ones :

```clojure
;; Local credentials, require them to have been created on your side
;; Default project will be used
(def client (st/init {}))

;; Service account usage for portable applications
(def client
  (st/init
    {:project-id "my-project"
     :credentials "my-service-account.json"}))
```

### Uploading and downloading files

Uploading and downloading files is one the most basic operation in Google Cloud Storage
and one of the core need you expect from it in most cases.
Note however that data can be streamed in a more sophisticated way from and to Storage blobs, however
it requires more work on byte arrays.

```clojure
;; Let's download a file and upload it back

;; Local credentials
(def client (st/init {}))

;; Actual (blocking) download
;; The file will be downloaded on disk
(download-file-from-storage storage-client "gs://mybucket/myfolder/myotherfolder/myfile.ext" "mylocalfile.ext")

;; Now we upload a the file again to a copy location
(copy-file-to-storage storage-client (io/file "mylocalfile.ext") "gs://mybucket/myfolder/myotherfolder/myfilecopy.ext")
```

### Objects creation, deletion and copy

Common operations you expect from a "file-system" are available.
Two main object types can be found in Google Storage : buckets which are "folders"
at the root of the project and blobs which are everything else.

#### Creation

```clojure
;; You can create a bucket this way
;; See other options in code and documentation
(create-bucket
  client
  (bucket-info
    "mybucket"
    {:location "europe-west1"}))

;; Blobs are created similarly
;; Subfolders under buckets (which must be constructed before)
;; are created automatically
;; Blob IDs can be inferred from a gs uri
(create-blob
  client
  (blob-info
    (->blob-id "mybucket" "whatever/again/myfile.ext")
    {}))
```

#### Deletion

```clojure
;; Bucket deletion is straightforward
(delete-bucket client "mybucket")

;; Blobs require coercion to BlobId
(delete-blob client (->blob-id "gs://mybucket/whatever/again/myfile.ext"))
```

#### Copy

```clojure
;; Buckets cannot be copied
;; Blobs can
;; Here is one method
(copy client
  (->blob-id "gs://mybucket/whatever/again/myfile.ext")
  (->blob-id "gs://mybucket/whatever/again-archive/myfile.ext"))
```

You can also get buckets or blobs to work directly on it.
This can be useful when you perform multiple operations
on the same blob.

### Listing blobs

You can list blobs under a specific uri. You can then coerce back
the result to uris.

```clojure
;; Basic usage
(ls client "gs://mybucket/whatever/again/")

;; Full coercion to GS uris
(->> (ls client "gs://mybucket/whatever/again/")
     (map
       (fn [obj]
         (let [{:keys [blob-id]} (clj-gcloud.coerce/->clj blob-id)]
           (str "gs://mybucket/" (:name blob-id))))))
```

## License

Copyright © 2017-2020 Oscaro

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

[google-cloud-storage]: https://github.com/googleapis/java-storage
