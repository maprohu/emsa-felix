package emsa.felix.client

/**
  * Created by martonpapp on 30/06/16.
  */
object RunClientLoopWeblogicTestNpr {

  def main(args: Array[String]) {
    FelixClientLoop.run(
      app = "npr-filter-tais-npr",
      host = "twls55",
      port = 7030
    )
  }

}
