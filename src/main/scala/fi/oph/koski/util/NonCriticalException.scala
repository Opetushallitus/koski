package fi.oph.koski.util

// Ei kriittinen poikkeus, lokita warn tasolla
class NonCriticalException(msg: String) extends Exception(msg)

object NonCriticalException {
  def apply(e: Throwable): Option[Throwable] = if (e.isInstanceOf[NonCriticalException]) {
    Some(e)
  } else if (e.getCause.isInstanceOf[NonCriticalException]) {
    Some(e.getCause)
  } else {
    None
  }
}
