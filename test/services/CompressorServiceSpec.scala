package services

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import domain.{ Repeat, Single, Compressed }
import org.specs2.matcher.DataTables
import org.specs2.mutable.Specification
import com.softwaremill.macwire._
import scala.concurrent.Await
import scala.concurrent.duration._

class CompressorServiceSpec extends Specification with DataTables {

  val actorSystem = ActorSystem("test-system")
  implicit val materializer = ActorMaterializer()(actorSystem)

  "a compressor service" should {
    "compress" in {
    // format: OFF
    "uncompressed"                 | "compressed"                        |
      Seq()                        ! Seq()                               |
      Seq("A")                     ! Seq(Single("A"))                    |
      Seq("A", "B")                ! Seq(Single("A"), Single("B"))       |
      Seq("A", "B", "B")           ! Seq(Single("A"), Repeat(2, "B"))    |
      Seq("A", "B", "B", "B")      ! Seq(Single("A"), Repeat(3, "B"))    |
      Seq("A", "B", "B", "B", "B") ! Seq(Single("A"), Repeat(4, "B"))    |
      Seq("A", "A")                ! Seq(Repeat(2, "A"))                 |
      Seq("A", "A", "B")           ! Seq(Repeat(2, "A"), Single("B"))    |
      Seq("A", "A", "B", "B")      ! Seq(Repeat(2, "A"), Repeat(2, "B")) |
      Seq("A", "A", "B", "B", "B") ! Seq(Repeat(2, "A"), Repeat(3, "B")) |>
        // format: ON
        { (uncompressed: Seq[String], compressed: Seq[Compressed[String]]) ⇒

          val compressorService = wire[CompressorService]
          val source = Source[String](uncompressed.to[scala.collection.immutable.Seq])

          Await.result(compressorService.compress[String](source), 5.seconds) must be equalTo compressed
        }
    }

    "return indexed item from compressed" in {
      // format: OFF
 "compressed"                                                   | "index" | "item"    |
  Seq()                                                         ! 0       ! None      |
  Seq(Single("A"), Repeat(2, "B"), Single("C"), Repeat(5, "D")) ! 0       ! Some("A") |
  Seq(Single("A"), Repeat(2, "B"), Single("C"), Repeat(5, "D")) ! 1       ! Some("B") |
  Seq(Single("A"), Repeat(2, "B"), Single("C"), Repeat(5, "D")) ! 2       ! Some("B") |
  Seq(Single("A"), Repeat(2, "B"), Single("C"), Repeat(5, "D")) ! 3       ! Some("C") |
  Seq(Single("A"), Repeat(2, "B"), Single("C"), Repeat(5, "D")) ! 4       ! Some("D") |
  Seq(Single("A"), Repeat(2, "B"), Single("C"), Repeat(5, "D")) ! 5       ! Some("D") |
  Seq(Single("A"), Repeat(2, "B"), Single("C"), Repeat(5, "D")) ! 6       ! Some("D") |
  Seq(Single("A"), Repeat(2, "B"), Single("C"), Repeat(5, "D")) ! 7       ! Some("D") |
  Seq(Single("A"), Repeat(2, "B"), Single("C"), Repeat(5, "D")) ! 8       ! Some("D") |
  Seq(Single("A"), Repeat(2, "B"), Single("C"), Repeat(5, "D")) ! 9       ! None      |
  Seq(Repeat(2, "A"), Single("B"))                              ! 0       ! Some("A") |
  Seq(Repeat(2, "A"), Single("B"))                              ! 1       ! Some("A") |
  Seq(Repeat(2, "A"), Single("B"))                              ! 2       ! Some("B") |>
        // format: ON
        { (compressed: Seq[Compressed[String]], index: Int, expectedItem) =>

          val compressorService = wire[CompressorService]

          compressorService.getOption[String](compressed, index) must be equalTo expectedItem
        }
    }

  }

}
