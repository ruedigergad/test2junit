;;;
;;;   Copyright 2017, Ruediger Gad <r.c.g@gmx.de>
;;;
;;;   This software is released under the terms of the Eclipse Public License 
;;;   (EPL) 1.0. You can find a copy of the EPL at: 
;;;   http://opensource.org/licenses/eclipse-1.0.php
;;;

(ns
 ^{:author "Ruediger Gad",
   :doc "Simple tests to illustrate failing, passing, and erroneous tests."} 
  test2junit.tests
  (:require
    [clojure.test :refer :all]))

(deftest failing-test
  (let [foo 1]
    (is
      (= foo 0))))

(deftest passing-test
  (let [foo 1]
    (is
      (= foo 1))))

(deftest erroneous-test
  (let [foo 1]
    (is
      (= foo (/ 1 0)))))

