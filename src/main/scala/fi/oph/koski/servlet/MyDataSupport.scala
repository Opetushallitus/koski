package fi.oph.koski.servlet

import java.net.URLEncoder

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

  def getLoginUrlForMember(memberId: String): String = {
    getConfigForMember(memberId).getString(s"login.${lan}.shibboleth") +
    "?" + getConfigForMember(memberId).getString("login.targetparam") + "=" + getLoginSuccessTarget(memberId, encode = true)
  }

  def getLoginSuccessTarget(memberId: String, encode: Boolean = false ): String = {
    getConfigForMember(memberId).getString(s"login.${lan}.target") + getCurrentUrlAsFinalTargetParameter(encode)
  }

  private def getCurrentUrlAsFinalTargetParameter(encode: Boolean): String = {
    if (encode) {
      URLEncoder.encode(s"?onLoginSuccess=${getCurrentURL}" , "UTF-8")
    } else {
      s"?onLoginSuccess=${getCurrentURL}"
    }
  }

  def getCurrentURL(implicit httpServletRequest: HttpServletRequest): String = {
    if (httpServletRequest.queryString.isEmpty) {
      httpServletRequest.getRequestURI
    } else {
      httpServletRequest.getRequestURI + s"?${httpServletRequest.queryString}"
    }
  }

  def lan(): String = {
    langFromCookie.getOrElse(langFromDomain)
  }
}

