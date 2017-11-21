(defproject com.oscaro/clj-gcloud-storage "0.2.0-SNAPSHOT"
  :description "Clojure wrapper for google-cloud-storage Java client"
  :url "http://gitlab.oscaroad.com"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :repositories {"libs-release"  {:url "http://artifactory.oscaroad.com/artifactory/libs-release"}
                 "libs-snapshot" {:url "http://artifactory.oscaroad.com/artifactory/libs-snapshot"}}
  :dependencies [[org.clojure/clojure "1.9.0-beta4"]
                 [com.oscaro/clj-gcloud-common "0.2.0"]
                 [com.google.cloud/google-cloud-storage "1.12.0"]
                 [org.clojure/tools.logging "0.4.0"]]
  :global-vars {*warn-on-reflection* true}
  :profiles
  {:dev
   {:resource-paths ["test-resources"]}})
