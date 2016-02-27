package main

import org.specs2.mutable.Specification

/**
  * Created by tomas on 27-02-16.
  */
class SeparatorIteratorTest extends Specification {

  "a SeparatorIterator with subclass Iterator" should {
    "not return hasNext true when finished" in {
      val text = "a b c"
      val p = SeparatorIterator(text, " ")

      p.hasNext must be equalTo true
      p.next must be equalTo "a"
      p.hasNext must be equalTo true
      p.next must be equalTo "b"
      p.hasNext must be equalTo true
      p.next must be equalTo "c"
      p.hasNext must be equalTo false
    }
  }

  "a SeparatorIterator with a certain separator" should {
      "separate spaces" in {
      val text = "a b c d e f g"
      val p = SeparatorIterator(text, " ")
      val sequence = p.toList

      sequence should have size 7
      sequence should contain("a")
      sequence should contain("b")
      sequence should contain("c")
      sequence should contain("d")
      sequence should contain("e")
      sequence should contain("f")
      sequence should contain("g")
    }

    "separate ;" in {
      val text = "a;b;c;d;e;f;g"
      val p = SeparatorIterator(text, ";")
      val sequence = p.toList

      sequence should have size 7
      sequence should contain("a")
      sequence should contain("b")
      sequence should contain("c")
      sequence should contain("d")
      sequence should contain("e")
      sequence should contain("f")
      sequence should contain("g")
    }

    "separate newline" in {
      val text = "a\nb\nc\nd\ne\nf\ng"
      val p = SeparatorIterator(text, "\n")
      val sequence = p.toList

      sequence should have size 7
      sequence should contain("a")
      sequence should contain("b")
      sequence should contain("c")
      sequence should contain("d")
      sequence should contain("e")
      sequence should contain("f")
      sequence should contain("g")
    }
  }

  "a SeparatorIterator with a certain edgecase placement of separator" should {
    "omit tailing ;" in {
      val text = "a;b;c;d;e;f;g;"
      val p = SeparatorIterator(text, ";")
      val sequence = p.toList

      sequence should have size 7
      sequence should contain("a")
      sequence should contain("b")
      sequence should contain("c")
      sequence should contain("d")
      sequence should contain("e")
      sequence should contain("f")
      sequence should contain("g")
    }

    "handle heading ;" in {
      val text = ";a;b;c;d;e;f;g"
      val p = SeparatorIterator(text, ";")
      val sequence = p.toList

      sequence should have size 8
      sequence should contain("")
      sequence should contain("b")
      sequence should contain("c")
      sequence should contain("d")
      sequence should contain("e")
      sequence should contain("f")
      sequence should contain("g")
    }

    "handle heading and tailing ;" in {
      val text = ";a;b;c;d;e;f;g;"
      val p = SeparatorIterator(text, ";")
      val sequence = p.toList

      sequence should have size 8
      sequence should contain("")
      sequence should contain("a")
      sequence should contain("b")
      sequence should contain("c")
      sequence should contain("d")
      sequence should contain("e")
      sequence should contain("f")
      sequence should contain("g")
    }
  }

}
