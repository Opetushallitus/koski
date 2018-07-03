package fi.oph.koski.servlet

import java.net.URLEncoder

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import javax.servlet.http.HttpServletRequest
import org.scalatra.ScalatraServlet

import scala.collection.JavaConverters._


trait MyDataSupport extends ScalatraServlet {
  def application: KoskiApplication

  def getConfigForMember(id: String =  memberCode): com.typesafe.config.Config = {
    application.config.getConfigList("mydata.members").asScala.find(member =>
      member.getString("id") == id).getOrElse(throw InvalidRequestException(KoskiErrorCategory.notFound.myDataMemberEiLöydy))
  }

  def getLoginUrlForMember(lang: String, memberId: String = memberCode): String = {
    application.config.getString(s"mydata.login.shibboleth.$lang") +
    application.config.getString("mydata.login.targetparam") + getLoginSuccessTarget(memberId, encode = true)
  }

  def getLoginSuccessTarget(memberId: String = memberCode, encode: Boolean = false): String = {
    getConfigForMember(memberId).getString(s"login.target") + getCurrentUrlAsFinalTargetParameter(encode)
  }

  private def getCurrentUrlAsFinalTargetParameter(encode: Boolean): String = {
    if (encode) {
      URLEncoder.encode(s"?onLoginSuccess=$getCurrentURL" , "UTF-8")
    } else {
      s"?onLoginSuccess=$getCurrentURL"
    }
  }

  def getCurrentURL(implicit httpServletRequest: HttpServletRequest): String = {
    if (httpServletRequest.queryString.isEmpty) {
      httpServletRequest.getRequestURI
    } else {
      httpServletRequest.getRequestURI + s"?${httpServletRequest.queryString}"
    }
  }

  def memberCode: String = {
    if (params("memberCode") == null) {
      throw InvalidRequestException(KoskiErrorCategory.badRequest.queryParam.missing("Vaadittu valtuutuksen kumppani-parametri puuttuu"))
    }

    params("memberCode")
  }
}

