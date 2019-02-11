package fi.oph.koski.organisaatio

import java.time.LocalDate

import fi.oph.koski.json.JsonResources
import fi.oph.koski.json.JsonSerializer.extract
import fi.oph.koski.koodisto.MockKoodistoViitePalvelu
import fi.oph.koski.schema.LocalizedString.finnish
import fi.oph.koski.schema.Oppilaitos

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
  val kulosaarenAlaAste = "1.2.246.562.10.64353470871"
  val montessoriPäiväkoti12241 = "1.2.246.562.10.52328612800"
  val ressunLukio = "1.2.246.562.10.62858797335"
  val helsinginMedialukio = "1.2.246.562.10.70411521654"
  val ylioppilastutkintolautakunta = "1.2.246.562.10.43628088406"
  val aapajoenKoulu = "1.2.246.562.10.26197302388"
  val stadinOppisopimuskeskus = "1.2.246.562.10.88592529245"
  val ytl = "1.2.246.562.10.43628088406"
  val evira = "1.2.246.562.10.25004584139"
  val saksalainenKoulu = "1.2.246.562.10.45093614456"
  val kouluyhdistysPestalozziSchulvereinSkolföreningen = "1.2.246.562.10.64976109716"

  val oppilaitokset: List[String] = List(
    stadinAmmattiopisto,
    omnia,
    winnova,
    jyväskylänNormaalikoulu,
    montessoriPäiväkoti12241,
    kulosaarenAlaAste,
    helsinginMedialukio,
    helsinginYliopisto,
    aaltoYliopisto,
    itäsuomenYliopisto,
    yrkehögskolanArcada,
    lahdenAmmattikorkeakoulu,
    ressunLukio,
    aapajoenKoulu,
    ytl,
    saksalainenKoulu
  )

  // Näille "juuriorganisaatioille" on haettu omat json-filet mockausta varten. Jos tarvitaan uusi juuri, lisätään se tähän
  // ja ajetaan OrganisaatioMockDataUpdater
  val roots = List(
    helsinginKaupunki,
    helsinginYliopisto, jyväskylänYliopisto, tampereenYliopisto, yrkehögskolanArcada, lahdenAmmattikorkeakoulu, itäsuomenYliopisto, aaltoYliopisto,
    omnia, winnova,
    ylioppilastutkintolautakunta, aapajoenKoulu,
    kouluyhdistysPestalozziSchulvereinSkolföreningen
  )
}

object MockOrganisaatioRepository extends JsonOrganisaatioRepository(MockKoodistoViitePalvelu) {
  val rootOrgs: List[OrganisaatioHierarkia] = MockOrganisaatiot.roots
    .flatMap(oid => JsonResources.readResourceIfExists(hierarchyResourcename(oid)))
    .flatMap(json => extract[OrganisaatioHakuTulos](json, ignoreExtras = true).organisaatiot)
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

  def hierarchyResourcename(oid: String): String = "/mockdata/organisaatio/hierarkia/" + oid + ".json"
  def hierarchyFilename(oid: String): String = "src/main/resources" + hierarchyResourcename(oid)

  override def findHierarkia(query: String) = OrganisaatioHierarkiaFilter(query, "fi").filter(rootOrgs).toList

  override def getOrganisaationNimiHetkellä(oid: String, localDate: LocalDate) = {
    getOrganisaatioHierarkia(oid).map {
      if (localDate.isEqual(LocalDate.of(2010, 10, 10))) {
        _.nimi.concat(finnish(" -vanha"))
      } else {
        _.nimi
      }
    }
  }

  override def findSähköpostiVirheidenRaportointiin(oid: String): Option[SähköpostiVirheidenRaportointiin] = {
    if (oid == MockOrganisaatiot.kulosaarenAlaAste) {
      None
    } else if (oid == MockOrganisaatiot.ytl) {
      getOrganisaatioHierarkia(oid).map(h => SähköpostiVirheidenRaportointiin(h.oid, h.nimi, "ytl.ytl@example.com"))
    } else {
      getOrganisaatioHierarkia(oid).map(h => SähköpostiVirheidenRaportointiin(h.oid, h.nimi, "joku.osoite@example.com"))
    }
  }

  override def findAllRaw: List[OrganisaatioPalveluOrganisaatio] = {
    MockOrganisaatiot.roots
      .flatMap(oid => JsonResources.readResourceIfExists(hierarchyResourcename(oid)))
      .flatMap(json => extract[OrganisaatioHakuTulos](json, ignoreExtras = true).organisaatiot)
  }
}
