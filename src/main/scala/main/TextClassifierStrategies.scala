package main

import main.ClassifierType.ClassifierTypeString
import main.FeatureSelect.FeatureSelectTypeString
import main.TextExtractorType.TextExtractorTypeString

trait StringRepresentable {
  val s: String
}

sealed trait ClassifierType extends StringRepresentable {
  val s: ClassifierTypeString
}
case object Multinomial extends ClassifierType {
  val s = "Multinomial"
}
case object Binarized extends ClassifierType {
  val s = "Binarized"
}
case object Bernoulli extends ClassifierType {
  val s = "Bernoulli"
}

sealed trait FeatureSelectType extends StringRepresentable {
  val s: FeatureSelectTypeString
}
case object Chisquare extends FeatureSelectType {
  val s = "Chisquare"
}
case object MutualInformation extends FeatureSelectType {
  val s = "MutualInformation"
}

sealed trait TextExtractorType extends StringRepresentable {
  val s: TextExtractorTypeString
}
case object Ngrams extends TextExtractorType {
  val s = "Ngrams"
}
case object UniqueWordSequence extends TextExtractorType {
  val s = "UniqueWordSequence"
}
case object WordSequence extends TextExtractorType {
  val s = "WordSequence"
}


object ClassifierType {
  type ClassifierTypeString = String

  def fromClassifierString(string: ClassifierTypeString): Option[ClassifierType] = {
    string match {
      case "Multinomial" =>
        Some(Multinomial)

      case "Binarized" =>
        Some(Binarized)

      case "Bernoulli" =>
        Some(Bernoulli)
    }
  }
}

object FeatureSelect {
  type FeatureSelectTypeString = String
}

object TextExtractorType {
  type TextExtractorTypeString = String
}

case class StrategyClass(ct: ClassifierType, fs: FeatureSelectType, te: TextExtractorType)