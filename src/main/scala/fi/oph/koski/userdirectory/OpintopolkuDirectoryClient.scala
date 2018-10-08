package fi.oph.koski.userdirectory

import com.typesafe.config.Config
import fi.oph.koski.henkilo.{KäyttäjäHenkilö, OppijanumeroRekisteriClient}
import fi.oph.koski.http.Http
import fi.oph.koski.koskiuser.{Rooli, Palvelurooli, Käyttöoikeus, KäyttöoikeusOrg, KäyttöoikeusGlobal, KäyttöoikeusGlobalByKoulutusmuoto}
import fi.oph.koski.organisaatio.Opetushallitus
import fi.oph.koski.schema.OidOrganisaatio
import fi.vm.sade.utils.cas.CasClientException

/**
  * Replacement for the LDAP-based directory client
  */
class OpintopolkuDirectoryClient(virkailijaUrl: String, config: Config) extends DirectoryClient {
  import fi.vm.sade.utils.cas.CasClient._
  import org.http4s.Status.Created
  import org.http4s._
  import org.http4s.client._
  import org.http4s.dsl._
  import org.http4s.headers.Location

  import scalaz.concurrent.Task
  private val tgtPattern = "(.*TGT-.*)".r
  private val http = Http(virkailijaUrl)
  private val client = Http.newClient
  private val käyttöoikeusServiceClient = KäyttöoikeusServiceClient(config)
  private val oppijanumeroRekisteriClient = OppijanumeroRekisteriClient(config)

  override def findUser(userid: String): Option[DirectoryUser] = {
    Http.runTask(käyttöoikeusServiceClient.findKäyttöoikeudetByUsername(userid).map {
      case List(käyttäjä) =>
        Some(käyttäjä.oidHenkilo, käyttäjä.organisaatiot.map {
          case OrganisaatioJaKäyttöoikeudet(organisatioOid, käyttöoikeudet) =>
            val roolit = käyttöoikeudet.map { case PalveluJaOikeus(palvelu, oikeus) => Palvelurooli(palvelu, oikeus)}
            organisatioOid match {
              case Opetushallitus.organisaatioOid => KäyttöoikeusGlobal(roolit)
              case _ => if (hasGlobalKoulutusmuotoRoles(roolit)) {
                KäyttöoikeusGlobalByKoulutusmuoto(roolit)
              } else {
                KäyttöoikeusOrg(OidOrganisaatio(organisatioOid), roolit, true, None)
              }
            }
        })
      case Nil =>
        None
      case _ => throw new RuntimeException(s"More than 1 user found with username $userid")
    }).flatMap {
      case (oid: String, käyttöoikeudet: List[Käyttöoikeus]) =>
        Http.runTask(oppijanumeroRekisteriClient.findKäyttäjäByOid(oid)).map { (käyttäjä: KäyttäjäHenkilö) =>
          DirectoryUser(käyttäjä.oidHenkilo, käyttöoikeudet, käyttäjä.etunimet, käyttäjä.sukunimi, käyttäjä.asiointiKieli.map(_.kieliKoodi))
        }
    }
  }

  private def hasGlobalKoulutusmuotoRoles(roolit: List[Palvelurooli]) =
    roolit.map(_.rooli).exists(Rooli.globaalitKoulutusmuotoRoolit.contains)

  override def authenticate(userid: String, wrappedPassword: Password): Boolean = {
    val tgtUri: TGTUrl = resolve(Uri.fromString(virkailijaUrl).toOption.get, uri("/cas/v1/tickets"))
    Http.runTask(client.fetch(POST(tgtUri, UrlForm("username" -> userid, "password" -> wrappedPassword.password))) {
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
            throw new CasClientException("TGT decoding failed at ${tgtUri}: No location header at")
        }
        Task.now(true)
      case r => r.as[String].map { body =>
        if (body.contains("authentication_exceptions") || body.contains("error.authentication.credentials.bad") ) {
          false
        } else {
          throw new CasClientException(s"TGT decoding failed at ${tgtUri}: invalid TGT creation status: ${r.status.code}: ${body.take(200).replace('\n', ' ').replace('\r', ' ')}")
        }
      }
    })
  }
}
