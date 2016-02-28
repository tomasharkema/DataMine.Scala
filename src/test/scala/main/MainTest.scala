package main

import org.specs2.mutable.Specification

class MainTest extends Specification {

  "main class" should {
    "run learn" in {
      Main.main(Array("test", "learn"))
      Main.main(Array("test", "classify"))
      true should be equalTo true
    }
  }
}
