package fi.oph.tor.organisaatio

import fi.oph.tor.json.Json
import fi.oph.tor.koodisto.KoodistoViitePalvelu
import fi.oph.tor.organisaatio.MockOrganisaatioRepository._

// Testeissä käytetyt organisaatio-oidit
object MockOrganisaatiot {
  val omnomnia = "1.2.246.562.10.51720121923"
  val stadinAmmattiopisto = "1.2.246.562.10.52251087186"
  val winnova = "1.2.246.562.10.93135224694"
  val helsinginKaupunki = "1.2.246.562.10.346830761110"
  val lehtikuusentienToimipiste = "1.2.246.562.10.42456023292"
  val jyväskylänNormaalikoulu = "1.2.246.562.10.14613773812"
  val helsinginYliopisto = "1.2.246.562.10.39218317368"
  val aaltoYliopisto = "1.2.246.562.10.56753942459"

  val oppilaitokset: List[String] = List(
    stadinAmmattiopisto,
    omnomnia,
    winnova,
    jyväskylänNormaalikoulu,
    helsinginYliopisto,
    aaltoYliopisto
  )

  val organisaatiot: List[String] = oppilaitokset ++ List(helsinginKaupunki, lehtikuusentienToimipiste)
}

class MockOrganisaatioRepository(koodisto: KoodistoViitePalvelu) extends JsonOrganisaatioRepository(koodisto) {
  override def fetch(oid: String) = {
    Json.readFileIfExists(hierarchyFilename(oid))
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
  def hierarchyFilename(oid: String): String = {
    "src/main/resources/mockdata/organisaatio/hierarkia/" + oid + ".json"
  }

  def searchFilename(oid: String): String = {
    "src/main/resources/mockdata/organisaatio/search/" + oid + ".json"
  }
}