(ns user
  (:require [clojure.tools.namespace.repl :refer (refresh)]
            [clojure.java.javadoc :refer  (javadoc)]
            [clojure.pprint :refer  (pprint)]
            [clojure.reflect :refer  (reflect)]
            [clojure.repl :refer  (apropos dir doc find-doc pst source)]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clj-gcloud [coerce :refer [->clj page->seq accessor->name]]]
            [clj-gcloud.storage :as gs]))

(def st nil)

(defn init
  []
  (alter-var-root #'st (constantly (gs/init {})))
  nil)

(defn go
  ([] (init)))

(defn reset
  []
  (refresh :after 'user/go))

