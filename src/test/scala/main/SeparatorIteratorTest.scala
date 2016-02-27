package main

import org.scalatest.{Matchers, FlatSpec}
import scala.collection.mutable.Stack

/**
  * Created by tomas on 27-02-16.
  */
class SeparatorIteratorTest extends FlatSpec with Matchers {

  "a SeparatorIterator" should "separate spaces" in {
    val text = "a b c d e f g"
    val p = SeparatorIterator(text, " ")
    val sequence = p.toList

    sequence should have length 7
    sequence should contain("a")
    sequence should contain("b")
    sequence should contain("c")
    sequence should contain("d")
    sequence should contain("e")
    sequence should contain("f")
    sequence should contain("g")
  }

  should "asdfasdf" in {
    true should equal(true)
  }

}
