package emsa.felix.war

import javax.servlet.ServletConfig
import javax.servlet.annotation.WebServlet
import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}

import emsa.felix.api.FelixApi
import emsa.felix.embedded.FelixEmbedded
import org.osgi.framework.launch.Framework

import scala.concurrent.duration._

/**
  * Created by pappmar on 23/06/2016.
  */
@WebServlet(name="felix", urlPatterns = Array("/"), loadOnStartup = 1 )
class FelixServlet extends HttpServlet {

  var fw : Framework = null

  override def init(): Unit = {
    fw = FelixEmbedded.initStar("starfelix")
  }

  override def destroy(): Unit = {
    fw.stop()
    fw.waitForStop(10.seconds.toMillis)
    fw = null
    super.destroy()
  }

  override def service(req: HttpServletRequest, resp: HttpServletResponse): Unit = {
    FelixApi.dispatch(req, resp)
  }
}
