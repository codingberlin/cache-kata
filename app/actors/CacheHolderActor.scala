package actors

import actors.CacheHolderActor._
import akka.actor.{ ActorRef, Actor }
import domain.Compressed

class CacheHolderActor extends Actor {

  var itemsCache: Seq[Compressed[String]] = Seq()
  var askedForCacheStatus: Option[ActorRef] = None

  override def receive: Receive = {
    case UpdateCache(items) =>
      itemsCache = items
      askedForCacheStatus.foreach(_ ! CacheInitialized)
    case RetrieveCache =>
      sender ! CachedItems(itemsCache)
    case CacheStatus =>
      if (itemsCache.nonEmpty)
        sender ! CacheInitialized
      else
        askedForCacheStatus = Option(sender)

  }
}

object CacheHolderActor {
  case object CacheStatus
  case object CacheInitialized
  case object RetrieveCache
  case class UpdateCache(newItems: Seq[Compressed[String]])
  case class CachedItems(items: Seq[Compressed[String]])
}

case class CacheHolderActorRef(ref: ActorRef)
