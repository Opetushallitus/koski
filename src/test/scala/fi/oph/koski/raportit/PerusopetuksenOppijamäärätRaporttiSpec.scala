package fi.oph.koski.raportit

import fi.oph.koski.documentation.ExampleData.vahvistusPaikkakunnalla
import fi.oph.koski.documentation.{PerusopetusExampleData, YleissivistavakoulutusExampleData}
import fi.oph.koski.documentation.PerusopetusExampleData.{kahdeksannenLuokanSuoritus, perusopetuksenOppimääränSuoritus, seitsemännenLuokanLuokallejääntiSuoritus, yhdeksännenLuokanSuoritus}
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat.vuonna2005SyntynytEiOpiskeluoikeuksiaFikstuurissa
import fi.oph.koski.henkilo.VerifiedHenkilöOid
import fi.oph.koski.http.HttpStatus

import java.time.LocalDate.{of => date}
import fi.oph.koski.{DirtiesFixtures, KoskiApplicationForTests}
import fi.oph.koski.koskiuser.KoskiMockUser
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.organisaatio.MockOrganisaatiot.jyväskylänNormaalikoulu
import fi.oph.koski.raportit.perusopetus.{PerusopetuksenOppijamäärätAikajaksovirheetRaportti, PerusopetuksenOppijamäärätAikajaksovirheetRaporttiRow, PerusopetuksenOppijamäärätRaportti, PerusopetuksenOppijamäärätRaporttiRow}
import fi.oph.koski.raportointikanta.RaportointikantaTestMethods
import fi.oph.koski.schema.{Aikajakso, ErityisenTuenPäätös, Opiskeluoikeus, PerusopetuksenOpiskeluoikeudenLisätiedot, PerusopetuksenOpiskeluoikeus}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class PerusopetuksenOppijamäärätRaporttiSpec extends AnyFreeSpec with Matchers with RaportointikantaTestMethods with BeforeAndAfterAll with DirtiesFixtures
{
  private val raportointipäivä = date(2012, 1, 1)

  var rikkinäisetOpiskeluoikeusOidit: Seq[Opiskeluoikeus.Oid] = Seq()

  override protected def alterFixture(): Unit = {
    // Lisää validointien osalta rikkinäisiä opiskeluoikeuksia suoraan tietokantaan, koska raportti kertoo
    // rikkinäisyyksistä.

    def create(oo: PerusopetuksenOpiskeluoikeus): Either[HttpStatus, Opiskeluoikeus.Oid] = {
      val createResult = application.opiskeluoikeusRepository.createOrUpdate(
        oppijaOid = VerifiedHenkilöOid(vuonna2005SyntynytEiOpiskeluoikeuksiaFikstuurissa),
        opiskeluoikeus = oo,
        allowUpdate = false,
        disableDuplicateChecks = true,
      )(session(defaultUser))
      createResult.map(_.created) should be(Right(true))
      createResult.map(_.oid)
    }

    eiRikkinäinäRaportoitavatTestiopiskeluoikeudet.map(create)
    rikkinäisetOpiskeluoikeusOidit = rikkinäisetTestiopiskeluoikeudet.map(create).map(_.getOrElse(throw new Error))

    application.perustiedotIndexer.sync(refresh = true)
    reloadRaportointikanta
  }

  private val eiRikkinäinäRaportoitavatTestiopiskeluoikeudet =
    List(
      rikkinäinenMuttaRaportoidaanVainKotiopetuksenaPidennettyOppivelvollisuusOpiskeluoikeus,
      ehjäPidennettyOppivelvollisuusTarvittavienTietojenKanssaOpiskeluoikeus,
      ehjäPelkkäErityinenTukiOpiskeluoikeus
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
        AuditLogTester.verifyAuditLogMessage(
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
        AuditLogTester.verifyAuditLogMessage(
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
    rows.length should be(4)
    rows.toList should equal(List(
      PerusopetuksenOppijamäärätRaporttiRow(
        oppilaitosNimi = "Jyväskylän normaalikoulu",
        organisaatioOid = "1.2.246.562.10.14613773812",
        opetuskieli = "suomi",
        vuosiluokka = "6",
        oppilaita = 2,
        vieraskielisiä = 0,
        pidOppivelvollisuusEritTukiJaVaikeastiVammainen = 0,
        pidOppivelvollisuusEritTukiJaMuuKuinVaikeimminVammainen = 0,
        virheellisestiSiirrettyjaTukitietoja = 0,
        erityiselläTuella = 0,
        majoitusetu = 0,
        kuljetusetu = 0,
        sisäoppilaitosmainenMajoitus = 0,
        koulukoti = 0,
        joustavaPerusopetus = 0,
        kotiopetus = 1
      ),
      PerusopetuksenOppijamäärätRaporttiRow(
        oppilaitosNimi = "Jyväskylän normaalikoulu",
        organisaatioOid = "1.2.246.562.10.14613773812",
        opetuskieli = "suomi",
        vuosiluokka = "7",
        oppilaita = 2,
        vieraskielisiä = 1,
        pidOppivelvollisuusEritTukiJaVaikeastiVammainen = 1,
        pidOppivelvollisuusEritTukiJaMuuKuinVaikeimminVammainen = 1,
        virheellisestiSiirrettyjaTukitietoja = 0,
        erityiselläTuella = 2,
        majoitusetu = 1,
        kuljetusetu = 1,
        sisäoppilaitosmainenMajoitus = 1,
        koulukoti = 1,
        joustavaPerusopetus = 1,
        kotiopetus = 0
      ),
      PerusopetuksenOppijamäärätRaporttiRow(
        oppilaitosNimi = "Jyväskylän normaalikoulu",
        organisaatioOid = "1.2.246.562.10.14613773812",
        opetuskieli = "suomi",
        vuosiluokka = "8",
        oppilaita = 1 + ylimääräisetLkm,
        vieraskielisiä = 0,
        pidOppivelvollisuusEritTukiJaVaikeastiVammainen = 1 + ylimääräisetVaikeastiVammaisetLkm,
        pidOppivelvollisuusEritTukiJaMuuKuinVaikeimminVammainen = 0 + ylimääräisetMuuKuinVaikeastiVammaisetLkm,
        virheellisestiSiirrettyjaTukitietoja = 0 + rikkinäisetYlimääräisetLkm,
        erityiselläTuella = 1 + ylimääräisetErityiselläTuellaOpiskeluoikeudet,
        majoitusetu = 1,
        kuljetusetu = 1,
        sisäoppilaitosmainenMajoitus = 1,
        koulukoti = 1,
        joustavaPerusopetus = 1,
        kotiopetus = 0 + ylimääräisetKotiopetusLkm
      ),
      PerusopetuksenOppijamäärätRaporttiRow(
        oppilaitosNimi = "Jyväskylän normaalikoulu",
        organisaatioOid = "1.2.246.562.10.14613773812",
        opetuskieli = "suomi",
        vuosiluokka = "Kaikki vuosiluokat yhteensä",
        oppilaita = 5 + ylimääräisetLkm,
        vieraskielisiä = 1,
        pidOppivelvollisuusEritTukiJaVaikeastiVammainen = 2 + ylimääräisetVaikeastiVammaisetLkm,
        pidOppivelvollisuusEritTukiJaMuuKuinVaikeimminVammainen = 1 + ylimääräisetMuuKuinVaikeastiVammaisetLkm,
        virheellisestiSiirrettyjaTukitietoja = 0 + rikkinäisetYlimääräisetLkm,
        erityiselläTuella = 3 + ylimääräisetErityiselläTuellaOpiskeluoikeudet,
        majoitusetu = 2,
        kuljetusetu = 2,
        sisäoppilaitosmainenMajoitus = 2,
        koulukoti = 2,
        joustavaPerusopetus = 2,
        kotiopetus = 1 + ylimääräisetKotiopetusLkm
      )
    ))
  }

  private val perusopetuksenOppijamäärätRaporttiBuilder = PerusopetuksenOppijamäärätRaportti(application.raportointiDatabase.db, application.organisaatioService)
  private lazy val perusopetuksenOppijamäärätRaportti = perusopetuksenOppijamäärätRaporttiBuilder
    .build(Seq(jyväskylänNormaalikoulu), raportointipäivä, t)(session(defaultUser))
    .rows.map(_.asInstanceOf[PerusopetuksenOppijamäärätRaporttiRow])

  "Perusopetuksen oppijamäärien raportti - aikajaksovirheet" in {
    val rows = perusopetuksenOppijamäärätAikajaksovirheetRaportti.filter(_.oppilaitosNimi.equals("Jyväskylän normaalikoulu"))
    rows.length should be(rikkinäisetYlimääräisetLkm)

    val expectedRows: Seq[PerusopetuksenOppijamäärätAikajaksovirheetRaporttiRow] =
      rikkinäisetOpiskeluoikeusOidit.toList.map(opiskeluoikeusOid =>
        PerusopetuksenOppijamäärätAikajaksovirheetRaporttiRow(
          oppilaitosNimi = "Jyväskylän normaalikoulu",
          organisaatioOid = "1.2.246.562.10.14613773812",
          oppijaOid = vuonna2005SyntynytEiOpiskeluoikeuksiaFikstuurissa.oid,
          opiskeluoikeusOid = opiskeluoikeusOid
        )
      ).sortBy(_.opiskeluoikeusOid)

    rows.toList should equal(expectedRows)
  }

  private val perusopetuksenOppijamäärätAikajaksovirheetRaporttiBuilder = PerusopetuksenOppijamäärätAikajaksovirheetRaportti(application.raportointiDatabase.db, application.organisaatioService)
  private lazy val perusopetuksenOppijamäärätAikajaksovirheetRaportti = perusopetuksenOppijamäärätAikajaksovirheetRaporttiBuilder
    .build(Seq(jyväskylänNormaalikoulu), raportointipäivä, t)(session(defaultUser))
    .rows.map(_.asInstanceOf[PerusopetuksenOppijamäärätAikajaksovirheetRaporttiRow])
}
