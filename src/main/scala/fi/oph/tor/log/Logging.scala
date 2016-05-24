package fi.oph.tor.log

import org.log4s._

trait Logging {
  protected lazy val logger: Logger = getLogger(getClass)
}