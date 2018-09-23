package controllers

import launch.ItemCacheComponents
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import play.api.{ BuiltInComponentsFromContext, Mode, Environment, ApplicationLoader }

import play.api.test._
import play.api.test.Helpers._

class StatusControllerSpec extends Specification {

  "status controller" should {
    "respond with Ok" in new WithItemControllerSpec {
      val result = route(testModule.application, FakeRequest(Helpers.GET, controllers.routes.StatusController.status.url)).get

      status(result) must be equalTo OK
      contentAsString(result) must be equalTo "Ok"
    }
  }

  trait WithItemControllerSpec extends Scope {

    val context = ApplicationLoader.createContext(new Environment(new java.io.File("."), ApplicationLoader.getClass.getClassLoader, Mode.Test))

    val testModule = new BuiltInComponentsFromContext(context) with ItemCacheComponents
  }
}
