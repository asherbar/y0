(ns y0.main
  (:require [y0.core :refer [all test]]
            [y0.modules :refer [load-with-dependencies]]
            [y0.rules :refer [add-rule]]
            [y0.status :refer [ok let-s]]
            [y0.testing :refer [apply-test-block]]
            [y0.explanation :refer [explanation-to-str code-location]]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.term.colors :refer [red green]])
  (:gen-class))

(def cli-options
  [["-p" "--path PATH" "Add to Y0_PATH"
    :multi true
    :default []
    :update-fn conj]
   ["-h" "--help"]])

(defn- apply-statement [statement ps]
  (let [[form & _] statement]
    (case form
      y0.core/all (add-rule ps statement)
      y0.core/test (apply-test-block ps statement)
      (ok ps))))

(defn- apply-statements [statements]
  (loop [statements statements
         ps {}]
    (if (empty? statements)
      ps
      (let [[statement & statements] statements]
        (let-s [ps (apply-statement statement ps)]
               (recur statements ps))))))

(defn- main [modules path]
  (let [[statements _] (load-with-dependencies modules path)
        status (apply-statements statements)]
    (if (:err status)
      (let [explanation (:err status)
            message (explanation-to-str explanation {})
            location (code-location explanation)]
        (binding [*out* *err*]
          (println (str (:path location) ":" (:row location) ": " (red "Error") ":") message))
        (System/exit 1))
      (println (green "Success")))))

(defn -main [& args]
  (let [cli (parse-opts args cli-options)
        {:keys [path help]} (:options cli)
        modlues (:arguments cli)]
    (cond
      help (println (:summary cli))
      :else (main modlues path))))
