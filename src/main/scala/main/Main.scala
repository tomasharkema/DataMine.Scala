package main

import java.util
import java.util.{Date, UUID}

import Helpers._
import com.datumbox.applications.nlp.TextClassifier
import com.datumbox.common.dataobjects.{AssociativeArray, Record, Dataset}
import com.datumbox.common.persistentstorage.ConfigurationFactory
import com.datumbox.framework.machinelearning.classification.{BernoulliNaiveBayes, MultinomialNaiveBayes, BinarizedNaiveBayes}
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
    "Mijn belasting aangifte lukt niet!"
  )

  def main(args: Array[String]) {

    println(args.toSeq)

    val test = args.contains("test")
    val learn = args.contains("learn")

    var isLearning = true
    val isLearningObj = new Object

    val databaseName = (if (test) "TEST_" else "") + "GemeenteClassify"
    val file = if (test) "small.csv" else "klachtendumpgemeente.csv"

    Future {
      while (isLearningObj.synchronized { isLearning }) {
        logger.debug("Is still doing shit " + new Date().toString + " ")
        Thread.sleep(60000)
      }
    }

    if (learn) {
      logger.info("Learn")
      val learnResults = Await.result(Classifier.learn(databaseName, file), Duration.Inf)
      logger.info(learnResults.toString)
    } else {
      logger.info("Classify")
      val classifyResults = Await.result(Classifier.classify(databaseName, checkSentences), Duration.Inf)
      classifyResults foreach { perd =>
        val (c, zin, rec) = perd
        logger.info(zin + " from: " + c)
        logger.info("hoort bij: " + rec.getYPredicted.toString)
        logger.info(rec.getYPredictedProbabilities.toString)
      }
    }

    isLearningObj.synchronized { isLearning = false }

    logger.info("Done")
  }

}
