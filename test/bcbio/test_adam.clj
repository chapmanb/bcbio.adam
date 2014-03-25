(ns bcbio.test-adam
  "Tests for processing ADAM variant files"
  (:require [clojure.java.io :as io]
            [midje.sweet :refer :all]
            [bcbio.adam :as adam]))

(facts "Access an ADAM generated file of variants."
  (let [vcf-file (str (io/file "test" "data" "NA12878-10-gatk-haplotype.vcf"))
        adam-file (str (io/file "test" "data" "NA12878-10-gatk-haplotype.adam"))]
    (adam/variant->rdd-genotype-w-adam vcf-file)
    ;(adam/variant->rdd-genotype vcf-file)
    ))
