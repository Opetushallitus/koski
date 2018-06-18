package fi.oph.koski.mydata

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.AuthenticationSupport
import fi.oph.koski.log.Logging
import fi.oph.koski.servlet.{ApiServlet, InvalidRequestException, MyDataSupport, NoCache}

import scala.collection.JavaConverters._

class MyDataServlet(implicit val application: KoskiApplication) extends ApiServlet
  with AuthenticationSupport with Logging with NoCache with MyDataSupport {

  get("/kumppani/:memberCode") {
    def memberConf = getConfigForMember(params("memberCode"))

    renderObject(Map(
      "id" -> memberConf.getString("id"),
      "name" -> memberConf.getString("name")
    ))
  }

  get("/valtuutus") {
    logger.info(s"Requesting authorizations for user: ${koskiSessionOption.getOrElse()}")
    requireKansalainen
    render(application.mydataService.getAllValid(koskiSessionOption.get.oid))
  }

  post("/valtuutus/:memberCode") {
    def memberCode = params("memberCode")

    if (memberCode == null) throw InvalidRequestException(KoskiErrorCategory.badRequest.queryParam.missingXRoadMemberId)

    logger.info(s"Authorizing ${memberCode} for user: ${koskiSessionOption.getOrElse()}")

    requireKansalainen

    def isValidCode = application.config.getConfigList("mydata.members").asScala.exists(member =>
      member.getString("id") == memberCode)

    if (isValidCode) {
      renderObject(Map("success" -> application.mydataService.put(koskiSessionOption.get.oid, memberCode)(koskiSessionOption.get)))
    } else {
      throw InvalidRequestException(KoskiErrorCategory.badRequest.queryParam.invalidXRoadMemberId)
    }
  }
}
