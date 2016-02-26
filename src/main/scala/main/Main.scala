package main
/**
  * Created by tomas on 26-02-16.
  */
object Main {



  def main(args: Array[String]) {

    val stream = getClass.getResourceAsStream("/klachtendumpgemeente.csv")
    val lines = scala.io.Source.fromInputStream(stream).getLines

    val rows = lines.map(SeparatorIterator(_, ";")) drop 1
    val categoryAndInput = rows.map { el =>
      (el take 1, Seq(el take 1) ++ Seq(el take 1) flatten)
    }

    categoryAndInput.foreach { l =>
      l._1 foreach println
      l._2 foreach println
    }

  }

}
