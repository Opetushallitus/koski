package fi.oph.koski.raportit

import com.typesafe.config.ConfigValueFactory.fromAnyRef
import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.{KoskiSpecificMockOppijat, LaajatOppijaHenkilöTiedot}
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryVSTVapaatavoitteinen
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.raportit.vst.VSTVapaatavoitteinenRow
import fi.oph.koski.raportointikanta.RaportointikantaTestMethods
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate

class VstVapaatavoitteinenRaporttiSpec
  extends AnyFreeSpec
    with Matchers
    with RaportointikantaTestMethods {

  "Vapaan sivistystyön vapaatavoitteisen koulutuksen raportti" - {
    val koulutustoimijaOid: String = MockOrganisaatiot.varsinaisSuomenAikuiskoulutussäätiö
    val oppilaitosOid: String = MockOrganisaatiot.varsinaisSuomenKansanopisto

    val expectedColumns: List[String] = List(
      "Opiskeluoikeuden oid",
      "Lähdejärjestelmä",
      "Opiskeluoikeuden tunniste lähdejärjestelmässä",
      "Koulutustoimijan nimi",
      "Oppilaitoksen nimi",
      "Toimipisteen nimi",
      "Päivitetty",
      "Yksilöity",
      "Oppijan oid",
      "Oppijan master-oid",
      "Hetu",
      "Sukunimi",
      "Etunimet",
      "Kotikunta",
      "Opiskeluoikeuden alkamispäivä",
      "Opiskeluoikeuden päättymispäivä",
      "Viimeisin opiskeluoikeuden tila",
      "Opintokokonaisuuden koodiarvo",
      "Opintokokonaisuuden nimi",
      "Osasuoritusten yhteislaajuus (opintopistettä)",
      "Osasuorituksia yhteensä",
      "Arvioituja osasuorituksia",
      "Osasuorituksia ilman arviointia",
      "Suoritus vahvistettu",
      "Suorituksen vahvistuspäivä",
    )

    val vahvistettuOppija = KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus
    val keskeytynytOppija = KoskiSpecificMockOppijat.vstVapaatavoitteinenKeskeytynyt

    val expectedVahvistettuRow: VSTVapaatavoitteinenRow = VSTVapaatavoitteinenRow(
      opiskeluoikeusOid = "",
      lähdejärjestelmä = None,
      lähdejärjestelmänId = None,
      koulutustoimijaNimi = "Varsinais-Suomen Aikuiskoulutussäätiö sr",
      oppilaitoksenNimi = "Varsinais-Suomen kansanopisto",
      toimipisteNimi = "Varsinais-Suomen kansanopisto",
      päivitetty = LocalDate.now(),
      yksilöity = true,
      oppijaOid = vahvistettuOppija.oid,
      oppijaMasterOid = Some(vahvistettuOppija.oid),
      hetu = vahvistettuOppija.hetu,
      sukunimi = vahvistettuOppija.sukunimi,
      etunimet = vahvistettuOppija.etunimet,
      kotikunta = vahvistettuOppija.kotikunta.getOrElse(""),
      opiskeluoikeudenAlkamispäivä = Some(LocalDate.of(2022, 5, 31)),
      opiskeluoikeudenPäättymispäivä = Some(LocalDate.of(2022, 5, 31)),
      viimeisinTila = Some("hyvaksytystisuoritettu"),
      opintokokonaisuusKoodiarvo = Some("1138"),
      opintokokonaisuusNimi = Some("Kuvallisen ilmaisun perusteet ja välineet"),
      yhteislaajuus = 5.0,
      osasuorituksiaYhteensä = 2,
      arvioitujaOsasuorituksia = 2,
      arviointiPuuttuuOsasuorituksia = 0,
      suoritusVahvistettu = true,
      suorituksenVahvistuspäivä = Some(LocalDate.of(2022, 5, 31)),
    )

    val expectedKeskeytynytRow: VSTVapaatavoitteinenRow = expectedVahvistettuRow.copy(
      oppijaOid = keskeytynytOppija.oid,
      oppijaMasterOid = Some(keskeytynytOppija.oid),
      hetu = keskeytynytOppija.hetu,
      sukunimi = keskeytynytOppija.sukunimi,
      etunimet = keskeytynytOppija.etunimet,
      kotikunta = keskeytynytOppija.kotikunta.getOrElse(""),
      opiskeluoikeudenAlkamispäivä = Some(LocalDate.of(2023, 12, 20)),
      opiskeluoikeudenPäättymispäivä = Some(LocalDate.of(2023, 12, 20)),
      viimeisinTila = Some("keskeytynyt"),
      yhteislaajuus = 4.0,
      osasuorituksiaYhteensä = 2,
      arvioitujaOsasuorituksia = 1,
      arviointiPuuttuuOsasuorituksia = 1,
      suoritusVahvistettu = false,
      suorituksenVahvistuspäivä = None,
    )

    "Oppilaitoksen raportti latautuu ja sisältää oikeat datat" in {
      val sheet = getMainDataSheet(getRaportti(oppilaitosOid))
      sheet.columnSettings.map(_._2.title) should equal(expectedColumns)
      verifyRow(sheet, vahvistettuOppija, expectedVahvistettuRow)
      verifyRow(sheet, keskeytynytOppija, expectedKeskeytynytRow)
      verifyOppijat(sheet, Seq(vahvistettuOppija, keskeytynytOppija))
    }

    "Koulutustoimijan raportti latautuu ja sisältää oikeat datat" in {
      val sheet = getMainDataSheet(getRaportti(koulutustoimijaOid))
      sheet.columnSettings.map(_._2.title) should equal(expectedColumns)
      verifyOppijat(sheet, Seq(vahvistettuOppija, keskeytynytOppija))
    }

    "Aikarajaus toimii" in {
      val pvm = LocalDate.of(2023, 12, 20)
      val sheet = getMainDataSheet(getRaportti(oppilaitosOid, alku = pvm, loppu = pvm))
      verifyOppijat(sheet, Seq(keskeytynytOppija))
    }
  }

  "Raportti ei ole käytössä tuotantoympäristössä" - {
    val app = KoskiApplicationForTests
    val tuotantoConfig = app.config.withValue("opintopolku.virkailija.url", fromAnyRef("https://virkailija.opintopolku.fi"))
    val oppilaitosOid = MockOrganisaatiot.varsinaisSuomenKansanopisto
    implicit val session = MockUsers.varsinaisSuomiPalvelukäyttäjä.toKoskiSpecificSession(app.käyttöoikeusRepository)
    val hierarkia = app.organisaatioRepository.getOrganisaatioHierarkia(oppilaitosOid).get
    val koulutusmuodot = Map(oppilaitosOid -> Seq("vapaansivistystyonkoulutus"))
    val kysely = MassaluovutusQueryVSTVapaatavoitteinen(
      organisaatioOid = Some(oppilaitosOid),
      alku = LocalDate.of(2022, 1, 1),
      loppu = LocalDate.of(2025, 1, 1),
    )

    "raporttityyppiä ei tarjota käyttöliittymälle" in {
      val tyypit = RaportitAccessResolver(app.organisaatioRepository, app.koodistoPalvelu, app.raportointiDatabase, tuotantoConfig)
        .mahdollisetRaporttienTyypitOrganisaatiolle(hierarkia, koulutusmuodot)
      tyypit should contain(JatkuvanOppimisenVapaanSivistystyönRaportti)
      tyypit should not contain VapaanSivistystyönVapaatavoitteisenKoulutuksenRaportti
    }

    "massaluovutuskysely ei ole sallittu" in {
      kysely.queryAllowed(KoskiApplication(tuotantoConfig)) should be(false)
    }

    "muissa ympäristöissä raportti ja kysely ovat käytössä" in {
      RaportitAccessResolver(app).mahdollisetRaporttienTyypitOrganisaatiolle(hierarkia, koulutusmuodot) should contain(VapaanSivistystyönVapaatavoitteisenKoulutuksenRaportti)
      kysely.queryAllowed(app) should be(true)
    }
  }

  private lazy val raportitService = new RaportitService(KoskiApplicationForTests)

  private def getRaportti(
    organisaatioOid: String,
    alku: LocalDate = LocalDate.of(2020, 1, 1),
    loppu: LocalDate = LocalDate.of(2025, 1, 1),
  ): OppilaitosRaporttiResponse = {
    val request = AikajaksoRaporttiRequest(organisaatioOid, None, "password", alku, loppu, "fi")
    val t = new LocalizationReader(KoskiApplicationForTests.koskiLocalizationRepository, "fi")
    raportitService.vstVapaatavoitteinen(request, t)
  }

  private def getMainDataSheet(raportti: OppilaitosRaporttiResponse): DataSheet =
    raportti.sheets.head.asInstanceOf[DataSheet]

  private def verifyRow(sheet: DataSheet, oppija: LaajatOppijaHenkilöTiedot, expected: VSTVapaatavoitteinenRow): Unit = {
    val row = sheet.rows.map(_.asInstanceOf[VSTVapaatavoitteinenRow]).find(_.oppijaOid == oppija.oid)
      .getOrElse(fail(s"Riviä oppijalle ${oppija.oid} ei löytynyt"))
    row should equal(expected.copy(opiskeluoikeusOid = row.opiskeluoikeusOid))
  }

  private def verifyOppijat(sheet: DataSheet, oppijat: Seq[LaajatOppijaHenkilöTiedot]): Unit = {
    val actualOppijat = sheet.rows
      .map(_.asInstanceOf[VSTVapaatavoitteinenRow])
      .map(o => s"${o.sukunimi} ${o.etunimet} (${o.oppijaOid})")
    val expectedOppijat = oppijat
      .map(o => s"${o.sukunimi} ${o.etunimet} (${o.oid})")
    actualOppijat should contain theSameElementsAs expectedOppijat
  }
}
