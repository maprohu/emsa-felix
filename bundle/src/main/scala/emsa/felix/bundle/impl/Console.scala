package emsa.felix.bundle.impl

import java.io.PrintStream

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.{HttpEncoding, HttpEncodingRange}
import akka.http.scaladsl.model.ws.{BinaryMessage, Message}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep, StreamConverters}
import com.typesafe.config.ConfigFactory
import emsa.felix.route.api.FelixRouteApi
import org.apache.felix.service.command.CommandProcessor
import org.osgi.framework.BundleContext
import org.osgi.util.tracker.ServiceTracker
import rx.Var

/**
  * Created by pappmar on 24/06/2016.
  */
object Console {

  def init(ctx: BundleContext)(implicit actorSystem : ActorSystem) = {
    implicit val actorMaterializer = ActorMaterializer()
    import actorSystem.dispatcher
    import Directives._

    val tracker = new ServiceTracker[CommandProcessor, CommandProcessor](
      ctx,
      classOf[CommandProcessor],
      null
    )

    tracker.open()

    val route : Route =
      path( "test" ) {
        complete( "OK" )
      } ~
      path("ws") {
        get {

          val flow =
            Flow
              .fromSinkAndSourceMat(
                Flow[Message]
                  .collect({case bm : BinaryMessage => bm.dataStream})
                  .flatMapConcat(s => s)
                  .toMat(
                    StreamConverters.asInputStream()
                  )(Keep.right),
                StreamConverters.asOutputStream()
                  .map(bs => BinaryMessage(bs))
              )({ case (in, out) =>
                val ps = new PrintStream(out)
                val session = tracker.getService.createSession(in, ps, ps)
                session.execute("gosh --login --noshutdown");
                ()
              })

          handleWebSocketMessages(
            flow
          )
        }
      }

    FelixRouteApi.register(Var(Some(route)))

//    HttpEncodingRange(HttpEncoding("X"))

//    Http().bindAndHandle(
//      route,
//      "localhost",
//      3004
//    )
  }

}
