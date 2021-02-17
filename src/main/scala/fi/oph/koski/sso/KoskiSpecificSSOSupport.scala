package fi.oph.koski.sso

trait KoskiSpecificSSOSupport extends SSOSupport {

  def serviceRoot: String = {
    val host = request.getServerName()
    val portStr = request.getServerPort match {
      case 80 | 443 => ""
      case port: Int => ":" + port
    }
    protocol + "://" + host + portStr + "/koski"
  }

  def localLoginPage: String = "/login"
  def localOppijaLoginPage: String = "/login/oppija/local"
}
