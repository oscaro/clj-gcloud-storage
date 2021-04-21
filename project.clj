(defproject com.oscaro/clj-gcloud-storage "0.150-1.0-SNAPSHOT"
  :description "Clojure wrapper for google-cloud-storage Java client"
  :url "https://github.com/oscaro/clj-gcloud-storage"
  :license {:name "Eclipse Public License"
            :url  "https://www.eclipse.org/legal/epl-v10.html"}
  :deploy-repositories [["snapshots" {:url "https://repo.clojars.org"
                                      :username :env/clojars_username
                                      :password :env/clojars_password
                                      :sign-releases false}]
                        ["releases"  {:url "https://repo.clojars.org"
                                      :creds :gpg}]]
  :managed-dependencies
  ;; Google “Bill of Materials” (BOM) defines a combination of
  ;; dependency versions that work well with each other.
  [[com.google.cloud/google-cloud-bom "0.150.0"
    :extension "pom"
    :scope "import"]
   [com.google.cloud/google-cloud-shared-dependencies "0.20.1"
    :extension "pom"
    :scope "import"]
   [io.grpc/grpc-api "1.36.0"]
   ;; Select non-android Guava to work around Guava versioning mess
   ;; <https://github.com/google/guava/issues/2914>.
   [com.google.guava/guava "30.1-jre"]]
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [com.oscaro/clj-gcloud-common "0.150-1.0"]
                 [com.google.cloud/google-cloud-storage "1.113.14"]
                 [io.grpc/grpc-api]
                 [org.clojure/tools.logging "1.1.0"]]
  :global-vars {*warn-on-reflection* true}
  :profiles {:dev {:source-paths ["dev"]
                   :dependencies [[org.clojure/tools.namespace "1.1.0"]
                                  [com.google.cloud/google-cloud-nio "0.122.11"]]}}
  :repl-options {:init-ns user})
