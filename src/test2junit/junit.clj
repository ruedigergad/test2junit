; This file had originally been taken from:
; https://github.com/clojure/clojure/blob/clojure-1.5.1/src/clj/clojure/test/junit.clj
; It was modified such that the emitted XML can be used with junitreport for
; generating reports in HTML format.
; Additionally, functionality was added to output summaries about the number of
; tests, failed test, errors, and the duration of tests.
; The modifications are released under the EPL 1.0, which is the same license 
; as the original code.
; The author of the modifications is: Ruediger Gad <r.c.g@gmx.de>
; The original copyright etc. is written below.
;
;
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
            [clojure.test :as t]
            [clojure.java.io :as io])
  (:import (java.text SimpleDateFormat)
           (java.util Date)))

(def testsuite-temp-string (ref ""))
(def testsuite-start-time (ref 0.0))
(def testcase-start-time (ref 0.0))
(def result-temp-string (ref ""))

(def out-wrtr (io/writer System/out))

;; copied from clojure.contrib.lazy-xml
(def ^{:private true}
     escape-xml-map
     (zipmap "'<>\"&" (map #(str \& % \;) '[apos lt gt quot amp])))

(defn- escape-xml [text]
  (.replaceAll (apply str (map #(escape-xml-map % %) text)) "[\\p{C}&&[^\\s]]" ""))

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
                  :time (format "%.4f" (/ (- (System/nanoTime) @testcase-start-time) 1000000000.0))}))

(defn finish-case
  []
  (finish-element 'testcase true))

(defn simple-element
  [tag content]
  (start-element tag true)
  (element-content content)
  (finish-element tag true))

(defn suite-attrs
  [package classname]
  (let [attrs {:name (str package "." classname)
               :errors (str (:error @t/*report-counters*))
               :failures (str (:fail @t/*report-counters*))
               :tests (str (:test @t/*report-counters*))
               :time (format "%.4f" (/ (- (System/nanoTime) @testsuite-start-time) 1000000000.0))
               :timestamp (-> (SimpleDateFormat. "yyyy-MM-dd_HH:mm:ssZ") (.format (Date.))) }]
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
   (let [[file line] (t/file-position 15)
         detail (apply str (interpose
                            "\n"
                            [(str "expected: " expected-str)
                             (str "  actual: " actual-str)
                             (str "      at: " file ":" line)]))]
     detail))
  (finish-element tag false)
  (println))

(defn message-or-location
  [message]
  (if message
    message
    (let [[file line] (t/file-position 15)]
      (str "In file " file " at line " line ":"))))

(defn failure-el
  [message expected actual]
  (message-el 'failure
              (message-or-location message)
              (pr-str expected)
              (pr-str actual)))

(defn error-el
  [message expected actual]
  (message-el 'error
              (message-or-location message)
              (pr-str expected)
              (if (instance? Throwable actual)
                (with-out-str (stack/print-cause-trace actual t/*stack-trace-depth*))
                (prn actual))))

;; This multimethod will override test-is/report
(defmulti ^:dynamic junit-report :type)

(defmethod junit-report :begin-test-ns [m]
  (binding [*out* out-wrtr]
    (println "Running tests in:" (ns-name (:ns m))))
  (set! *depth* (inc *depth*))
  (dosync (ref-set testsuite-temp-string ""))
  (dosync (ref-set testsuite-start-time (System/nanoTime))))

(defmethod junit-report :end-test-ns [m]
  (binding [*out* out-wrtr]
    (println "Finished tests in:" (ns-name (:ns m))))
  (set! *depth* (dec *depth*))
  (t/with-test-out
    (start-suite (name (ns-name (:ns m))))
    (print @testsuite-temp-string)))

(defn add-properties
  []
  (let [system-properties (merge {} (System/getProperties))]
    (start-element 'properties true)
    (doseq [[k v] system-properties]
      (start-element 'property true {:name k :value v})
      (finish-element 'property true))
    (finish-element 'properties true)))

(defn close-suite
  [eo-map]
  (t/with-test-out
    (binding [test2junit.junit/*depth* 1]
      (simple-element 'system-err (:stderr eo-map))
      (simple-element 'system-out (:all eo-map))
      (add-properties)
      (finish-suite))))

(defmethod junit-report :begin-test-var [m]
  (binding [*out* out-wrtr]
    (println "  Running test:" (:var m)))
  (dosync (ref-set result-temp-string ""))
  (dosync (ref-set testcase-start-time (System/nanoTime))))

(defmethod junit-report :end-test-var [m]
  (binding [*out* out-wrtr]
    (println "  Finished test:" (:var m)))
  (dosync (alter testsuite-temp-string str
    (with-out-str
      (let [var (:var m)]
        (binding [*var-context* (conj *var-context* var)]
          (start-case (test-name *var-context*) (name (ns-name (:ns (meta var)))))))
      (print @result-temp-string)
      (finish-case)))))

(defmethod junit-report :pass [m]
  (binding [*out* out-wrtr]
    (println "    PASS"))
  (t/inc-report-counter :pass))

(defmethod junit-report :fail [m]
  (do
    (binding [*out* out-wrtr]
      (println "    FAIL")
      (println "      Expected:" (:expected m))
      (println "      Actual:" (:actual m)))
    (t/inc-report-counter :fail)
    (dosync (alter result-temp-string str
      (with-out-str
        (failure-el (:message m)
                    (:expected m)
                    (:actual m)))))))

(defmethod junit-report :error [m]
  (do
    (binding [*out* out-wrtr]
      (println "    ERROR")
      (let [ex (:actual m)]
        (println "      Message:" (.getMessage ex))
        (println "      Cause:" (.getCause  ex))
        (println "      Trace:")
        (doseq [trace-line (.getStackTrace ex)]
          (println "       " trace-line))))
    (t/inc-report-counter :error)
    (dosync (alter result-temp-string str
      (with-out-str
        (error-el (:message m)
                  (:expected m)
                  (:actual m)))))))

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
