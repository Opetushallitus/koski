package fi.oph.koski.eperusteet

import fi.oph.koski.json.Json
import fi.oph.koski.schema.JsonSerializer

object MockEPerusteetRepository extends EPerusteetRepository {
  lazy val rakenteet: List[EPerusteRakenne] = List(
    "rakenne-autoalan-perustutkinto",
    "rakenne-luonto-ja-ymparistoala",
    "rakenne-autoalan-tyonjohto",
    "rakenne-perusopetus",
    "rakenne-aikuisten-perusopetus2017",
    "rakenne-lukio",
    "rakenne-hiusalan-perustutkinto",
    "rakenne-puutarhatalouden-perustutkinto").map { id =>
    JsonSerializer.extract[EPerusteRakenne](Json.readResourceIfExists("/mockdata/eperusteet/" + id + ".json").get, ignoreExtras = true)
  }

  def findPerusteet(query: String): List[EPeruste] = {
    // Hakee aina samoilla kriteereillä "auto"
    JsonSerializer.extract[EPerusteet](Json.readFile("src/main/resources/mockdata/eperusteet/hakutulokset-auto.json"), ignoreExtras = true).data.filter(_.nimi("fi").toLowerCase.contains(query.toLowerCase))
  }

  def findPerusteetByDiaarinumero(diaarinumero: String): List[EPeruste] = {
    rakenteet.filter(_.diaarinumero == diaarinumero).map(_.toEPeruste)
  }

  def findRakenne(diaariNumero: String): Option[EPerusteRakenne] = {
    rakenteet.find(_.diaarinumero == diaariNumero)
  }
}
