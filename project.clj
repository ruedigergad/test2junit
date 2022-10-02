;;;
;;;   Copyright 2013-2017, Ruediger Gad <r.c.g@gmx.de>
;;;
;;;   This software is released under the terms of the Eclipse Public License 
;;;   (EPL) 1.0. You can find a copy of the EPL at: 
;;;   http://opensource.org/licenses/eclipse-1.0.php
;;;

; Remember to update the version info in src/leiningen/test2junit.clj as well.
; (Or find a way to automatically do this on compilation/deployment.)
(defproject test2junit "1.4.3"
  :description "Leiningen plug-in for emitting test output in JUnit XML format"
  :url "https://github.com/ruedigergad/test2junit"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :eval-in-leiningen true
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [clj-assorted-utils "1.19.0"]]
  :profiles  {:repl  {:dependencies  [[jonase/eastwood "1.3.0" :exclusions  [org.clojure/clojure]]]}
              :test {:dependencies [[prismatic/schema "1.4.1"]]}}
  :test2junit-run-ant true
  :test2junit-output-dir "ghpages/test-results"
)
