package fi.oph.koski.sso

import fi.oph.koski.integrationtest.KoskidevHttpSpecification
import fi.oph.koski.xml.NodeSeqImplicits._

object CasInitiatedLogoutTester extends App with KoskidevHttpSpecification {
  val ticket = "ST-346468-mGdQkPeZ7pWbUZJX429T-cas.tordev-authentication-app"
  post("cas", params = List("logoutRequest" -> <samlp:LogoutRequest><saml:NameID>wat</saml:NameID><samlp:SessionIndex>{ticket}</samlp:SessionIndex></samlp:LogoutRequest>.toString ), headers = authHeaders()) {

  }
}
