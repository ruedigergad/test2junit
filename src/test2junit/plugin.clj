(ns test2junit.plugin)

(defn middleware [project]
  (let [output-dir (if (nil? (:test2junit-output-dir project))
                     "test2junit"
                     (:test2junit-output-dir project))
        prjct (update-in project [:injections] concat
                       `[(require 'test2junit.core)
                         (test2junit.core/apply-junit-output-hook ~output-dir)])]
    (update-in prjct [:dependencies] concat
               [['robert/hooke "1.3.0"]
                ['clj-assorted-utils "1.2.0"]
                ['test2junit "0.1.0-SNAPSHOT"]])))

