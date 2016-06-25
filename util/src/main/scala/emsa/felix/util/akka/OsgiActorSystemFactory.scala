/**
 * Copyright (C) 2009-2014 Typesafe Inc. <http://www.typesafe.com>
 */
package emsa.felix.util.akka

import akka.actor.ActorSystem
import com.typesafe.config.{Config, ConfigFactory}

/**
 * Factory class to create ActorSystem implementations in an OSGi environment.  This mainly involves dealing with
 * bundle classloaders appropriately to ensure that configuration files and classes get loaded properly
 */
class OsgiActorSystemFactory(val classloader: ClassLoader, val fallbackClassLoader: Option[ClassLoader], config: Config = ConfigFactory.empty) {

  /**
   * Creates the [[akka.actor.ActorSystem]], using the name specified
   */
  def createActorSystem(name: String): ActorSystem = createActorSystem(Option(name))

  /**
   * Creates the [[akka.actor.ActorSystem]], using the name specified.
   *
   * A default name (`bundle-<bundle id>-ActorSystem`) is assigned when you pass along [[scala.None]] instead.
   */
  def createActorSystem(name: Option[String]): ActorSystem =
    ActorSystem(actorSystemName(name), actorSystemConfig(), classloader)

  /**
   * Strategy method to create the Config for the ActorSystem
   * ensuring that the default/reference configuration is loaded from the akka-actor bundle.
   * Configuration files found in akka-actor bundle
   */
  def actorSystemConfig(): Config = {
    config.withFallback(ConfigFactory.load(classloader).withFallback(ConfigFactory.defaultReference(OsgiActorSystemFactory.akkaActorClassLoader)))
  }

  /**
   * Determine the name for the [[akka.actor.ActorSystem]]
   * Returns a default value of `bundle-<bundle id>-ActorSystem` is no name is being specified
   */
  def actorSystemName(name: Option[String]): String =
    name.getOrElse("bundle-ActorSystem")

}

object OsgiActorSystemFactory {
  /**
   * Class loader of akka-actor bundle.
   */
  def akkaActorClassLoader = classOf[ActorSystemActivator].getClassLoader

  /*
   * Create an [[OsgiActorSystemFactory]] instance to set up Akka in an OSGi environment
   */
  def apply(classLoader: ClassLoader, config: Config): OsgiActorSystemFactory = new OsgiActorSystemFactory(classLoader, Some(akkaActorClassLoader), config)
}
