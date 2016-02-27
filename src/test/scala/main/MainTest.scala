package main

import org.specs2.mutable.Specification

/**
  * Created by tomas on 27-02-16.
  */
class MainTest extends Specification {

  "main class" should {
    "run" in {
      Main.main(Array())
      true should be equalTo true
    }
  }
}
