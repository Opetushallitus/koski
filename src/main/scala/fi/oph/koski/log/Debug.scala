package fi.oph.koski.log

object Debug extends Logging {
  def debug[A](x: A): A = {
    logger.info(x.toString)
    x
  }
}
