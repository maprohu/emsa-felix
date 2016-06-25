package emsa.felix.war

import java.io.File

import emsa.felix.tomcat.MainTomcat
import sbt.io.IO

/**
  * Created by pappmar on 22/06/2016.
  */
object RunWar {

  def main(args: Array[String]) {

    IO.delete(new File("/wl_domains/star/star-apps/data/starfelix"))

    MainTomcat.main(args)

  }

}
