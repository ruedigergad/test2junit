(ns leiningen.test2junit
  (:require [leiningen.test]
            [robert.hooke]))

(defn add-test-var-println [f & args]
  (println "Running Tests...")
  (apply f args))

(defn test2junit
  "Print test results to junit format."
  [project & keys]
  (robert.hooke/add-hook #'leiningen.test/form-for-testing-namespaces
                         add-test-var-println)
  (apply leiningen.test/test project keys))

