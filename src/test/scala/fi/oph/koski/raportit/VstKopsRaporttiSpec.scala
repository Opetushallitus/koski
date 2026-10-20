package fi.oph.koski.raportit

import com.typesafe.config.ConfigValueFactory.fromAnyRef
import fi.oph.koski.{DirtiesFixtures, KoskiApplicationForTests}
import fi.oph.koski.api.misc.PutOpiskeluoikeusTestMethods
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.documentation.ExampleData.opiskeluoikeusLäsnä
import fi.oph.koski.documentation.VapaaSivistystyöExample
import fi.oph.koski.documentation.VapaaSivistystyöExampleData._
import fi.oph.koski.henkilo.{KoskiSpecificMockOppijat, LaajatOppijaHenkilöTiedot}
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryVSTKOPS
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.raportointikanta.RaportointikantaTestMethods
import fi.oph.koski.schema._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate
import java.time.LocalDate.{of => date}
import scala.reflect.runtime.universe

class VstKopsRaporttiSpec
  extends AnyFreeSpec
    with Matchers
    with RaportointikantaTestMethods
    with DirtiesFixtures
    with PutOpiskeluoikeusTestMethods[VapaanSivistystyönOpiskeluoikeus] {

  override def tag: universe.TypeTag[VapaanSivistystyönOpiskeluoikeus] = implicitly[universe.TypeTag[VapaanSivistystyönOpiskeluoikeus]]
  override def defaultOpiskeluoikeus: VapaanSivistystyönOpiskeluoikeus = VapaaSivistystyöExample.opiskeluoikeusKOPS

  private val keskeneräinenOppija = KoskiSpecificMockOppijat.vapaaSivistystyöMaksuttomuus

  private val keskeneräinenSuoritus = VapaaSivistystyöExample.suoritusKOPS.copy(
    vahvistus = None,
    osasuoritukset = Some(List(
      osaamiskokonaisuudenSuoritus("1002", List(
        opintokokonaisuudenSuoritus(opintokokonaisuus("A01", "Arjen rahankäyttö", "Arjen rahankäyttö", 2.0)),
        arvioimatonOpintokokonaisuus(opintokokonaisuus("M01", "Mielen liikkeet", "Mielen liikkeet", 1.0)),
      )),
      osaamiskokonaisuudenSuoritus("1003", List(
        arvioimatonOpintokokonaisuus(opintokokonaisuus("OP01", "Oma opiskelutyyli", "Oman opiskelutyylin analysointi", 4.0)),
      )),
      suuntautumisopintojenSuoritus(List(
        opintokokonaisuudenSuoritus(opintokokonaisuus("ATX01", "Tietokoneen huolto", "Tietokoneen huolto", 5.0)),
      )),
    )),
  )

  override protected def alterFixture(): Unit = {
    val nykyinen = lastOpiskeluoikeusByHetu(keskeneräinenOppija)
    createOrUpdate(keskeneräinenOppija, VapaanSivistystyönOpiskeluoikeus(
      oid = nykyinen.oid,
      tila = VapaanSivistystyönOpiskeluoikeudenTila(List(
        OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso(date(2024, 8, 1), opiskeluoikeusLäsnä)
      )),
      lisätiedot = Some(VapaanSivistystyönOpiskeluoikeudenLisätiedot(
        maksuttomuus = Some(List(Maksuttomuus(alku = date(2024, 8, 1), loppu = None, maksuton = true))),
        oikeuttaMaksuttomuuteenPidennetty = Some(List(OikeuttaMaksuttomuuteenPidennetty(date(2025, 1, 1), date(2025, 3, 31)))),
      )),
      oppilaitos = Some(varsinaisSuomenKansanopisto),
      suoritukset = List(keskeneräinenSuoritus),
    ))
    reloadRaportointikanta()
  }

  "Vapaan sivistystyön oppivelvollisille suunnatun koulutuksen raportti" - {
    val koulutustoimijaOid: String = MockOrganisaatiot.varsinaisSuomenAikuiskoulutussäätiö
    val oppilaitosOid: String = MockOrganisaatiot.varsinaisSuomenKansanopisto

    val expectedColumns: List[String] = List(
      "Oppijan oid",
      "Oppijan master-oid",
      "Hetu",
      "Sukunimi",
      "Etunimet",
      "Yksilöity",
      "Opetuskieli",
      "Opiskeluoikeuden oid",
      "Lähdejärjestelmä",
      "Opiskeluoikeuden tunniste lähdejärjestelmässä",
      "Koulutustoimijan nimi",
      "Oppilaitoksen nimi",
      "Toimipisteen nimi",
      "Päivitetty",
      "Opiskeluoikeuden alkamispäivä",
      "Opiskeluoikeuden päättymispäivä",
      "Viimeisin opiskeluoikeuden tila",
      "Maksuttomuus",
      "Maksullisuus",
      "Oikeutta maksuttomuuteen pidennetty",
      "Suoritus vahvistettu",
      "Suorituksen vahvistuspäivä",
      "Päätason suorituksen laajuus (opintopistettä)",
      "Arjen taidot ja elämänhallinta (opintopistettä)",
      "Opiskelu-, itsetuntemus- ja työelämätaidot (opintopistettä)",
      "Vuorovaikutus- ja viestintätaidot (opintopistettä)",
      "Matemaattiset perustaidot ja ongelmanratkaisutaidot (opintopistettä)",
      "Aktiivinen kansalaisuus (opintopistettä)",
      "Opiskelu- ja urasuunnittelutaidot (opintopistettä)",
      "Valinnaiset suuntautumisopinnot (opintopistettä)",
      "Alle 4 opintopisteen osaamiskokonaisuuksia",
      "Opintokokonaisuuksia yhteensä",
      "Arvioituja opintokokonaisuuksia",
      "Opintokokonaisuuksia ilman arviointia",
      "Arvioitujen opintokokonaisuuksien yhteislaajuus (opintopistettä)",
    )

    val vahvistettuOppija = KoskiSpecificMockOppijat.vapaaSivistystyöOppivelvollinen

    "Oppilaitoksen raportti latautuu ja sisältää oikeat sarakkeet" in {
      val sheet = getSheet(getRaportti(oppilaitosOid))
      sheet.columnSettings.map(_.title) should equal(expectedColumns)
      sheet.rows.foreach(_ should have length expectedColumns.length)
    }

    "Vahvistetun suorituksen rivi" in {
      val row = getRow(getSheet(getRaportti(oppilaitosOid)), vahvistettuOppija)
      row("Oppijan oid") should equal(vahvistettuOppija.oid)
      row("Opetuskieli") should equal(Some("suomi"))
      row("Opiskeluoikeuden alkamispäivä") should equal(Some(date(2021, 9, 1)))
      row("Viimeisin opiskeluoikeuden tila") should equal(Some("lasna"))
      row("Maksuttomuus") should equal(None)
      row("Oikeutta maksuttomuuteen pidennetty") should equal(None)
      row("Suoritus vahvistettu") should equal(true)
      row("Suorituksen vahvistuspäivä") should equal(Some(date(2022, 5, 31)))
      row("Päätason suorituksen laajuus (opintopistettä)") should equal(55.0)
      row("Arjen taidot ja elämänhallinta (opintopistettä)") should equal(Some(4.0))
      row("Vuorovaikutus- ja viestintätaidot (opintopistettä)") should equal(None)
      row("Matemaattiset perustaidot ja ongelmanratkaisutaidot (opintopistettä)") should equal(Some(8.0))
      row("Valinnaiset suuntautumisopinnot (opintopistettä)") should equal(Some(31.0))
      row("Alle 4 opintopisteen osaamiskokonaisuuksia") should equal(0)
      row("Opintokokonaisuuksia yhteensä") should equal(12)
      row("Arvioituja opintokokonaisuuksia") should equal(12)
      row("Opintokokonaisuuksia ilman arviointia") should equal(0)
      row("Arvioitujen opintokokonaisuuksien yhteislaajuus (opintopistettä)") should equal(55.0)
    }

    "Keskeneräisen suorituksen rivi" in {
      val row = getRow(getSheet(getRaportti(oppilaitosOid)), keskeneräinenOppija)
      row("Opiskeluoikeuden alkamispäivä") should equal(Some(date(2024, 8, 1)))
      row("Maksuttomuus").asInstanceOf[Option[String]].get should startWith("2024-08-01 – ")
      row("Maksullisuus") should equal(None)
      row("Oikeutta maksuttomuuteen pidennetty") should equal(Some("2025-01-01 – 2025-03-31"))
      row("Suoritus vahvistettu") should equal(false)
      row("Suorituksen vahvistuspäivä") should equal(None)
      row("Päätason suorituksen laajuus (opintopistettä)") should equal(12.0)
      row("Arjen taidot ja elämänhallinta (opintopistettä)") should equal(Some(3.0))
      row("Opiskelu-, itsetuntemus- ja työelämätaidot (opintopistettä)") should equal(Some(4.0))
      row("Aktiivinen kansalaisuus (opintopistettä)") should equal(None)
      row("Valinnaiset suuntautumisopinnot (opintopistettä)") should equal(Some(5.0))
      row("Alle 4 opintopisteen osaamiskokonaisuuksia") should equal(1)
      row("Opintokokonaisuuksia yhteensä") should equal(4)
      row("Arvioituja opintokokonaisuuksia") should equal(2)
      row("Opintokokonaisuuksia ilman arviointia") should equal(2)
      row("Arvioitujen opintokokonaisuuksien yhteislaajuus (opintopistettä)") should equal(7.0)
    }

    "Koulutustoimijan raportti sisältää samat oppijat" in {
      val oppilaitoksenOppijat = getSheet(getRaportti(oppilaitosOid)).rows.map(_.head)
      getSheet(getRaportti(koulutustoimijaOid)).rows.map(_.head) should contain theSameElementsAs oppilaitoksenOppijat
      oppilaitoksenOppijat should contain allOf(vahvistettuOppija.oid, keskeneräinenOppija.oid)
    }

    "Aikarajaus toimii" in {
      val oppijat = getSheet(getRaportti(oppilaitosOid, alku = date(2020, 1, 1), loppu = date(2021, 8, 31))).rows.map(_.head)
      oppijat shouldBe empty
      getSheet(getRaportti(oppilaitosOid, alku = date(2024, 9, 1), loppu = date(2024, 9, 1))).rows.map(_.head) should contain(keskeneräinenOppija.oid)
    }
  }

  "Raportti ei ole käytössä tuotantoympäristössä" - {
    val app = KoskiApplicationForTests
    val tuotantoConfig = app.config.withValue("opintopolku.virkailija.url", fromAnyRef("https://virkailija.opintopolku.fi"))
    val oppilaitosOid = MockOrganisaatiot.varsinaisSuomenKansanopisto
    implicit val session = MockUsers.varsinaisSuomiPalvelukäyttäjä.toKoskiSpecificSession(app.käyttöoikeusRepository)
    val hierarkia = app.organisaatioRepository.getOrganisaatioHierarkia(oppilaitosOid).get
    val koulutusmuodot = Map(oppilaitosOid -> Seq("vapaansivistystyonkoulutus"))
    val kysely = MassaluovutusQueryVSTKOPS(
      organisaatioOid = Some(oppilaitosOid),
      alku = date(2022, 1, 1),
      loppu = date(2025, 1, 1),
    )

    "raporttityyppiä ei tarjota käyttöliittymälle" in {
      val tyypit = RaportitAccessResolver(app.organisaatioRepository, app.koodistoPalvelu, app.raportointiDatabase, tuotantoConfig)
        .mahdollisetRaporttienTyypitOrganisaatiolle(hierarkia, koulutusmuodot)
      tyypit should contain(JatkuvanOppimisenVapaanSivistystyönRaportti)
      tyypit should not contain VapaanSivistystyönOppivelvollisilleSuunnatunKoulutuksenRaportti
    }

    "massaluovutuskysely ei ole sallittu" in {
      kysely.queryAllowed(KoskiApplication(tuotantoConfig)) should be(false)
    }

    "muissa ympäristöissä raportti ja kysely ovat käytössä" in {
      RaportitAccessResolver(app).mahdollisetRaporttienTyypitOrganisaatiolle(hierarkia, koulutusmuodot) should contain(VapaanSivistystyönOppivelvollisilleSuunnatunKoulutuksenRaportti)
      kysely.queryAllowed(app) should be(true)
    }
  }

  private lazy val raportitService = new RaportitService(KoskiApplicationForTests)

  private def arvioimatonOpintokokonaisuus(koulutusmoduuli: OppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus) =
    OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus(koulutusmoduuli = koulutusmoduuli, arviointi = None)

  private def getRaportti(
    organisaatioOid: String,
    alku: LocalDate = date(2020, 1, 1),
    loppu: LocalDate = date(2026, 1, 1),
  ): OppilaitosRaporttiResponse = {
    val request = AikajaksoRaporttiRequest(organisaatioOid, None, "password", alku, loppu, "fi")
    val t = new LocalizationReader(KoskiApplicationForTests.koskiLocalizationRepository, "fi")
    raportitService.vstKops(request, t)
  }

  private def getSheet(raportti: OppilaitosRaporttiResponse): DynamicDataSheet =
    raportti.sheets.head.asInstanceOf[DynamicDataSheet]

  private def getRow(sheet: DynamicDataSheet, oppija: LaajatOppijaHenkilöTiedot): Map[String, Any] =
    sheet.rows
      .find(_.head == oppija.oid)
      .map(row => sheet.columnSettings.map(_.title).zip(row).toMap)
      .getOrElse(fail(s"Riviä oppijalle ${oppija.oid} ei löytynyt"))
}
