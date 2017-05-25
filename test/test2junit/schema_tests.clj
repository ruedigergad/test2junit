;;;
;;;   Copyright 2017, Ruediger Gad <r.c.g@gmx.de>
;;;
;;;   This software is released under the terms of the Eclipse Public License 
;;;   (EPL) 1.0. You can find a copy of the EPL at: 
;;;   http://opensource.org/licenses/eclipse-1.0.php
;;;

(ns
 ^{:author "Ruediger Gad",
   :doc "Simple tests for testing the effect of prismatic/schema output.
         See also: https://github.com/ruedigergad/test2junit/issues/12"} 
  test2junit.schema-tests
  (:require
    [clojure.test :refer :all]
    [clj-assorted-utils.util :refer :all]
    [schema.core :as s]))

(deftest schema-validate-num-success
  (s/validate s/Num 1701))

(deftest schema-validate-num-fail
  (s/validate s/Num "1864"))

;;;
;;; The example schema data and validation snippets were taken from the README at:
;;; https://github.com/plumatic/schema#meet-schema
;;;
(def Data
  "A schema for a nested data type"
  {:a {:b s/Str
       :c s/Int}
   :d [{:e s/Keyword
        :f [s/Num]}]})

(deftest schema-validate-success
  (s/validate
    Data
    {:a {:b "abc"
         :c 123}
     :d [{:e :bc
          :f [12.2 13 100]}
         {:e :bc
          :f [-1]}]}))

(deftest schema-validate-fail
  (s/validate
    Data
    {:a {:b 123
         :c "ABC"}}))

;;;
;;; The example schema data and validation snippets are based on examples from the README at:
;;; https://github.com/plumatic/schema#beyond-type-hints
;;;

(s/defrecord StampedNames
  [date :- Long
   names :- [s/Str]])

(s/defn stamped-names-correct :- StampedNames
  [names :- [s/Str]]
  (StampedNames. (System/currentTimeMillis) names))

(s/defn stamped-names-failing :- StampedNames
  [names :- [s/Str]]
  (StampedNames. (str (System/currentTimeMillis)) names))

(deftest schema-explain-record
  (s/explain StampedNames))

(deftest schema-explain-correct-fn
  (s/explain (s/fn-schema stamped-names-correct)))

(deftest schema-explain-failing-fn
  (s/explain (s/fn-schema stamped-names-failing)))

(deftest schema-fn-validation-correct-fn
  (s/with-fn-validation
    (stamped-names-correct ["bob"])))

(deftest schema-fn-validation-failing-fn
  (s/with-fn-validation
    (stamped-names-failing ["bob"])))

