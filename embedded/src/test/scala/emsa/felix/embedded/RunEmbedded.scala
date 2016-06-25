package emsa.felix.embedded

import java.io.{File, PrintStream}

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{BinaryMessage, Message}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep, StreamConverters}
import com.typesafe.config.ConfigFactory
import sbt.io.IO

/**
  * Created by pappmar on 22/06/2016.
  */
object RunEmbedded {

  def main(args: Array[String]) {

    IO.delete(new File("/wl_domains/star/star-apps/data/starfelix"))

    val fw = FelixEmbedded.initStar("starfelix")


//    implicit val actorSystem = ActorSystem("test", ConfigFactory.parseString(
//      """
//        |akka.loglevel = "DEBUG"
//      """.stripMargin).withFallback(ConfigFactory.load()))
//    implicit val actorMaterializer = ActorMaterializer()
//    import actorSystem.dispatcher
//    import Directives._
//
//    val tracker = FelixEmbedded.initTerminal(fw)
//
//    val route : Route =
//      path("ws") {
//        get {
//
//          val flow =
//            Flow
//              .fromSinkAndSourceMat(
//                Flow[Message]
//                  .collect({case bm : BinaryMessage => bm.dataStream})
//                  .flatMapConcat(s => s)
//                  .toMat(
//                    StreamConverters.asInputStream()
//                  )(Keep.right),
//                StreamConverters.asOutputStream()
//                  .map(bs => BinaryMessage(bs))
//              )({ case (in, out) =>
//                val ps = new PrintStream(out)
//                val session = tracker.getService.createSession(in, ps, ps)
//                session.execute("gosh --login --noshutdown");
//                ()
//              })
//
//          handleWebSocketMessages(
//            flow
//          )
//        }
//      }
//
//    Http().bindAndHandle(
//      route,
//      "localhost",
//      3004
//    )

  }

}
