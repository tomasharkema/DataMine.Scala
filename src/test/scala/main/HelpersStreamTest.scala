package main

import org.specs2.mutable.Specification

import Helpers._

/**
  * Created by tomas on 27-02-16.
  */
class HelpersStreamTest extends Specification {

  "a Stream of items" should {
    "be grouped by first character and single items" in {
      val stream = Stream("Aap", "Noot", "Mies", "Wim", "Zus", "Jet")

      val grouped = stream
        .groupByKeyAndValue(key => key.head.toString)(value => Stream(value))
        .map(el => (el._1, el._2.toList))

      grouped should have size 6

      grouped should havePair("A" -> List("Aap"))
      grouped should havePair("N" -> List("Noot"))
      grouped should havePair("M" -> List("Mies"))
      grouped should havePair("W" -> List("Wim"))
      grouped should havePair("Z" -> List("Zus"))
      grouped should havePair("J" -> List("Jet"))

      grouped should haveKeys("N", "J", "A", "M", "W", "Z")
    }

    "be grouped by first character and double items" in {
      val stream = Stream("Aap", "Noot", "Mies", "Wim", "Zus", "Jet", "Aap", "Noot", "Mies", "Wim", "Zus", "Jet")

      val grouped = stream
        .groupByKeyAndValue(key => key.head.toString)(value => Stream(value))
        .map(el => (el._1, el._2.toList))

      grouped should have size 6

      grouped should havePair("A" -> List("Aap", "Aap"))
      grouped should havePair("N" -> List("Noot", "Noot"))
      grouped should havePair("M" -> List("Mies", "Mies"))
      grouped should havePair("W" -> List("Wim", "Wim"))
      grouped should havePair("Z" -> List("Zus", "Zus"))
      grouped should havePair("J" -> List("Jet", "Jet"))

      grouped should haveKeys("N", "J", "A", "M", "W", "Z")
    }

    "be grouped by first character and triple items" in {
      val stream = Stream("Aap", "Noot", "Mies", "Wim", "Zus", "Jet", "Aap", "Noot", "Mies", "Wim", "Zus", "Jet", "Aap", "Noot", "Mies", "Wim", "Zus", "Jet")

      val grouped = stream
        .groupByKeyAndValue(key => key.head.toString)(value => Stream(value))
        .map(el => (el._1, el._2.toList))

      grouped should have size 6

      grouped should havePair("A" -> List("Aap", "Aap", "Aap"))
      grouped should havePair("N" -> List("Noot", "Noot", "Noot"))
      grouped should havePair("M" -> List("Mies", "Mies", "Mies"))
      grouped should havePair("W" -> List("Wim", "Wim", "Wim"))
      grouped should havePair("Z" -> List("Zus", "Zus", "Zus"))
      grouped should havePair("J" -> List("Jet", "Jet", "Jet"))

      grouped should haveKeys("N", "J", "A", "M", "W", "Z")
    }
  }

}
