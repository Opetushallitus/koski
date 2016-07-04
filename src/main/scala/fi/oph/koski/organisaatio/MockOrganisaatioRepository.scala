package fi.oph.koski.organisaatio

import fi.oph.koski.json.Json
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.organisaatio.MockOrganisaatioRepository._

// Testeissä käytetyt organisaatio-oidit
object MockOrganisaatiot {
  val omnomnia = "1.2.246.562.10.51720121923"
  val stadinAmmattiopisto = "1.2.246.562.10.52251087186"
  val winnova = "1.2.246.562.10.93135224694"
  val helsinginKaupunki = "1.2.246.562.10.346830761110"
  val lehtikuusentienToimipiste = "1.2.246.562.10.42456023292"
  val jyväskylänNormaalikoulu = "1.2.246.562.10.14613773812"
  val helsinginMedialukio = "1.2.246.562.10.70411521654"
  val helsinginYliopisto = "1.2.246.562.10.39218317368"
  val aaltoYliopisto = "1.2.246.562.10.56753942459"
  val itäsuomenYliopisto = "1.2.246.562.10.38515028629"
  val yrkehögskolanArcada = "1.2.246.562.10.25619624254"
  val lahdenAmmattikorkeakoulu = "1.2.246.562.10.27756776996"

  val oppilaitokset: List[String] = List(
    stadinAmmattiopisto,
    omnomnia,
    winnova,
    jyväskylänNormaalikoulu,
    helsinginMedialukio,
    helsinginYliopisto,
    aaltoYliopisto,
    itäsuomenYliopisto,
    yrkehögskolanArcada,
    lahdenAmmattikorkeakoulu
  )

  val organisaatiot: List[String] = oppilaitokset ++ List(helsinginKaupunki, lehtikuusentienToimipiste)
}

case class MockOrganisaatioRepository(koodisto: KoodistoViitePalvelu) extends JsonOrganisaatioRepository(koodisto) {
  override def fetch(oid: String) = {
    Json.readResourceIfExists(hierarchyResourcename(oid))
      .map(json => Json.fromJValue[OrganisaatioHakuTulos](json))
      .getOrElse(OrganisaatioHakuTulos(Nil))
  }

  override def fetchSearch(searchTerm: String) = {
    Json.readFileIfExists(searchFilename(searchTerm))
      .map(json => Json.fromJValue[OrganisaatioHakuTulos](json))
      .getOrElse(OrganisaatioHakuTulos(Nil))
  }
}

object MockOrganisaatioRepository {
  def hierarchyResourcename(oid: String): String = "/mockdata/organisaatio/hierarkia/" + oid + ".json"
  def hierarchyFilename(oid: String): String = "src/main/resources" + hierarchyResourcename(oid)

  def searchFilename(searchTerm: String): String = {
    "src/main/resources/mockdata/organisaatio/search/" + searchTerm + ".json"
  }
}