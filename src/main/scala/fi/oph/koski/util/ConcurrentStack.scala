package fi.oph.koski.util

import java.util.concurrent.locks.ReentrantReadWriteLock

import fi.oph.koski.util.Locks.withWriteLock

class ConcurrentStack[T] {
  implicit private[this] val lock: ReentrantReadWriteLock = new ReentrantReadWriteLock
  private[this] var xs: List[T] = Nil

  def push(x: T): Unit = withWriteLock {
    xs = x :: xs
  }

  def popAll: List[T] = withWriteLock {
    val all = xs
    xs = Nil
    all
  }
}
