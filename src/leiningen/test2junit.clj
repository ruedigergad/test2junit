;;;
;;;   Copyright 2013, Ruediger Gad <r.c.g@gmx.de>
;;;
;;;   This software is released under the terms of the Eclipse Public License
;;;   (EPL) 1.0. You can find a copy of the EPL at:
;;;   http://opensource.org/licenses/eclipse-1.0.php
;;;

(ns leiningen.test2junit
  "Output test results to JUnit XML format."
  (:require [clj-assorted-utils.util]
            [leiningen.core.main]
            [leiningen.core.project]
            [leiningen.test]
            [robert.hooke]
            [test2junit.core]))

(defn add-test-var-println [f & args]
  (println "Running Tests...")
  (apply f args))

(defn- generate-html
  [project]
  (when (:test2junit-run-ant project)
    (println "\nRunning ant to generate HTML report...")
    (let [ret (.waitFor (clj-assorted-utils.util/exec-with-out "ant" println))]
      (if (= 0 ret)
        (println "Report was successfully generated.")
        (println "There was a problem during report generation. Ant returned with:" ret)))))

(defn test2junit
  "Output test results to JUnit XML format.

   This plug-in writes test results to files in JUnit XML format.
   These files can be used, e.g., with junitreport for creating reports in HTML format.
   Please see the webpage for more information: https://github.com/ruedigergad/test2junit

   You can tweak some setting via your project.clj file:
   The directory to which the results are written can be set with :test2junit-output-dir
   To run Ant automatically set :test2junit-run-ant to true.
  "
  [project & keys]
  (robert.hooke/add-hook #'leiningen.test/form-for-testing-namespaces
                         add-test-var-println)
  (let [output-dir (test2junit.core/get-output-dir project)
        test2junit-version ((first (filter #(= (first %) 'test2junit/test2junit) (:plugins project))) 1)
        _ (println "Using test2junit version:" test2junit-version)
        test2junit-profile [{:injections `[(require 'test2junit.core)
                                           (test2junit.core/apply-junit-output-hook ~output-dir)]
                             :dependencies [['robert/hooke "1.3.0"]
                                            ['clj-assorted-utils "1.11.0"]
                                            ['test2junit test2junit-version]]}]]
    (binding [leiningen.core.main/*exit-process?* false]
      (try
        (apply leiningen.test/test (leiningen.core.project/merge-profiles project test2junit-profile) keys)
        (catch clojure.lang.ExceptionInfo e
          (if (:exit-code (ex-data e))
            (do (generate-html project)
                (throw e))
            (println "Caught exception:" e))))))
  (generate-html project))
