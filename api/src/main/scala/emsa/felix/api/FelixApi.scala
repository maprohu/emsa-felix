package emsa.felix.api

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import javax.websocket.{CloseReason, EndpointConfig, Session}

import scala.concurrent.Future

/**
  * Created by pappmar on 23/06/2016.
  */
object FelixApi {

  val defaultHandler = new FelixApiHandler {
    override def process(req: HttpServletRequest, res: HttpServletResponse): Unit = {
      res.getWriter.println("no handler")
    }

    override def socketOpen(session: Session, endpointConfig: EndpointConfig, endpoint: String): Unit = ()

    override def socketClose(session: Session, closeReason: CloseReason): Unit = ()

  }

  var handlers = List[FelixApiHandler](defaultHandler)

  def activeHandler = this.synchronized { handlers.head }

  def dispatch(req: HttpServletRequest, res: HttpServletResponse): Unit = {
    activeHandler.process(req, res)
  }


  def register(handler: FelixApiHandler) : Unit = this.synchronized {
    handlers = handler +: handlers
  }

  def unregister(handler: FelixApiHandler) : Unit = this.synchronized {
    handlers = handlers diff Seq(handler)
  }

}

trait FelixApiHandler {

  def process(request: HttpServletRequest, response: HttpServletResponse)

  def socketOpen(session: Session, endpointConfig: EndpointConfig, endpoint: String)
  def socketClose(session: Session, closeReason: CloseReason)


}

trait SessionClose {
  def onClose(reason: CloseReason)
}

trait SessionEvents {

  def registerClose(onClose: SessionClose)

}
