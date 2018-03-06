package fi.oph.koski.log

import javax.servlet.http.HttpServletRequest

import fi.oph.koski.koskiuser.{UserWithOid, UserWithUsername}
import org.scalatra.servlet.RichRequest

object LogUserContext {
  def apply(request: HttpServletRequest) = new LogUserContext {
    def userOption = None
    override def clientIp = clientIpFromRequest(new RichRequest(request))
  }

  def apply(request: HttpServletRequest, userOid: String, un: String) = new LogUserContext {
    def userOption = Some(new UserWithOid with UserWithUsername {
      override def oid = userOid
      override def username = un
    })
    override def clientIp = clientIpFromRequest(new RichRequest(request))
  }

  def clientIpFromRequest(request: RichRequest): String = {
    request.headers.getOrElse("HTTP_X_FORWARDED_FOR", request.remoteAddress)
  }

  def userAgent(request: RichRequest): String = request.header("User-Agent").getOrElse("")
}

trait LogUserContext {
  def userOption: Option[UserWithOid with UserWithUsername]
  def clientIp: String
}
