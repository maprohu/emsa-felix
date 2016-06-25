package emsa.felix.client

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import com.typesafe.config.ConfigFactory

/**
  * Created by pappmar on 23/06/2016.
  */
object RunEchoServer {

  val port = 3004
  val wsPath = "ws"

  def main(args: Array[String]) {

    implicit val actorSystem = ActorSystem("test", ConfigFactory.parseString(
      """
        |akka.loglevel = "DEBUG"
      """.stripMargin).withFallback(ConfigFactory.load()))
    implicit val actorMaterializer = ActorMaterializer()
    import actorSystem.dispatcher
    import Directives._

    val route : Route =
      path(wsPath) {
        get {
          handleWebSocketMessages(
            Flow[Message]
          )
        }
      }

    Http().bindAndHandle(
      route,
      "localhost",
      port

    )

  }

}
