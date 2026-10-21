package fi.oph.koski.raportit

import com.typesafe.config.ConfigValueFactory.fromAnyRef
import fi.oph.koski.{DirtiesFixtures, KoskiApplicationForTests}
import fi.oph.koski.api.misc.PutOpiskeluoikeusTestMethods
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.documentation.VapaaSivistystyöExample
import fi.oph.koski.henkilo.{KoskiSpecificMockOppijat, LaajatOppijaHenkilöTiedot}
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.massaluovutus.raportit.MassaluovutusQueryVSTOsaamismerkit
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.raportit.vst.VSTOsaamismerkkiRow
import fi.oph.koski.raportointikanta.RaportointikantaTestMethods
import fi.oph.koski.schema._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate
import java.time.LocalDate.{of => date}
import scala.reflect.runtime.universe

class VstOsaamismerkitRaporttiSpec
  extends AnyFreeSpec
    with Matchers
    with RaportointikantaTestMethods
    with DirtiesFixtures
    with PutOpiskeluoikeusTestMethods[VapaanSivistystyönOpiskeluoikeus] {

  override def tag: universe.TypeTag[VapaanSivistystyönOpiskeluoikeus] = implicitly[universe.TypeTag[VapaanSivistystyönOpiskeluoikeus]]
  override def defaultOpiskeluoikeus: VapaanSivistystyönOpiskeluoikeus = VapaaSivistystyöExample.opiskeluoikeusOsaamismerkki()

  private val vahvistettuOppija = KoskiSpecificMockOppijat.vapaaSivistystyöOsaamismerkki
  private val keskeneräinenOppija = KoskiSpecificMockOppijat.vapaaSivistystyöLukutaitoKoulutus

  // Käyttöliittymästä tallennetulta ensimmäiseltä versiolta saa puuttua arviointi ja vahvistus
  override protected def alterFixture(): Unit = {
    createOrUpdate(keskeneräinenOppija, VapaaSivistystyöExample.opiskeluoikeusOsaamismerkki(päivä = date(2025, 2, 2), koodiarvo = "1003").copy(
      suoritukset = List(VapaaSivistystyöExample.suoritusOsaamismerkki(koodiarvo = "1003").copy(vahvistus = None, arviointi = None))
    ))
    reloadRaportointikanta()
  }

  "Vapaan sivistystyön osaamismerkkien raportti" - {
    val koulutustoimijaOid: String = MockOrganisaatiot.varsinaisSuomenAikuiskoulutussäätiö
    val oppilaitosOid: String = MockOrganisaatiot.varsinaisSuomenKansanopisto

    val expectedColumns: List[String] = List(
      "Oppijan oid",
      "Oppijan master-oid",
      "Hetu",
      "Sukunimi",
      "Etunimet",
      "Yksilöity",
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
      "Osaamismerkin koodiarvo",
      "Osaamismerkin nimi",
      "Suoritus arvioitu",
      "Suorituksen arviointipäivä",
      "Suoritus vahvistettu",
      "Suorituksen vahvistuspäivä",
    )

    "Oppilaitoksen raportti latautuu ja sisältää oikeat sarakkeet" in {
      val sheet = getSheet(getRaportti(oppilaitosOid))
      sheet.columnSettings.map(_._2.title) should equal(expectedColumns)
    }

    "Vahvistetun osaamismerkin rivi" in {
      val row = getRow(getSheet(getRaportti(oppilaitosOid)), vahvistettuOppija)
      row("opiskeluoikeudenAlkamispäivä") should equal(Some(date(2024, 1, 1)))
      row("opiskeluoikeudenPäättymispäivä") should equal(Some(date(2024, 1, 1)))
      row("viimeisinTila") should equal(Some("hyvaksytystisuoritettu"))
      row("osaamismerkkiKoodiarvo") should equal("1001")
      row("osaamismerkkiNimi").asInstanceOf[Option[String]] should not be empty
      row("suoritusArvioitu") should equal(true)
      row("suorituksenArviointipaiva") should equal(Some(date(2024, 1, 1)))
      row("suoritusVahvistettu") should equal(true)
      row("suorituksenVahvistuspäivä") should equal(Some(date(2024, 1, 1)))
    }

    "Arvioimattoman ja vahvistamattoman osaamismerkin rivi" in {
      val row = getRow(getSheet(getRaportti(oppilaitosOid)), keskeneräinenOppija)
      row("opiskeluoikeudenPäättymispäivä") should equal(Some(date(2025, 2, 2)))
      row("osaamismerkkiKoodiarvo") should equal("1003")
      row("suoritusArvioitu") should equal(false)
      row("suorituksenArviointipaiva") should equal(None)
      row("suoritusVahvistettu") should equal(false)
      row("suorituksenVahvistuspäivä") should equal(None)
    }

    "Oppijan jokainen osaamismerkki on omalla rivillään" in {
      val koodiarvot = getSheet(getRaportti(oppilaitosOid)).rows.collect {
        case r: VSTOsaamismerkkiRow if r.oppijaOid == KoskiSpecificMockOppijat.moniaEriOpiskeluoikeuksia.oid => r.osaamismerkkiKoodiarvo
      }
      koodiarvot should contain theSameElementsAs List("1001", "1002")
    }

    "Koulutustoimijan raportti sisältää samat oppijat" in {
      val oppilaitoksenOppijat = oppijaOidit(getRaportti(oppilaitosOid))
      oppijaOidit(getRaportti(koulutustoimijaOid)) should contain theSameElementsAs oppilaitoksenOppijat
      oppilaitoksenOppijat should contain allOf(vahvistettuOppija.oid, keskeneräinenOppija.oid)
    }

    "Aikarajaus toimii" in {
      oppijaOidit(getRaportti(oppilaitosOid, alku = date(2020, 1, 1), loppu = date(2023, 12, 31))) shouldBe empty
      val oppijat = oppijaOidit(getRaportti(oppilaitosOid, alku = date(2025, 2, 2), loppu = date(2025, 2, 2)))
      oppijat should contain(keskeneräinenOppija.oid)
      oppijat should not contain vahvistettuOppija.oid
    }
  }

  "Raportti ei ole käytössä tuotantoympäristössä" - {
    val app = KoskiApplicationForTests
    val tuotantoConfig = app.config.withValue("opintopolku.virkailija.url", fromAnyRef("https://virkailija.opintopolku.fi"))
    val oppilaitosOid = MockOrganisaatiot.varsinaisSuomenKansanopisto
    implicit val session = MockUsers.varsinaisSuomiPalvelukäyttäjä.toKoskiSpecificSession(app.käyttöoikeusRepository)
    val hierarkia = app.organisaatioRepository.getOrganisaatioHierarkia(oppilaitosOid).get
    val koulutusmuodot = Map(oppilaitosOid -> Seq("vapaansivistystyonkoulutus"))
    val kysely = MassaluovutusQueryVSTOsaamismerkit(
      organisaatioOid = Some(oppilaitosOid),
      alku = date(2024, 1, 1),
      loppu = date(2025, 12, 31),
    )

    "raporttityyppiä ei tarjota käyttöliittymälle" in {
      val tyypit = RaportitAccessResolver(app.organisaatioRepository, app.koodistoPalvelu, app.raportointiDatabase, tuotantoConfig)
        .mahdollisetRaporttienTyypitOrganisaatiolle(hierarkia, koulutusmuodot)
      tyypit should contain(JatkuvanOppimisenVapaanSivistystyönRaportti)
      tyypit should not contain VapaanSivistystyönOsaamismerkkienRaportti
    }

    "massaluovutuskysely ei ole sallittu" in {
      kysely.queryAllowed(KoskiApplication(tuotantoConfig)) should be(false)
    }

    "muissa ympäristöissä raportti ja kysely ovat käytössä" in {
      RaportitAccessResolver(app).mahdollisetRaporttienTyypitOrganisaatiolle(hierarkia, koulutusmuodot) should contain(VapaanSivistystyönOsaamismerkkienRaportti)
      kysely.queryAllowed(app) should be(true)
    }
  }

  private lazy val raportitService = new RaportitService(KoskiApplicationForTests)

  private def getRaportti(
    organisaatioOid: String,
    alku: LocalDate = date(2020, 1, 1),
    loppu: LocalDate = date(2026, 1, 1),
  ): OppilaitosRaporttiResponse = {
    val request = AikajaksoRaporttiRequest(organisaatioOid, None, "password", alku, loppu, "fi")
    val t = new LocalizationReader(KoskiApplicationForTests.koskiLocalizationRepository, "fi")
    raportitService.vstOsaamismerkit(request, t)
  }

  private def getSheet(raportti: OppilaitosRaporttiResponse): DataSheet =
    raportti.sheets.head.asInstanceOf[DataSheet]

  private def oppijaOidit(raportti: OppilaitosRaporttiResponse): Seq[String] =
    getSheet(raportti).rows.collect { case r: VSTOsaamismerkkiRow => r.oppijaOid }

  private def getRow(sheet: DataSheet, oppija: LaajatOppijaHenkilöTiedot): Map[String, Any] =
    sheet.rows
      .collectFirst { case r: VSTOsaamismerkkiRow if r.oppijaOid == oppija.oid => r }
      .map(r => r.productElementNames.zip(r.productIterator).toMap)
      .getOrElse(fail(s"Riviä oppijalle ${oppija.oid} ei löytynyt"))
}
