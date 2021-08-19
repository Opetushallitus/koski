package fi.oph.koski.organisaatio

import java.time.LocalDate

import fi.oph.koski.json.JsonResources
import fi.oph.koski.json.JsonSerializer.extract
import fi.oph.koski.koodisto.{KoodistoViitePalvelu, MockKoodistoViitePalvelu}
import fi.oph.koski.schema.LocalizedString.finnish
import fi.oph.koski.schema.{LocalizedString, Oppilaitos}

// Testeissä käytetyt organisaatio-oidit
object MockOrganisaatiot {
  val helsinginKaupunki = "1.2.246.562.10.346830761110"
  val tornionKaupunki = "1.2.246.562.10.25412665926"
  val pyhtäänKunta = "1.2.246.562.10.69417312936"
  val helsinginYliopisto = "1.2.246.562.10.39218317368"
  val aaltoYliopisto = "1.2.246.562.10.56753942459"
  val itäsuomenYliopisto = "1.2.246.562.10.38515028629"
  val jyväskylänYliopisto = "1.2.246.562.10.77055527103"
  val tampereenYliopisto = "1.2.246.562.10.72255864398"
  val yrkehögskolanArcada = "1.2.246.562.10.25619624254"
  val lahdenAmmattikorkeakoulu = "1.2.246.562.10.27756776996"
  val omnia = "1.2.246.562.10.51720121923"
  val omniaArbetarInstitutToimipiste = "1.2.246.562.10.28436066758"
  val stadinAmmattiopisto = "1.2.246.562.10.52251087186"
  val winnova = "1.2.246.562.10.93135224694"
  val lehtikuusentienToimipiste = "1.2.246.562.10.42456023292"
  val jyväskylänNormaalikoulu = "1.2.246.562.10.14613773812"
  val kulosaarenAlaAste = "1.2.246.562.10.64353470871"
  val montessoriPäiväkoti12241 = "1.2.246.562.10.52328612800"
  val vironniemenPäiväkoti = "1.2.246.562.10.95641123252"
  val ressunLukio = "1.2.246.562.10.62858797335"
  val helsinginMedialukio = "1.2.246.562.10.70411521654"
  val ylioppilastutkintolautakunta = "1.2.246.562.10.43628088406"
  val aapajoenKoulu = "1.2.246.562.10.26197302388"
  val stadinOppisopimuskeskus = "1.2.246.562.10.88592529245"
  val ytl = "1.2.246.562.10.43628088406"
  val evira = "1.2.246.562.10.25004584139"
  val kela = "1.2.246.562.10.2013121014482686198719"
  val saksalainenKoulu = "1.2.246.562.10.45093614456"
  val viikinNormaalikoulu = "1.2.246.562.10.81927839589"
  val kouluyhdistysPestalozziSchulvereinSkolföreningen = "1.2.246.562.10.64976109716"
  val helsinginKansainvälisenKoulunVanhempainyhdistys = "1.2.246.562.10.27056241949"
  val internationalSchool = "1.2.246.562.10.67636414343"
  val päiväkotiTouhula = "1.2.246.562.10.63518646078"
  val päiväkotiMajakka = "1.2.246.562.10.90219092054"
  val norlandiaPäiväkodit = "1.2.246.562.10.15679231819"
  val päiväkotiTarina = "1.2.246.562.10.21747360762"
  val varsinaisSuomenKansanopisto = "1.2.246.562.10.31915273374"
  val varsinaisSuomenKansanopistoToimipiste = "1.2.246.562.10.78513447389"
  val lakkautettuOppilaitosHelsingissä = "1.2.246.562.10.56900408842"
  val lakkautettuKunta = "1.2.246.562.10.69417312937"

  val oppilaitokset: List[String] = List(
    stadinAmmattiopisto,
    omnia,
    winnova,
    jyväskylänNormaalikoulu,
    vironniemenPäiväkoti,
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
    saksalainenKoulu,
    internationalSchool,
    varsinaisSuomenKansanopisto,
    lakkautettuOppilaitosHelsingissä
  )
}

object MockOrganisaatioRepository extends OrganisaatioRepository {
  override val koodisto: KoodistoViitePalvelu = MockKoodistoViitePalvelu

  private val rootOrgs: List[OrganisaatioHierarkia] = {
    val json = JsonResources.readResource(hierarchyResourcename(Opetushallitus.organisaatioOid))
    val organisaatiot = extract[OrganisaatioHakuTulos](json, ignoreExtras = true).organisaatiot
    organisaatiot.map(convertOrganisaatio)
  }

  private val oppilaitokset: List[Oppilaitos] = OrganisaatioHierarkia.flatten(rootOrgs).flatMap(_.toOppilaitos)

  def findByOppilaitosnumero(numero: String): Option[Oppilaitos] =
    oppilaitokset.find(_.oppilaitosnumero.map(_.koodiarvo).contains(numero))

  override def findHierarkia(query: String): List[OrganisaatioHierarkia] =
    OrganisaatioHierarkiaFilter(query, "fi").filter(rootOrgs).toList

  override def getOrganisaationNimiHetkellä(oid: String, localDate: LocalDate): Option[LocalizedString] = {
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

  override def findAllHierarkiat: List[OrganisaatioHierarkia] = rootOrgs

  def hierarchyResourcename(oid: String): String = "/mockdata/organisaatio/hierarkia/" + oid + ".json"
}
