package actors

import actors.CacheHolderActor._
import akka.actor._
import akka.testkit.{ ImplicitSender, TestKit }
import akka.util.Timeout
import domain.Single
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.{ After, AfterAll }
import scala.concurrent.duration._
import scala.concurrent.{ Await, blocking }
import akka.pattern.ask

class CacheHolderActorSpec extends Specification with Mockito with AfterAll {

  implicit val actorSystem = ActorSystem()

  override def afterAll(): Unit = {
    actorSystem.terminate()
  }

  "cache holder actor" should {

    "return empty cache when no items were cached yet" in new WithCacheHolderActor {
      val item = Await.result(cacheHolderActorRef ? RetrieveCache, 5.seconds)

      item must be equalTo CachedItems(Seq())
    }

    "return cache when cache was updated" in new WithCacheHolderActor {
      cacheHolderActorRef ! UpdateCache(Seq(Single("A")))

      val item = Await.result(cacheHolderActorRef ? RetrieveCache, 5.seconds)

      item must be equalTo CachedItems(Seq(Single("A")))
    }

    "return cache status when cache is initialized already" in new WithCacheHolderActor {
      cacheHolderActorRef ! UpdateCache(Seq(Single("A")))

      Await.result(cacheHolderActorRef ? CacheStatus, 1.seconds) must be equalTo CacheInitialized
    }

    "return cache status when cache is initialized later" in new WithCacheHolderActor {
      val cacheStatusFuture = cacheHolderActorRef ? CacheStatus

      cacheHolderActorRef ! UpdateCache(Seq(Single("A")))

      Await.result(cacheStatusFuture, 1.seconds) must be equalTo CacheInitialized
    }

  }

  abstract class WithCacheHolderActor extends TestKit(ActorSystem()) with After with ImplicitSender {
    import com.softwaremill.macwire._
    implicit val executionContext = actorSystem.dispatcher
    implicit val timeout = Timeout(5.seconds)

    def after =
      Await.ready(system.terminate(), 5.seconds)

    val cacheHolderActorRef = system.actorOf(Props(wire[CacheHolderActor]))
  }

}

