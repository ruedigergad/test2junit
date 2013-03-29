(ns test2junit.plugin)

(defn middleware [project]
  (let [prjct (update-in project [:injections] concat
                       `[(require 'clj-assorted-utils.util)
                         (require 'clojure.java.io)
                         (require 'clojure.test.junit)
                         (require 'robert.hooke)
                         (robert.hooke/add-hook 
                           #'clojure.test/test-ns
                           (fn [f# & args#]
                             (clj-assorted-utils.util/mkdir "test2junit")
                             (clj-assorted-utils.util/mkdir "test2junit/xml")
                             (let [ns# (first args#)]
                               (println "Testing:" ns#)
                               (with-open [wrtr# (clojure.java.io/writer (str "test2junit/xml/" ns# ".xml"))]
                                 (binding [clojure.test/*test-out* wrtr#]
                                   (clojure.test.junit/with-junit-output
                                     (apply f# args#)))))))])]
    (update-in prjct [:dependencies] concat
               [['robert/hooke "1.3.0"]
                ['clj-assorted-utils "1.2.0"]])))

