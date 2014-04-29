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

(defn- get-spark-context
  "Retrieve spark context from configured inputs.
   XXX Currently all local, needs to be generalized."
  [name]
  (ADAMContext/createSparkContext name "local" nil
                                  (clj->scala []) (clj->scala []) false 4 true None$/MODULE$))

(defn avc->avar
  "Convert a ADAM VariantContext to ADAMVariant"
  [avc]
  (-> avc .variant .variant))

(defn- process-items
  "Unpack Scala Tuple2 sets of coordinates/ADAMVariants to process in region.
   XXX Need to figure out how to get a spark context and RDD from our
   current list of ADAMVariantContexts"
  [out-base]
  (sfn/fn process-items-inner [coord-avars]
    (let [avcs (map #(._2 %) (iterator-seq coord-avars))
          out-file (format "%s-%s-%s.vcf" out-base (-> avcs first avc->avar .contig .contigName)
                           (-> avcs first avc->avar .position))
          ;sc (get-spark-context out-file)
          ]
      (println "-----")
      (doseq [x avcs]
        (println (avc->avar x)))
      (println "-----")
      ;(.adamVCFSave (ADAMVariationContext. sc) out-file avcs nil)
      [out-file])))

(defn add-pos-to-rec
  "Retrieve ADAM records for partitioning by region"
  [avc]
  (let [avar (avc->avar avc)]
    [(clj->scala {:chrom (.contigName (.contig avar)) :start (.position avar)})
     avc]))

(defn- rdd-partition-by-regions
  "Partition a RDD by predefined regions"
  [rdd bed-file]
  (-> rdd
      (.map (PairFunction. add-pos-to-rec))
      (.partitionBy (BedPartitioner. bed-file))))

(defn split-by-region
  "Process ADAM variant contexts partitioned by pre-defined genomic regions."
  [vcf-file bed-file]
  (let [sc (get-spark-context vcf-file)
        out-base (str (io/file (fsp/safe-mkdir (io/file (fs/parent vcf-file) "partitions"))
                               (fs/base-name (fsp/file-root vcf-file))))]
    (-> (ADAMVariationContext. sc)
        (.adamVCFLoad vcf-file false)
        .toJavaRDD
        (rdd-partition-by-regions bed-file)
        (.mapPartitions (FlatMapFunction. (process-items out-base)))
        k/collect
        println)))
