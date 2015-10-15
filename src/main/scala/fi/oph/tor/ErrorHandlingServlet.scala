package fi.oph.tor

import fi.vm.sade.utils.slf4j.Logging
import org.scalatra.ScalatraServlet

trait ErrorHandlingServlet extends ScalatraServlet with Logging {
  error {
    case InvalidRequestException(msg) =>
      halt(status = 400, msg)
    case e: Throwable =>
      logger.error("Error while processing request", e)
      halt(status = 500, "Internal server error")
  }
}
