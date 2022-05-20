package fi.oph.koski.valpas.sso

import fi.oph.koski.sso.SSOSupport

trait ValpasSSOSupport extends SSOSupport {

  def serviceRoot: String = {
    val host = request.getServerName()
    val portStr = request.getServerPort match {
      case 80 | 443 => ""
      // Lokaalissa kehitysmoodissa ollaan cas-kontekstissa olevinaan portissa 7021, josta proxy ohjaa oikeaan porttiin
      case port: Int if host == "localhost" => ":7021"
      case port: Int => ":" + port
    }
    protocol + "://" + host + portStr
  }

  def valpasRoot = serviceRoot + "/valpas"

  def casValpasOppijaServiceUrl: String = serviceRoot + "/koski/cas/valpas/oppija"

  def localLoginPage: String = "/valpas/virkailija/"
  def localOppijaLoginPage: String = "/valpas/login/"
}
