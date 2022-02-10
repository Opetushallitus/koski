package fi.oph.koski.userdirectory

import com.typesafe.config.Config
import fi.oph.koski.cache.{CacheManager, Cached, CachingProxy, ExpiringCache}
import fi.oph.koski.koskiuser.{Käyttöoikeus, KäyttöoikeusGlobal, KäyttöoikeusOrg, KäyttöoikeusViranomainen, Palvelurooli, Rooli}
import fi.oph.koski.log.NotLoggable
import fi.oph.koski.organisaatio.Opetushallitus
import fi.oph.koski.schema.OidOrganisaatio
import fi.oph.koski.sso.CasService

import scala.concurrent.duration.DurationInt

case class Password(password: String) extends NotLoggable

trait DirectoryClient {
  def findUser(username: String): Option[DirectoryUser]
  def authenticate(userid: String, wrappedPassword: Password): Boolean
}

object DirectoryClient {
  def apply(config: Config, casService: CasService)(implicit cacheInvalidator: CacheManager): DirectoryClient with Cached = {
    val cacheStrategy = ExpiringCache("DirectoryClient", 60.seconds, maxSize = 100)
    CachingProxy[DirectoryClient](cacheStrategy, config.getString("opintopolku.virkailija.url") match {
      case "mock" => new MockDirectoryClient()
      case _ => new OpintopolkuDirectoryClient(config, casService)
    })
  }

  def resolveKäyttöoikeudet(käyttäjä: HenkilönKäyttöoikeudet): (String, List[Käyttöoikeus]) =
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

  private def hasViranomaisRooli(roolit: List[Palvelurooli]) =
    roolit.exists(r => Rooli.globaalitKoulutusmuotoRoolit.contains(r.rooli)) ||
      roolit.map(_.rooli).contains(Rooli.TIEDONSIIRTO_LUOVUTUSPALVELU)

}

case class DirectoryUser(oid: String, käyttöoikeudet: List[Käyttöoikeus], etunimet: String, sukunimi: String, asiointikieli: Option[String])








