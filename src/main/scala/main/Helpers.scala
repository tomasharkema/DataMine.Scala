package main

/**
  * Created by tomas on 26-02-16.
  */
object Helpers {
  implicit class StreamGroup[A](stream: scala.collection.immutable.Stream[A]) {
    def groupByKeyAndValue[K, V](k: A => K)(v: A => scala.collection.immutable.Stream[V]) = stream
      .groupBy(k)
      .map(value => (value._1, value._2.flatMap(v)))
  }
}
