package emsa.felix.util.scalarx

import rx._

/**
  * Created by pappmar on 25/11/2015.
  */
trait RxRef[T] {
  rxRefThis =>

  private val refs = Var(Seq[Rx[Option[T]]]())
  val ref : Rx[Option[T]] = Rx.unsafe(refs().headOption.flatMap(_()))

  def register(value: Rx[Option[T]]) : RxRegistration = this.synchronized {
    refs() = value +: refs.now

    new RxRegistration {
      override def unregister(): Unit = {
        rxRefThis.synchronized {
          refs() = refs.now diff Seq(value)
        }
        value.kill()
      }
    }
  }

}

trait RxRegistration {
  def unregister() : Unit
}
