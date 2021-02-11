package fi.oph.koski.userdirectory

import cas.CasClient.TGTUrl
import com.typesafe.config.Config
import fi.oph.koski.henkilo.{KäyttäjäHenkilö, OppijanumeroRekisteriClient}
import fi.oph.koski.http.{Http, OpintopolkuCallerId}
import fi.oph.koski.koskiuser._
import fi.oph.koski.log.Logging
import fi.oph.koski.organisaatio.Opetushallitus
import fi.oph.koski.schema.OidOrganisaatio
import cas.CasClientException
import fi.oph.koski.config.Features
import fi.oph.koski.valpas.userdirectory.ValpasKäyttöoikeusRoolit

/**
  * Replacement for the LDAP-based directory client
  */
class OpintopolkuDirectoryClient(virkailijaUrl: String, config: Config) extends DirectoryClient with Logging {

  import org.http4s.Status.Created
  import org.http4s._
  import org.http4s.client._
  import org.http4s.dsl._
  import org.http4s.headers.Location
  import scalaz.concurrent.Task
  private val tgtPattern = "(.*TGT-.*)".r
  private val http = Http(virkailijaUrl, "kayttoikeuspalvelu")
  private val käyttöoikeusServiceClient = KäyttöoikeusServiceClient(config)
  private val oppijanumeroRekisteriClient = OppijanumeroRekisteriClient(config)
  private lazy val features = Features(config)

  override def findUser(userid: String): Option[DirectoryUser] =
    Http.runTask(käyttöoikeusServiceClient.findKäyttöoikeudetByUsername(userid).map {
      case List(käyttäjä) => Some(resolveKäyttöoikeudet(käyttäjä))
      case Nil => None
      case _ => throw new RuntimeException(s"More than 1 user found with username $userid")
    }).flatMap { case (oid: String, käyttöoikeudet: List[Käyttöoikeus]) => findKäyttäjä(oid, käyttöoikeudet) }

  override def authenticate(userid: String, wrappedPassword: Password): Boolean = {
    val tgtUri: TGTUrl = resolve(Uri.fromString(virkailijaUrl).toOption.get, uri("/cas/v1/tickets"))

    Http.runTask(http.client.fetch(
      POST(tgtUri, UrlForm("username" -> userid, "password" -> wrappedPassword.password))
        .putHeaders(Header("Caller-Id", OpintopolkuCallerId.koski))) {
      case Created(resp) =>
        val found: TGTUrl = resp.headers.get(Location).map(_.value) match {
          case Some(tgtPattern(tgtUrl)) =>
            Uri.fromString(tgtUrl).fold(
              (pf: ParseFailure) => throw new CasClientException(pf.message),
              (tgt) => tgt
            )
          case Some(nontgturl) =>
            throw new CasClientException(s"TGT decoding failed at ${tgtUri}: location header has wrong format $nontgturl")
          case None =>
            throw new CasClientException(s"TGT decoding failed at ${tgtUri}: No location header at")
        }
        Task.now(true)
      case Locked(resp) =>
        logger.warn(s"Access denied, username $userid is locked")
        Task.now(false)
      case r => r.as[String].map { body =>
        if (body.contains("authentication_exceptions") || body.contains("error.authentication.credentials.bad")) {
          false
        } else {
          throw new CasClientException(s"TGT decoding failed at ${tgtUri}: invalid TGT creation status: ${r.status.code}: ${body.take(200).replace('\n', ' ').replace('\r', ' ')}")
        }
      }
    })
  }

  private def resolveKäyttöoikeudet(käyttäjä: HenkilönKäyttöoikeudet) = {
    (käyttäjä.oidHenkilo,
      KoskiSpecificKäyttöoikeusRoolit.resolveKäyttäjänKäyttöoikeudet(käyttäjä.organisaatiot)
    )
  }

  private def findKäyttäjä(oid: String, käyttöoikeudet: List[Käyttöoikeus]) = {
    Http.runTask(oppijanumeroRekisteriClient.findKäyttäjäByOid(oid)).map { (käyttäjä: KäyttäjäHenkilö) =>
      DirectoryUser(käyttäjä.oidHenkilo, käyttöoikeudet, käyttäjä.etunimet, käyttäjä.sukunimi, käyttäjä.asiointiKieli.map(_.kieliKoodi))
    }
  }

}

object KoskiSpecificKäyttöoikeusRoolit {
  def resolveKäyttäjänKäyttöoikeudet(organisaatiot: List[OrganisaatioJaKäyttöoikeudet]) =
    organisaatiot.flatMap {
      case OrganisaatioJaKäyttöoikeudet(organisaatioOid, käyttöoikeudet) =>
        val roolit = käyttöoikeudet.collect { case PalveluJaOikeus(palvelu, oikeus) => Palvelurooli(palvelu, oikeus) }
        if (!roolit.map(_.palveluName).contains("KOSKI") && !roolit.map(_.palveluName).contains("VALPAS")) {
          Nil
        } else if (organisaatioOid == Opetushallitus.organisaatioOid) {
          List(KäyttöoikeusGlobal(roolit))
        } else if (hasViranomaisRooli(roolit)) {
          List(KäyttöoikeusViranomainen(roolit))
        } else {
          List(KäyttöoikeusOrg(OidOrganisaatio(organisaatioOid), roolit, juuri = true, oppilaitostyyppi = None))
        }
    }

  private def hasViranomaisRooli(roolit: List[Palvelurooli]) =
    roolit.exists(r => Rooli.globaalitKoulutusmuotoRoolit.contains(r.rooli)) ||
      roolit.map(_.rooli).contains(Rooli.TIEDONSIIRTO_LUOVUTUSPALVELU)
}
