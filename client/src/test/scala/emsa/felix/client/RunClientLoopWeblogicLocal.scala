package emsa.felix.client

/**
  * Created by martonpapp on 30/06/16.
  */
object RunClientLoopWeblogicLocal {

  def main(args: Array[String]) {
    FelixClientLoop.run(
      port = 7001
    )
  }

}
