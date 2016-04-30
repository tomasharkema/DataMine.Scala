package main

import java.util.Date
import java.util.concurrent.atomic.AtomicBoolean

import com.datumbox.framework.core.machinelearning.featureselection.categorical.{ChisquareSelect, MutualInformation}
import com.datumbox.framework.core.utilities.text.extractors.{NgramsExtractor, UniqueWordSequenceExtractor, WordSequenceExtractor}
import com.typesafe.scalalogging.LazyLogging
import main.ClassifierType.ClassifierTypeString
import main.FeatureSelect.FeatureSelectTypeString
import main.TextExtractorType.TextExtractorTypeString
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

    val classifiers: Seq[ClassifierType] = Seq(Multinomial, Binarized, Bernoulli)

    val featureSelector: Seq[FeatureSelectType] = Seq(Chisquare, MutualInformation)

    val textExtractors: Seq[TextExtractorType] = Seq(Ngrams, UniqueWordSequence, WordSequence)

    val types = classifiers.flatMap { c =>
      featureSelector.flatMap { f =>
        textExtractors.map { te =>
          StrategyClass(c, f, te)
        }
      }
    }

    if (learn) {
      logger.info("Learn")
      val results = types.map { strategy =>
        val entries = Classifier.prepareCSV(file).takeFirstHalf
        Classifier.learn(databaseName, entries, strategy)
      }
      val learnResults = Await.result(Future.sequence(results), Duration.Inf)
      logger.info(learnResults.toString)
    } else {
      logger.info("Classify")

      val results = types.map { strategy =>
        val entries = Classifier.prepareCSV(file).takeLastHalf
        Classifier.classify(databaseName, entries, strategy).map {
          (_, strategy)
        }
      }

      val classifyResults = Await.result(Future.sequence(results), Duration.Inf)

      classifyResults.foreach { element =>
        val (classifyResults, strategy) = element
        val n = classifyResults.length
        val correctItems = classifyResults.groupBy(_.isCorrect)
        val correct = correctItems.get(true).get.length
        val percentage = (correct.toFloat / n.toFloat) * 100
        logger.info("STRATEGY: " + strategy)
        logger.info("SCORE: n: " + n + " correct: " + correct)
        logger.info("Percentage: " + percentage + "%")
      }
    }

    isLearning.set(false)

    logger.info("Done")
  }

}
