package main.helpers

object StreamHelpers {
  implicit class StreamHelpers[T](stream: Stream[T]) {
    implicit def takeFirstHalf: Stream[T] =
      stream.take(Math.floor(stream.length.toFloat / 2).toInt)
    implicit def takeLastHalf: Stream[T] =
      stream.takeRight(Math.ceil(stream.length.toFloat / 2).toInt)
  }
}