(defproject com.oscaro/clj-gcloud-storage "0.112-1.1-SNAPSHOT"
  :description "Clojure wrapper for google-cloud-storage Java client"
  :url "https://github.com/oscaro/clj-gcloud-storage"
  :license {:name "Eclipse Public License"
            :url  "https://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.1" :scope "provided"]
                 [com.oscaro/clj-gcloud-common "0.112-1.0"]
                 [com.google.cloud/google-cloud-storage "1.100.0"]
                 [org.clojure/tools.logging "0.5.0"]]
  :global-vars {*warn-on-reflection* true}
  :profiles {:dev {:source-paths ["dev"]
                   :dependencies [[org.clojure/tools.namespace "0.3.1"]
                                  [com.google.cloud/google-cloud-nio "0.118.0-alpha"]]}}
  :repl-options {:init-ns user})
