(ns bcbio.adam.main
  (:require [bcbio.adam :as adam])
  (:gen-class))

(defn -main [& args]
  (adam/variant->rdd-genotype (first args)))
