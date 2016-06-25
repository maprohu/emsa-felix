package emsa.felix.standalone

import java.util.ServiceLoader

import org.osgi.framework.Constants
import org.osgi.framework.launch.FrameworkFactory
import sun.misc.Service

import scala.collection.JavaConversions._

/**
  * Created by pappmar on 22/06/2016.
  */
object Main {

  def main(args: Array[String]) {

    val factory = Service.providers(classOf[FrameworkFactory])

    val props = Map[String, String](
      Constants.FRAMEWORK_STORAGE -> "target/felix-cache"

    )

    val fw = factory.next().newFramework(props)
    fw.init()

    fw.start()
  }

}
