package fi.oph.koski.koskiuser

import fi.oph.koski.henkilo.AuthenticationServiceClient.Palvelurooli
import fi.oph.koski.log.Logging
import fi.oph.koski.organisaatio.Opetushallitus
import fi.oph.koski.schema.OidOrganisaatio
import fi.vm.sade.security.ldap.LdapUser

object LdapKayttooikeudet extends Logging {
  val pattern = """APP_([A-Z]+)_([A-Z_]+)_([0-9\.]+)""".r
  val apps = List("KOSKI", "LOKALISOINTI")

  def käyttöoikeudet(user: LdapUser): List[Käyttöoikeus] = {
    try {
      user.roles.flatMap {
        case pattern(app, rooli, oid) if apps.contains(app) && oid == Opetushallitus.organisaatioOid =>
          Some(KäyttöoikeusGlobal(List(Palvelurooli(app, rooli))))
        case pattern(app, rooli, oid) if apps.contains(app) =>
          Some(KäyttöoikeusOrg(OidOrganisaatio(oid), List(Palvelurooli(app, rooli)), true, None))
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
