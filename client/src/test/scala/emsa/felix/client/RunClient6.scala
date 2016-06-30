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


  def main(args: Array[String]) {

    FelixClient6.run()

  }


}
