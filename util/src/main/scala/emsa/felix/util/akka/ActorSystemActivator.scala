/**
 * Copyright (C) 2009-2014 Typesafe Inc. <http://www.typesafe.com>
 */
package emsa.felix.util.akka

import java.util.{Dictionary, Properties}

import akka.actor.ActorSystem
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.server.Directives
import akka.stream.Materializer
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.sslconfig.akka.AkkaSSLConfig
import com.typesafe.sslconfig.ssl.SSLConfigSettings
import org.osgi.framework._
import org.osgi.service.log.LogService

import scala.util.control.NonFatal

/**
 * Abstract bundle activator implementation to bootstrap and configure an actor system in an
 * OSGi environment.  It also provides a convenience method to register the actor system in
 * the OSGi Service Registry for sharing it with other OSGi bundles.
 *
 * This convenience activator is mainly useful for setting up a single [[akka.actor.ActorSystem]] instance and sharing that
 * with other bundles in the OSGi Framework.  If you want to set up multiple systems in the same bundle context, look at
 * the [[OsgiActorSystemFactory]] instead.
 */
abstract class ActorSystemActivator(classLoader: ClassLoader) extends BundleActivator {

  def this(clazz: Class[_]) = this(
    CompositeClassloader(
      Seq(
        clazz.getClassLoader,
        classOf[ActorSystemActivator].getClassLoader,
        classOf[ActorSystem].getClassLoader,
        classOf[Materializer].getClassLoader,
        classOf[AkkaSSLConfig].getClassLoader,
        classOf[SSLConfigSettings].getClassLoader,
        classOf[Directives].getClassLoader,
        classOf[HttpExt].getClassLoader
      )
    )

  )

  private var system: Option[ActorSystem] = None
  private var registration: Option[ServiceRegistration[_]] = None

  /**
   * Implement this method to add your own actors to the ActorSystem.  If you want to share the actor
   * system with other bundles, call the `registerService(BundleContext, ActorSystem)` method from within
   * this method.
   *
   * @param context the bundle context
   * @param system the ActorSystem that was created by the activator
   */
  def configure(context: BundleContext, system: ActorSystem): Unit

  /**
   * Sets up a new ActorSystem
   *
   * @param context the BundleContext
   */
  def start(context: BundleContext): Unit = {
    system = Some(OsgiActorSystemFactory(classLoader, getActorSystemConfiguration(context)).createActorSystem(Option(getActorSystemName(context))))
    system foreach (addLogServiceListener(context, _))
    system foreach { actorSystem =>
      try {
        configure(context, actorSystem)
      } catch {
        case NonFatal(ex) =>
          actorSystem.shutdown()
          throw ex
      }
    }
  }

  /**
   * Adds a LogService Listener that will advertise the ActorSystem on LogService registration and unregistration
   *
   * @param context  the BundleContext
   * @param  system  the ActorSystem to be advertised
   */
  def addLogServiceListener(context: BundleContext, system: ActorSystem) {
    val logServiceListner = new ServiceListener {
      def serviceChanged(event: ServiceEvent) {
        event.getType match {
          case ServiceEvent.REGISTERED ⇒
            system.eventStream.publish(serviceForReference[LogService](context, event.getServiceReference))
          case ServiceEvent.UNREGISTERING ⇒ system.eventStream.publish(UnregisteringLogService)
        }
      }
    }
    val filter = s"(objectclass=${classOf[LogService].getName})"
    context.addServiceListener(logServiceListner, filter)

    //Small trick to create an event if the service is registred before this start listing for
    Option(context.getServiceReference(classOf[LogService].getName)).foreach(x ⇒ {
      logServiceListner.serviceChanged(new ServiceEvent(ServiceEvent.REGISTERED, x))
    })
  }

  /**
   * Convenience method to find a service by its reference.
   */
  def serviceForReference[T](context: BundleContext, reference: ServiceReference[_]): T =
    context.getService(reference).asInstanceOf[T]

  /**
   * Shuts down the ActorSystem when the bundle is stopped and, if necessary, unregisters a service registration.
   *
   * @param context the BundleContext
   */
  def stop(context: BundleContext): Unit = {
    registration foreach (_.unregister())
    system foreach { system => system.shutdown() }
  }

  /**
   * Register the actor system in the OSGi service registry.  The activator itself will ensure that this service
   * is unregistered again when the bundle is being stopped.
   *
   * Only one ActorSystem can be registered at a time, so any previous registration will be unregistered prior to registering the new.
   *
   * @param context the bundle context
   * @param system the actor system
   */
  def registerService(context: BundleContext, system: ActorSystem): Unit = {
    registration.foreach(_.unregister()) //Cleanup
    val properties = new Properties()
    properties.put("name", system.name)
    registration = Some(context.registerService(classOf[ActorSystem].getName, system,
      properties.asInstanceOf[Dictionary[String, Any]]))
  }

  /**
   * By default, the [[akka.actor.ActorSystem]] name will be set to `bundle-<bundle id>-ActorSystem`.  Override this
   * method to define another name for your [[akka.actor.ActorSystem]] instance.
   *
   * @param context the bundle context
   * @return the actor system name
   */
  def getActorSystemName(context: BundleContext): String = null

  /**
   * Override this method to define a configuration for your [[akka.actor.ActorSystem]] instance.
   * This configuration will be merged with fallback on
   *    the application.conf of your bundle
   *    the reference.conf of the akka bundles
   *    the System properties.
   *
   * @param context the bundle context
   * @return the actor system specific configuration, ConfigFactory.empty by default
   */
  def getActorSystemConfiguration(context: BundleContext): Config = ConfigFactory.empty

}
