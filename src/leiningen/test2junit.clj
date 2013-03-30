(ns leiningen.test2junit
  "Output test results to JUnit XML format."
  (:require [leiningen.test]
            [robert.hooke]))

(defn add-test-var-println [f & args]
  (println "Running Tests...")
  (apply f args))

(defn test2junit
  "Output test results to JUnit XML format.
  
   This plug-in writes test results to files in JUnit XML format.
   These files can be used, e.g., with junitreport for creating reports in HTML format.
   Please see the webpage for more information: https://github.com/ruedigergad/test2junit"
  [project & keys]
  (robert.hooke/add-hook #'leiningen.test/form-for-testing-namespaces
                         add-test-var-println)
  (apply leiningen.test/test project keys))

