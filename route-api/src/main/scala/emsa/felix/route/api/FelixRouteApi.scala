package emsa.felix.route.api


import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.scaladsl.Flow
import emsa.felix.util.scalarx.RxRef

/**
  * Created by pappmar on 24/06/2016.
  */
object FelixRouteApi extends RxRef[Route] {

  val safe = ref.map( _.getOrElse( Directives.complete( "no route" ) ) )

}

object Types {
  type Provider = String => Flow[Message, Message, Any]
}

object FelixWebsocketApi extends RxRef[Types.Provider] {

  val safe = ref.map(_.get)

}
