package main

import com.datumbox.applications.nlp.TextClassifier
import com.datumbox.common.dataobjects.{Dataset, AssociativeArray, Record}
import com.datumbox.common.persistentstorage.ConfigurationFactory
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration
import com.datumbox.framework.machinelearning.classification.MultinomialNaiveBayes
import com.datumbox.framework.machinelearning.common.bases.mlmodels.BaseMLmodel
import com.datumbox.framework.machinelearning.featureselection.categorical.ChisquareSelect
import com.datumbox.framework.utilities.text.extractors.{NgramsExtractor, TextExtractor}
import com.typesafe.scalalogging.LazyLogging
import main.Main._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

import Helpers._

/**
  * Created by tomas on 28-02-16.
  */
object Classifier extends LazyLogging {

  //  = classOf[MultinomialNaiveBayes]
  //  = new MultinomialNaiveBayes.TrainingParameters()

//  private def prepareDatabase[ML <: BaseMLmodel](name: String,
//                                  mlmodelClass: Class[_ <: ML],
//                                  baseMLmodel: BaseMLmodel.TrainingParameters) = {
//    val dbConf = ConfigurationFactory.MAPDB.getConfiguration
//    (TextClassifierInvoker.apply(name, mlmodelClass, baseMLmodel, dbConf), dbConf)
//  }



  def createDatabase[ML <: BaseMLmodel[_ <: com.datumbox.framework.machinelearning.common.bases.mlmodels.BaseMLmodel.ModelParameters, _ <: com.datumbox.framework.machinelearning.common.bases.mlmodels.BaseMLmodel.TrainingParameters, _ <: com.datumbox.framework.machinelearning.common.bases.mlmodels.BaseMLmodel.ValidationMetrics]]
  (name: String, mlmodelClass: Class[_ <: ML], t: BaseMLmodel.TrainingParameters, dbConf: DatabaseConfiguration) = {

    val trainingParameters: TextClassifier.TrainingParameters = new TextClassifier.TrainingParameters
    trainingParameters.setMLmodelClass(mlmodelClass)
    trainingParameters.setMLmodelTrainingParameters(t)
    trainingParameters.setDataTransformerClass(null)
    trainingParameters.setDataTransformerTrainingParameters(null)
    trainingParameters.setFeatureSelectionClass(classOf[ChisquareSelect])
    trainingParameters.setFeatureSelectionTrainingParameters(new ChisquareSelect.TrainingParameters)
    trainingParameters.setTextExtractorClass(classOf[NgramsExtractor])
    trainingParameters.setTextExtractorParameters(new NgramsExtractor.Parameters)

    (new TextClassifier(name, dbConf), trainingParameters)
  }

  private def prepareCSV(file: String)(implicit exec: ExecutionContext) = {
    logger.info("Load file " + file)

    val stream =  Future { getClass.getResourceAsStream("/" + file) }
    val lines = stream.flatMap { stream =>
      logger.info("Load loaded. Read it.")
      Future { scala.io.Source.fromInputStream(stream).getLines }
    }
    val rows = lines.flatMap { lines =>
      logger.info("Got lines. Parsing it")
      Future { lines.map(SeparatorIterator(_, ";")) drop 1 map { _ map replaceIrregularities } }
    }
    logger.info("Parsed lines. Crunching it")

    rows.map { rows =>
      rows.map { el =>
        val e = el.toStream
        new {
          val key = e.head
          val value = e.slice(1, 4)
        }
      }.toStream.groupByKeyAndValue(_.key)(_.value.toStream)
    }
  }

  private def createRecord(idx: Int, key: String, value: String)(implicit exec: ExecutionContext) = Future {
   logger.info("Create record for " + key + " no " + idx)
    val ex  = TextExtractor.newInstance(classOf[NgramsExtractor], new NgramsExtractor.Parameters())
    val extractedString = ex.extract(value)
    val casted = extractedString.asInstanceOf[java.util.Map[Object, Object]]
    new Record(new AssociativeArray(casted), key)
  }

  private def assembleRecords(data: Map[String, Stream[String]])(implicit exec: ExecutionContext): Future[Stream[Future[Record]]] = Future {
    data.zipWithIndex.map { case ((key, values), idx) => values.map(createRecord(idx, key, _)) }.toStream.flatten
  }

  private def populateDatabase(database: TextClassifier, dbConf: DatabaseConfiguration, trainingParameters: TextClassifier.TrainingParameters, records: Stream[Future[Record]])(implicit exec: ExecutionContext) = {

    val noItems = 100

//    val groups = records//.grouped(noItems)
//    val datasets = groups.zipWithIndex.map {
//      case (recordsGroup, idx) =>
//        logger.info("=========================================================")
//        logger.info("add next "+noItems+" items. N = " + idx)
//        logger.info("=========================================================")
//        val dataSet = new Dataset(dbConf)
////
//        recordsGroup foreach dataSet.add
//        dataSet
//    }

    val dataSet = new Dataset(dbConf)

    val recordsGenerated = Future.sequence(records)

    recordsGenerated map { rds =>
      rds foreach dataSet.add
      logger.info("Fitting database")
      database.fit(dataSet, trainingParameters)
      database
    }
  }

  def learn(databaseName: String, fileName: String)(implicit exec: ExecutionContext) = {
    val c = classOf[MultinomialNaiveBayes]
    val params = new MultinomialNaiveBayes.TrainingParameters()

    val dbConf = ConfigurationFactory.MAPDB.getConfiguration
    val (database, trainingParameters) = createDatabase(databaseName, c, params, dbConf)

    for {
      csvEntries <- prepareCSV(fileName)
      records <- assembleRecords(csvEntries)
      populatedDatabase <- populateDatabase(database, dbConf, trainingParameters, records)
    } yield populatedDatabase
  }

  def classify(databaseName: String,sentences: Iterator[String])(implicit exec: ExecutionContext) = Future {
    val c = classOf[MultinomialNaiveBayes]
    val params = new MultinomialNaiveBayes.TrainingParameters()

    val dbConf = ConfigurationFactory.MAPDB.getConfiguration
    val (database, _) = createDatabase(databaseName, c, params, dbConf)

    sentences.map(sentence => (c.getClass.getSimpleName, sentence, database.predict(sentence)))
  }

}
