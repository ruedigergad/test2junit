(ns test2junit.core
  (:require clj-assorted-utils.util
            clojure.java.io
            robert.hooke
            test2junit.junit))

(defn apply-junit-output-hook [output-dir]
  (println "Writing output to:" output-dir "\n")
  (robert.hooke/add-hook 
    #'clojure.test/test-ns
    (fn [f# & args#]
      (clj-assorted-utils.util/mkdir output-dir)
      (clj-assorted-utils.util/mkdir (str output-dir "/html"))
      (clj-assorted-utils.util/mkdir (str output-dir "/tmp"))
      (clj-assorted-utils.util/mkdir (str output-dir "/xml"))
      (let [ns# (first args#)]
        (println "Testing:" ns#)
        (with-open [wrtr# (clojure.java.io/writer (str "test2junit/xml/" ns# ".xml"))]
          (binding [clojure.test/*test-out* wrtr#]
            (test2junit.junit/with-junit-output
              (apply f# args#))))))))

