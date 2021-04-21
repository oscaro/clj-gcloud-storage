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
  :dependencies [[org.clojure/clojure "1.10.1" :scope "provided"]
                 [com.oscaro/clj-gcloud-common "0.112-1.0"]
                 [com.google.cloud/google-cloud-storage "1.100.0"]
                 [org.clojure/tools.logging "1.1.0"]]
  :global-vars {*warn-on-reflection* true}
  :profiles {:dev {:source-paths ["dev"]
                   :dependencies [[org.clojure/tools.namespace "1.0.0"]
                                  [com.google.cloud/google-cloud-nio "0.118.0-alpha"]]}}
  :repl-options {:init-ns user})
