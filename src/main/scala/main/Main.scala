package main

import java.util.UUID

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


    val recordSync = new Object

    val trainedSets = categoriesAndInput.flatMap { cat =>
      logger.info("Training TextClassifier")

      val dbConf = ConfigurationFactory.INMEMORY.getConfiguration
      //Setup Training Parameters
      //-------------------------
      val trainingParameters = new TextClassifier.TrainingParameters()

      //Classifier configuration
      trainingParameters.setMLmodelClass(classOf[MultinomialNaiveBayes])
      trainingParameters.setMLmodelTrainingParameters(new MultinomialNaiveBayes.TrainingParameters())

      //Set data transfomation configuration
      trainingParameters.setDataTransformerClass(null)
      trainingParameters.setDataTransformerTrainingParameters(null)

      //Set feature selection configuration
      trainingParameters.setFeatureSelectionClass(classOf[ChisquareSelect])
      trainingParameters.setFeatureSelectionTrainingParameters(new ChisquareSelect.TrainingParameters())

      //Set text extraction configuration
      trainingParameters.setTextExtractorClass(classOf[NgramsExtractor])
      trainingParameters.setTextExtractorParameters(new NgramsExtractor.Parameters())


      //Fit the classifier
      //------------------

      val classifier = new TextClassifier("GemeenteAfdelingPredictie" + UUID.randomUUID.toString, dbConf)

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

      val d = new Dataset(dbConf)

      records foreach { el =>
        d.add(el)
      }

      logger.info("Fitting TextClassifier")

      classifier.fit(d, trainingParameters)

      logger.info("Trained TextClassifier")
      Future(classifier)
    }

    val predictions = trainedSets.flatMap { classifier =>
      Future.sequence(Iterator(
        "ik heb een klacht",
        "wtf gebeurt hier",
        "ik wil naar huis",
        "In de brief stond dat hij kon parkeren in de Museumpleingarage!",
        "Ik kon door de marathon mijn huis niet berreiken",
        "Er is nooit plek bij de fietsenrekken",
        "Mijn belastingaangifte lukt niet!"
      ).map { zin =>
        Future {
          val record = classifier.predict(zin)
          (zin, record)
        }
      })
    }

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
