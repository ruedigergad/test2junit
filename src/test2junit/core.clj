;;;
;;;   Copyright 2013, Ruediger Gad <r.c.g@gmx.de>
;;;
;;;   This software is released under the terms of the Eclipse Public License 
;;;   (EPL) 1.0. You can find a copy of the EPL at: 
;;;   http://opensource.org/licenses/eclipse-1.0.php
;;;

(ns test2junit.core
  (:require clj-assorted-utils.util
            clojure.java.io
            [clojure.string :only (replace)]
            robert.hooke
            test2junit.junit))

(def default-ant-build-file-content
  "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>
<project basedir=\".\" default=\"create_html_report\" name=\"test2junit-html-report-generator\">
    <target name=\"create_html_report\">
        <junitreport todir=\"test2junit-dir/tmp\">
            <fileset dir=\"test2junit-dir/xml\" />
            <report todir=\"test2junit-dir/html\" />
        </junitreport>
    </target>
</project>")

(defn get-output-dir [project]
  (if (nil? (:test2junit-output-dir project))
    "test2junit"
    (:test2junit-output-dir project)))

(defn apply-junit-output-hook [output-dir]
  (println "Writing output to:" output-dir)
  (when (not (clj-assorted-utils.util/file-exists? "build.xml"))
    (println "Creating default build.xml file.")
    (spit "build.xml" (clojure.string/replace default-ant-build-file-content "test2junit-dir" output-dir)))
  (println "")
  (robert.hooke/add-hook 
    #'clojure.test/test-ns
    (fn [f# & args#]
      (clj-assorted-utils.util/mkdir output-dir)
      (clj-assorted-utils.util/mkdir (str output-dir "/html"))
      (clj-assorted-utils.util/mkdir (str output-dir "/tmp"))
      (clj-assorted-utils.util/mkdir (str output-dir "/xml"))
      (let [ns# (first args#)]
        (println "Testing:" ns#)
        (with-open [wrtr# (clojure.java.io/writer (str output-dir "/xml/" ns# ".xml"))]
          (binding [clojure.test/*test-out* wrtr#]
            (test2junit.junit/with-junit-output
              (apply f# args#))))))))

