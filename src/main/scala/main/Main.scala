package main

import java.util
import java.util.concurrent.Executors
import java.util.concurrent.atomic.{AtomicBoolean, AtomicInteger}
import java.util.{Date, UUID}

import Helpers._
import com.datumbox.framework.core.machinelearning.classification.{MultinomialNaiveBayes, BinarizedNaiveBayes, BernoulliNaiveBayes}
import com.datumbox.framework.core.machinelearning.featureselection.categorical.{MutualInformation, ChisquareSelect}
import com.datumbox.framework.core.utilities.text.extractors.{WordSequenceExtractor, UniqueWordSequenceExtractor, NgramsExtractor}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.duration.Duration
import scala.concurrent.{Promise, ExecutionContext, Await, Future}

import scala.util.{Failure, Success}
import helpers.StreamHelpers._

object Main extends LazyLogging {
  import scala.concurrent.ExecutionContext.Implicits.global
//  implicit val exec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(Runtime.getRuntime.availableProcessors))

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

    val classifiers = Iterator(
      (classOf[MultinomialNaiveBayes], new MultinomialNaiveBayes.TrainingParameters()),
      (classOf[BinarizedNaiveBayes], new BinarizedNaiveBayes.TrainingParameters()),
      (classOf[BernoulliNaiveBayes], new BernoulliNaiveBayes.TrainingParameters()))

    val featureSelector = Iterator(
      (classOf[ChisquareSelect], new ChisquareSelect.TrainingParameters),
      (classOf[MutualInformation], new MutualInformation.TrainingParameters)
    )

    val textExtractors = Iterator(
      (classOf[NgramsExtractor], new NgramsExtractor.Parameters),
      (classOf[UniqueWordSequenceExtractor], new UniqueWordSequenceExtractor.Parameters),
      (classOf[WordSequenceExtractor], new WordSequenceExtractor.Parameters)
    )

    if (learn) {
      logger.info("Learn")

      val learnResFuture = Classifier.prepareCSV(file)
        .map { _.takeFirstHalf }
        .flatMap { entries => Classifier.learn(databaseName, entries) }
      val learnResults = Await.result(learnResFuture, Duration.Inf)
      logger.info(learnResults.toString)
    } else {
      logger.info("Classify")
      val classifierResults =
        Classifier.prepareCSV(file)
          .map { _.takeLastHalf }
          .flatMap { entries => Classifier.classify(databaseName, entries) }

      val classifyResults: Stream[ClassifyResult] = Await.result(classifierResults, Duration.Inf)
      classifyResults foreach println
    }

    isLearning.set(false)

    logger.info("Done")

    System.exit(0)
  }

}
