package main

import java.util.{Date, UUID}

import Helpers._
import com.datumbox.applications.nlp.TextClassifier
import com.datumbox.common.dataobjects.{AssociativeArray, Record, Dataset}
import com.datumbox.common.persistentstorage.ConfigurationFactory
import com.datumbox.framework.machinelearning.classification.{MultinomialNaiveBayes, BinarizedNaiveBayes}
import com.datumbox.framework.machinelearning.featureselection.categorical.ChisquareSelect
import com.datumbox.framework.utilities.text.extractors.{TextExtractor, NgramsExtractor}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

import scala.concurrent.ExecutionContext.Implicits.global

object Main extends LazyLogging {

  def replaceIrregularities(string: String) = string
    .replaceAll("<br />", "\n")
    .replaceAll("<wbr />", "\n")

  val checkSentences = Iterator(
    "ik heb een klacht",
    "wtf gebeurt hier",
    "ik wil naar huis",
    "In de brief stond dat hij kon parkeren in de Museumpleingarage!",
    "Ik kon door de marathon mijn huis niet berreiken",
    "Er is nooit plek bij de fietsenrekken",
    "Mijn belastingaangifte lukt niet!"
  )

  def main(args: Array[String]) {

    println(args.toSeq)

    val isTest = args.contains("test")

    val file =
//      if (true) {
        if(isTest) {
        "/small.csv"
      } else {
      "/klachtendumpgemeente.csv"
      }

    logger.info("Load file " + file)

    val stream =  Future { getClass.getResourceAsStream(file) }
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
        val e = el.toStream
        new {
          val key = e.head
          val value = e.slice(1, 4)
        }
      }.toStream.groupByKeyAndValue(_.key)(_.value.toStream)
    }

//    val categoriesAndCountedWords = categoriesAndInput.map { categoriesAndInput =>
//      categoriesAndInput.map { el =>
//        Future {
//          (el._1, el._2
//            .map(_.replaceAll("\\.", " ")
//              .replaceAll("\\,", " ")
//              .replaceAll("\n", " "))
//            .flatMap(WordCount.countWords))
//        }
//      }
//    }

    val loggerSync = new Object

//    categoriesAndCountedWords.map { categoriesAndWords => categoriesAndWords.foreach { l =>
//      l.map { l =>
//        loggerSync.synchronized {
//          logger.debug("-------")
//          logger.debug("ONDERWERP: " + l._1)
//          logger.debug("top 5 words")
//          l._2.toSeq take 5 foreach (el => logger.debug(el._1 + " keer: " + el._2))
//          logger.debug("-------")
//        }
//      }
//    }}

    val trainedSets = categoriesAndInput.flatMap { cat =>
      logger.info("Training TextClassifier")

      val records = cat.map { el =>
        val (key, value) = el
        value.filter(_ != "")
          .map(_.replaceAll("\\.", "")
            .replaceAll("\\,", "")
            .replaceAll("\n", " "))
          .map { string =>
            val ex  = TextExtractor.newInstance(classOf[NgramsExtractor], new NgramsExtractor.Parameters())
            val extractedString = ex.extract(string)
            val casted = extractedString.asInstanceOf[java.util.Map[Object, Object]]
            new Record(new AssociativeArray(casted), key)
          }
      }.toStream.flatten

      Future(records)
    }

    val records = Await.result(trainedSets, Duration.Inf)

    var isLearning = true
    val isLearningObj = new Object

    Future {
      while (isLearningObj.synchronized { isLearning }) {
        logger.debug("Is still learning " + new Date().toString)
        Thread.sleep(30000)
      }
    }

    val classifier = TextClassifierInvoker.apply("GemeenteAfdelingPredictie" + UUID.randomUUID.toString, records.toArray)

    isLearning = false

    logger.info("Trained TextClassifier")

    val predictions = Future.sequence(checkSentences.map { zin =>
      Future {
        val record = classifier.synchronized(classifier.predict(zin))
        (zin, record)
      }
    })

//    Await.result(categoriesAndCountedWords.map(Future.sequence(_)), Duration.Inf)
    val res = Await.result(predictions, Duration.Inf)

    res foreach { perd =>
      val (zin, rec) = perd
      logger.info(zin)
      logger.info("hoort bij: " + rec.getYPredicted.toString)
      logger.info(rec.getYPredictedProbabilities.toString)
    }


    logger.info("Done")
  }

}
