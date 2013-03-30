(ns leiningen.test2junit
  "Output test results to JUnit XML format."
  (:require [clj-assorted-utils.util]
            [leiningen.core.main]
            [leiningen.test]
            [robert.hooke]
            [test2junit.core]))

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
  (binding [leiningen.core.main/*exit-process?* false]
    (try
      (apply leiningen.test/test project keys))
      (catch clojure.lang.ExceptionInfo e
        (let [msg (.getMessage e)]
          (if (not (= msg "Suppressed exit"))
            (println "Caught exception:" e)))))
  (when (and (not (nil? (:test2junit-run-ant project)))
             (:test2junit-run-ant project))
    (println "\nRunning ant to generate HTML report...")
    (let [ret (.waitFor (clj-assorted-utils.util/exec-with-out "ant" println))]
      (if (= 0 ret)
        (println "Report was successfully generated.")
        (println "There was a problem during report generation. Ant returned with:" ret)))))

