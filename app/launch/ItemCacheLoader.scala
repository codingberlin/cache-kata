package launch

import com.softwaremill.macwire._
import play.api.ApplicationLoader.Context
import play.api._
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.routing.Router
import router.Routes

class ItemCacheLoader extends ApplicationLoader {
  def load(context: Context) = {
    val module = new BuiltInComponentsFromContext(context) with ItemCacheComponents

    module.startActors()

    module.application
  }
}

trait ItemCacheComponents extends BuiltInComponents with ItemCacheModule with AhcWSComponents {

  implicit lazy val executionContext = actorSystem.dispatcher

  lazy val router: Router = {
    val prefix: String = "/"
    wire[Routes]
  }

}
