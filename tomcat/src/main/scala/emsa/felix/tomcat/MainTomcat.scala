package emsa.felix.tomcat

import java.io.File
import javax.websocket.ContainerProvider

import org.apache.catalina.core.StandardContext
import org.apache.catalina.startup.Tomcat
import org.apache.naming.resources.VirtualDirContext

/**
  * Created by pappmar on 22/06/2016.
  */
object MainTomcat {

  def main(args: Array[String]) {

    val tomcat = new Tomcat
    tomcat.setBaseDir("target/tomcat")
    tomcat.setPort(9977)

    val ctx = tomcat.addWebapp("/starfelix", new File("war/src/main/webapp").getAbsolutePath).asInstanceOf[StandardContext]

    val additionalWebInfClasses = new File("war/target/classes")
    val resources = new VirtualDirContext()
    resources.setExtraResourcePaths("/WEB-INF/classes=" + additionalWebInfClasses)
//    resources.addPreResources(new DirResourceSet(
//      resources,
//      "/WEB-INF/classes",
//      additionalWebInfClasses.getAbsolutePath,
//      "/"
//    ))
    ctx.setResources(resources)

    val wsContainer = ContainerProvider.getWebSocketContainer

    tomcat.start()
    tomcat.getServer.await()

  }

}
