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
    getConfigForMember(memberId).getString("login.fi.shibboleth") +
    "?login=" + getConfigForMember(memberId).getString("login.fi.shibbolethCallback") +
    URLEncoder.encode(s"?onLoginSuccess=${getCurrentURL}" , "UTF-8")
  }

  def getCurrentURL(implicit httpServletRequest: HttpServletRequest): String = { // this will return /koski/omadata/hsl, old one did not have 'koski'
    if (httpServletRequest.queryString.isEmpty) {
      httpServletRequest.getRequestURI
    } else {
      httpServletRequest.getRequestURI + s"?${httpServletRequest.queryString}"
    }
  }
}

