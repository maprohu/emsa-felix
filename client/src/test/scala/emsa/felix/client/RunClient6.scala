package emsa.felix.client

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpEntity, HttpRequest, HttpResponse}
import akka.http.scaladsl.model.ws.{BinaryMessage, WebSocketRequest}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep, Sink, Source, StreamConverters}
import com.typesafe.config.ConfigFactory

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by pappmar on 23/06/2016.
  */
object RunClient6 {

  def debug[T](future : Future[T])(implicit executionContext: ExecutionContext) =
    future.onComplete(println)

  def main(args: Array[String]) {

    val id = args match {
      case Array(id) => id
      case _ => "1"
    }

    implicit val actorSystem = ActorSystem("test", ConfigFactory.parseString(
      """
        |akka.loglevel = "DEBUG"
      """.stripMargin).withFallback(ConfigFactory.load()))
    implicit val actorMaterializer = ActorMaterializer()
    import actorSystem.dispatcher

    val connect =
      Http().outgoingConnection(
        "localhost",
        9977
      )


    val out =
      StreamConverters.fromInputStream(() => System.in)
        .map({
          bs =>
            HttpRequest(
              uri = s"/starfelix/console/${id}/client2server",
              entity = HttpEntity(bs)
            )
        })
        .viaMat(connect)(Keep.right)
        .to(Sink.ignore)
        .run()

    debug(out)

    val poll =
      HttpRequest(
        uri = s"/starfelix/console/${id}/server2client"
      )


    val in =
      Flow[HttpResponse]
        .alsoTo(
          Flow[HttpResponse]
          .flatMapConcat(_.entity.dataBytes)
          .to(StreamConverters.fromOutputStream(() => System.out))
        )
        .map(_ => poll)
        .prepend(Source.single(poll))

    debug(connect.join(in).run())

  }

}
