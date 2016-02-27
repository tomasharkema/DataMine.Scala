package main

/**
  * Created by tomas on 27-02-16.
  */
object WordCount {
  def countWords(message: String) = SeparatorIterator(message, " ")
    .filter(_ != "")
    .toStream
    .map(word => word.toLowerCase)
    .groupBy(word => word)
    .toStream
    .map(k => (k._1, k._2.toSeq.size))
    .sortBy(_._2)
    .reverse
}
