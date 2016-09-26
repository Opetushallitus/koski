package fi.oph.koski.log

object Debug extends Logging {
  def apply[A](x: A): A = debug(x)

  def debug[A](x: A): A = {
    logger.info(x.toString)
    x
  }

  def debug[A](format: A => String, x: A): A = {
    logger.info(format(x))
    x
  }

}
