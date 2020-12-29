package cas

import org.http4s.dsl.resolve
import org.http4s.{ParseFailure, Uri}

case class CasUser(username: String, password: String)

case class CasService(securityUri:Uri)

case class CasParams(service: CasService, user: CasUser) {
  override def toString: String = service.securityUri.toString
}

object CasParams {
  def apply(service: String, securityUriSuffix: String, username: String, password: String): CasParams ={
    Uri.fromString(ensureTrailingSlash(service)).fold(
      (e: ParseFailure) => throw new IllegalArgumentException(e),
      (service: Uri) => CasParams(CasService(resolve(service, Uri.fromString(securityUriSuffix).getOrElse {
        throw new IllegalArgumentException(s"Could not parse securityUriSuffix $securityUriSuffix")
      })), CasUser(username, password)))
  }

  def apply(service: String, username: String, password: String): CasParams = apply(service = service,
    securityUriSuffix = "j_spring_cas_security_check", username = username, password = password)

  private def ensureTrailingSlash(service:String) = service.last match {
    case '/' => service
    case _ => service + "/"
  }
}
