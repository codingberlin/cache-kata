package controllers

import play.api.mvc.{ Action, Controller }

/**
 * Purpose of this controller is to provide an interface for periodic external monitoring and alarming.
 * If preferred the static answer could be replaced by a status like "green" or "yellow" if the cache
 * is outdated due to not reachable external endpoint where the items are fetched from.
 */
class StatusController extends Controller {

  def status = Action {
    Ok("Ok")
  }

}
