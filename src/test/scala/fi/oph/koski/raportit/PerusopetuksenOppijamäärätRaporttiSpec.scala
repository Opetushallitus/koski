package fi.oph.koski.raportit

import fi.oph.koski.documentation.ExampleData.{opiskeluoikeusEronnut, opiskeluoikeusLäsnä, vahvistusPaikkakunnalla}
import fi.oph.koski.documentation.{PerusopetusExampleData, YleissivistavakoulutusExampleData}
import fi.oph.koski.documentation.PerusopetusExampleData.{kahdeksannenLuokanSuoritus, perusopetuksenOppimääränSuoritus, seitsemännenLuokanLuokallejääntiSuoritus, seitsemännenLuokanSuoritus, yhdeksännenLuokanSuoritus}
import fi.oph.koski.henkilo.{KoskiSpecificMockOppijat, VerifiedHenkilöOid}
import fi.oph.koski.http.HttpStatus

import java.time.LocalDate.{of => date}
import fi.oph.koski.{DirtiesFixtures, KoskiApplicationForTests}
import fi.oph.koski.koskiuser.KoskiMockUser
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.organisaatio.MockOrganisaatiot.jyväskylänNormaalikoulu
import fi.oph.koski.raportit.perusopetus.{PerusopetuksenOppijamäärätRaportti, PerusopetuksenOppijamäärätRaporttiRow}
import fi.oph.koski.raportointikanta.RaportointikantaTestMethods
import fi.oph.koski.schema.{Aikajakso, ErityisenTuenPäätös, NuortenPerusopetuksenOpiskeluoikeudenTila, NuortenPerusopetuksenOpiskeluoikeusjakso, Opiskeluoikeus, PerusopetuksenOpiskeluoikeudenLisätiedot, PerusopetuksenOpiskeluoikeus, Tukijakso}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class PerusopetuksenOppijamäärätRaporttiSpec extends AnyFreeSpec with Matchers with RaportointikantaTestMethods with BeforeAndAfterAll with DirtiesFixtures
{
  private val raportointipäivä = date(2012, 1, 1)
  private val tuenPäätöksenJaksonRaportointipäivä = date(2026, 8, 1)
  private val valmistumispäivänRaportointipäivä = date(2025, 5, 31)
  private val eronnutRaportointipäivä = date(2026, 1, 15)

  var rikkinäisetOpiskeluoikeusOidit: Seq[Opiskeluoikeus.Oid] = Seq()

  override protected def alterFixture(): Unit = {
    // Lisää validointien osalta rikkinäisiä opiskeluoikeuksia suoraan tietokantaan, koska raportti kertoo
    // rikkinäisyyksistä.
    def create(oo: PerusopetuksenOpiskeluoikeus): Either[HttpStatus, Opiskeluoikeus.Oid] = {
      val createResult = application.opiskeluoikeusRepository.createOrUpdate(
        oppijaOid = VerifiedHenkilöOid(KoskiSpecificMockOppijat.vuonna2005SyntynytEiOpiskeluoikeuksiaFikstuurissa),
        opiskeluoikeus = oo,
        allowUpdate = false,
      )(session(defaultUser))
      createResult.map(_.created) should be(Right(true))
      createResult.map(_.oid)
    }

    eiRikkinäinäRaportoitavatTestiopiskeluoikeudet.map(create)
    rikkinäisetOpiskeluoikeusOidit = rikkinäisetTestiopiskeluoikeudet.map(create).map(_.getOrElse(throw new Error))

    tuenPäätöksenJaksojenTestiopiskeluiokeudet.map(create)

    create(valmistumispäivänäNäkyväOpiskeluoikeus)
    create(eronnutOpiskeluoikeus)

    application.perustiedotIndexer.sync(refresh = true)
    reloadRaportointikanta()
  }

  private val eiRikkinäinäRaportoitavatTestiopiskeluoikeudet =
    List(
      rikkinäinenMuttaRaportoidaanVainKotiopetuksenaPidennettyOppivelvollisuusOpiskeluoikeus,
      ehjäPidennettyOppivelvollisuusTarvittavienTietojenKanssaOpiskeluoikeus,
      ehjäPelkkäErityinenTukiOpiskeluoikeus
    )

  private val tuenPäätöksenJaksojenTestiopiskeluiokeudet = List(
    tuenPäätöstenRaportilleOsuvaOpiskeluoikeus(
      PerusopetuksenOpiskeluoikeudenLisätiedot(
        tuenPäätöksenJaksot = Some(List(Tukijakso(Some(tuenPäätöksenJaksonRaportointipäivä), None))),
        opetuksenJärjestäminenVammanSairaudenTaiRajoitteenPerusteella = Some(List(Aikajakso(tuenPäätöksenJaksonRaportointipäivä, None)))
      )
    ),
    tuenPäätöstenRaportilleOsuvaOpiskeluoikeus(
      PerusopetuksenOpiskeluoikeudenLisätiedot(
        tuenPäätöksenJaksot = Some(List(Tukijakso(Some(tuenPäätöksenJaksonRaportointipäivä), None))),
        toimintaAlueittainOpiskelu = Some(List(Aikajakso(tuenPäätöksenJaksonRaportointipäivä, None)))
      )
    ),
    tuenPäätöstenRaportilleOsuvaOpiskeluoikeus(
      PerusopetuksenOpiskeluoikeudenLisätiedot(
        tuenPäätöksenJaksot = Some(List(Tukijakso(Some(tuenPäätöksenJaksonRaportointipäivä), None)))
      )
    )
  )

  private val rikkinäisetTestiopiskeluoikeudet =
    List(
      rikkinäinenPelkkäPidennettyOppivelvollisuusOpiskeluoikeus,
      rikkinäinenPidennettyOppivelvollisuusIlmanVammaisuuttaOpiskeluoikeus,
      rikkinäinenPidennettyOppivelvollisuusIlmanErityisenTuenPäätöstäOpiskeluoikeus,
      rikkinäinenPelkkäVaikeastiVammaisuusOpiskeluoikeus,
      rikkinäinenPelkkäVammaisuusOpiskeluoikeus,
      rikkinäinenPäällekäisetVammaisuudetOpiskeluoikeus,
    )

  private val ylimääräisetKotiopetusLkm = 1
  private val ylimääräisetLkm = eiRikkinäinäRaportoitavatTestiopiskeluoikeudet.length + rikkinäisetTestiopiskeluoikeudet.length - ylimääräisetKotiopetusLkm
  private val ylimääräisetErityiselläTuellaOpiskeluoikeudet = 4
  private val ylimääräisetVaikeastiVammaisetLkm = 0
  private val ylimääräisetMuuKuinVaikeastiVammaisetLkm = 1
  private val rikkinäisetYlimääräisetLkm = 6

  private def ehjäPidennettyOppivelvollisuusTarvittavienTietojenKanssaOpiskeluoikeus: PerusopetuksenOpiskeluoikeus =
    raportin8LuokanRivilleOsuvaOpiskeluoikeus(
      PerusopetuksenOpiskeluoikeudenLisätiedot(
        pidennettyOppivelvollisuus = raportointipäiväänOsuvaPidennettyOppivelvollisuus,
        vammainen = raportointipäiväänOsuvaVammaisuustieto,
        erityisenTuenPäätökset = raportointipäiväänOsuvaErityisenTuenPäätös
      )
    )

  private def rikkinäinenPelkkäPidennettyOppivelvollisuusOpiskeluoikeus: PerusopetuksenOpiskeluoikeus =
    raportin8LuokanRivilleOsuvaOpiskeluoikeus(
      PerusopetuksenOpiskeluoikeudenLisätiedot(
        pidennettyOppivelvollisuus = raportointipäiväänOsuvaPidennettyOppivelvollisuus
      )
    )

  private def rikkinäinenMuttaRaportoidaanVainKotiopetuksenaPidennettyOppivelvollisuusOpiskeluoikeus: PerusopetuksenOpiskeluoikeus =
    raportin8LuokanRivilleOsuvaOpiskeluoikeus(
      PerusopetuksenOpiskeluoikeudenLisätiedot(
        pidennettyOppivelvollisuus = raportointipäiväänOsuvaPidennettyOppivelvollisuus,
        kotiopetusjaksot = raportointipäiväänOsuvaKotiopetustieto
      )
    )

  private def rikkinäinenPidennettyOppivelvollisuusIlmanVammaisuuttaOpiskeluoikeus: PerusopetuksenOpiskeluoikeus =
    raportin8LuokanRivilleOsuvaOpiskeluoikeus(
      PerusopetuksenOpiskeluoikeudenLisätiedot(
        pidennettyOppivelvollisuus = raportointipäiväänOsuvaPidennettyOppivelvollisuus,
        erityisenTuenPäätökset = raportointipäiväänOsuvaErityisenTuenPäätös
      )
    )

  private def rikkinäinenPidennettyOppivelvollisuusIlmanErityisenTuenPäätöstäOpiskeluoikeus: PerusopetuksenOpiskeluoikeus =
    raportin8LuokanRivilleOsuvaOpiskeluoikeus(
      PerusopetuksenOpiskeluoikeudenLisätiedot(
        pidennettyOppivelvollisuus = raportointipäiväänOsuvaPidennettyOppivelvollisuus,
        vaikeastiVammainen = raportointipäiväänOsuvaVammaisuustieto
      )
    )

  private def rikkinäinenPelkkäVaikeastiVammaisuusOpiskeluoikeus: PerusopetuksenOpiskeluoikeus =
    raportin8LuokanRivilleOsuvaOpiskeluoikeus(
      PerusopetuksenOpiskeluoikeudenLisätiedot(
        vaikeastiVammainen = raportointipäiväänOsuvaVammaisuustieto
      )
    )

  private def rikkinäinenPelkkäVammaisuusOpiskeluoikeus: PerusopetuksenOpiskeluoikeus =
    raportin8LuokanRivilleOsuvaOpiskeluoikeus(
      PerusopetuksenOpiskeluoikeudenLisätiedot(
        vammainen = raportointipäiväänOsuvaVammaisuustieto
      )
    )

  private def rikkinäinenPäällekäisetVammaisuudetOpiskeluoikeus: PerusopetuksenOpiskeluoikeus =
    raportin8LuokanRivilleOsuvaOpiskeluoikeus(
      PerusopetuksenOpiskeluoikeudenLisätiedot(
        pidennettyOppivelvollisuus = raportointipäiväänOsuvaPidennettyOppivelvollisuus,
        vammainen = raportointipäiväänOsuvaVammaisuustieto,
        vaikeastiVammainen = raportointipäiväänOsuvaVammaisuustieto,
        erityisenTuenPäätökset = raportointipäiväänOsuvaErityisenTuenPäätös
      )
    )

  private def ehjäPelkkäErityinenTukiOpiskeluoikeus: PerusopetuksenOpiskeluoikeus =
    raportin8LuokanRivilleOsuvaOpiskeluoikeus(
      PerusopetuksenOpiskeluoikeudenLisätiedot(
        erityisenTuenPäätökset = raportointipäiväänOsuvaErityisenTuenPäätös
      )
    )

  private def raportin8LuokanRivilleOsuvaOpiskeluoikeus(
    lisätiedot: PerusopetuksenOpiskeluoikeudenLisätiedot
  ): PerusopetuksenOpiskeluoikeus =
    raportin8LuokanRivilleOsuvaOpiskeluoikeus(Some(lisätiedot))

  private def raportin8LuokanRivilleOsuvaOpiskeluoikeus(
    lisätiedot: Option[PerusopetuksenOpiskeluoikeudenLisätiedot] = None
  ): PerusopetuksenOpiskeluoikeus = {
    val oppilaitos = YleissivistavakoulutusExampleData.jyväskylänNormaalikoulu
    val toimipiste = YleissivistavakoulutusExampleData.jyväskylänNormaalikoulu
    val luokka = "C"
    val alkamispäivä = date(2006, 8, 15)
    val päättymispäivä = Some(date(2013, 6, 4))

    PerusopetusExampleData.opiskeluoikeus(
      oppilaitos = oppilaitos,
      suoritukset = List(
        seitsemännenLuokanLuokallejääntiSuoritus.copy(toimipiste = toimipiste, luokka = "7" + luokka, alkamispäivä = Some(date(2010, 8, 15)), vahvistus = vahvistusPaikkakunnalla(date(2011, 5, 30))),
        kahdeksannenLuokanSuoritus.copy(toimipiste = toimipiste, luokka = "8" + luokka, alkamispäivä = Some(date(2011, 8, 15)), vahvistus = vahvistusPaikkakunnalla(date(2012, 5, 30))),
        yhdeksännenLuokanSuoritus.copy(toimipiste = toimipiste, luokka = "9" + luokka, alkamispäivä = Some(date(2012, 8, 15)), vahvistus = vahvistusPaikkakunnalla(date(2013, 5, 30))),
        perusopetuksenOppimääränSuoritus.copy(toimipiste = toimipiste, vahvistus = vahvistusPaikkakunnalla(date(2013, 6, 4)))
      ),
      alkamispäivä = alkamispäivä,
      päättymispäivä = päättymispäivä
    ).copy(
      lisätiedot = lisätiedot
    )
  }

  private def tuenPäätöstenRaportilleOsuvaOpiskeluoikeus(
    lisätiedot: PerusopetuksenOpiskeluoikeudenLisätiedot
  ): PerusopetuksenOpiskeluoikeus = {
    PerusopetusExampleData.opiskeluoikeus(
      suoritukset = List(
        seitsemännenLuokanSuoritus.copy(
          alkamispäivä = Some(tuenPäätöksenJaksonRaportointipäivä),
          vahvistus = None
        ),
      ),
      alkamispäivä = tuenPäätöksenJaksonRaportointipäivä,
      päättymispäivä = None
    ).copy(
      lisätiedot = Some(lisätiedot)
    )
  }

  private def valmistumispäivänäNäkyväOpiskeluoikeus: PerusopetuksenOpiskeluoikeus = {
    val oppilaitos = YleissivistavakoulutusExampleData.jyväskylänNormaalikoulu
    val toimipiste = YleissivistavakoulutusExampleData.jyväskylänNormaalikoulu
    val alkamispäivä = date(2016, 8, 15)
    val päättymispäivä = Some(date(2025, 6, 4))

    PerusopetusExampleData.opiskeluoikeus(
      oppilaitos = oppilaitos,
      suoritukset = List(
        yhdeksännenLuokanSuoritus.copy(
          toimipiste = toimipiste,
          luokka = "9D",
          alkamispäivä = Some(date(2024, 8, 15)),
          vahvistus = vahvistusPaikkakunnalla(valmistumispäivänRaportointipäivä)
        ),
        perusopetuksenOppimääränSuoritus.copy(
          toimipiste = toimipiste,
          vahvistus = vahvistusPaikkakunnalla(päättymispäivä.get)
        )
      ),
      alkamispäivä = alkamispäivä,
      päättymispäivä = päättymispäivä
    )
  }

  private def eronnutOpiskeluoikeus: PerusopetuksenOpiskeluoikeus = {
    val alkamispäivä = date(2025, 8, 15)

    PerusopetusExampleData.opiskeluoikeus(
      suoritukset = List(
        yhdeksännenLuokanSuoritus.copy(
          alkamispäivä = Some(alkamispäivä),
          vahvistus = None
        ),
      ),
      alkamispäivä = alkamispäivä,
      päättymispäivä = None
    ).copy(
      tila = NuortenPerusopetuksenOpiskeluoikeudenTila(List(
        NuortenPerusopetuksenOpiskeluoikeusjakso(alkamispäivä, opiskeluoikeusLäsnä),
        NuortenPerusopetuksenOpiskeluoikeusjakso(eronnutRaportointipäivä, opiskeluoikeusEronnut)
      ))
    )
  }

  private def raportointipäiväänOsuvaVammaisuustieto = Some(List(raportointipäiväänOsuvaAikajakso))
  private def raportointipäiväänOsuvaPidennettyOppivelvollisuus = Some(raportointipäiväänOsuvaAikajakso)
  private def raportointipäiväänOsuvaKotiopetustieto = Some(List(raportointipäiväänOsuvaAikajakso))
  private def raportointipäiväänOsuvaErityisenTuenPäätös =  Some(List(
    ErityisenTuenPäätös(
      alku = Some(raportointipäiväänOsuvaAikajakso.alku),
      loppu = raportointipäiväänOsuvaAikajakso.loppu,
      erityisryhmässä = None
    )
  ))

  private def raportointipäiväänOsuvaAikajakso =
    Aikajakso(raportointipäivä.minusDays(10), Some(raportointipäivä.plusDays(10)))

  private def session(user: KoskiMockUser) = user.toKoskiSpecificSession(application.käyttöoikeusRepository)

  private val application = KoskiApplicationForTests
  private val t = new LocalizationReader(KoskiApplicationForTests.koskiLocalizationRepository, "fi")

  "Perusopetuksen oppijamäärien raportti" - {
    "Raportti voidaan ladata ja lataaminen tuottaa auditlogin" in {
      authGet(s"api/raportit/perusopetuksenoppijamaaratraportti?oppilaitosOid=$jyväskylänNormaalikoulu&paiva=2007-01-01&lang=fi&password=salasana") {
        verifyResponseStatusOk()
        response.headers("Content-Disposition").head should equal(s"""attachment; filename="perusopetus_tunnuslukuraportti-2007-01-01.xlsx"""")
        response.bodyBytes.take(ENCRYPTED_XLSX_PREFIX.length) should equal(ENCRYPTED_XLSX_PREFIX)
        AuditLogTester.verifyLastAuditLogMessage(
          Map(
            "operation" -> "OPISKELUOIKEUS_RAPORTTI",
            "target" -> Map(
              "hakuEhto" -> s"raportti=perusopetuksenoppijamaaratraportti&oppilaitosOid=$jyväskylänNormaalikoulu&paiva=2007-01-01&lang=fi"
            )
          )
        )
      }
    }

    "Raportti voidaan ladata eri lokalisaatiolla ja lataaminen tuottaa auditlogin" in {
      authGet(s"api/raportit/perusopetuksenoppijamaaratraportti?oppilaitosOid=$jyväskylänNormaalikoulu&paiva=2007-01-01&lang=sv&password=salasana") {
        verifyResponseStatusOk()
        response.headers("Content-Disposition").head should equal(s"""attachment; filename="grundläggande_nyckeltal_rapport-2007-01-01.xlsx"""")
        response.bodyBytes.take(ENCRYPTED_XLSX_PREFIX.length) should equal(ENCRYPTED_XLSX_PREFIX)
        AuditLogTester.verifyLastAuditLogMessage(
          Map(
            "operation" -> "OPISKELUOIKEUS_RAPORTTI",
            "target" -> Map(
              "hakuEhto" -> s"raportti=perusopetuksenoppijamaaratraportti&oppilaitosOid=$jyväskylänNormaalikoulu&paiva=2007-01-01&lang=sv"
            )
          )
        )
      }
    }
  }

  "Perusopetuksen oppijamäärien raportti - päävälilehti" in {
    val rows = perusopetuksenOppijamäärätRaportti.filter(_.oppilaitosNimi.equals("Jyväskylän normaalikoulu"))
    rows.length should be(5)
    rows.toList should equal(List(
      PerusopetuksenOppijamäärätRaporttiRow(
        oppilaitosNimi = "Jyväskylän normaalikoulu",
        organisaatioOid = "1.2.246.562.10.14613773812",
        opetuskieli = "ruotsi,suomi",
        vuosiluokka = "5",
        oppilaita = 1,
        vieraskielisiä = 0,
        pidOppivelvollisuusEritTukiJaVaikeastiVammainen = 0,
        pidOppivelvollisuusEritTukiJaMuuKuinVaikeimminVammainen = 0,
        tuenPäätöksenJakso = 0,
        opetuksenJärjestäminenVammanSairaudenTaiRajoitteenPerusteella = 0,
        toimintaAlueittainOpiskelu = 0,
        tavoitekokonaisuuksittainOpiskelu = 0,
        erityiselläTuella = 0,
        majoitusetu = 0,
        kuljetusetu = 0,
        sisäoppilaitosmainenMajoitus = 0,
        koulukoti = 0,
        joustavaPerusopetus = 0
      ),
      PerusopetuksenOppijamäärätRaporttiRow(
        oppilaitosNimi = "Jyväskylän normaalikoulu",
        organisaatioOid = "1.2.246.562.10.14613773812",
        opetuskieli = "ruotsi,suomi",
        vuosiluokka = "6",
        oppilaita = 2,
        vieraskielisiä = 0,
        pidOppivelvollisuusEritTukiJaVaikeastiVammainen = 0,
        pidOppivelvollisuusEritTukiJaMuuKuinVaikeimminVammainen = 0,
        tuenPäätöksenJakso = 0,
        opetuksenJärjestäminenVammanSairaudenTaiRajoitteenPerusteella = 0,
        toimintaAlueittainOpiskelu = 0,
        tavoitekokonaisuuksittainOpiskelu = 0,
        erityiselläTuella = 0,
        majoitusetu = 0,
        kuljetusetu = 0,
        sisäoppilaitosmainenMajoitus = 0,
        koulukoti = 0,
        joustavaPerusopetus = 0
      ),
      PerusopetuksenOppijamäärätRaporttiRow(
        oppilaitosNimi = "Jyväskylän normaalikoulu",
        organisaatioOid = "1.2.246.562.10.14613773812",
        opetuskieli = "ruotsi,suomi",
        vuosiluokka = "7",
        oppilaita = 2,
        vieraskielisiä = 1,
        pidOppivelvollisuusEritTukiJaVaikeastiVammainen = 1,
        pidOppivelvollisuusEritTukiJaMuuKuinVaikeimminVammainen = 1,
        tuenPäätöksenJakso = 0,
        opetuksenJärjestäminenVammanSairaudenTaiRajoitteenPerusteella = 0,
        toimintaAlueittainOpiskelu = 0,
        tavoitekokonaisuuksittainOpiskelu = 0,
        erityiselläTuella = 2,
        majoitusetu = 1,
        kuljetusetu = 1,
        sisäoppilaitosmainenMajoitus = 1,
        koulukoti = 1,
        joustavaPerusopetus = 1,
      ),
      PerusopetuksenOppijamäärätRaporttiRow(
        oppilaitosNimi = "Jyväskylän normaalikoulu",
        organisaatioOid = "1.2.246.562.10.14613773812",
        opetuskieli = "ruotsi,suomi",
        vuosiluokka = "8",
        oppilaita = 1 + ylimääräisetLkm,
        vieraskielisiä = 0,
        pidOppivelvollisuusEritTukiJaVaikeastiVammainen = 1 + ylimääräisetVaikeastiVammaisetLkm,
        pidOppivelvollisuusEritTukiJaMuuKuinVaikeimminVammainen = 0 + ylimääräisetMuuKuinVaikeastiVammaisetLkm,
        tuenPäätöksenJakso = 0,
        opetuksenJärjestäminenVammanSairaudenTaiRajoitteenPerusteella = 0,
        toimintaAlueittainOpiskelu = 0,
        tavoitekokonaisuuksittainOpiskelu = 0,
        erityiselläTuella = 1 + ylimääräisetErityiselläTuellaOpiskeluoikeudet,
        majoitusetu = 1,
        kuljetusetu = 1,
        sisäoppilaitosmainenMajoitus = 1,
        koulukoti = 1,
        joustavaPerusopetus = 1,
      ),
      PerusopetuksenOppijamäärätRaporttiRow(
        oppilaitosNimi = "Jyväskylän normaalikoulu",
        organisaatioOid = "1.2.246.562.10.14613773812",
        opetuskieli = "ruotsi,suomi",
        vuosiluokka = "Kaikki vuosiluokat yhteensä",
        oppilaita = 6 + ylimääräisetLkm,
        vieraskielisiä = 1,
        pidOppivelvollisuusEritTukiJaVaikeastiVammainen = 2 + ylimääräisetVaikeastiVammaisetLkm,
        pidOppivelvollisuusEritTukiJaMuuKuinVaikeimminVammainen = 1 + ylimääräisetMuuKuinVaikeastiVammaisetLkm,
        tuenPäätöksenJakso = 0,
        opetuksenJärjestäminenVammanSairaudenTaiRajoitteenPerusteella = 0,
        toimintaAlueittainOpiskelu = 0,
        tavoitekokonaisuuksittainOpiskelu = 0,
        erityiselläTuella = 3 + ylimääräisetErityiselläTuellaOpiskeluoikeudet,
        majoitusetu = 2,
        kuljetusetu = 2,
        sisäoppilaitosmainenMajoitus = 2,
        koulukoti = 2,
        joustavaPerusopetus = 2,
      )
    ))
  }

  "Perusopetuksen oppijamäärien raportti - tuen päätöksen jaksoja raportilla" in {
    val rows = perusopetuksenOppijamäärätRaporttiBuilder
      .build(Seq(jyväskylänNormaalikoulu), tuenPäätöksenJaksonRaportointipäivä, t)(session(defaultUser))
      .rows.map(_.asInstanceOf[PerusopetuksenOppijamäärätRaporttiRow])
      .filter(_.oppilaitosNimi.equals("Jyväskylän normaalikoulu"))

    rows should contain (
      PerusopetuksenOppijamäärätRaporttiRow(
        oppilaitosNimi = "Jyväskylän normaalikoulu",
        organisaatioOid = "1.2.246.562.10.14613773812",
        opetuskieli = "ruotsi,suomi",
        vuosiluokka = "7",
        oppilaita = 5,
        vieraskielisiä = 1,
        pidOppivelvollisuusEritTukiJaVaikeastiVammainen = 0,
        pidOppivelvollisuusEritTukiJaMuuKuinVaikeimminVammainen = 0,
        tuenPäätöksenJakso = 3,
        opetuksenJärjestäminenVammanSairaudenTaiRajoitteenPerusteella = 1,
        toimintaAlueittainOpiskelu = 1,
        tavoitekokonaisuuksittainOpiskelu = 0,
        erityiselläTuella = 0,
        majoitusetu = 0,
        kuljetusetu = 0,
        sisäoppilaitosmainenMajoitus = 0,
        koulukoti = 0,
        joustavaPerusopetus = 0,
      )
    )
  }

  "Perusopetuksen oppijamäärien raportti - ei useita rivejä vaikka kieliä olisi useampi" in {
    val rows = perusopetuksenOppijamäärätRaportti
    rows.groupBy(it => it.organisaatioOid).values
      .foreach(rowsForOrg => rowsForOrg.map(_.opetuskieli).distinct should have length 1)
  }

  "Perusopetuksen oppijamäärien raportti - eronnut-tilaiset oppijat näkyvät raportilla" in {
    val rows = perusopetuksenOppijamäärätRaporttiBuilder
      .build(Seq(jyväskylänNormaalikoulu), eronnutRaportointipäivä, t)(session(defaultUser))
      .rows.map(_.asInstanceOf[PerusopetuksenOppijamäärätRaporttiRow])
      .filter(_.oppilaitosNimi.equals("Jyväskylän normaalikoulu"))

    rows.find(_.vuosiluokka == "9").map(_.oppilaita).getOrElse(0) should be > 0
  }

  "Perusopetuksen oppijamäärien raportti - vuosiluokan suorituksen vahvistuspäivänä oppija näkyy raportilla" in {
    def oppilaita9Luokalla(päivä: java.time.LocalDate): Int = {
      perusopetuksenOppijamäärätRaporttiBuilder
        .build(Seq(jyväskylänNormaalikoulu), päivä, t)(session(defaultUser))
        .rows.map(_.asInstanceOf[PerusopetuksenOppijamäärätRaporttiRow])
        .filter(_.oppilaitosNimi.equals("Jyväskylän normaalikoulu"))
        .find(_.vuosiluokka == "9")
        .map(_.oppilaita)
        .getOrElse(0)
    }

    val edellisenPäivänOppilaita = oppilaita9Luokalla(valmistumispäivänRaportointipäivä.minusDays(1))
    val vahvistuspäivänOppilaita = oppilaita9Luokalla(valmistumispäivänRaportointipäivä)

    vahvistuspäivänOppilaita should equal(edellisenPäivänOppilaita)
  }

  private val perusopetuksenOppijamäärätRaporttiBuilder = PerusopetuksenOppijamäärätRaportti(application.raportointiDatabase.db, application.organisaatioService)
  private lazy val perusopetuksenOppijamäärätRaportti = perusopetuksenOppijamäärätRaporttiBuilder
    .build(Seq(jyväskylänNormaalikoulu), raportointipäivä, t)(session(defaultUser))
    .rows.map(_.asInstanceOf[PerusopetuksenOppijamäärätRaporttiRow])
}
