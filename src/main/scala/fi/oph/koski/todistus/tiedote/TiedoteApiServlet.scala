package fi.oph.koski.todistus.tiedote

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.{KoskiCookieAndBasicAuthenticationSupport, KoskiSpecificSession}
import fi.oph.koski.koskiuser.Rooli.OPHPAAKAYTTAJA
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}

class TiedoteApiServlet(implicit val application: KoskiApplication)
  extends KoskiSpecificApiServlet
    with NoCache
    with KoskiCookieAndBasicAuthenticationSupport
{
  implicit def session: KoskiSpecificSession = koskiSessionOption.get

  before() {
    if (!session.hasRole(OPHPAAKAYTTAJA)) {
      haltWithStatus(KoskiErrorCategory.forbidden("Sallittu vain OPH-pääkäyttäjälle"))
    }
  }

  private val repository = application.kielitutkintotodistusTiedoteRepository
  private val service = application.kielitutkintotodistusTiedoteService

  get("/jobs") {
    val state = params.get("state")
    val limit = params.get("limit").map(_.toInt).getOrElse(100)
    val offset = params.get("offset").map(_.toInt).getOrElse(0)

    renderObject(repository.findAll(limit, offset, state))
  }

  get("/stats") {
    renderObject(repository.countByState)
  }

  post("/run") {
    val processed = service.processAll()
    val retried = service.retryAllFailed()

    renderObject(Map(
      "processed" -> processed,
      "retried" -> retried
    ))
  }
}
