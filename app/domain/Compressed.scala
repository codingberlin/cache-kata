package domain

import scala.concurrent.Future

import akka.stream.scaladsl.Source

trait Compressor {
  def compress[A](source: Source[A, _]): Future[Seq[Compressed[A]]]
  def getOption[A](compressed: Seq[Compressed[A]], index: Int): Option[A]
}

sealed trait Compressed[+A]
case class Single[A](element: A) extends Compressed[A]
case class Repeat[A](count: Int, element: A) extends Compressed[A]
