package fi.oph.koski.mydata

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.AuthenticationSupport
import fi.oph.koski.log.Logging
import fi.oph.koski.servlet._

class MyDataServlet(implicit val application: KoskiApplication) extends ApiServlet
  with AuthenticationSupport with Logging with NoCache with MyDataSupport with LanguageSupport {

  get("/kumppani/:memberCode") {
    def memberConf = getConfigForMember()

    val lang = s"name.${langFromCookie.getOrElse(langFromDomain)}"
    val memberName = if (memberConf.hasPath(lang)) memberConf.getString(lang) else memberConf.getString("name.fi")

    renderObject(Map(
      "id" -> memberConf.getString("id"),
      "name" -> memberName
    ))
  }

  get("/valtuutus") {
    logger.info(s"Requesting authorizations for user: ${koskiSessionOption.getOrElse()}")
    requireKansalainen
    render(application.mydataService.getAllValid(koskiSessionOption.get.oid, lang))
  }

  post("/valtuutus/:memberCode") {
    logger.info(s"Authorizing $memberCodeParam for user: ${koskiSessionOption.getOrElse()}")
    requireKansalainen

    val id = getConfigForMember().getString("id") // will throw if memberCode is not valid
    renderObject(Map("success" -> application.mydataService.put(koskiSessionOption.get.oid, id)(koskiSessionOption.get)))
  }

  delete("/valtuutus/:memberCode") {
    logger.info(s"Unauthorizing $memberCodeParam for user: ${koskiSessionOption.getOrElse()}")
    requireKansalainen
    application.mydataService.delete(koskiSessionOption.get.oid ,memberCodeParam)
  }
}
