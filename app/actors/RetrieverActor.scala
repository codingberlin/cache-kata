package actors

import actors.CacheHolderActor.UpdateCache
import actors.RetrieverActor.RetrieveItems
import akka.actor.{ ActorRef, Actor }
import services.{ CompressorService, ItemSourceService }

import scala.concurrent.ExecutionContext

class RetrieverActor(
    cacheHolderActorRef: CacheHolderActorRef,
    itemSourceService: ItemSourceService,
    compressorService: CompressorService
)(implicit executionContext: ExecutionContext) extends Actor {

  def receive = {
    case RetrieveItems =>
      itemSourceService
        .retrieveItems
        .flatMap(compressorService.compress[String])
        .foreach(cacheHolderActorRef.ref ! UpdateCache(_))
  }
}

object RetrieverActor {
  case object RetrieveItems
}

case class RetrieverActorRef(ref: ActorRef)

