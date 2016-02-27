package main

import Helpers._

/**
  * Created by tomas on 26-02-16.
  */
object Main {

  def replaceIrregularities(string: String) = string
    .replaceAll("<br />", "\n")
    .replaceAll("<wbr />", "\n")

  def main(args: Array[String]) {

    println("Load file")
    val stream = getClass.getResourceAsStream("/klachtendumpgemeente.csv")
    println("Load loaded. Read it.")
    val lines = scala.io.Source.fromInputStream(stream).getLines
    println("Got lines. Parsing it")
    val rows = lines.map(SeparatorIterator(_, ";")) drop 1 map { _ map replaceIrregularities }
    println("Parsed lines. Crunching it")

    val categoriesAndInput = rows
      .map { el =>
        new {
          val key = el.take(1).toStream.head.toLowerCase
          val value = el// slice(1, 4)
        }
      }
      .toStream.groupByKeyAndValue(_.key)(_.value.toStream)


    val categoriesAndWords = categoriesAndInput
      .map { el =>
        (el._1, el._2
          .map(_.replaceAll("\\.", " ")
            .replaceAll("\\,", " ")
            .replaceAll("\n", " "))
          .flatMap( message =>
            SeparatorIterator(message, " ")
              .filter(_ != "")
              .toStream
              .map(word => word.toLowerCase)
              .groupBy(word => word)
              .toStream
              .map(k => (k._1, k._2.toSeq.size))
              .sortBy(_._2)
              .reverse
         ))
      }

    categoriesAndWords.foreach { l =>
      println("-------")
      println("ONDERWERP: " + l._1)
      println("top 5 words")
      l._2.toSeq take 5 foreach println
    }

  }

}
