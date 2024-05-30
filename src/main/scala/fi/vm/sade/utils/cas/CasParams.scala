package fi.vm.sade.utils.cas

import org.http4s.{ParseFailure, Uri}


case class CasUser(username: String, password: String)

case class CasService(securityUri: Uri)

case class CasParams(service: CasService, user: CasUser) {
  override def toString: String = service.securityUri.toString
}

object CasParams {
  def apply(servicePath: String, securityUriSuffix: String, username: String, password: String): CasParams = {
    Uri.fromString(removeTrailingSlash(ensureLeadingSlash(servicePath))).fold(
      (e: ParseFailure) => throw new IllegalArgumentException(e),
      (service: Uri) => {
        CasParams(CasService(Uri.resolve(service / "", Uri.fromString(securityUriSuffix).getOrElse {
          throw new IllegalArgumentException(s"Could not parse securityUriSuffix $securityUriSuffix")
        })), CasUser(username, password))
      }
    )
  }

  def apply(servicePath: String, username: String, password: String): CasParams = apply(
    servicePath = servicePath,
    securityUriSuffix = "j_spring_cas_security_check",
    username = username,
    password = password
  )

  private def removeTrailingSlash(servicePath: String): String = servicePath.last match {
    case '/' => servicePath.dropRight(1)
    case _ => servicePath
  }

  private def ensureLeadingSlash(servicePath: String): String = servicePath.head match {
    case '/' => servicePath
    case _ => "/" + servicePath
  }
}
