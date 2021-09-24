package fi.oph.koski.userdirectory

import com.typesafe.config.Config
import fi.oph.koski.henkilo.{KäyttäjäHenkilö, OppijanumeroRekisteriClient}
import fi.oph.koski.http.Http
import fi.oph.koski.koskiuser._
import fi.oph.koski.log.Logging
import fi.oph.koski.organisaatio.Opetushallitus
import fi.oph.koski.schema.OidOrganisaatio
import fi.oph.koski.sso.CasService

/**
  * Replacement for the LDAP-based directory client
  */
class OpintopolkuDirectoryClient(config: Config, casService: CasService) extends DirectoryClient with Logging {
  private val käyttöoikeusServiceClient = KäyttöoikeusServiceClient(config)
  private val oppijanumeroRekisteriClient = OppijanumeroRekisteriClient(config)

  override def findUser(userid: String): Option[DirectoryUser] =
    Http.runIO(käyttöoikeusServiceClient.findKäyttöoikeudetByUsername(userid).map {
      case List(käyttäjä) => Some(resolveKäyttöoikeudet(käyttäjä))
      case Nil => None
      case _ => throw new RuntimeException(s"More than 1 user found with username $userid")
    }).flatMap { case (oid: String, käyttöoikeudet: List[Käyttöoikeus]) => findKäyttäjä(oid, käyttöoikeudet) }

  override def authenticate(userid: String, wrappedPassword: Password): Boolean =
    casService.authenticateVirkailija(userid, wrappedPassword)

  private def resolveKäyttöoikeudet(käyttäjä: HenkilönKäyttöoikeudet) =
    (käyttäjä.oidHenkilo, käyttäjä.organisaatiot.flatMap {
      case OrganisaatioJaKäyttöoikeudet(organisaatioOid, käyttöoikeudet) =>
        val roolit = käyttöoikeudet.collect { case PalveluJaOikeus(palvelu, oikeus) => Palvelurooli(palvelu, oikeus) }
        if (!roolit.map(_.palveluName).exists(List("KOSKI", "VALPAS").contains)) {
          Nil
        } else if (organisaatioOid == Opetushallitus.organisaatioOid) {
          List(KäyttöoikeusGlobal(roolit))
        } else if (hasViranomaisRooli(roolit)) {
          List(KäyttöoikeusViranomainen(roolit))
        } else {
          List(KäyttöoikeusOrg(OidOrganisaatio(organisaatioOid), roolit, juuri = true, oppilaitostyyppi = None))
        }
    })

  private def findKäyttäjä(oid: String, käyttöoikeudet: List[Käyttöoikeus]) = {
    Http.runIO(oppijanumeroRekisteriClient.findKäyttäjäByOid(oid)).map { (käyttäjä: KäyttäjäHenkilö) =>
      DirectoryUser(käyttäjä.oidHenkilo, käyttöoikeudet, käyttäjä.etunimet, käyttäjä.sukunimi, käyttäjä.asiointiKieli.map(_.kieliKoodi))
    }
  }

  private def hasViranomaisRooli(roolit: List[Palvelurooli]) =
    roolit.exists(r => Rooli.globaalitKoulutusmuotoRoolit.contains(r.rooli)) ||
    roolit.map(_.rooli).contains(Rooli.TIEDONSIIRTO_LUOVUTUSPALVELU)
}
