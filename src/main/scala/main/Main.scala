package main

import Helpers._
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.Future

import scala.concurrent.ExecutionContext.Implicits.global

object Main extends LazyLogging {

  def replaceIrregularities(string: String) = string
    .replaceAll("<br />", "\n")
    .replaceAll("<wbr />", "\n")

  def main(args: Array[String]) {

    logger.info("Load file")
    val stream =  Future { getClass.getResourceAsStream("/klachtendumpgemeente.csv") }
    val lines = stream.flatMap { stream =>
      logger.info("Load loaded. Read it.")
      Future { scala.io.Source.fromInputStream(stream).getLines }
    }
    val rows = lines.flatMap { lines =>
      logger.info("Got lines. Parsing it")
      Future { lines.map(SeparatorIterator(_, ";")) drop 1 map { _ map replaceIrregularities } }
    }
    logger.info("Parsed lines. Crunching it")

    val categoriesAndInput = rows.map { rows =>
      rows.map { el =>
        new {
          val key = el.take(1).toStream.head.toLowerCase
          val value = el // slice(1, 4)
        }
      }.toStream.groupByKeyAndValue(_.key)(_.value.toStream)
    }


    val categoriesAndWords = categoriesAndInput.map { categoriesAndInput =>
      categoriesAndInput.map { el =>
        Future {
          (el._1, el._2
            .map(_.replaceAll("\\.", " ")
              .replaceAll("\\,", " ")
              .replaceAll("\n", " "))
            .flatMap { message =>
              SeparatorIterator(message, " ")
                .filter(_ != "")
                .toStream
                .map(word => word.toLowerCase)
                .groupBy(word => word)
                .toStream
                .map(k => (k._1, k._2.toSeq.size))
                .sortBy(_._2)
                .reverse
            })
        }
      }
    }

    val loggerSync = new Object

    categoriesAndWords.map { categoriesAndWords => categoriesAndWords.foreach { l =>
      l.map { l =>
        loggerSync.synchronized {
          logger.debug("-------")
          logger.debug("ONDERWERP: " + l._1)
          logger.debug("top 5 words")
          l._2.toSeq take 5 foreach (el => logger.debug(el._1 + " keer: " + el._2))
          logger.debug("-------")
        }
      }
    }}
  }

}
