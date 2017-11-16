package fi.oph.koski.henkilo.kayttooikeusservice

import com.typesafe.config.Config
import fi.oph.koski.henkilo.RemoteOpintopolkuHenkilöFacade
import fi.oph.koski.http.Http.{parseJson, _}
import fi.oph.koski.http.{Http, VirkailijaHttpClient}
import fi.oph.koski.koskiuser.Käyttöoikeusryhmät
import fi.oph.koski.koskiuser.Käyttöoikeusryhmät.käyttöoikeusryhmät

import scalaz.concurrent.Task
import scalaz.concurrent.Task.gatherUnordered

case class KäyttöoikeusServiceClient(config: Config) {
  private val http = VirkailijaHttpClient(RemoteOpintopolkuHenkilöFacade.makeServiceConfig(config), "/kayttooikeus-service", config.getBoolean("authentication-service.useCas"))

  def getKäyttäjätiedot(oid: String) = http.get(uri"/kayttooikeus-service/henkilo/$oid/kayttajatiedot")(Http.parseJsonOptional[Käyttäjätiedot])

  // TODO: rename
  def getKäyttöikeusRyhmät: Task[Map[String, List[String]]] =
    findKäyttöoikeusryhmät.flatMap { ryhmät =>
      gatherUnordered(ryhmät
        .filter(_.description.texts.exists(t => t.lang == "FI" && käyttöoikeusryhmät.map(_.nimi).contains(t.text)))
        .map { ryhmä =>
          http.get(uri"/kayttooikeus-service/kayttooikeusryhma/${ryhmä.id}/henkilot")(parseJson[KäyttöoikeusRyhmäHenkilöt]).map(h => (ryhmä.nimi, h.personOids))
        }
      )
    }.map(_.toMap)

  def findKäyttöoikeusryhmät = http.get(uri"/kayttooikeus-service/kayttooikeusryhma")(parseJson[List[KäyttöoikeusRyhmä]])
}

case class KäyttöoikeusRyhmä(id: Int, name: String, description: KäyttöoikeusRyhmäDescriptions) {
  def nimi = description.texts.find(_.lang == "FI").map(_.text).getOrElse("")
  def toKoskiKäyttöoikeusryhmä = {
    val name: String = this.name.replaceAll("_.*", "")
    Käyttöoikeusryhmät.byName(name)
  }

}
case class KäyttöoikeusRyhmäDescriptions(texts: List[KäyttöoikeusRyhmäDescription])
case class KäyttöoikeusRyhmäDescription(text: String, lang: String)
case class KäyttöoikeusRyhmäHenkilöt(personOids: List[String])
case class Käyttäjätiedot(username: Option[String])