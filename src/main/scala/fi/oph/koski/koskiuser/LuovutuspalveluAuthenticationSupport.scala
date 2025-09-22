package fi.oph.koski.koskiuser

import fi.oph.koski.http.{ErrorDetail, HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import org.scalatra.auth.strategy.BasicAuthStrategy

trait LuovutuspalveluAuthenticationSupport extends AuthenticationSupport {

  // TODO fetch client list from parameter store
  val clientListJson = """[{"subjectDn": "CN=oa2-dev.koski-luovutuspalvelu-test-certs.testiopintopolku.fi", "ips": ["0.0.0.0/0"],  "user": "koskioauth2sampledevpk"}, {"subjectDn":"CN=migri", "ips":[], "user": "Migri"}]"""

  val clientList = JsonSerializer.parse[List[LuovutuspalveluClient]](clientListJson)

  override def authenticateUser: Either[HttpStatus, AuthenticationUser] = {

    val subjectDnHeader = request.header("x-amzn-mtls-clientcert-subject")
    val albAuthUser = subjectDnHeader.flatMap(header => clientList.find(_.subjectDn == header))
      .flatMap(client => DirectoryClientLogin.findUser(application.directoryClient, request, client.user))

    albAuthUser match {
      case Some(user) => Right(user) // TODO validate suorce ip
      case None => userFromBasicAuth
    }

  }

  private def userFromBasicAuth: Either[HttpStatus, AuthenticationUser] = {
    val basicAuthRequest = new BasicAuthStrategy.BasicAuthRequest(request)
    if (basicAuthRequest.isBasicAuth && basicAuthRequest.providesAuth) {
      tryLogin(basicAuthRequest.username, basicAuthRequest.password)
    } else {
      Left(KoskiErrorCategory.unauthorized.notAuthenticated())
    }
  }


}

case class LuovutuspalveluClient(subjectDn: String, ips: List[String], user: String, xroadSecurityServer: Option[Boolean])

