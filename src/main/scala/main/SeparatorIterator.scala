package main

/**
  * Created by tomas on 26-02-16.
  */

case class SeparatorIterator(fileContents: String, separator: String) extends Iterator[String] {

  var pos: Int = 0

  override def hasNext: Boolean = pos < (fileContents.length - 1)

  override def next(): String = {
    val startPos = pos

    def char() = fileContents.slice(pos, pos + 1)

    while (char() != separator) {
      pos += 1
    }

    pos += 1

    fileContents slice(startPos, pos - 1)
  }

}
