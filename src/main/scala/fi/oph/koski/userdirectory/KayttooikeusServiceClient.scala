package fi.oph.koski.userdirectory

import cats.effect.IO
import com.typesafe.config.Config
import fi.oph.koski.http.Http.{parseJson, _}
import fi.oph.koski.http.{Http, ServiceConfig, VirkailijaHttpClient}
import fi.oph.koski.json.Json4sHttp4s

import cats.syntax.parallel._

case class KäyttöoikeusServiceClient(config: Config) {
  private val http = VirkailijaHttpClient(makeServiceConfig(config), "/kayttooikeus-service", true)

  private def makeServiceConfig(config: Config) = ServiceConfig.apply(
    config,
    "authentication-service",
    "authentication-service.virkailija",
    "opintopolku.virkailija"
  )

  def getKäyttäjätiedot(oid: String): IO[Option[Käyttäjätiedot]] =
    http.get(uri"/kayttooikeus-service/henkilo/$oid/kayttajatiedot")(Http.parseJsonOptional[Käyttäjätiedot])

  def findKäyttöoikeusryhmät: IO[List[KäyttöoikeusRyhmä]] = {
    http.get(uri"/kayttooikeus-service/kayttooikeus/KOSKI")(parseJson[List[KäyttöoikeusRooli]])
      .flatMap { roolit =>
        roolit.parTraverse { r =>
          http.post(
            uri"/kayttooikeus-service/kayttooikeusryhma/ryhmasByKayttooikeus",
            Map("KOSKI" -> r.rooli)
          )(Json4sHttp4s.json4sEncoderOf[Map[String, String]])(Http.parseJson[List[KäyttöoikeusRyhmä]])
        }
      }.map(_.flatten)
  }

  def findKäyttöoikeusRyhmänHenkilöt(ryhmäId: Int): IO[List[String]] =
    http.get(uri"/kayttooikeus-service/kayttooikeusryhma/$ryhmäId/henkilot")(parseJson[KäyttöoikeusRyhmäHenkilöt])
      .map(_.personOids.getOrElse(List.empty))

  def findKäyttöoikeudetByUsername(username: String): IO[List[HenkilönKäyttöoikeudet]] =
    http.get(uri"/kayttooikeus-service/kayttooikeus/kayttaja?username=$username")(parseJson[List[HenkilönKäyttöoikeudet]])
}

case class KäyttöoikeusRyhmä(id: Int, nimi: KäyttöoikeusRyhmäDescriptions) {
  def fi: String = nimi.texts.find(_.lang == "FI").map(_.text).getOrElse("")
}
case class KäyttöoikeusRyhmäDescriptions(texts: List[KäyttöoikeusRyhmäDescription])
case class KäyttöoikeusRyhmäDescription(text: String, lang: String)
case class KäyttöoikeusRyhmäHenkilöt(personOids: Option[List[String]])
case class Käyttäjätiedot(username: Option[String])
case class HenkilönKäyttöoikeudet(oidHenkilo: String, organisaatiot: List[OrganisaatioJaKäyttöoikeudet])
case class OrganisaatioJaKäyttöoikeudet(organisaatioOid: String, kayttooikeudet: List[PalveluJaOikeus])
case class PalveluJaOikeus(palvelu: String, oikeus: String)
case class KäyttöoikeusRooli(rooli: String)
