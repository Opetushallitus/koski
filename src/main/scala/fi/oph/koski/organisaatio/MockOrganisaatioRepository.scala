package fi.oph.koski.organisaatio

import fi.oph.koski.json.Json
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.organisaatio.MockOrganisaatioRepository._
import fi.oph.koski.schema.{Koodistokoodiviite, Oppilaitos, OrganisaatioWithOid}

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
  val helsinginYliopistoRoot = "1.2.246.562.10.53814745062"
  val aaltoYliopisto = "1.2.246.562.10.56753942459"
  val itäsuomenYliopisto = "1.2.246.562.10.38515028629"
  val yrkehögskolanArcada = "1.2.246.562.10.25619624254"
  val lahdenAmmattikorkeakoulu = "1.2.246.562.10.27756776996"
  val jyväskylänYliopisto = "1.2.246.562.10.77055527103"
  val tampereenYliopisto = "1.2.246.562.10.72255864398"
  val ylioppilastutkintolautakunta = "1.2.246.562.10.43628088406"

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

  val roots = List(helsinginKaupunki, helsinginYliopistoRoot, jyväskylänYliopisto, tampereenYliopisto, yrkehögskolanArcada, lahdenAmmattikorkeakoulu, itäsuomenYliopisto, ylioppilastutkintolautakunta, omnomnia, winnova, aaltoYliopisto)
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