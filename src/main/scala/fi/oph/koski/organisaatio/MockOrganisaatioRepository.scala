package fi.oph.koski.organisaatio

import fi.oph.koski.json.Json
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.organisaatio.MockOrganisaatioRepository._
import fi.oph.koski.schema.{Koodistokoodiviite, Oppilaitos, OrganisaatioWithOid}

// Testeissä käytetyt organisaatio-oidit
object MockOrganisaatiot {
  val helsinginKaupunki = "1.2.246.562.10.346830761110"
  val helsinginYliopisto = "1.2.246.562.10.39218317368"
  val aaltoYliopisto = "1.2.246.562.10.56753942459"
  val itäsuomenYliopisto = "1.2.246.562.10.38515028629"
  val jyväskylänYliopisto = "1.2.246.562.10.77055527103"
  val tampereenYliopisto = "1.2.246.562.10.72255864398"
  val yrkehögskolanArcada = "1.2.246.562.10.25619624254"
  val lahdenAmmattikorkeakoulu = "1.2.246.562.10.27756776996"
  val omnia = "1.2.246.562.10.51720121923"
  val stadinAmmattiopisto = "1.2.246.562.10.52251087186"
  val winnova = "1.2.246.562.10.93135224694"
  val lehtikuusentienToimipiste = "1.2.246.562.10.42456023292"
  val jyväskylänNormaalikoulu = "1.2.246.562.10.14613773812"
  val helsinginMedialukio = "1.2.246.562.10.70411521654"
  val ylioppilastutkintolautakunta = "1.2.246.562.10.43628088406"

  val oppilaitokset: List[String] = List(
    stadinAmmattiopisto,
    omnia,
    winnova,
    jyväskylänNormaalikoulu,
    helsinginMedialukio,
    helsinginYliopisto,
    aaltoYliopisto,
    itäsuomenYliopisto,
    yrkehögskolanArcada,
    lahdenAmmattikorkeakoulu
  )

  // Näille "juuriorganisaatioille" on haettu omat json-filet mockausta varten. Jos tarvitaan uusi juuri, lisätään se tähän
  // ja ajetaan OrganisaatioMockDataUpdater
  val roots = List(
    helsinginKaupunki,
    helsinginYliopisto, jyväskylänYliopisto, tampereenYliopisto, yrkehögskolanArcada, lahdenAmmattikorkeakoulu, itäsuomenYliopisto, aaltoYliopisto,
    omnia, winnova,
    ylioppilastutkintolautakunta)
}

case class MockOrganisaatioRepository(koodisto: KoodistoViitePalvelu) extends JsonOrganisaatioRepository(koodisto) {
  val rootOrgs: List[OrganisaatioHierarkia] = MockOrganisaatiot.roots
    .flatMap(oid => Json.readResourceIfExists(hierarchyResourcename(oid)))
    .flatMap(json => Json.fromJValue[OrganisaatioHakuTulos](json).organisaatiot)
    .map(convertOrganisaatio(_))

  val oppilaitokset: List[Oppilaitos] = {
    def flatten(hierarkia: OrganisaatioHierarkia): List[OrganisaatioHierarkia] = hierarkia :: hierarkia.children.flatMap(flatten)
    rootOrgs.flatMap(flatten).flatMap(_.toOppilaitos)
  }

  override def getOrganisaatioHierarkiaIncludingParents(oid: String): Option[OrganisaatioHierarkia] = {
    rootOrgs.find(_.find(oid).isDefined)
  }

  def findByOppilaitosnumero(numero: String): Option[Oppilaitos] = {
    oppilaitokset.find(_.oppilaitosnumero.map(_.koodiarvo) == Some(numero))
  }
}

object MockOrganisaatioRepository {
  def hierarchyResourcename(oid: String): String = "/mockdata/organisaatio/hierarkia/" + oid + ".json"
  def hierarchyFilename(oid: String): String = "src/main/resources" + hierarchyResourcename(oid)
}