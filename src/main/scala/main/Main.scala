package main

import java.util
import java.util.concurrent.Executors
import java.util.concurrent.atomic.{AtomicBoolean, AtomicInteger}
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
import scala.concurrent.{Promise, ExecutionContext, Await, Future}

import scala.util.{Failure, Success}
import helpers.StreamHelpers._

object Main extends LazyLogging {

  implicit val exec = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

  def replaceIrregularities(string: String) = string
    .replaceAll("<br />", "\n")
    .replaceAll("<wbr />", "\n")

  def main(args: Array[String]) {

    println(args.toSeq)

    val test = args.contains("test")
    val learn = args.contains("learn")

    val isLearning = new AtomicBoolean(true)

    val databaseName = (if (test) "TEST_" else "") + "GemeenteClassify"
    val file = if (test) "small.csv" else "klachtendumpgemeente.csv"

    Future {
      Thread.currentThread().setName("Activity Logger Thread")
      while (isLearning.get()) {
        logger.debug("Is still doing shit " + new Date().toString + " ")
        Thread.sleep(60000)
      }
    }

    if (learn) {
      logger.info("Learn")

      val learnResFuture = Classifier.prepareCSV(file)
        .map { _.takeFirstHalf }
        .flatMap { Classifier.learn(databaseName, _) }
      val learnResults = Await.result(learnResFuture, Duration.Inf)
      logger.info(learnResults.toString)
    } else {
      logger.info("Classify")
      val classifierResults =
        Classifier.prepareCSV(file)
          .map { _.takeLastHalf }
          .map { Classifier.classify(databaseName, _) }
          .flatMap { Future.sequence(_) }

      val classifyResults = Await.result(classifierResults, Duration.Inf)
      classifyResults foreach println
    }

    isLearning.set(false)

    logger.info("Done")
  }

}
