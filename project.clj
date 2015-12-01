;;;
;;;   Copyright 2013, Ruediger Gad <r.c.g@gmx.de>
;;;
;;;   This software is released under the terms of the Eclipse Public License 
;;;   (EPL) 1.0. You can find a copy of the EPL at: 
;;;   http://opensource.org/licenses/eclipse-1.0.php
;;;

(defproject test2junit "1.1.3"
  :description "Leiningen plug-in for emitting test output in JUnit XML format"
  :url "https://github.com/ruedigergad/test2junit"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :eval-in-leiningen true
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [clj-assorted-utils "1.11.0"]]
  :profiles  {:repl  {:dependencies  [[jonase/eastwood "0.2.2" :exclusions  [org.clojure/clojure]]]}} 
)
