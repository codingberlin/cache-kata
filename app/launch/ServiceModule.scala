package launch

import java.net.URL

import akka.stream.Materializer
import com.typesafe.config.ConfigFactory
import play.api.libs.ws.WSClient
import services.{ CompressorService, ItemSourceService }
import com.softwaremill.macwire._

import scala.concurrent.ExecutionContext

trait ServiceModule {

  implicit def materializer: Materializer
  implicit def executionContext: ExecutionContext
  def wsClient: WSClient
  def itemSource: URL = new URL(ConfigFactory.load().getString("items.source"))

  lazy val itemSourceService: ItemSourceService = wire[ItemSourceService]
  lazy val compressorService: CompressorService = wire[CompressorService]

}
