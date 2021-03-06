package emsa.felix.client

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import emsa.felix.deploy.{Bundle, FelixDeploy}

/**
  * Created by martonpapp on 25/06/16.
  */
object RunMaven {

  def main(args: Array[String]) {
//    FelixDeploy.runMaven(
//      FelixDeploy.pom(<build></build>),
//      "install"
//    )(_ => ())
    implicit val actorSystem = ActorSystem()
    implicit val materializer = ActorMaterializer()
    import actorSystem.dispatcher

    FelixDeploy.perform(
      Bundle(
        "emsa",
        "felix-bundle",
        "1.0.0.dev"
      ),
      "http://twls55:7030/npr-filter-tais-npr/repo",
//      "http://localhost:9977/starfelix/repo",
      "file:/wl_domains/imdate/imdate-ext/data/starfelix/repo"
//    "file:/wl_domains/star/star-apps/data/starfelix/repo"
    ).onComplete{ res =>
      println(res)
      Http().shutdownAllConnectionPools().onComplete { _ =>
        actorSystem.terminate()
      }
    }
  }

}
