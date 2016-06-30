package emsa.felix.client

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.{HttpEntity, HttpRequest, HttpResponse, Uri}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Framing, Keep, Sink, Source, StreamConverters}
import akka.util.ByteString
import com.typesafe.config.ConfigFactory

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

/**
  * Created by pappmar on 23/06/2016.
  */
object FelixClientLoop {

  def debug[T](future : Future[T])(implicit executionContext: ExecutionContext) =
    future.onComplete(println)

  def run(
    id: String = "1",
    host: String = "localhost",
    port: Int = 9977,
    app: String = "starfelix"
  ) {

    implicit val actorSystem = ActorSystem("test", ConfigFactory.parseString(
      """
        |akka.loglevel = "DEBUG"
      """.stripMargin).withFallback(ConfigFactory.load()))
    implicit val actorMaterializer = ActorMaterializer()
    import actorSystem.dispatcher

//    val connect =
//      Http().outgoingConnection(
//        host,
//        port
//      )

    val connect =
      Flow[HttpRequest]
        .map(r => (r, ()))
        .via(
          Http().superPool()
        )
        .collect({
          case (Success(r), _) => r
        })


    StreamConverters.fromInputStream(() => System.in)
      .via(
        Framing.delimiter(
          ByteString(System.lineSeparator().getBytes),
          maximumFrameLength = 100,
          allowTruncation = true
        ))
      .map(_.utf8String.trim)
      .map({
        cmd =>
          HttpRequest(
            uri = Uri(s"/${app}/command")
              .withScheme("http")
              .withHost(host)
              .withPort(port)
              .withQuery(
                Query(
                  "cmd" -> cmd
                )
              )
          )
      })
      .viaMat(connect)(Keep.right)
      .flatMapConcat(_.entity.dataBytes)
      .to(StreamConverters.fromOutputStream(() => System.out))
      .run()



  }

}
