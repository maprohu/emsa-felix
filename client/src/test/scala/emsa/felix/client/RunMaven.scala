package emsa.felix.client

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

    FelixDeploy.perform(
      Bundle(
        "emsa",
        "felix-bundle",
        "1.0.0.dev"
      ),
      "file:/wl_domains/star/star-apps/data/starfelix/repo"
    )
  }

}
