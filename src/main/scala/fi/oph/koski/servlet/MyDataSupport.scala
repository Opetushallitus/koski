package fi.oph.koski.servlet

import java.net.URLEncoder

import fi.oph.koski.http.KoskiErrorCategory
import org.scalatra.ScalatraServlet
import com.typesafe.config.{Config => TypeSafeConfig}
import fi.oph.koski.mydata.MyDataConfig


trait MyDataSupport extends ScalatraServlet with MyDataConfig {
  override def hasConfigForMember(id: String = memberCodeParam): Boolean
  override def getConfigForMember(id: String = memberCodeParam): TypeSafeConfig
  def mydataLoginServletURL: String = conf.getString("login.servlet")

  def getLoginURL(target: String = getCurrentURL, encode: Boolean = false): String = {
    if (encode) {
      URLEncoder.encode(s"${mydataLoginServletURL}?onSuccess=${target}", "UTF-8")
    } else {
      s"${mydataLoginServletURL}?onSuccess=${target}"
    }
  }

  def getShibbolethLoginURL(target: String = getCurrentURL, lang: String) = {
    conf.getString(s"login.shibboleth.$lang") +
      conf.getString("login.shibboleth.targetparam") + getLoginURL(target, encode = true)
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

