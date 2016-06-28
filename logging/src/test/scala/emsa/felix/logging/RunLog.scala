package emsa.felix.logging

import java.nio.file.{Path, Paths}

import emsa.felix.api.{Context, FelixApi}
import org.slf4j.LoggerFactory

/**
  * Created by martonpapp on 26/06/16.
  */
object RunLog {

  def main(args: Array[String]) {

    FelixApi.context = new Context {
      override def name: String = "appname"

      override def log: Path = Paths.get("target/logs")

      override def data: Path = ???

      override def debug: Boolean = false
    }
    val logger = LoggerFactory.getLogger(RunLog.getClass)

    (0 to 1000000) foreach { i =>
      logger.debug(s"boo at ${i}")
    }
  }

}
