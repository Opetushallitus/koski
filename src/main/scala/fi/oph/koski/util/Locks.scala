package fi.oph.koski.util

import java.util.concurrent.locks.ReentrantReadWriteLock

object Locks {
  def withWriteLock[A](f: => A)(implicit l: ReentrantReadWriteLock): A = {
    l.writeLock.lock()
    try {
      f
    } finally {
      l.writeLock.unlock()
    }
  }
}
