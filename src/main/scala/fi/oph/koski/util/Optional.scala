package fi.oph.koski.util

object Optional {
  def when[T](condition: Boolean)(f: => T): Option[T] =
    if (condition) Some(f) else None

  def coalesce[T](opts: Option[T]*): Option[T] =
    opts.find(_.isDefined).flatten
}
