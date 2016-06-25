package emsa.felix.war

import javax.servlet.{ServletContextEvent, ServletContextListener}

/**
  * Created by pappmar on 22/06/2016.
  */
class FelixContextListener extends ServletContextListener {
  override def contextDestroyed(sce: ServletContextEvent): Unit = ()

  override def contextInitialized(sce: ServletContextEvent): Unit = {
    println("boooooo2")

  }
}
