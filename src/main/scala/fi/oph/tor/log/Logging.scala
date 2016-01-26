package fi.oph.tor.log

import org.slf4j.LoggerFactory

trait Logging {
  protected lazy val logger = LoggerFactory.getLogger(getClass())
}