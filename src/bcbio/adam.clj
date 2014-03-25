(ns bcbio.adam
  (:import [edu.berkeley.cs.amplab.adam.rdd AdamContext]
           [edu.berkeley.cs.amplab.adam.rdd.variation ADAMVariationContext]
           [scala.collection JavaConversions])
  (:require [clj-spark.api :as k]))

(defn- to-scala-seq
  "Convert Clojure collection into a Scala Seq.
   http://stackoverflow.com/questions/17393019/clojure-to-scala-type-conversions"
  [coll]
  (-> coll JavaConversions/asScalaBuffer .toList))

(defn variant->rdd-genotype-w-adam
  "ADAM compatible Spark RDD using only ADAM code."
  [vcf-file]
  (-> (AdamContext/createSparkContext vcf-file "local" nil
                                      (to-scala-seq []) (to-scala-seq []) false 4)
      (ADAMVariationContext.)
      (.adamVCFLoad vcf-file)
      println))

(defn variant->rdd-genotype
  "Retrieve a Adam compatible Spark RDD from a pre-created input file."
  [vcf-adam-file]
  (k/with-context [jsc "local" vcf-adam-file]
    (-> (.sc jsc)
        (ADAMVariationContext.)
        (.adamVCFLoad vcf-adam-file)
        println)))
