package emsa.felix.embedded

import java.io.{InputStream, OutputStream}

import org.apache.commons.net.SocketClient
import org.apache.commons.net.telnet.TelnetClient
import sbt.io.IO


/**
  * Created by pappmar on 22/06/2016.
  */
object RunClient {

  def copy(from: InputStream, to: OutputStream) = {

    new Thread() {
      override def run(): Unit = {
        Stream.continually(from.read())
          .takeWhile(_ != -1)
          .foreach{ b =>
            to.write(b)
            to.flush()
          }
      }
    }.start()
  }

  def main(args: Array[String]) {
    val client = new TelnetClient()
    client.connect("localhost", 2019)

    val from = client.getInputStream
    val to = client.getOutputStream

    copy(from, System.out)
    copy(System.in, to)

  }

}
