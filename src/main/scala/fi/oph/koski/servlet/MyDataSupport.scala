package fi.oph.koski.servlet

import java.net.URLEncoder

import fi.oph.koski.http.KoskiErrorCategory
import org.scalatra.ScalatraServlet
import com.typesafe.config.{Config => TypeSafeConfig}
import fi.oph.koski.mydata.MyDataConfig


trait MyDataSupport extends ScalatraServlet with MyDataConfig {
  override def hasConfigForMember(id: String = memberCodeParam): Boolean
  override def getConfigForMember(id: String = memberCodeParam): TypeSafeConfig
  def mydataLoginServlet: String = conf.getString("login.servlet")

  def loginWithTarget(target: String = getCurrentURL, encode: Boolean = false): String = {
    if (encode) {
      s"${mydataLoginServlet}${URLEncoder.encode(s"?onLoginSuccess=${target}", "UTF-8")}"
    } else {
      s"${mydataLoginServlet}?onLoginSuccess=${target}"
    }
  }

  def shibbolethLoginWithTarget(target: String = getCurrentURL, lang: String) = {
    conf.getString(s"login.shibboleth.$lang") +
      conf.getString("login.shibboleth.targetparam") + loginWithTarget(target, encode = true)
  }

  def getLoginUrlForMember(lang: String, id: String = memberCodeParam): String = {
    conf.getString(s"login.shibboleth.$lang") +
    conf.getString("login.targetparam") + getLoginSuccessTarget(id, encode = true)
  }

  def getLoginSuccessTarget(id: String = memberCodeParam, encode: Boolean = false): String = {
    getConfigForMember(id).getString(s"login.target") + getCurrentUrlAsFinalTargetParameter(encode)
  }

  private def getCurrentUrlAsFinalTargetParameter(encode: Boolean): String = {
    if (encode) {
      URLEncoder.encode(s"?onLoginSuccess=$getCurrentURL" , "UTF-8")
    } else {
      s"?onLoginSuccess=$getCurrentURL"
    }
  }

  def getCurrentURL: String = {
    if (request.queryString.isEmpty) {
      request.getRequestURI
    } else {
      request.getRequestURI + s"?${request.queryString}"
    }
  }

  def memberCodeParam: String = {
    if (params("memberCode") == null) {
      throw InvalidRequestException(KoskiErrorCategory.badRequest.queryParam.missing("Vaadittu valtuutuksen kumppani-parametri puuttuu"))
    }

    params("memberCode")
  }
}

