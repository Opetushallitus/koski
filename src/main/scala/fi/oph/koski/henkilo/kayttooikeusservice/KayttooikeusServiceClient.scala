package fi.oph.koski.henkilo.kayttooikeusservice

import com.typesafe.config.Config
import fi.oph.koski.henkilo.RemoteOpintopolkuHenkilöFacade
import fi.oph.koski.http.Http.{parseJson, _}
import fi.oph.koski.http.{Http, VirkailijaHttpClient}

case class KäyttöoikeusServiceClient(config: Config) {
  private val http = VirkailijaHttpClient(RemoteOpintopolkuHenkilöFacade.makeServiceConfig(config), "/kayttooikeus-service", config.getBoolean("authentication-service.useCas"))

  def getKäyttäjätiedot(oid: String) = http.get(uri"/kayttooikeus-service/henkilo/$oid/kayttajatiedot")(Http.parseJsonOptional[Käyttäjätiedot])

  def findKäyttöoikeusryhmät = http.get(uri"/kayttooikeus-service/kayttooikeusryhma")(parseJson[List[KäyttöoikeusRyhmä]])

  def findKäyttöoikeusRyhmänHenkilöt(ryhmäId: Int) = http.get(uri"/kayttooikeus-service/kayttooikeusryhma/${ryhmäId}/henkilot")(parseJson[KäyttöoikeusRyhmäHenkilöt]).map(_.personOids)

  def findKäyttöoikeudetByUsername(username: String) = http.get(uri"/kayttooikeus-service/kayttooikeus/kayttaja?username=$username")(parseJson[List[HenkilönKäyttöoikeudet]])
}

case class KäyttöoikeusRyhmä(id: Int, name: String, description: KäyttöoikeusRyhmäDescriptions) {
  def nimi = description.texts.find(_.lang == "FI").map(_.text).getOrElse("")
}
case class KäyttöoikeusRyhmäDescriptions(texts: List[KäyttöoikeusRyhmäDescription])
case class KäyttöoikeusRyhmäDescription(text: String, lang: String)
case class KäyttöoikeusRyhmäHenkilöt(personOids: List[String])
case class Käyttäjätiedot(username: Option[String])
case class HenkilönKäyttöoikeudet(oidHenkilo: String, organisaatiot: List[OrganisaatioJaKäyttöoikeudet])
case class OrganisaatioJaKäyttöoikeudet(organisaatioOid: String, kayttooikeudet: List[PalveluJaOikeus])
case class PalveluJaOikeus(palvelu: String, oikeus: String)
