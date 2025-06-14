(defproject com.oscaro/clj-gcloud-storage "0.240-4.0-SNAPSHOT"
  :description "Clojure wrapper for google-cloud-storage Java client"
  :url "https://github.com/oscaro/clj-gcloud-storage"
  :license {:name "Eclipse Public License"
            :url  "https://www.eclipse.org/legal/epl-v10.html"}
  :deploy-repositories [["snapshots" {:url "https://repo.clojars.org"
                                      :username :env/clojars_username
                                      :password :env/clojars_password
                                      :sign-releases false}]
                        ["releases"  {:url "https://repo.clojars.org"
                                      :username :env/clojars_username
                                      :password :env/clojars_password
                                      :sign-releases true}]]
  :signing {:gpg-key "github-cicd@oscaro.com"}
  :parent-project [:coords [com.oscaro/clj-gcloud-common "0.240-1.0"]
                   :inherit [:managed-dependencies]]
  ;; based on versions from
  ;;   https://mvnrepository.com/artifact/com.google.cloud/google-cloud-bom/0.240.0
  ;; which corresponds to
  ;;   https://mvnrepository.com/artifact/com.google.cloud/lib
  :dependencies [[org.clojure/clojure "1.11.4"]
                 [com.oscaro/clj-gcloud-common "0.240-1.0"]
                 [com.google.cloud/google-cloud-nio "0.127.33"]
                 [com.google.cloud/google-cloud-storage "2.50.0"]
                 [org.clojure/tools.logging "1.3.0"]]
  :plugins [[io.github.borkdude/lein-lein2deps "0.1.0"]]
  :prep-tasks [["lein2deps" "--write-file" "deps.edn" "--print" "false"]]
  :profiles {:dev {:source-paths ["dev"]
                   :dependencies [[org.clojure/tools.namespace "1.5.0"]]
                   :global-vars {*warn-on-reflection* true}}}
  :repl-options {:init-ns user})
