(ns bcbio.adam.partition.bed
  "Partition RDDs by a pre-defined set of genomic regions in a BED file"
  (:import [org.apache.spark.Partitioner])
  (:require [clojure.java.io :as io]
            [bcbio.adam.util.convert :refer [scala->clj]]
            [bcbio.align.bed :as bed])
  (:gen-class
   :name bcbio.adam.partition.bed.BedPartitioner
   :init init
   :state state
   :constructors {[String] []}
   :extends org.apache.spark.Partitioner))

(defn -init
  [bed-file]
  [[] bed-file])

(defn -numPartitions
  [this]
  (let [bed-file (.state this)]
    (with-open [rdr (io/reader bed-file)]
      (count (line-seq rdr)))))

(defn -getPartition [this orig-key]
  (let [bed-file (.state this)
        key (scala->clj orig-key)]
    (->> (bed/reader bed-file)
         (map-indexed (fn [i p]
                        (when (and (= (:chrom p) (:chrom key))
                                   (>= (:start key) (:start p))
                                   (< (:start key) (:end p)))
                          i)))
         (remove nil?)
         first)))
