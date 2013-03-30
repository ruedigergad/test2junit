;;;
;;;   Copyright 2013, Ruediger Gad
;;;
;;;   This software is released under the terms of the Eclipse Public License 
;;;   (EPL) 1.0. You can find a copy of the EPL at: 
;;;   http://opensource.org/licenses/eclipse-1.0.php
;;;

(ns test2junit.plugin
  (require test2junit.core))

(defn middleware [project]
  (let [output-dir (test2junit.core/get-output-dir project)
        prjct (update-in project [:injections] concat
                       `[(require 'test2junit.core)
                         (test2junit.core/apply-junit-output-hook ~output-dir)])]
    (update-in prjct [:dependencies] concat
               [['robert/hooke "1.3.0"]
                ['clj-assorted-utils "1.2.4"]
                ['test2junit "0.1.0-SNAPSHOT"]])))

