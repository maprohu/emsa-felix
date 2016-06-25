package emsa.felix.util.akka

import java.net.URL
import java.util.Enumeration

import scala.annotation.tailrec
import scala.util.{Failure, Success, Try}

/**
  * Created by pappmar on 14/01/2016.
  */
case class CompositeClassloader(delegates: Seq[ClassLoader]) extends ClassLoader(null) {

  override def findClass(name: String): Class[_] = {
    @tailrec def find(remaining: Seq[ClassLoader]): Class[_] = {
      if (remaining.isEmpty) throw new ClassNotFoundException(name)
      else Try { remaining.head.loadClass(name) } match {
        case Success(cls) ⇒ cls
        case Failure(_)   ⇒ find(remaining.tail)
      }
    }
    find(delegates)
  }

  override def findResource(name: String): URL = {
    @tailrec def find(remaining: Seq[ClassLoader]): URL = {
      if (remaining.isEmpty) getParent.getResource(name)
      else Option { remaining.head.getResource(name) } match {
        case Some(r) ⇒ r
        case None    ⇒ find(remaining.tail)
      }
    }
    find(delegates)
  }

  override def findResources(name: String): Enumeration[URL] = {
    import scala.collection.JavaConversions._

    val resources = delegates.flatMap {
      bundle => Option(bundle.getResources(name)).map { ress => ress.toList }.getOrElse(Nil)
    }
    java.util.Collections.enumeration(resources)
  }

}
