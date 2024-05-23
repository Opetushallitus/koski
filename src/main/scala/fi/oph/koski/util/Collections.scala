package fi.oph.koski.util

object Collections {
  def asNonEmpty[S <: Seq[_]](s: S): Option[S] =
    if (s.isEmpty) None else Some(s)
}
