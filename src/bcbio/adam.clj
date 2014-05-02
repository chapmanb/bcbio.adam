(ns bcbio.adam
  (:import [bcbio.adam.partition.bed BedPartitioner]
           [clj_spark.fn PairFunction Function FlatMapFunction]
           [org.bdgenomics.adam.rdd ADAMContext]
           [org.bdgenomics.adam.rdd.variation ADAMVariationContext]
           [scala None$])
  (:require [bcbio.adam.util.convert :refer [clj->scala]]
            [bcbio.run.fsp :as fsp]
            [clj-spark.api :as k]
            [clojure.java.io :as io]
            [me.raynes.fs :as fs]
            [serializable.fn :as sfn]))

(defn avc->avar
  "Convert a ADAM VariantContext to ADAMVariant"
  [avc]
  (-> avc .variant .variant))

(defn- process-items
  "Unpack Scala Tuple2 sets of coordinates/ADAMVariants to process in region.
   XXX Need to figure out how to get a RDD/context from our current list of ADAMVariantContexts
       or write file locally without needing hadoop configuration from context."
  [out-base]
  (sfn/fn process-items-inner [coord-iter]
    (let [avcs (map #(._2 %) (iterator-seq coord-iter))
          ;rdd  TODO -- how best to access/generate this?
          ;sc (.context rdd)
          out-file (format "%s-%s-%s.vcf" out-base (-> avcs first avc->avar .contig .contigName)
                           (-> avcs first avc->avar .position))]
      (println "-----")
      (doseq [x avcs]
        (println (avc->avar x)))
      (println "-----")
      ;(.adamVCFSave (ADAMVariationContext. sc) out-file rdd nil)
      [out-file])))

(defn- add-pos-to-rec
  "Retrieve ADAM records keyed by region, for partitioning."
  [avc]
  (let [avar (avc->avar avc)]
    [(clj->scala {:chrom (.contigName (.contig avar)) :start (.position avar)})
     avc]))

(defn- get-spark-context
  "Retrieve spark context from configured inputs.
   XXX Hardcoded to local execution, needs to be generalized."
  [name]
  (ADAMContext/createSparkContext name "local" nil
                                  (clj->scala []) (clj->scala []) false 4 true None$/MODULE$))

(defn split-by-region
  "Process ADAM variant contexts partitioned by pre-defined genomic regions."
  [vcf-file bed-file]
  (let [sc (get-spark-context vcf-file)
        out-base (str (io/file (fsp/safe-mkdir (io/file (fs/parent vcf-file) "partitions"))
                               (fs/base-name (fsp/file-root vcf-file))))]
    (-> (ADAMVariationContext. sc)
        (.adamVCFLoad vcf-file false)
        .toJavaRDD
        (.map (PairFunction. add-pos-to-rec))
        (.partitionBy (BedPartitioner. bed-file))
        (.mapPartitions (FlatMapFunction. (process-items out-base)))
        k/collect
        println)))
