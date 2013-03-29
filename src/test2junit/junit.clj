;   Copyright (c) Rich Hickey. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

;; test/junit.clj: Extension to clojure.test for JUnit-compatible XML output

;; by Jason Sankey
;; June 2009

;; DOCUMENTATION
;;

(ns ^{:doc "clojure.test extension for JUnit-compatible XML output.

  JUnit (http://junit.org/) is the most popular unit-testing library
  for Java.  As such, tool support for JUnit output formats is
  common.  By producing compatible output from tests, this tool
  support can be exploited.

  To use, wrap any calls to clojure.test/run-tests in the
  with-junit-output macro, like this:

    (use 'clojure.test)
    (use 'clojure.test.junit)

    (with-junit-output
      (run-tests 'my.cool.library))

  To write the output to a file, rebind clojure.test/*test-out* to
  your own PrintWriter (perhaps opened using
  clojure.java.io/writer)."
  :author "Jason Sankey"}
  test2junit.junit
  (:require [clojure.stacktrace :as stack]
            [clojure.test :as t]))

(def testsuite-temp-string (ref ""))
(def testsuite-start-time (ref 0.0))
(def testcase-start-time (ref 0.0))
(def result-temp-string (ref ""))

;; copied from clojure.contrib.lazy-xml
(def ^{:private true}
     escape-xml-map
     (zipmap "'<>\"&" (map #(str \& % \;) '[apos lt gt quot amp])))
(defn- escape-xml [text]
  (apply str (map #(escape-xml-map % %) text)))

(def ^:dynamic *var-context*)
(def ^:dynamic *depth*)

(defn indent
  []
  (dotimes [n (* *depth* 4)] (print " ")))

(defn start-element
  [tag pretty & [attrs]]
  (if pretty (indent))
  (print (str "<" tag))
  (if (seq attrs)
    (doseq [[key value] attrs]
      (print (str " " (name key) "=\"" (escape-xml value) "\""))))
  (print ">")
  (if pretty (println))
  (set! *depth* (inc *depth*)))

(defn element-content
  [content]
  (print (escape-xml content)))

(defn finish-element
  [tag pretty]
  (set! *depth* (dec *depth*))
  (if pretty (indent))
  (print (str "</" tag ">"))
  (if pretty (println)))

(defn test-name
  [vars]
  (apply str (interpose "."
                        (reverse (map #(:name (meta %)) vars)))))

(defn package-class
  [name]
  (let [i (.lastIndexOf name ".")]
    (if (< i 0)
      [nil name]
      [(.substring name 0 i) (.substring name (+ i 1))])))

(defn start-case
  [name classname]
  (start-element 'testcase true 
                 {:name name 
                  :classname classname
                  :time (str (/ (- (System/nanoTime) @testcase-start-time) 1000000000.0))}))

(defn finish-case
  []
  (finish-element 'testcase true))

(defn suite-attrs
  [package classname]
  (let [attrs {:name (str package "." classname)
               :errors (str (:error @t/*report-counters*))
               :failures (str (:fail @t/*report-counters*))
               :tests (str (:test @t/*report-counters*))
               :time (str (/ (- (System/nanoTime) @testsuite-start-time) 1000000000.0))}]
    attrs))

(defn start-suite
  [name]
  (let [[package classname] (package-class name)]
    (start-element 'testsuite true (suite-attrs package classname))))

(defn finish-suite
  []
  (finish-element 'testsuite true))

(defn message-el
  [tag message expected-str actual-str]
  (indent)
  (start-element tag false (if message {:message message} {}))
  (element-content
   (let [[file line] (t/file-position 5)
         detail (apply str (interpose
                            "\n"
                            [(str "expected: " expected-str)
                             (str "  actual: " actual-str)
                             (str "      at: " file ":" line)]))]
     (if message (str message "\n" detail) detail)))
  (finish-element tag false)
  (println))

(defn failure-el
  [message expected actual]
  (message-el 'failure message (pr-str expected) (pr-str actual)))

(defn error-el
  [message expected actual]
  (message-el 'error
              message
              (pr-str expected)
              (if (instance? Throwable actual)
                (with-out-str (stack/print-cause-trace actual t/*stack-trace-depth*))
                (prn actual))))

;; This multimethod will override test-is/report
(defmulti ^:dynamic junit-report :type)

(defmethod junit-report :begin-test-ns [m]
  (set! *depth* (inc *depth*))
  (dosync (ref-set testsuite-temp-string ""))
  (dosync (ref-set testsuite-start-time (System/nanoTime))))

(defmethod junit-report :end-test-ns [m]
  (set! *depth* (dec *depth*))
  (t/with-test-out
    (start-suite (name (ns-name (:ns m))))
    (print @testsuite-temp-string)
    (finish-suite)))

(defmethod junit-report :begin-test-var [m]
  (dosync (ref-set result-temp-string ""))
  (dosync (ref-set testcase-start-time (System/nanoTime))))

(defmethod junit-report :end-test-var [m]
  (dosync (alter testsuite-temp-string str
    (with-out-str
      (let [var (:var m)]
        (binding [*var-context* (conj *var-context* var)]
          (start-case (test-name *var-context*) (name (ns-name (:ns (meta var)))))))
      (print @result-temp-string)
      (finish-case)))))

(defmethod junit-report :pass [m]
  (t/inc-report-counter :pass))

(defmethod junit-report :fail [m]
  (dosync (alter result-temp-string str
    (with-out-str
      (t/inc-report-counter :fail)
      (failure-el (:message m)
                  (:expected m)
                  (:actual m))))))

(defmethod junit-report :error [m]
  (dosync (alter result-temp-string str
    (with-out-str
      (t/inc-report-counter :error)
      (error-el (:message m)
                (:expected m)
                (:actual m))))))

(defmethod junit-report :default [_])

(defmacro with-junit-output
  "Execute body with modified test-is reporting functions that write
  JUnit-compatible XML output."
  {:added "1.1"}
  [& body]
  `(binding [t/report junit-report
             *var-context* (list)
             *depth* 0]
     (t/with-test-out
       (println "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"))
     (let [result# ~@body]
       result#)))
