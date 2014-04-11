(ns bcbio.adam
  (:import [org.bdgenomics.adam.rdd ADAMContext]
           [org.bdgenomics.adam.rdd.variation ADAMVariationContext])
  (:require [bcbio.adam.util.convert :refer [clj->scala]]
            [clj-spark.api :as k]))

(defn variant->rdd-genotype-w-adam
  "ADAM compatible Spark RDD using only ADAM code."
  [vcf-file]
  (-> (ADAMContext/createSparkContext vcf-file "local" nil
                                      (clj->scala []) (clj->scala []) false 4)
      (ADAMVariationContext.)
      (.adamVCFLoad vcf-file false)
      println))

(defn avc->avar
  "Convert a ADAM VariantContext to ADAMVariant"
  [avc]
  (-> avc .variant .variant))

(defn- examine-item
  [avc]
  (let [avar (avc->avar avc)]
    (clj->scala
     {:contig (.contigName (.contig avar)) :start (.position avar)
      :ref (.referenceAllele avar) :alt (.variantAllele avar)})))

(defn variant->rdd-genotype
  "Retrieve a Adam compatible Spark RDD from a pre-created input file."
  [vcf-adam-file]
  (k/with-context [jsc "local" vcf-adam-file]
    (-> (.sc jsc)
        ADAMVariationContext.
        (.adamVCFLoad vcf-adam-file false)
        .toJavaRDD
        (k/map examine-item)
        k/collect
        println)))
