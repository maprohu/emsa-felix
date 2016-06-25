package emsa.felix.war

import java.io.InputStream
import javax.websocket.MessageHandler.{Partial, Whole}
import javax.websocket._
import javax.websocket.server.{PathParam, ServerEndpoint}

import akka.Done
import emsa.felix.api.{FelixApi, SessionClose, SessionEvents}
import sbt.io.IO

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Promise

/**
  * Created by pappmar on 24/06/2016.
  */
@ServerEndpoint("/websocket/{endpoint}")
class FelixSocket extends Endpoint {

//  @OnMessage
//  def onMessage(data: Array[Byte], session: Session): Unit = {
//    System.out.write(data)
//  }

//  val ClosePromise = "CLOSE_PROMISE"

//  @OnOpen
//  def onOpen(session: Session, @PathParam("endpoint") endpoint: String): Unit = {
////    session.addMessageHandler(new Partial[Array[Byte]] {
////      override def onMessage(partialMessage: Array[Byte], last: Boolean): Unit = {
////        System.out.write(partialMessage)
////      }
////    })
//
//
//    val promise = Promise[CloseReason]()
//    session.getUserProperties.put(ClosePromise, promise)
//    FelixApi.dispatch(session, endpoint,
//      new SessionEvents {
//        override def registerClose(onClose: SessionClose): Unit = {
//          promise.future.onComplete(r => onClose.onClose(r.getOrElse(
//            new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, r.failed.get.getMessage)
//          )))
//        }
//      }
//    )
//  }

//  @OnClose
//  override  def onClose(session: Session, reason: CloseReason): Unit = {
//    session.getUserProperties.get(ClosePromise).asInstanceOf[Promise[CloseReason]].success(reason)
//  }

//  override def onOpen(session: Session, config: EndpointConfig): Unit = {
//
//    val promise = Promise[CloseReason]()
//    session.getUserProperties.put(ClosePromise, promise)
//
//    session.addMessageHandler(new Whole[String] {
//      override def onMessage(message: String): Unit = {
//        println(message)
//      }
//    })
//
//    val endpoint = session.getRequestParameterMap.get("endpoint").get(0)
//
//    FelixApi.dispatch(session, endpoint,
//      new SessionEvents {
//        override def registerClose(onClose: SessionClose): Unit = {
//          promise.future.onComplete(r => onClose.onClose(r.getOrElse(
//            new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, r.failed.get.getMessage)
//          )))
//        }
//      }
//    )
//  }

  override def onOpen(session: Session, config: EndpointConfig): Unit = {
    val endpoint = session.getRequestParameterMap.get("endpoint").get(0)

    try {
      FelixApi.activeHandler.socketOpen(session, config, endpoint)
    } catch {
      case ex : Throwable =>
        ex.printStackTrace()
        throw ex
    }
  }

  override def onClose(session: Session, closeReason: CloseReason): Unit = {
    FelixApi.activeHandler.socketClose(session, closeReason)
  }
}
