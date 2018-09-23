package services

import java.net.URL
import akka.stream.scaladsl.{ Framing, Source }
import akka.util.ByteString
import play.api.libs.ws.WSClient
import scala.concurrent.{ ExecutionContext, Future }

class ItemSourceService(itemSource: URL, ws: WSClient)(implicit executionContext: ExecutionContext) {

  def retrieveItems: Future[Source[String, _]] = {
    ws
      .url(itemSource.toExternalForm)
      .withMethod("GET")
      .stream()
      .map(_.body)
      .map(_
        .via(Framing.delimiter(ByteString.fromString("\n"), Integer.MAX_VALUE, allowTruncation = true))
        .map(_.utf8String))
  }

}
