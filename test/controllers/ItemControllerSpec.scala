package controllers

import actors.CacheHolderActor.{ CachedItems, RetrieveCache }
import actors.CacheHolderActorRef
import akka.actor.{ Actor, Props }
import domain.Repeat
import launch.ItemCacheComponents
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import play.api._
import play.api.test.Helpers._
import play.api.test._
import router.Routes

class ItemControllerSpec extends Specification {

  "item controller" should {
    "respond with Not Found when cache actor returns None" in new WithItemController {
      val result = route(testModule.application, FakeRequest(Helpers.GET, controllers.routes.ItemController.retrieveItem(42).url)).get

      status(result) must be equalTo NOT_FOUND
    }

    "respond with item when cache exists" in new WithItemController {
      val result = route(testModule.application, FakeRequest(Helpers.GET, controllers.routes.ItemController.retrieveItem(1).url)).get

      status(result) must be equalTo OK
      contentAsString(result) must be equalTo "C"
    }
  }

  trait WithItemController extends Scope {

    val context = ApplicationLoader.createContext(new Environment(new java.io.File("."), ApplicationLoader.getClass.getClassLoader, Mode.Test))

    val testModule = new BuiltInComponentsFromContext(context) with ItemCacheComponents {

      override lazy val cacheHolderActorRef: CacheHolderActorRef = CacheHolderActorRef(actorSystem.actorOf(Props(new Actor {
        override def receive: Receive = {
          case RetrieveCache =>
            sender ! CachedItems(Seq(Repeat(5, "C")))
        }
      })))

    }
  }

}
