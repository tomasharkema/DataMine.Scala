package main

import java.util.UUID

import com.datumbox.framework.common.dataobjects.{AssociativeArray, Record}
import com.datumbox.framework.core.utilities.text.extractors.{AbstractTextExtractor, NgramsExtractor}
import org.specs2.mutable.Specification

/**
  * Created by tomas on 27-02-16.
  */
class TextClassifierInvokerTest extends Specification {

  "TextClassifierInvokerTest" should {
    "match records" in {

      val ex = AbstractTextExtractor.newInstance(classOf[NgramsExtractor], new NgramsExtractor.Parameters())
      val extractedString = ex.extract("Mijn naam is Tomas Harkema")
      val casted = extractedString.asInstanceOf[java.util.Map[Object, Object]]

      val la = Array(new Record(new AssociativeArray(casted), "A"))

//      val classifier = TextClassifierInvoker.apply("GemeenteAfdelingPredictieTest" + UUID.randomUUID.toString, la, classOf[MultinomialNaiveBayes], new MultinomialNaiveBayes.TrainingParameters())

//      println(classifier)

      true should be equalTo true
    }
  }

}
