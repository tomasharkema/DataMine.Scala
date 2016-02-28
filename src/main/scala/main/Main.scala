package main

import java.util
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
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

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object Main extends LazyLogging {

  def replaceIrregularities(string: String) = string
    .replaceAll("<br />", "\n")
    .replaceAll("<wbr />", "\n")

  val checkSentences = Stream.fill(2)(Stream(
    "ik heb een klacht",
    "wtf gebeurt hier",
    "ik wil naar huis",
    "In de brief stond dat hij kon parkeren in de Museumpleingarage!",
    "Ik kon door de marathon mijn huis niet berreiken",
    "Er is nooit plek bij de fietsenrekken",
    "Mijn belasting aangifte lukt niet!",
    "Ik ben verhuisd begin November",
    "Bij het ontvangen van de acceptgiro, schrok ik erg van het bedrag",
    "Parkeerautomaat 11022 heeft sinds herindeling Willemsstraat geen electriciteit.", "beschikking 131114-1341-71074<br />Feitnummer R402C<br /><br /><br />Op 13-11-2014 werd mij een aankondiging van beschikking uitgereikt omdat ik geparkeerd zou hebben op een invalide parkeerplaats in de Gustav Mahlerlaan th 405 t voor het pand met het opschrift Pro Logis<br />Volgens mij was dat geen invalide parkeer plaats want:<br />Er was geen duidelijke markering van het vak waar ik stond en het bord voor de invalide parkeer plaats stond achter mijn auto met de tekst gericht naar het vak achter mijn auto (zie foto 1 ik stond op de plaats waar de auto op de foto ook staat). Dat vak achter mijn auto was wel duidelijk gemarkeerd met een kruis in het vak. (zie foto's 1. 2 en 3  voor de situatie ter plekke. ( Er waren 4 vakken met kruizen 1 voor de invalide parkeerplaats en 3 daarachter voor 3 taxi's zoals het bord in het midden van deze 3 aangeeft zie de foto's 2 en 3)<br />Ik heb de verbalisant op deze situatie gewezen,zijn antwoord was het staat al in de computer en daar kan ik niets aan veranderen"
  )).flatten

  def main(args: Array[String]) {

    println(args.toSeq)

    val test = args.contains("test")
    val learn = args.contains("learn")

    var isLearning = true
    val isLearningObj = new Object

    val databaseName = (if (test) "TEST_" else "") + "GemeenteClassify"
    val file = if (test) "small.csv" else "klachtendumpgemeente.csv"

    Future {
      Thread.currentThread().setName("Activity Logger Thread")
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

      val classifierResults = Classifier.classify(databaseName, checkSentences) map { perds =>
        perds map { perd =>
          val (c, zin, rec) = perd
          logger.info(zin + " from: " + c)
          logger.info("hoort bij: " + rec.getYPredicted.toString)
          logger.info(rec.getYPredictedProbabilities.toString)
          perd
        }
      }

      val classifyResults = Await.result(Future.sequence(classifierResults), Duration.Inf)
      classifyResults foreach println
    }

    isLearningObj.synchronized { isLearning = false }

    logger.info("Done")
  }

}
