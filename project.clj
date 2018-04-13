(defproject com.oscaro/clj-gcloud-storage "0.3.0-SNAPSHOT"
  :description "Clojure wrapper for google-cloud-storage Java client"
  :url "http://gitlab.oscaroad.com"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :repositories {"libs-release"  {:url "http://artifactory.oscaroad.com/artifactory/libs-release"}
                 "libs-snapshot" {:url "http://artifactory.oscaroad.com/artifactory/libs-snapshot"}}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [com.oscaro/clj-gcloud-common "0.3.0"]
                 [com.google.cloud/google-cloud-storage "1.12.0"]
                 [org.clojure/tools.logging "0.4.0"]]
  :global-vars {*warn-on-reflection* true}
  :profiles {:dev {:source-paths ["dev"]
                   :dependencies [[org.clojure/tools.namespace "0.2.11"]
                                  [com.google.cloud/google-cloud-nio "0.30.0-alpha"]]}}
  :repl-options {:init-ns user})
