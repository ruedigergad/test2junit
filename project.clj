;;;
;;;   Copyright 2013-2017, Ruediger Gad <r.c.g@gmx.de>
;;;
;;;   This software is released under the terms of the Eclipse Public License 
;;;   (EPL) 1.0. You can find a copy of the EPL at: 
;;;   http://opensource.org/licenses/eclipse-1.0.php
;;;

(defproject test2junit "1.4.2"
  :description "Leiningen plug-in for emitting test output in JUnit XML format"
  :url "https://github.com/ruedigergad/test2junit"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :eval-in-leiningen true
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clj-assorted-utils "1.18.2"]]
  :profiles  {:repl  {:dependencies  [[jonase/eastwood "0.2.3" :exclusions  [org.clojure/clojure]]]}
              :test {:dependencies [[prismatic/schema "1.1.6"]]}}
  :test2junit-run-ant true
  :test2junit-output-dir "ghpages/test-results"
)
