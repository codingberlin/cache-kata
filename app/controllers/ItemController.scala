package controllers

import actors.CacheHolderActor.{ CachedItems, RetrieveCache }
import actors.CacheHolderActorRef
import akka.util.Timeout
import play.api.mvc.{ Action, Controller }
import services.CompressorService
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import akka.pattern.ask

class ItemController(
    cacheHolderActorRef: CacheHolderActorRef,
    compressorService: CompressorService
)(implicit executionContext: ExecutionContext) extends Controller {

  implicit val timeout = Timeout(5.seconds)

  def retrieveItem(index: Int) = Action.async {
    val futureCompressedItems = cacheHolderActorRef.ref ? RetrieveCache
    futureCompressedItems.map {
      case CachedItems(compressedItems) =>
        compressorService.getOption(compressedItems, index) match {
          case None =>
            NotFound
          case Some(item) =>
            Ok(item)
        }
    }
  }

}
