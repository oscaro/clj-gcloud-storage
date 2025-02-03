(defproject com.oscaro/clj-gcloud-storage "0.215-2.0-SNAPSHOT"
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
  ;;
  ;; based on versions from
  ;;   https://mvnrepository.com/artifact/com.google.cloud/google-cloud-bom/0.234.0
  ;; which corresponds to
  ;;   https://mvnrepository.com/artifact/com.google.cloud/libraries-bom/26.53.0
  [[com.google.cloud/google-cloud-bom "0.234.0"
    :extension "pom"
    :scope "import"]
   [com.google.cloud/google-cloud-shared-dependencies "3.42.0"
    :extension "pom"
    :scope "import"]]
  :dependencies [[org.clojure/clojure "1.11.4"]
                 [com.oscaro/clj-gcloud-common "0.234-1.0"]
                 [com.google.cloud/google-cloud-nio "0.127.29"]
                 [com.google.cloud/google-cloud-storage "2.47.0"]
                 [org.clojure/tools.logging "1.3.0"]]
  :plugins [[io.github.borkdude/lein-lein2deps "0.1.0"]]
  :prep-tasks [["lein2deps" "--write-file" "deps.edn" "--print" "false"]]
  :profiles {:dev {:source-paths ["dev"]
                   :dependencies [[org.clojure/tools.namespace "1.5.0"]]
                   :global-vars {*warn-on-reflection* true}}}
  :repl-options {:init-ns user})
