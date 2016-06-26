package emsa.felix.embedded

import java.io.File
import java.nio.file.Path

import emsa.felix.api.{Context, FelixApi}
import org.apache.felix.main.AutoProcessor
import org.apache.felix.service.command.CommandProcessor
import org.osgi.framework.Constants
import org.osgi.framework.launch.{Framework, FrameworkFactory}
import org.osgi.util.tracker.ServiceTracker
import sbt.io.IO
import sbt.io.Path._
import sun.misc.Service

import scala.collection.JavaConversions._

/**
  * Created by pappmar on 22/06/2016.
  */
object FelixEmbedded {

  case class Ctx(
    name: String,
    data: Path,
    log: Path,
    debug: Boolean
  ) extends Context

  def contextImdate(app: String) =
    contextEmsa("imdate", "imdate-ext", app)

  def contextStar(app: String) =
    contextEmsa("star", "star-apps", app)

  def contextEmsa(domain: String, sub: String, app: String) =
    context(new File("/wl_domains") / domain / sub, app)


  def context(dir: File, app: String) = {


    val data = dir / "data" / app
    val log = dir / "logs"

    Ctx(
      name = app,
      data = data.toPath,
      log = log.toPath,
      debug = false
    )
  }

  def init(ctx: Context) = {
    FelixApi.context = ctx

    val data = ctx.data.toFile

    if (!data.exists()) {
      data.mkdirs()

      IO.unzipURL(getClass.getResource("resources.zip"), data)
    }

    val props = Map[String, String](
      Constants.FRAMEWORK_STORAGE -> (data / "felix-cache").getAbsolutePath,
      Constants.FRAMEWORK_BOOTDELEGATION ->
        """
          |sun.misc
        """.stripMargin.replaceAll("\\s", ""),
      Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA ->
        """
          |emsa.felix.api,
          |javax.servlet;version="3.0.1",
          |javax.servlet.http;version="3.0.1",
          |javax.websocket;version="1.1.0"
          |""".stripMargin.replaceAll("\\s", ""),
      AutoProcessor.AUTO_DEPLOY_DIR_PROPERTY -> (data / "bundle").getAbsolutePath,
      AutoProcessor.AUTO_DEPLOY_ACTION_PROPERTY -> "install,start",
      "obr.repository.url" -> (data / "repo" / "repository.xml").toURI.toString,
//      FelixApi.DataDir -> data.absolutePath,
      "gosh.args" -> ""
//      "gosh.args" -> "--xtrace --command telnetd start"
//    "gosh.args" -> "--nointeractive --xtrace --command telnetd start"
    )

    val factory = Service.providers(classOf[FrameworkFactory])
    val fw = factory.next().newFramework(props)
    fw.init()
    AutoProcessor.process(props, fw.getBundleContext)
    fw.start()

    fw
  }

//  def initTerminal(fw: Framework) = {
//
//    val tracker = new ServiceTracker[CommandProcessor, CommandProcessor](
//      fw.getBundleContext,
//      classOf[CommandProcessor],
//      null
//    )
//
//    tracker.open()
//
//    tracker
//
//  }

}
