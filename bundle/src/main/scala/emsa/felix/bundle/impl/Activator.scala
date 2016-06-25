package emsa.felix.bundle.impl

import java.nio.ByteBuffer
import java.nio.channels.Channels
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import javax.websocket.MessageHandler.Partial
import javax.websocket.RemoteEndpoint.Async
import javax.websocket.{CloseReason, EndpointConfig, MessageHandler, Session}

import akka.Done
import akka.actor.ActorSystem
import akka.event.LoggingFilter
import akka.http.scaladsl.model.HttpHeader.ParsingResult.Ok
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.http.scaladsl.model.{ContentTypes, HttpProtocols, HttpResponse, _}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Keep, Sink, Source, SourceQueueWithComplete, StreamConverters}
import akka.util.{ByteString, Unsafe}
import com.typesafe.config.ConfigFactory
import com.typesafe.sslconfig.util.ConfigLoader
import emsa.felix.api.{FelixApi, FelixApiHandler, SessionClose, SessionEvents}
import emsa.felix.route.api.{FelixRouteApi, FelixWebsocketApi}
import emsa.felix.util.akka.ActorSystemActivator
import org.osgi.framework.{BundleActivator, BundleContext}

import scala.collection.JavaConversions
import scala.concurrent.{Await, Future, Promise}
import scala.concurrent.duration._

/**
  * Created by pappmar on 22/06/2016.
  */
//class Activator extends BundleActivator {
class Activator extends ActorSystemActivator(classOf[Activator]) {


  var stop : () => Unit = null

//  override def start(context: BundleContext): Unit = {
  override def configure(context: BundleContext, system: ActorSystem): Unit = {

//    val classLoader = classOf[LoggingFilter].getClassLoader
//
//    val system = ActorSystem(
//      "felix",
//      ConfigFactory.load(),
//      classLoader
//    )

    implicit val actorSystem = system


    val api = new DefaultFelixApiHandler

    FelixApi.register(api)

    Console.init(context)

    stop = () => {
      Await.ready(
        actorSystem.terminate(),
        5.seconds
      )

      FelixApi.unregister(api)
    }
  }

  override def stop(context: BundleContext): Unit = {
    stop()

    stop = null
  }


}

class DefaultFelixApiHandler(implicit actorSystem: ActorSystem) extends FelixApiHandler {

  implicit val materializer = ActorMaterializer()
  import actorSystem.dispatcher

  val routeHandler = Route.asyncHandler(
    pathPrefix( Segment ) { _ =>
      FelixRouteApi.safe.now
    }
  )

  override def process(req: HttpServletRequest, res: HttpServletResponse): Unit = {
    import JavaConversions._

    val httpRequest: HttpRequest = HttpRequest(
      method = HttpMethods.getForKey(req.getMethod).get,
      uri = Uri(req.getRequestURI).copy(rawQueryString = Option(req.getQueryString)),
      headers = req.getHeaderNames.toIterable.map({ headerName =>
        HttpHeader.parse(headerName, req.getHeader(headerName)).asInstanceOf[Ok].header
      })(collection.breakOut),
      entity = HttpEntity(Option(req.getContentType).map(ct => ContentType.parse(ct).right.get).getOrElse(ContentTypes.`application/octet-stream`), StreamConverters.fromInputStream(() => req.getInputStream)),
      protocol = HttpProtocols.getForKey(req.getProtocol).get
    )

    val httpResponse: HttpResponse = Await.result(routeHandler(httpRequest), Duration.Inf)

    httpResponse.headers.foreach { h =>
      res.setHeader(h.name(), h.value())
    }
    res.setStatus(httpResponse.status.intValue())
    res.setContentType(httpResponse.entity.contentType.toString())
    httpResponse.entity.contentLengthOption.foreach { cl =>
      res.setContentLength(cl.toInt)
    }

    val os = res.getOutputStream
    val out = Channels.newChannel(os)

    val writer = httpResponse.entity.dataBytes.runForeach { bytes =>
      bytes.asByteBuffers.foreach { bb =>
        out.write(bb)
        os.flush()
      }
    }

    Await.result(writer, Duration.Inf)

    out.close()
    os.close()

  }

  val ClosePromise = "CLOSE_PROMISE"

  override def socketClose(session: Session, closeReason: CloseReason): Unit = {
    session.getUserProperties.get(ClosePromise).asInstanceOf[Promise[CloseReason]].success(closeReason)
  }

  override def socketOpen(session: Session, endpointConfig: EndpointConfig, endpoint: String): Unit = {
    val flow = FelixWebsocketApi.safe.now(endpoint)

    val closePromise = Promise[CloseReason]()
    session.getUserProperties.put(ClosePromise, closePromise)

    val (source, sink) =
      Source.queue[Message](1, OverflowStrategy.backpressure)
        .via(flow)
        .mapAsync(1)({
          case msg : BinaryMessage =>
            val fut =
              msg.dataStream
                .toMat(StreamConverters.fromOutputStream(session.getBasicRemote.getSendStream))(Keep.right)
                .run()

            fut
        })
        .toMat(Sink.ignore)(Keep.both)
        .run()

    closePromise.future.onComplete( _ => source.complete() )

    sink.onComplete( _ => session.close() )

    session.addMessageHandler(
      new Partial[Array[Byte]] {
        var out : SourceQueueWithComplete[ByteString] = null

        override def onMessage(partialMessage: Array[Byte], last: Boolean): Unit = {
          val first = out == null
          val bs = ByteString(partialMessage)

          (first, last) match {
            case (true, true) => // single
              enqueue(BinaryMessage.Strict(bs), source)
            case (true, false) => // start
              val promise = Promise[SourceQueueWithComplete[ByteString]]()

              val str =
                Source.queue[ByteString](0, OverflowStrategy.backpressure)
                  .prepend(Source.single(bs))
                  .mapMaterializedValue({ sq =>
                    promise.success(sq)
                  })

              enqueue(BinaryMessage.Streamed(str), source)

              out = Await.result(promise.future, Duration.Inf)

            case (false, false) => // middle
              enqueue(bs, out)

            case (false, true) => // end
              enqueue(bs, out)
              out.complete()
              out = null
          }
        }
      }
    )
  }

  def enqueue[T](msg: T, queue: SourceQueueWithComplete[T]) = {
    Await.result(queue.offer(msg), Duration.Inf)
  }
}