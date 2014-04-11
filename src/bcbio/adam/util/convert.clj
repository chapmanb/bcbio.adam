(ns bcbio.adam.util.convert
  "Clojure/Scala/Java interop of standard types.
   Started with code from @richo:
   https://github.com/richo/gordon-riemann/blob/master/src/gordon/edda/scala.clj"
  (import [scala Predef]
          [scala.collection JavaConverters JavaConversions]))

;; Clojure/Java -> Scala

(defmulti clj->scala
  "Convert Clojure things to Scala things."
  class)

(defmethod clj->scala java.util.Map [m]
  (-> (JavaConverters/mapAsScalaMapConverter m)
      (.asScala)
      (.toMap (Predef/conforms))))

(defmethod clj->scala java.util.Set [s]
  (-> (JavaConverters/asScalaSetConverter s)
      (.asScala)
      (.toSet)))

(defmethod clj->scala java.util.Collection [coll]
  ^{:doc "Convert Clojure collection into a Scala Seq.
    http://stackoverflow.com/questions/17393019/clojure-to-scala-type-conversions"}
  (-> coll JavaConversions/asScalaBuffer .toList))

(defmethod clj->scala :default [x]
  x)

;; Scala -> Clojure/Java

(defmulti scala->clj
  "Convert Scala things to Clojure things"
  class)

(defmethod scala->clj scala.collection.Map [m]
  (into {} (map (fn [[k v]]
                  [(scala->clj k)
                   (scala->clj v)])
                (JavaConversions/asJavaMap m))))

(defmethod scala->clj scala.collection.Iterable [coll]
  (map scala->clj (JavaConversions/asJavaIterable coll)))

(defmethod scala->clj :default [x]
  x)
