package fi.oph.koski.sso

trait KoskiSpecificSSOSupport extends SSOSupport {

  def serviceRoot: String = {
    val host = request.getServerName()
    val portStr = request.getServerPort match {
      case 80 | 443 => ""
      case port: Int => ":" + port
    }
    protocol + "://" + host + portStr
  }

  def localLoginPage: String = "/koski/login"
  def localOppijaLoginPage: String = "/koski/login/oppija/local"
}
