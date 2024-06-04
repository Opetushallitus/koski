package fi.oph.koski.util

object Optional {
  def when[T](condition: Boolean)(f: => T): Option[T] =
    if (condition) Some(f) else None
}
