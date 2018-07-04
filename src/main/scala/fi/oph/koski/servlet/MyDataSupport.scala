package fi.oph.koski.servlet

import java.net.URLEncoder

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.log.Logging
import org.scalatra.ScalatraServlet
import scala.collection.JavaConverters._
import com.typesafe.config.{Config => TypeSafeConfig}


trait MyDataSupport extends ScalatraServlet with Logging {
  def application: KoskiApplication
  private def conf: TypeSafeConfig = application.config.getConfig("mydata")

  def hasConfigForMember(id: String = memberCodeParam): Boolean = getConfigOption(id).isDefined

  def getConfigForMember(id: String = memberCodeParam): TypeSafeConfig = {
    getConfigOption(memberCodeParam).getOrElse({
      logger.warn("No MyData configuration found for member: " + id)
      throw InvalidRequestException(KoskiErrorCategory.notFound.myDataMemberEiLöydy)
    })
  }

  def findMemberForMemberCode(memberCode: String): Option[TypeSafeConfig] = {
    conf.getConfigList("members").asScala.find(member =>
      member.getStringList("membercodes").contains(memberCode))
  }

  private def getConfigOption(id: String = memberCodeParam): Option[TypeSafeConfig] = {
    conf.getConfigList("members").asScala.find(member => member.getString("id") == id)
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

