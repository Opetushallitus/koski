package fi.oph.koski.servlet

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import javax.servlet.http.HttpServletRequest

import scala.collection.JavaConverters._


trait MyDataSupport extends LanguageSupport {
  def application: KoskiApplication

  def getConfigForMember(id: String): com.typesafe.config.Config = {
    application.config.getConfigList("mydata.members").asScala.find(member =>
      member.getString("id") == id).getOrElse(throw InvalidRequestException(KoskiErrorCategory.notFound.myDataMemberEiLöydy))
  }

  def getLoginUrlForMember(id: String): String = {
    getCallbackParameter match {
      case Some(param) => getConfiguredLoginUrl(id).concat(s"?callback=${param}")
      case None => getConfiguredLoginUrl(id)
    }
  }

  private def getConfiguredLoginUrl(memberId: String): String = {
    getConfigForMember(memberId).getString("login." + langFromCookie.getOrElse(langFromDomain))
  }

  def getCallbackParameter(implicit request: HttpServletRequest): Option[String] = {
    if (request.parameters.contains("callback")) {
      Some(params("callback"))
    } else {
      None
    }
  }
}

