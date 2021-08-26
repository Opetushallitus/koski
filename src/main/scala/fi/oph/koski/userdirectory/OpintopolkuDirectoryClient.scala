package fi.oph.koski.userdirectory

import cats.effect.IO
import com.typesafe.config.Config
import fi.oph.koski.cas.CasClient.TGTUrl
import fi.oph.koski.cas.{FetchHelper, CasClientException}
import fi.oph.koski.henkilo.{KäyttäjäHenkilö, OppijanumeroRekisteriClient}
import fi.oph.koski.http.{Http, OpintopolkuCallerId}
import fi.oph.koski.koskiuser._
import fi.oph.koski.log.Logging
import fi.oph.koski.organisaatio.Opetushallitus
import fi.oph.koski.schema.OidOrganisaatio
import org.http4s._
import org.typelevel.ci.CIString
import org.http4s.Status.{Created, Locked}

/**
  * Replacement for the LDAP-based directory client
  */
class OpintopolkuDirectoryClient(virkailijaUrl: String, config: Config) extends DirectoryClient with Logging {

  private val tgtPattern = "(.*TGT-.*)".r
  private val http = Http(virkailijaUrl, "kayttoikeuspalvelu")
  private val käyttöoikeusServiceClient = KäyttöoikeusServiceClient(config)
  private val oppijanumeroRekisteriClient = OppijanumeroRekisteriClient(config)

  override def findUser(userid: String): Option[DirectoryUser] =
    Http.runIO(käyttöoikeusServiceClient.findKäyttöoikeudetByUsername(userid).map {
      case List(käyttäjä) => Some(resolveKäyttöoikeudet(käyttäjä))
      case Nil => None
      case _ => throw new RuntimeException(s"More than 1 user found with username $userid")
    }).flatMap { case (oid: String, käyttöoikeudet: List[Käyttöoikeus]) => findKäyttäjä(oid, käyttöoikeudet) }

  // TODO: Miksi täällä on copypaste CAS-toteutus? Pystyisikö hyödyntämään scala-cas:n validateServiceTicketWithVirkailijaUsername
  override def authenticate(userid: String, wrappedPassword: Password): Boolean = {
    val tgtUri: TGTUrl = Uri.fromString(virkailijaUrl).toOption.get.withPath(Uri.Path.unsafeFromString("/cas/v1/tickets"))
    val urlForm = UrlForm("username" -> userid, "password" -> wrappedPassword.password)

    val request = FetchHelper.addDefaultHeaders(
      Request[IO](
        Method.POST,
        tgtUri
      ).withEntity(urlForm)(UrlForm.entityEncoder),
      OpintopolkuCallerId.koski
    )

    Http.runIO(http.client.run(request).use {
      case Created(resp) =>
        val locationHeader = resp.headers.get(CIString("Location")).map(_.head)
        val found: TGTUrl = locationHeader.map(_.value) match {
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
        IO.pure(true)
      case Locked(resp) =>
        logger.warn(s"Access denied, username $userid is locked")
        IO.pure(false)
      case resp: Response[IO] => resp.as[String].map { body =>
        if (body.contains("authentication_exceptions") || body.contains("error.authentication.credentials.bad")) {
          false
        } else {
          throw new CasClientException(s"TGT decoding failed at ${tgtUri}: invalid TGT creation status: ${resp.status.code}: ${body.take(200).replace('\n', ' ').replace('\r', ' ')}")
        }
      }
    })
  }

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
