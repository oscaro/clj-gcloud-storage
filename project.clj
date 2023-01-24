(defproject com.oscaro/clj-gcloud-storage "0.182-2.0-SNAPSHOT"
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
  [[com.google.cloud/google-cloud-bom "0.185.0"
    :extension "pom"
    :scope "import"]
   [com.google.cloud/google-cloud-shared-dependencies "3.1.2"
    :extension "pom"
    :scope "import"]]
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [com.oscaro/clj-gcloud-common "0.185-1.0"]
                 [com.google.cloud/google-cloud-storage "2.17.2"]
                 [org.clojure/tools.logging "1.2.4"]]
  :global-vars {*warn-on-reflection* true}
  :profiles {:dev {:source-paths ["dev"]
                   :dependencies [[org.clojure/tools.namespace "1.3.0"]
                                  [com.google.cloud/google-cloud-nio "0.126.3"]]}}
  :repl-options {:init-ns user})
