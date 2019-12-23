package fi.oph.koski.organisaatio

import java.time.LocalDate

import fi.oph.koski.documentation.ExampleData
import fi.oph.koski.json.JsonResources
import fi.oph.koski.json.JsonSerializer.extract
import fi.oph.koski.koodisto.MockKoodistoViitePalvelu
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.schema.LocalizedString.finnish
import fi.oph.koski.schema._

// Testeissä käytetyt organisaatio-oidit
object MockOrganisaatiot {
  val helsinginKaupunki = Koulutustoimija("1.2.246.562.10.346830761110", nimi = Some(Finnish("Helsingin kaupunki", Some("Helsingfors stad"))), yTunnus = Some("0201256-6"))
  val helsinginYliopisto = Oppilaitos(oid = "1.2.246.562.10.39218317368", oppilaitosnumero = Some(Koodistokoodiviite("01901", None, "oppilaitosnumero", None)), nimi = Some("Helsingin yliopisto"))
  val aaltoYliopisto = Oppilaitos("1.2.246.562.10.56753942459", nimi = Some("Aalto yliopisto"), oppilaitosnumero = Some(Koodistokoodiviite("10076", None, "oppilaitosnumero", None)))
  val itäsuomenYliopisto = Oppilaitos("1.2.246.562.10.38515028629", nimi = Some("Itä-Suomen yliopisto"), oppilaitosnumero = Some(Koodistokoodiviite("10088", None, "oppilaitosnumero", None)))
  val jyväskylänYliopisto = Koulutustoimija("1.2.246.562.10.77055527103", nimi = Some("Jyväskylän yliopisto"), yTunnus = Some("0245894-7"))
  val tampereenYliopisto = Koulutustoimija("1.2.246.562.10.72255864398", nimi = Some("Tampereen yliopisto"), yTunnus = Some("0155668-4"))
  val yrkehögskolanArcada = Oppilaitos("1.2.246.562.10.25619624254", nimi = Some("Yrkeshögskolan Arcada"), oppilaitosnumero = Some(Koodistokoodiviite("02535", None, "oppilaitosnumero", None)))
  val lahdenAmmattikorkeakoulu = Oppilaitos("1.2.246.562.10.27756776996", nimi = Some("Lahden ammattikorkeakoulu"), oppilaitosnumero = Some(Koodistokoodiviite("02470", None, "oppilaitosnumero", None)))
  val omnia = Oppilaitos("1.2.246.562.10.51720121923", nimi = Some("Omnian ammattiopisto"), oppilaitosnumero = Some(Koodistokoodiviite("10054", None, "oppilaitosnumero", None)))
  val stadinAmmattiopisto = Oppilaitos("1.2.246.562.10.52251087186", nimi = Some("Stadin ammattiopisto"), oppilaitosnumero = Some(Koodistokoodiviite("10105", None, "oppilaitosnumero", None)))
  val winnova = Oppilaitos("1.2.246.562.10.93135224694", nimi = Some("WinNova"), oppilaitosnumero = Some(Koodistokoodiviite("10095", None, "oppilaitosnumero", None)))
  val jyväskylänNormaalikoulu = Oppilaitos(oid = "1.2.246.562.10.14613773812", oppilaitosnumero = Some(Koodistokoodiviite("00204", None, "oppilaitosnumero", None)), nimi = Some("Jyväskylän normaalikoulu"))
  val kulosaarenAlaAste = Oppilaitos(oid = "1.2.246.562.10.64353470871", oppilaitosnumero = Some(Koodistokoodiviite("03016", None, "oppilaitosnumero", None)), nimi = Some("Kulosaaren ala-aste"))
  val pkVironniemi: Oppilaitos = Oppilaitos(oid = "1.2.246.562.10.95641123252", nimi = Some("PK Vironniemi"))
  val ressunLukio = Oppilaitos(oid = "1.2.246.562.10.62858797335", oppilaitosnumero = Some(Koodistokoodiviite("00082", None, "oppilaitosnumero", None)), nimi = Some("Ressun lukio"))
  val helsinginMedialukio = Oppilaitos("1.2.246.562.10.70411521654", nimi = Some("Helsingin medialukio"), oppilaitosnumero = Some(Koodistokoodiviite("00648", None, "oppilaitosnumero", None)))
  val aapajoenKoulu = Oppilaitos("1.2.246.562.10.26197302388", nimi = Some("Aapajoen koulu"), oppilaitosnumero = Some(Koodistokoodiviite("04044", None, "oppilaitosnumero", None)))
  val stadinOppisopimuskeskus = Oppilaitos("1.2.246.562.10.88592529245", nimi = Some("Stadin oppisopimuskeskus"))
  val ytl = Koulutustoimija("1.2.246.562.10.43628088406", nimi = Some("Ylioppilastutkintolautakunta"), kotipaikka = Some(ExampleData.helsinki))
  val evira = OidOrganisaatio("1.2.246.562.10.25004584139")
  val kela = OidOrganisaatio("1.2.246.562.10.2013121014482686198719")
  val saksalainenKoulu = Oppilaitos("1.2.246.562.10.45093614456", nimi = Some("Helsingin Saksalainen koulu"), oppilaitosnumero = Some(Koodistokoodiviite("00085", None, "oppilaitosnumero", None)))
  val internationalSchool = Oppilaitos(oid = "1.2.246.562.10.67636414343", oppilaitosnumero = Some(Koodistokoodiviite("03510", None, "oppilaitosnumero", None)), nimi = Some("International School of Helsinki"))

  val lehtikuusentienToimipiste = Toimipiste("1.2.246.562.10.42456023292", nimi = Some("Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka"))

  val koulutustoimijat: Map[Koulutustoimija, List[OrganisaatioWithOid]] =
    Map(
      helsinginKaupunki -> List(
        stadinAmmattiopisto,
        pkVironniemi,
        kulosaarenAlaAste,
        helsinginMedialukio,
        ressunLukio,
        lehtikuusentienToimipiste
      ),
      MockKoulutustoimijat.espoonSeudunKoulutuskuntayhtymäOmnia -> List(omnia),
      MockKoulutustoimijat.länsirannikonKoulutusOy -> List(winnova),
      jyväskylänYliopisto -> List(jyväskylänNormaalikoulu),
      MockKoulutustoimijat.helsinginYliopisto -> List(helsinginYliopisto),
      MockKoulutustoimijat.aaltoKorkeakoulusäätiöSr -> List(aaltoYliopisto),
      MockKoulutustoimijat.itäsuomenYliopisto -> List(itäsuomenYliopisto),
      MockKoulutustoimijat.yrkeshögskolanArcadaAb -> List(yrkehögskolanArcada),
      MockKoulutustoimijat.lahdenAmmattikorkeakouluOy -> List(lahdenAmmattikorkeakoulu),
      MockKoulutustoimijat.tornionKaupunki -> List(aapajoenKoulu),
      ytl -> List(ytl),
      MockKoulutustoimijat.kouluyhdistysPestalozziSchulvereinSkolföreningen -> List(saksalainenKoulu),
      MockKoulutustoimijat.helsinginKansainvälisenKoulunVanhempainyhdistys -> List(internationalSchool)
    )

  val oppilaitokset: List[OrganisaatioWithOid] = koulutustoimijat.values.flatten.toList

  // Näille "juuriorganisaatioille" on haettu omat json-filet mockausta varten. Jos tarvitaan uusi juuri, lisätään se tähän
  // ja ajetaan OrganisaatioMockDataUpdater
  val roots = List(
    helsinginKaupunki,
    helsinginYliopisto, jyväskylänYliopisto, tampereenYliopisto, yrkehögskolanArcada, lahdenAmmattikorkeakoulu, itäsuomenYliopisto, aaltoYliopisto,
    omnia, winnova,
    ytl, aapajoenKoulu,
    MockKoulutustoimijat.kouluyhdistysPestalozziSchulvereinSkolföreningen,
    MockKoulutustoimijat.helsinginKansainvälisenKoulunVanhempainyhdistys
  )

  def koulutustoimija(organisaatio: OrganisaatioWithOid): Koulutustoimija = koulutustoimijat.collectFirst {
    case (kt, orgs) if orgs.map(_.oid).contains(organisaatio.oid) || kt.oid == organisaatio.oid => kt
  }.get
}

object MockKoulutustoimijat {
  val espoonSeudunKoulutuskuntayhtymäOmnia = Koulutustoimija("1.2.246.562.10.53642770753")
  val länsirannikonKoulutusOy = Koulutustoimija("1.2.246.562.10.82246911869")
  val helsinginYliopisto = Koulutustoimija("1.2.246.562.10.53814745062")
  val aaltoKorkeakoulusäätiöSr = Koulutustoimija("1.2.246.562.10.82388989657")
  val itäsuomenYliopisto = Koulutustoimija("1.2.246.562.10.240484683010")
  val yrkeshögskolanArcadaAb = Koulutustoimija("1.2.246.562.10.72194164959")
  val lahdenAmmattikorkeakouluOy = Koulutustoimija("1.2.246.562.10.85149969462")
  val tornionKaupunki = Koulutustoimija("1.2.246.562.10.25412665926")
  val kouluyhdistysPestalozziSchulvereinSkolföreningen = Koulutustoimija("1.2.246.562.10.64976109716")
  val helsinginKansainvälisenKoulunVanhempainyhdistys = Koulutustoimija("1.2.246.562.10.27056241949")
}

object MockOrganisaatioRepository extends JsonOrganisaatioRepository(MockKoodistoViitePalvelu) {
  val rootOrgs: List[OrganisaatioHierarkia] = MockOrganisaatiot.roots.map(_.oid)
    .flatMap(oid => JsonResources.readResourceIfExists(hierarchyResourcename(oid)))
    .flatMap(json => extract[OrganisaatioHakuTulos](json, ignoreExtras = true).organisaatiot)
    .map(convertOrganisaatio(_))

  val oppilaitokset: List[Oppilaitos] = {
    def flatten(hierarkia: OrganisaatioHierarkia): List[OrganisaatioHierarkia] = hierarkia :: hierarkia.children.flatMap(flatten)
    rootOrgs.flatMap(flatten).flatMap(_.toOppilaitos)
  }

  override def getOrganisaatioHierarkiaIncludingParents(oid: String): List[OrganisaatioHierarkia] = {
    rootOrgs.find(_.find(oid).isDefined).toList
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
    if (oid == MockOrganisaatiot.kulosaarenAlaAste.oid) {
      None
    } else if (oid == MockOrganisaatiot.ytl.oid) {
      getOrganisaatioHierarkia(oid).map(h => SähköpostiVirheidenRaportointiin(h.oid, h.nimi, "ytl.ytl@example.com"))
    } else {
      getOrganisaatioHierarkia(oid).map(h => SähköpostiVirheidenRaportointiin(h.oid, h.nimi, "joku.osoite@example.com"))
    }
  }

  override def findAllRaw: List[OrganisaatioPalveluOrganisaatio] = {
    MockOrganisaatiot.roots.map(_.oid)
      .flatMap(oid => JsonResources.readResourceIfExists(hierarchyResourcename(oid)))
      .flatMap(json => extract[OrganisaatioHakuTulos](json, ignoreExtras = true).organisaatiot)
  }

  override def findAllVarhaiskasvatusToimipisteet: List[OrganisaatioPalveluOrganisaatioTyyppi] = {
    val json = JsonResources.readResource("/mockdata/organisaatio/varhaiskasvatustoimipisteet.json")
    extract[OrganisaatioTyyppiHakuTulos](json, ignoreExtras = true).organisaatiot
  }
}
