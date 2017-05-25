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
;;; The example schema data and validation snippets are based on examples from the README at:
;;; https://github.com/plumatic/schema#beyond-type-hints
;;;

(s/defrecord StampedNames
  [date :- Long
   names :- [s/Str]])

(s/defn stamped-names-failing :- StampedNames
  [names :- [s/Str]]
  (StampedNames. (str (System/currentTimeMillis)) names))

(deftest schema-fn-validation-failing-fn
  (s/with-fn-validation
    (stamped-names-failing ["bob"])))

