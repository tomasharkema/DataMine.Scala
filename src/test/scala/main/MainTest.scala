package main

import org.specs2.mutable.Specification

class MainTest extends Specification {

  "main class" should {
    "run" in {
      Main.main(Array("test"))
      true should be equalTo true
    }
  }
}
