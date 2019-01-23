# clj-gcloud-storage

Clojure wrapper for [google-cloud-storage] Java client

## Usage

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

### Downloading files

Downloading file can be done through this library components but can also
directly be done from a convenience function :

```clojure
;; Local credentials
(def client (st/init {}))

;; Actual (blocking) download
(download-file-from-storage storage-client "gs://mybucket/myfolder/myotherfolder/myfile.ext" "mylocalfile.ext")
```

## License

Copyright © 2017 Oscaro

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

[google-cloud-storage]: https://github.com/GoogleCloudPlatform/google-cloud-java/tree/master/google-cloud-storage
