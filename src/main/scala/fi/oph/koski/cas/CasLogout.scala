package fi.oph.koski.cas

import scala.xml.{Utility, XML}

object CasLogout {
  def parseTicketFromLogoutRequest(logoutRequest: String): Option[String] = {
    Utility.trim(XML.loadString(logoutRequest)) match {
      case <samlp:LogoutRequest><saml:NameID>{nameID}</saml:NameID><samlp:SessionIndex>{ticket}</samlp:SessionIndex></samlp:LogoutRequest> =>
        Some(ticket.text)
      case _ => None
    }
  }
}

