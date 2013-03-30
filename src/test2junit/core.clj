(ns test2junit.core
  (:require clj-assorted-utils.util
            clojure.java.io
            robert.hooke
            test2junit.junit))

(defn apply-junit-output-hook []
  (robert.hooke/add-hook 
    #'clojure.test/test-ns
    (fn [f# & args#]
      (clj-assorted-utils.util/mkdir "test2junit")
      (clj-assorted-utils.util/mkdir "test2junit/html")
      (clj-assorted-utils.util/mkdir "test2junit/tmp")
      (clj-assorted-utils.util/mkdir "test2junit/xml")
      (let [ns# (first args#)]
        (println "Testing:" ns#)
        (with-open [wrtr# (clojure.java.io/writer (str "test2junit/xml/" ns# ".xml"))]
          (binding [clojure.test/*test-out* wrtr#]
            (test2junit.junit/with-junit-output
              (apply f# args#))))))))

