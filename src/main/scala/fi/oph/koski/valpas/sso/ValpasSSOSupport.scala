package fi.oph.koski.valpas.sso

import fi.oph.koski.sso.SSOSupport

trait ValpasSSOSupport extends SSOSupport {

  def serviceRoot: String = {
    val host = request.getServerName()
    val portStr = request.getServerPort match {
      case 80 | 443 => ""
      case port: Int => ":" + port
    }
    protocol + "://" + host + portStr + "/valpas"
  }

  def localLoginPage: String = "http://localhost:1234/valpas/"
  def localOppijaLoginPage: String = "http://localhost:1234/valpas/oppija/"
}
