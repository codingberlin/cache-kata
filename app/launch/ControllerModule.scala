package launch

import actors.CacheHolderActorRef
import com.softwaremill.macwire._
import controllers.{ ItemController, StatusController }
import services.CompressorService

import scala.concurrent.ExecutionContext

trait ControllerModule {

  implicit val executionContext: ExecutionContext
  def cacheHolderActorRef: CacheHolderActorRef
  def compressorService: CompressorService

  lazy val statusController = wire[StatusController]
  lazy val itemController = wire[ItemController]

}
