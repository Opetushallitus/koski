package fi.oph.koski.mydata

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.AuthenticationSupport
import fi.oph.koski.log.Logging
import fi.oph.koski.servlet._

class MyDataServlet(implicit val application: KoskiApplication) extends ApiServlet
  with AuthenticationSupport with Logging with NoCache with MyDataSupport with LanguageSupport {

  get("/kumppani/:memberCode") {
    val conf = getConfigForMember()

    renderObject(Map(
      "id" -> conf.getString("id"),
      "name" -> conf.getString("name"),
      "purpose" -> conf.getString("purpose")
    ))
  }

  get("/valtuutus") {
    logger.info(s"Requesting authorizations for user: ${koskiSessionOption.getOrElse("none")}")
    requireKansalainen
    render(application.mydataService.getAllValid(koskiSessionOption.get.oid))
  }

  post("/valtuutus/:memberCode") {
    logger.info(s"Authorizing $memberCodeParam for user: ${koskiSessionOption.getOrElse("none")}")
    requireKansalainen

    val id = getConfigForMember().getString("id") // will throw if memberCode is not valid
    renderObject(Map("success" -> application.mydataService.put(koskiSessionOption.get.oid, id)(koskiSessionOption.get)))
  }

  delete("/valtuutus/:memberCode") {
    logger.info(s"Unauthorizing $memberCodeParam for user: ${koskiSessionOption.getOrElse("none")}")
    requireKansalainen
    application.mydataService.delete(koskiSessionOption.get.oid ,memberCodeParam)
  }
}
