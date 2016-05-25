package fi.oph.koski.log

import javax.servlet.http.HttpServletRequest

import org.scalatra.servlet.RichRequest

object LogUserContext {
  def apply(request: HttpServletRequest) = new LogUserContext {
    def oidOption = None
    override def clientIp = clientIpFromRequest(request)
  }

  def apply(request: HttpServletRequest, oid: String) = new LogUserContext {
    def oidOption = Some(oid)
    override def clientIp = clientIpFromRequest(request)
  }

  def clientIpFromRequest(request: HttpServletRequest): String = {
    RichRequest(request).headers.getOrElse("HTTP_X_FORWARDED_FOR", RichRequest(request).remoteAddress)
  }
}

trait LogUserContext {
  def oidOption: Option[String]
  def clientIp: String
}