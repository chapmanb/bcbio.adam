(defproject bcbio.adam "0.0.1-SNAPSHOT"
  :description "Clojure interface to ADAM distributed file formats for variants and aligned reads"
  :url "https://github.com/bigdatagenomics/adam"
  :license {:name "MIT" :url "http://www.opensource.org/licenses/mit-license.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojars.r0man/clj-spark "0.1.0-SNAPSHOT"]
                 [org.apache.spark/spark-streaming_2.10 "0.9.0-incubating"
                  :exclusions [org.apache.hadoop/hadoop-client
                               org.apache.spark/spark-core_2.10
                               org.codehaus.jackson/jackson-mapper-asl
                               org.eclipse.jetty/jetty-server
                               org.scala-lang/scala-library
                               com.thoughtworks.paranamer/paranamer
                               org.slf4j/slf4j-api
                               com.google.protobuf/protobuf-java
                               commons-codec commons-io commons-lang]]
                 [edu.berkeley.cs.amplab.adam/adam-cli "0.7.1"]
                 [edu.berkeley.cs.amplab.adam/adam-core "0.7.1"]
                 [edu.berkeley.cs.amplab.adam/adam-format "0.7.1"]]
  :plugins [[lein-midje "3.1.3"]]
  :profiles {:dev {:dependencies
                   [[midje "1.6.3" :exclusions [commons-codec]]]}
             :uberjar {:aot [bcbio.adam.main]}}
  :uberjar-merge-with {"reference.conf" [slurp str spit]
                       "META-INF/services/org.apache.hadoop.fs.FileSystem" [slurp str spit]}
  :main ^:skip-aot bcbio.adam.main)
