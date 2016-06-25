package emsa.felix.war

import java.io.InputStream
import javax.websocket._
import javax.websocket.server.{PathParam, ServerEndpoint}

import akka.Done
import emsa.felix.api.FelixApi
import sbt.io.IO

import scala.concurrent.Promise

/**
  * Created by pappmar on 24/06/2016.
  */
@ServerEndpoint("/websocket/{endpoint}")
class FelixSocket {

//  @OnMessage
//  def onMessage(data: InputStream, session: Session): Unit = {
//    IO.transferAndClose(
//      data,
//      session.getBasicRemote.getSendStream
//    )
//  }

  val ClosePromise = "CLOSE_PROMISE"

  @OnOpen
  def onOpen(session: Session, @PathParam("endpoint") endpoint: String): Unit = {
    val promise = Promise[CloseReason]()
    session.getUserProperties.put(ClosePromise, promise)
    FelixApi.dispatch(session, endpoint, promise.future)
  }

  @OnClose
  def onClose(session: Session): Unit = {
    session.getUserProperties.get(ClosePromise).asInstanceOf[Promise[CloseReason]].success(CloseReason)
  }

}
