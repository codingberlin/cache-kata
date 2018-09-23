package services

import akka.stream.Materializer
import akka.stream.scaladsl.Source
import domain.{ Repeat, Single, Compressed, Compressor }

import scala.annotation.tailrec
import scala.concurrent.Future

class CompressorService(implicit val materializer: Materializer) extends Compressor {
  override def compress[A](source: Source[A, _]): Future[Seq[Compressed[A]]] =
    source.runFold(Seq.empty[Compressed[A]]) {
      case (Seq(), item) =>
        Seq(Single(item))
      case (compressed, item) if compressed.last == Single(item) =>
        compressed.dropRight(1) :+ Repeat(2, item)
      case (compressed, item) if compressed.last.isInstanceOf[Repeat[A]] && compressed.last.asInstanceOf[Repeat[A]].element == item =>
        val updatedRepead = Repeat(compressed.last.asInstanceOf[Repeat[A]].count + 1, item)
        compressed.dropRight(1) :+ updatedRepead
      case (compressed, item) =>
        compressed :+ Single(item)
    }

  @tailrec
  override final def getOption[A](compressed: Seq[Compressed[A]], index: Int): Option[A] =
    compressed match {
      case Seq() =>
        None
      case Single(item) :: tail if index == 0 =>
        Some(item)
      case Single(_) :: tail =>
        getOption(tail, index - 1)
      case Repeat(count, item) :: tail if index < count =>
        Some(item)
      case Repeat(count, _) :: tail =>
        getOption(tail, index - count)
    }
}
