package emsa.felix.client

/**
  * Created by pappmar on 30/06/2016.
  */
object RunClientWeblogicLocalNpr {

  def main(args: Array[String]) {

    FelixClient6.run(
      id = "2",
      port = 7002,
      app = "npr-filter-tais-npr"
    )

  }


}
