package emsa.felix.bundle.impl

import java.io.{File, FileOutputStream, PrintStream}

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.{HttpEncoding, HttpEncodingRange}
import akka.http.scaladsl.model.ws.{BinaryMessage, Message}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep, StreamConverters}
import com.typesafe.config.ConfigFactory
import emsa.felix.api.FelixApi
import emsa.felix.route.api.{FelixRouteApi, FelixWebsocketApi, Types}
import org.apache.felix.service.command.CommandProcessor
import org.osgi.framework.BundleContext
import org.osgi.util.tracker.ServiceTracker
import rx.Var

import scala.concurrent.duration._


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

    val consoleFlow =
      Flow
        .fromSinkAndSourceMat(
          Flow[Message]
            .collect({case bm : BinaryMessage => bm.dataStream})
            .flatMapConcat(s => s)
            .watchTermination()(Keep.right)
            .toMat(
              StreamConverters.asInputStream(10.minutes)
            )(Keep.both),
          StreamConverters.asOutputStream(10.minutes)
            .map(bs => BinaryMessage(bs))
        )({ case ((end, in), out) =>
          val ps = new PrintStream(out)
          val session = tracker.getService.createSession(in, ps, ps)

          val thread = new Thread() {
            override def run(): Unit = {
              session.execute("gosh --login --noshutdown");
            }
          }
          thread.start()
          end.onComplete( _ => thread.interrupt() )

          ()
        })

    val dataDir = FelixApi.context.data.toFile
    val m2Dir = new File(dataDir, "repo")
    m2Dir.mkdirs()

    val route : Route =
      path( "test" ) {
        complete( "OK" )
      } ~
      pathPrefix( "repo" ) {
        put {
          path(Segments) { pathElements =>
            extractMaterializer { materializer =>
              extractRequest { req =>
                val target = new File(m2Dir, pathElements.mkString("/"))
                target.getParentFile.mkdirs()

                val sink = StreamConverters.fromOutputStream(() => new FileOutputStream(target))

                val result = req.entity.getDataBytes()
                  .runWith(sink, materializer)

                onSuccess(result) { _ =>
                  complete("OK")
                }
              }
            }
          }
        } ~
        getFromBrowseableDirectory(m2Dir.getAbsolutePath)
      }
//      path("ws") {
//        get {
//
//
//          handleWebSocketMessages(
//            consoleFlow
//          )
//        }
//      }

    FelixRouteApi.register(Var(Some(route)))

    val ws : Types.Provider = {
      case "console" =>
        consoleFlow
    }

    FelixWebsocketApi.register(Var(Some(ws)))

//    HttpEncodingRange(HttpEncoding("X"))

//    Http().bindAndHandle(
//      route,
//      "localhost",
//      3004
//    )
  }

}
