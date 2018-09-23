package launch

import actors.CacheHolderActor.CacheStatus
import actors.RetrieverActor.RetrieveItems
import actors.{ RetrieverActorRef, CacheHolderActorRef, CacheHolderActor, RetrieverActor }
import akka.actor.{ Props, ActorSystem }
import akka.util.Timeout
import com.softwaremill.macwire._
import play.api.inject.ApplicationLifecycle
import com.typesafe.config.ConfigFactory
import services.{ CompressorService, ItemSourceService }

import scala.concurrent.{ Await, ExecutionContext }
import scala.concurrent.duration.MILLISECONDS
import scala.concurrent.duration.FiniteDuration

trait ActorModule {

  implicit val executionContext: ExecutionContext
  def actorSystem: ActorSystem
  def itemSourceService: ItemSourceService
  def compressorService: CompressorService
  def applicationLifecycle: ApplicationLifecycle

  lazy val retrieverActorSchedule = FiniteDuration(ConfigFactory.load().getDuration("items.updateInterval").toMillis, MILLISECONDS)

  lazy val cacheHolderActorRef = CacheHolderActorRef(actorSystem.actorOf(Props(wire[CacheHolderActor])))
  lazy val retrieverActorRef = RetrieverActorRef(actorSystem.actorOf(Props(wire[RetrieverActor])))

  def startActors(): Unit = {
    import scala.concurrent.duration._
    import akka.pattern.ask
    implicit val timeout = Timeout(5.seconds)

    actorSystem.scheduler.schedule(0.seconds, retrieverActorSchedule, retrieverActorRef.ref, RetrieveItems)

    Await.ready(cacheHolderActorRef.ref ? CacheStatus, 5.seconds)

    applicationLifecycle.addStopHook(() =>
      actorSystem.terminate())
  }

}
