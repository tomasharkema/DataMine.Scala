package main

import java.util.Date
import java.util.concurrent.atomic.AtomicBoolean

import com.datumbox.framework.core.machinelearning.classification.{BernoulliNaiveBayes, BinarizedNaiveBayes, MultinomialNaiveBayes}
import com.datumbox.framework.core.machinelearning.featureselection.categorical.{ChisquareSelect, MutualInformation}
import com.datumbox.framework.core.utilities.text.extractors.{NgramsExtractor, UniqueWordSequenceExtractor, WordSequenceExtractor}
import com.typesafe.scalalogging.LazyLogging
import main.helpers.StreamHelpers._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object Main extends LazyLogging {

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

      val entries = Classifier.prepareCSV(file).takeFirstHalf
      val classifier = Classifier.learn(databaseName, entries)
      val learnResults = Await.result(classifier, Duration.Inf)
      logger.info(learnResults.toString)
    } else {
      logger.info("Classify")
      val entries = Classifier.prepareCSV(file).takeLastHalf
      val classifierResults = Classifier.classify(databaseName, entries)
      val classifyResults: Stream[ClassifyResult] = Await.result(classifierResults, Duration.Inf)
      val correctItems = classifyResults.groupBy(_.isCorrect)

      val n = classifyResults.length
      val correct = correctItems.get(true).get.length
      val percentage = (correct.toFloat / n.toFloat) * 100

      logger.info("SCORE: n: " + n + " correct: " + correct)
      logger.info("Percentage: " + percentage + "%")
    }

    isLearning.set(false)

    logger.info("Done")
  }

}
