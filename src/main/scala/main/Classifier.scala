package main

import java.io.File
import java.util.regex.Pattern

import com.datumbox.framework.applications.nlp.TextClassifier
import com.datumbox.framework.applications.nlp.TextClassifier.TrainingParameters
import com.datumbox.framework.common.Configuration
import com.datumbox.framework.common.dataobjects.{AssociativeArray, Dataframe, Record}
import com.datumbox.framework.common.persistentstorage.mapdb.MapDBConfiguration
import com.datumbox.framework.core.machinelearning.classification.{BernoulliNaiveBayes, BinarizedNaiveBayes, MultinomialNaiveBayes}
import com.datumbox.framework.core.machinelearning.featureselection.categorical.ChisquareSelect
import com.datumbox.framework.core.utilities.text.extractors.{AbstractTextExtractor, NgramsExtractor}
import com.typesafe.scalalogging.LazyLogging
import main.Helpers._
import main.Main._
import helpers.StopWords._
import scala.concurrent.{ExecutionContext, Future}

case class CsvLine(key: String, value: String)

object CsvLine {
  def fromRawInput(key: String, value: String) = {
    CsvLine(key
      .toLowerCase
      .replaceAll(" ", "")
      .trim

      , value
        .toLowerCase
        .filterStopWords
        .replaceAll(Pattern.quote("<br>"), " ")
        .replaceAll(Pattern.quote("<wbr>"), " ")
        .replaceAll(Pattern.quote("<br />"), " ")
        .replaceAll(Pattern.quote("<wbr />"), " ")
        .replaceAll(Pattern.quote("."), "")
        .replaceAll(Pattern.quote(","), "")
        .replaceAll(Pattern.quote(";"), "")
        .replaceAll(Pattern.quote(":"), "")
        .replaceAll(Pattern.quote("("), "")
        .replaceAll(Pattern.quote(")"), "")
        .replaceAll(Pattern.quote("\n"), "")
        .replaceAll(Pattern.quote("\'"), "")
        .replaceAll(Pattern.quote("\""), "")
        .replaceAll(Pattern.quote("  "), "")
        .replaceAll(Pattern.quote("   "), ""))
  }
}

case class ClassifyResult(key: String, sentence: String, record: Record) {
  def isCorrect: Boolean = key == record.getYPredicted.toString
}

object Classifier extends LazyLogging {

  def createConfiguration(name: String, strategyClass: StrategyClass) = {
    val configuration = Configuration.getConfiguration
    val mapDBConfiguration = new MapDBConfiguration()

    val folder = "./database"
    val folderRef = new File(folder)
    folderRef.mkdir()

    mapDBConfiguration.setOutputFolder(folder)
    configuration.setDbConfig(mapDBConfiguration)
    configuration.getConcurrencyConfig.setMaxNumberOfThreadsPerTask(0)
    configuration.getConcurrencyConfig.setParallelized(true)

    val trainingParameters = new TrainingParameters()
    strategyClass.ct match {
      case Multinomial =>
        trainingParameters.setModelerClass(classOf[MultinomialNaiveBayes])
        trainingParameters.setModelerTrainingParameters(new MultinomialNaiveBayes.TrainingParameters())

      case Binarized =>
        trainingParameters.setModelerClass(classOf[BinarizedNaiveBayes])
        trainingParameters.setModelerTrainingParameters(new BinarizedNaiveBayes.TrainingParameters())

      case Bernoulli =>
        trainingParameters.setModelerClass(classOf[BernoulliNaiveBayes])
        trainingParameters.setModelerTrainingParameters(new BernoulliNaiveBayes.TrainingParameters())

    }
    trainingParameters.setDataTransformerClass(null)
    trainingParameters.setDataTransformerTrainingParameters(null)
    trainingParameters.setFeatureSelectorClass(classOf[ChisquareSelect])
    trainingParameters.setFeatureSelectorTrainingParameters(new ChisquareSelect.TrainingParameters)
    trainingParameters.setTextExtractorClass(classOf[NgramsExtractor])
    trainingParameters.setTextExtractorParameters(new NgramsExtractor.Parameters)

    val dbName = name + "" + md5(trainingParameters.getModelerClass.toString)

    val classifier = new TextClassifier(dbName, configuration)

    logger.info("Created db with name: " + dbName)

    (configuration, classifier, trainingParameters)
  }

  def md5(s: String): String = {
    val m = java.security.MessageDigest.getInstance("MD5")
    val b = s.getBytes("UTF-8")
    m.update(b, 0, b.length)
    new java.math.BigInteger(1, m.digest()).toString(16)
  }

  def prepareCSV(file: String)(implicit exec: ExecutionContext): Stream[CsvLine] = {
    logger.info("Load file " + file)

    val stream =  getClass.getResourceAsStream("/" + file)
    val lines = scala.io.Source.fromInputStream(stream).getLines
    val rows = lines.map(SeparatorIterator(_, ";")) drop 1
    logger.info("Parsed lines. Crunching it")

    rows.map { el =>
      val e = el.toStream
      CsvLine.fromRawInput(e.head, e.slice(1, 4).reduceLeft { (prev, element) =>
        prev + " " + element
      })
    }.toStream
  }

  private def csvGroupByKey(stream: Stream[CsvLine]): Map[String, Stream[String]] =
    stream.groupByKeyAndValue(_.key) { el =>  Stream(el.value) }

  private def createRecord(idx: Int, key: String, value: String)(implicit exec: ExecutionContext) = Future {
    val ex  = AbstractTextExtractor.newInstance(classOf[NgramsExtractor], new NgramsExtractor.Parameters())
    val extractedString = ex.extract(value)
    val casted = extractedString.asInstanceOf[java.util.Map[Object, Object]]
    new Record(new AssociativeArray(casted), key)
  }

  private def assembleRecords(data: Map[String, Stream[String]])(implicit exec: ExecutionContext): Future[Stream[Future[Record]]] = Future {
    data.zipWithIndex.map { case ((key, values), idx) => values.map(createRecord(idx, key, _)) }.toStream.flatten
  }

  private def populateDatabase(database: TextClassifier, configuration: Configuration, trainingParameters: TrainingParameters, records: Stream[Future[Record]])
                              (implicit exec: ExecutionContext) = {
    val dataFrame = new Dataframe(configuration)

    val recordsGenerated = Future.sequence(records)

    recordsGenerated map { rds =>
      rds foreach dataFrame.add
      logger.info("Fitting database")
      database.fit(dataFrame, trainingParameters)

      val vm = database.validate(dataFrame)
      database.setValidationMetrics(vm)

      database
    }
  }

  def learn(databaseName: String, csvEntries: Stream[CsvLine], strategyClass: StrategyClass)(implicit exec: ExecutionContext) = {
    val (conf, classifier, trainingParameters) = createConfiguration(databaseName, strategyClass)
    for {
      grouped <- Future { csvGroupByKey(csvEntries) }
      records <- assembleRecords(grouped)
      populatedDatabase <- populateDatabase(classifier, conf, trainingParameters, records)
    } yield populatedDatabase
  }

  var _dbForThreadMap = Map[String, TextClassifier]()
  private def dbForThread(dbName: String, strategyClass: StrategyClass): TextClassifier = {
    val threadName = Thread.currentThread().getName
    _dbForThreadMap.get(threadName) match {
      case Some(db) =>
        db

      case None =>
        _dbForThreadMap.synchronized {
          val (_, classifier, _) = createConfiguration(dbName, strategyClass)
          _dbForThreadMap = _dbForThreadMap updated(threadName, classifier)
          classifier
        }
    }
  }


  def classify(dbName: String, lines: Stream[CsvLine], strategyClass: StrategyClass)(implicit exec: ExecutionContext): Future[Stream[ClassifyResult]] = {
    val futures = lines.map { csvLine =>
      Future {
        ClassifyResult(csvLine.key, csvLine.value, dbForThread(dbName, strategyClass).predict(csvLine.value))
      }
    }

    Future.sequence(futures)
  }

}
