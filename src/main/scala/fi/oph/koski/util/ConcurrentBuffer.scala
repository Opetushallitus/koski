package fi.oph.koski.util

import scala.collection.mutable.ListBuffer

class ConcurrentBuffer[T] {
  private[this] val xs: ListBuffer[T] = new ListBuffer[T]

  def append(x: T): Unit = synchronized {
    xs += x
  }

  def popAll: List[T] = synchronized {
    val values = xs.toList
    xs.clear()
    values
  }
}
