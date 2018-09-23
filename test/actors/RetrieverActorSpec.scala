package actors

import actors.CacheHolderActor.UpdateCache
import actors.RetrieverActor.RetrieveItems
import akka.NotUsed
import akka.actor._
import akka.stream.scaladsl.Source
import domain.Single
import org.specs2.mutable.Specification
import akka.testkit.{ ImplicitSender, TestKit }
import org.specs2.specification.{ After, AfterAll }
import org.specs2.mock.Mockito
import services.{ CompressorService, ItemSourceService }
import scala.collection.immutable
import scala.concurrent.{ Future, Await, blocking }
import scala.concurrent.duration._

class RetrieverActorSpec extends Specification with Mockito with AfterAll {

  implicit val actorSystem = ActorSystem()

  override def afterAll(): Unit = {
    actorSystem.terminate()
  }

  "retriever actor" should {

    "call cache holder actor" in new WithRetrieverActor {
      retrieverActorRef ! RetrieveItems

      Await.ready(Future { while (CacheHolderActorMock.lastReceivedMessage.isEmpty) { Thread.sleep(100) } }, 5.seconds)

      CacheHolderActorMock.lastReceivedMessage must beSome(UpdateCache(Seq(Single("A"))))
    }

  }

  abstract class WithRetrieverActor extends TestKit(ActorSystem()) with After with ImplicitSender {
    import com.softwaremill.macwire._

    def after =
      Await.ready(system.terminate(), 5.seconds)

    implicit lazy val executionContext = actorSystem.dispatcher
    val itemSourceService: ItemSourceService = smartMock[ItemSourceService]
    val compressorService: CompressorService = smartMock[CompressorService]
    val itemSource: Source[String, NotUsed] = Source[String](Seq("A").to[immutable.Seq])
    itemSourceService.retrieveItems returns Future.successful(itemSource)
    compressorService.compress[String](itemSource) returns Future.successful(Seq(Single("A")))

    lazy val cacheHolderActorRef = CacheHolderActorRef(system.actorOf(Props(wire[CacheHolderActorMock])))
    lazy val retrieverActorRef = system.actorOf(Props(wire[RetrieverActor]))
  }

  class CacheHolderActorMock extends Actor {

    override def receive: Actor.Receive = {
      case UpdateCache(items) =>
        CacheHolderActorMock.lastReceivedMessage = Some(UpdateCache(items))
    }
  }

  object CacheHolderActorMock {
    var lastReceivedMessage: Option[UpdateCache] = None
  }

}

