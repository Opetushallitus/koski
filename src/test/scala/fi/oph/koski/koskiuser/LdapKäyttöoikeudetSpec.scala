package fi.oph.koski.koskiuser

import fi.oph.koski.henkilo.AuthenticationServiceClient.Palvelurooli
import fi.oph.koski.koskiuser.Rooli._
import fi.oph.koski.schema.OidOrganisaatio
import fi.vm.sade.security.ldap.LdapUser
import org.scalatest.{FreeSpec, Matchers}

class LdapKäyttöoikeudetSpec extends FreeSpec with Matchers {
  "LDAP-käyttöoikeudet" - {
    "Parsitaan" in {
      LdapKayttooikeudet.käyttöoikeudet(LdapUser(List(
        "APP_KOSKI_READ_1.2.246.562.10.346830761110",
        "APP_KOSKI_OPHKATSELIJA_1.2.246.562.10.00000000001"
      ), "testi", "testi", "0")) should equal(List(
        KäyttöoikeusOrg(OidOrganisaatio("1.2.246.562.10.346830761110"), List(Palvelurooli("KOSKI", READ)), true, None),
        KäyttöoikeusGlobal(List(Palvelurooli("KOSKI", OPHKATSELIJA)))
      ))
    }
    "Vain Koski-applikaation oikeudet huomioidaan" in {
      LdapKayttooikeudet.käyttöoikeudet(LdapUser(List(
        "APP_LOL_READ_1.2.246.562.10.346830761110"
      ), "testi", "testi", "0")) should equal(Nil)
    }
  }
}
