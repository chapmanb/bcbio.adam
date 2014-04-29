(ns bcbio.adam.main
  (:require [bcbio.adam :as adam])
  (:gen-class))

(defn -main [& args]
  (adam/split-by-region (first args)))
