package fi.oph.tor.log

object Debug extends Logging {
  def debug[A](x: A): A = {
    logger.info(x.toString)
    x
  }
}
