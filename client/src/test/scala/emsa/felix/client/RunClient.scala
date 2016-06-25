package emsa.felix.client

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{BinaryMessage, WebSocketRequest}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, StreamConverters}
import com.typesafe.config.ConfigFactory

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by pappmar on 23/06/2016.
  */
object RunClient {

  def debug[T](future : Future[T])(implicit executionContext: ExecutionContext) =
    future.onComplete(println)

  def main(args: Array[String]) {

    implicit val actorSystem = ActorSystem("test", ConfigFactory.parseString(
      """
        |akka.loglevel = "DEBUG"
      """.stripMargin).withFallback(ConfigFactory.load()))
    implicit val actorMaterializer = ActorMaterializer()
    import actorSystem.dispatcher

    val wsFlow =
      Http().webSocketClientFlow(WebSocketRequest("ws://localhost:9977/starfelix/socket"))
//    Http().webSocketClientFlow(WebSocketRequest(s"ws://localhost:${RunEchoServer.port}/${RunEchoServer.wsPath}"))


    val mat1 =
      StreamConverters.fromInputStream(() => System.in, 1)
        .map(bs => BinaryMessage(bs))
        .viaMat(wsFlow)(Keep.right)
        .collect({case bm : BinaryMessage => bm.dataStream})
        .flatMapConcat(s => s)
        .to(StreamConverters.fromOutputStream(() => System.out))
        .run()

    debug(mat1)







  }

}
