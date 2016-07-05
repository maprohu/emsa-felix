package emsa.felix.client

/**
  * Created by martonpapp on 30/06/16.
  */
object RunClientLoopWeblogicPreprodNpr {

  def main(args: Array[String]) {
    FelixClientLoop.run(
      app = "npr-filter-tais-npr",
      host = "qwls56",
      port = 7036
    )
  }

}
