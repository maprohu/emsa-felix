package emsa.felix.client

/**
  * Created by pappmar on 30/06/2016.
  */
object RunClientTomcatLocalNpr {

  def main(args: Array[String]) {

    FelixClient6.run(
      id = "1",
      app = "npr-filter-tais-npr"
    )

  }


}
