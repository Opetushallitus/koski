package fi.oph.koski.koskiuser

import fi.oph.koski.henkilo.AuthenticationServiceClient.Palvelurooli
import fi.oph.koski.log.Logging
import fi.oph.koski.organisaatio.Opetushallitus
import fi.oph.koski.schema.OidOrganisaatio
import fi.vm.sade.security.ldap.LdapUser

object LdapKayttooikeudet extends Logging {
  val pattern = """APP_KOSKI_([A-Z_]+)_([0-9\.]+)""".r

  def käyttöoikeudet(user: LdapUser): List[Käyttöoikeus] = {
    try {
      user.roles.flatMap {
        case pattern(rooli, oid) if (oid == Opetushallitus.organisaatioOid) =>
          Some(KäyttöoikeusGlobal(List(Palvelurooli(rooli))))
        case pattern(rooli, oid) =>
          Some(KäyttöoikeusOrg(OidOrganisaatio(oid), List(Palvelurooli(rooli)), true, None))
        case _ =>
          None
      }
    } catch {
      case e: Exception =>
        logger.error(e)("Virhe määritettäessä käyttöoikeuksia")
        Nil
    }
  }

  def roleString(palvelu: String, rooli: String, oid: String) = {
    "APP_" + palvelu + "_" + rooli + "_" + oid
  }
}
