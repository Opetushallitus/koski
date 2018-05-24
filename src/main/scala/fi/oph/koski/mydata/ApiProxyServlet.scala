package fi.oph.koski.mydata

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.GlobalExecutionContext
import fi.oph.koski.log.Logging
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusQueries
import fi.oph.koski.servlet.{ApiServlet, NoCache}
import fi.oph.koski.util.{Pagination, Timing}
import org.scalatra.ContentEncodingSupport


class ApiProxyServlet(implicit val application: KoskiApplication) extends ApiServlet with Logging with GlobalExecutionContext with OpiskeluoikeusQueries with ContentEncodingSupport with NoCache with Timing with Pagination {

  get("/:oid") {
    logger.info("Proxying OID request to API")
    servletContext.getRequestDispatcher("/api/oppija").forward(request, response)
  }

}
