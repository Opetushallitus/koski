package fi.oph.koski.henkilo.kayttooikeusservice

import com.typesafe.config.Config
import fi.oph.koski.henkilo.RemoteOpintopolkuHenkilöFacade
import fi.oph.koski.http.Http.{parseJson, _}
import fi.oph.koski.http.{Http, VirkailijaHttpClient}
import fi.oph.koski.json.Json4sHttp4s
import scalaz.concurrent.Task
import scalaz.concurrent.Task.gatherUnordered

case class KäyttöoikeusServiceClient(config: Config) {
  private val http = VirkailijaHttpClient(RemoteOpintopolkuHenkilöFacade.makeServiceConfig(config), "/kayttooikeus-service", config.getBoolean("authentication-service.useCas"))

  def getKäyttäjätiedot(oid: String): Task[Option[Käyttäjätiedot]] = http.get(uri"/kayttooikeus-service/henkilo/$oid/kayttajatiedot")(Http.parseJsonOptional[Käyttäjätiedot])

  def findKäyttöoikeusryhmät: Task[List[KäyttöoikeusRyhmä]] = http.get(uri"/kayttooikeus-service/kayttooikeus/KOSKI")(parseJson[List[KäyttöoikeusRooli]]).flatMap { roolit =>
    gatherUnordered(roolit.map(r => http.post(uri"/kayttooikeus-service/kayttooikeusryhma/ryhmasByKayttooikeus", Map("KOSKI" -> r.rooli))(Json4sHttp4s.json4sEncoderOf[Map[String, String]])(Http.parseJson[List[KäyttöoikeusRyhmä]])))
  }.map(_.flatten)

  def findKäyttöoikeusRyhmänHenkilöt(ryhmäId: Int): Task[List[String]] = http.get(uri"/kayttooikeus-service/kayttooikeusryhma/$ryhmäId/henkilot")(parseJson[KäyttöoikeusRyhmäHenkilöt]).map(_.personOids.getOrElse(List.empty))

  def findKäyttöoikeudetByUsername(username: String): Task[List[HenkilönKäyttöoikeudet]] = http.get(uri"/kayttooikeus-service/kayttooikeus/kayttaja?username=$username")(parseJson[List[HenkilönKäyttöoikeudet]])
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
