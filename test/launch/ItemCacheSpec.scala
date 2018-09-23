package launch

import java.net.URL
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

import com.github.tomakehurst.wiremock.WireMockServer
import org.specs2.mutable.Specification
import org.specs2.specification.{ BeforeAfterAll, Scope }
import play.api._
import play.api.test.Helpers._
import play.api.test._
import com.github.tomakehurst.wiremock.client.WireMock._
import scala.concurrent.duration._
import scala.concurrent.{ Await, Future }

class ItemCacheSpec extends Specification with BeforeAfterAll {

  val mockedEndpoint = new WireMockServer()

  def beforeAll = {
    mockedEndpoint.start()
    stubItemSourceEndpointWith("A\nB\nC\nD\nE")
  }

  def afterAll = {
    mockedEndpoint.stop()
  }

  "item cache" should {
    "should return the requested cached item from item source after application startup and should cache and return the updated item source " +
      "endpoint later" in new WithItemCache {
        implicit val executionContext = testModule.executionContext

        requestItem(1) must be equalTo "B"

        stubItemSourceEndpointWith("W\nW\nW")

        Await.ready(Future { while (requestItem(1) == "B") { Thread.sleep(100) } }, 5.seconds)

        requestItem(1) must be equalTo "W"
      }

  }

  def stubItemSourceEndpointWith(result: String): Unit = {
    stubFor(get(urlEqualTo("/items-source"))
      .willReturn(aResponse()
        .withHeader("Content-Type", "text/plain")
        .withBody(result)))
  }

  trait WithItemCache extends Scope {

    val context = ApplicationLoader.createContext(new Environment(new java.io.File("."), ApplicationLoader.getClass.getClassLoader, Mode.Test))
    val testModule = new BuiltInComponentsFromContext(context) with ItemCacheComponents {

      // configure mocked endpoint as item source url
      override def itemSource: URL = new URL("http://localhost:8080/items-source")

      //reduce update interval to have a short running test
      override lazy val retrieverActorSchedule = FiniteDuration(100, TimeUnit.MILLISECONDS)
    }
    testModule.startActors()

    def requestItem(index: Int): String = {
      val secondResult = route(testModule.application, FakeRequest(Helpers.GET, controllers.routes.ItemController.retrieveItem(1).url)).get
      status(secondResult) must be equalTo OK
      contentAsString(secondResult)
    }
  }

}
