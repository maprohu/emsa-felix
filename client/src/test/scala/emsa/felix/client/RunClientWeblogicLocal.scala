package emsa.felix.client

/**
  * Created by pappmar on 30/06/2016.
  */
object RunClientWeblogicLocal {

  def main(args: Array[String]) {

    FelixClient6.run(
      id = "1",
      port = 7002
    )

  }


}
