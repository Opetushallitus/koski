package fi.oph.koski.raportit

import fi.oph.koski.documentation.ExamplesPerusopetuksenLisaopetus
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat.vuonna2005SyntynytEiOpiskeluoikeuksiaFikstuurissa
import fi.oph.koski.henkilo.VerifiedHenkilöOid
import fi.oph.koski.{DirtiesFixtures, KoskiApplicationForTests}
import fi.oph.koski.koskiuser.KoskiMockUser
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.organisaatio.MockOrganisaatiot.jyväskylänNormaalikoulu
import fi.oph.koski.raportointikanta.RaportointikantaTestMethods
import fi.oph.koski.schema.{Aikajakso, ErityisenTuenPäätös, PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot, PerusopetuksenLisäopetuksenOpiskeluoikeus}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate.{of => date}

class PerusopetuksenLisäopetusOppijamäärätRaporttiSpec extends AnyFreeSpec with Matchers with RaportointikantaTestMethods with BeforeAndAfterAll with DirtiesFixtures {

  private val raportointipäivä = date(2012, 1, 1)

  override protected def alterFixture(): Unit = {
    // Lisää validointien osalta rikkinäisiä opiskeluoikeuksia suoraan tietokantaan, koska raportti kertoo
    // rikkinäisyyksistä.

    testiOpiskeluoikeudet.map(oo => {
      val createResult = application.opiskeluoikeusRepository.createOrUpdate(
        oppijaOid = VerifiedHenkilöOid(vuonna2005SyntynytEiOpiskeluoikeuksiaFikstuurissa),
        opiskeluoikeus = oo,
        allowUpdate = false
      )(session(defaultUser))
      createResult.map(_.created) should be(Right(true))
    })

    application.perustiedotIndexer.sync(refresh = true)
    reloadRaportointikanta
  }

  private val testiOpiskeluoikeudet =
    List(
      ehjäPidennettyOppivelvollisuusTarvittavienTietojenKanssaOpiskeluoikeus,
      rikkinäinenPelkkäPidennettyOppivelvollisuusOpiskeluoikeus,
      rikkinäinenPidennettyOppivelvollisuusIlmanVammaisuuttaOpiskeluoikeus,
      rikkinäinenPidennettyOppivelvollisuusIlmanErityisenTuenPäätöstäOpiskeluoikeus,
      rikkinäinenPelkkäVaikeastiVammaisuusOpiskeluoikeus,
      rikkinäinenPelkkäVammaisuusOpiskeluoikeus,
      rikkinäinenPäällekäisetVammaisuudetOpiskeluoikeus,
      ehjäPelkkäErityinenTukiOpiskeluoikeus
    )

  private val ylimääräisetLkm = testiOpiskeluoikeudet.length
  private val ylimääräisetErityiselläTuellaOpiskeluoikeudet = 4
  private val ylimääräisetVaikeastiVammaisetLkm = 0
  private val ylimääräisetMuuKuinVaikeastiVammaisetLkm = 1
  private val rikkinäisetYlimääräisetLkm = 6

  private def ehjäPidennettyOppivelvollisuusTarvittavienTietojenKanssaOpiskeluoikeus: PerusopetuksenLisäopetuksenOpiskeluoikeus =
    raportilleOsuvaOpiskeluoikeus(
      PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot(
        pidennettyOppivelvollisuus = raportointipäiväänOsuvaPidennettyOppivelvollisuus,
        vammainen = raportointipäiväänOsuvaVammaisuustieto,
        erityisenTuenPäätökset = raportointipäiväänOsuvaErityisenTuenPäätös
      )
    )

  private def rikkinäinenPelkkäPidennettyOppivelvollisuusOpiskeluoikeus: PerusopetuksenLisäopetuksenOpiskeluoikeus =
    raportilleOsuvaOpiskeluoikeus(
      PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot(
        pidennettyOppivelvollisuus = raportointipäiväänOsuvaPidennettyOppivelvollisuus
      )
    )

  private def rikkinäinenPidennettyOppivelvollisuusIlmanVammaisuuttaOpiskeluoikeus: PerusopetuksenLisäopetuksenOpiskeluoikeus =
    raportilleOsuvaOpiskeluoikeus(
      PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot(
        pidennettyOppivelvollisuus = raportointipäiväänOsuvaPidennettyOppivelvollisuus,
        erityisenTuenPäätökset = raportointipäiväänOsuvaErityisenTuenPäätös
      )
    )

  private def rikkinäinenPidennettyOppivelvollisuusIlmanErityisenTuenPäätöstäOpiskeluoikeus: PerusopetuksenLisäopetuksenOpiskeluoikeus =
    raportilleOsuvaOpiskeluoikeus(
      PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot(
        pidennettyOppivelvollisuus = raportointipäiväänOsuvaPidennettyOppivelvollisuus,
        vaikeastiVammainen = raportointipäiväänOsuvaVammaisuustieto
      )
    )

  private def rikkinäinenPelkkäVaikeastiVammaisuusOpiskeluoikeus: PerusopetuksenLisäopetuksenOpiskeluoikeus =
    raportilleOsuvaOpiskeluoikeus(
      PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot(
        vaikeastiVammainen = raportointipäiväänOsuvaVammaisuustieto
      )
    )

  private def rikkinäinenPelkkäVammaisuusOpiskeluoikeus: PerusopetuksenLisäopetuksenOpiskeluoikeus =
    raportilleOsuvaOpiskeluoikeus(
      PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot(
        vammainen = raportointipäiväänOsuvaVammaisuustieto
      )
    )

  private def rikkinäinenPäällekäisetVammaisuudetOpiskeluoikeus: PerusopetuksenLisäopetuksenOpiskeluoikeus =
    raportilleOsuvaOpiskeluoikeus(
      PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot(
        pidennettyOppivelvollisuus = raportointipäiväänOsuvaPidennettyOppivelvollisuus,
        vammainen = raportointipäiväänOsuvaVammaisuustieto,
        vaikeastiVammainen = raportointipäiväänOsuvaVammaisuustieto,
        erityisenTuenPäätökset = raportointipäiväänOsuvaErityisenTuenPäätös
      )
    )

  private def ehjäPelkkäErityinenTukiOpiskeluoikeus: PerusopetuksenLisäopetuksenOpiskeluoikeus =
    raportilleOsuvaOpiskeluoikeus(
      PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot(
        erityisenTuenPäätökset = raportointipäiväänOsuvaErityisenTuenPäätös
      )
    )

  private def raportilleOsuvaOpiskeluoikeus(
    lisätiedot: PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot
  ): PerusopetuksenLisäopetuksenOpiskeluoikeus =
    raportilleOsuvaOpiskeluoikeus(Some(lisätiedot))

  private def raportilleOsuvaOpiskeluoikeus(
    lisätiedot: Option[PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot] = None
  ): PerusopetuksenLisäopetuksenOpiskeluoikeus = {
    ExamplesPerusopetuksenLisaopetus.lisäopetuksenOpiskeluoikeus.copy(
      lisätiedot = lisätiedot
    )
  }

  private def raportointipäiväänOsuvaVammaisuustieto = Some(List(raportointipäiväänOsuvaAikajakso))
  private def raportointipäiväänOsuvaPidennettyOppivelvollisuus = Some(raportointipäiväänOsuvaAikajakso)
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
  private val raporttiBuilder = PerusopetuksenLisäopetusOppijamäärätRaportti(application.raportointiDatabase.db, application.organisaatioService)
  private val t = new LocalizationReader(KoskiApplicationForTests.koskiLocalizationRepository, "fi")
  private lazy val raportti = raporttiBuilder
    .build(Seq(jyväskylänNormaalikoulu), raportointipäivä, t)(session(defaultUser))
    .rows.map(_.asInstanceOf[PerusopetuksenLisäopetusOppijamäärätRaporttiRow])

  "Perusopetuksen lisäopetuksen oppijamäärien raportti" - {
    "Raportti voidaan ladata ja lataaminen tuottaa auditlogin" in {
      authGet(s"api/raportit/perusopetuksenlisaopetuksenoppijamaaratraportti?oppilaitosOid=$jyväskylänNormaalikoulu&paiva=2007-01-01&password=salasana&lang=fi") {
        verifyResponseStatusOk()
        response.headers("Content-Disposition").head should equal(s"""attachment; filename="lisaopetus_tunnuslukuraportti-2007-01-01.xlsx"""")
        response.bodyBytes.take(ENCRYPTED_XLSX_PREFIX.length) should equal(ENCRYPTED_XLSX_PREFIX)
        AuditLogTester.verifyAuditLogMessage(
          Map(
            "operation" -> "OPISKELUOIKEUS_RAPORTTI",
            "target" -> Map(
              "hakuEhto" -> s"raportti=perusopetuksenlisaopetuksenoppijamaaratraportti&oppilaitosOid=$jyväskylänNormaalikoulu&paiva=2007-01-01&lang=fi"
            )
          )
        )
      }
    }

    "Raportti voidaan ladata eri lokalisaatiolla ja lataaminen tuottaa auditlogin" in {
      authGet(s"api/raportit/perusopetuksenlisaopetuksenoppijamaaratraportti?oppilaitosOid=$jyväskylänNormaalikoulu&paiva=2007-01-01&password=salasana&lang=sv") {
        verifyResponseStatusOk()
        response.headers("Content-Disposition").head should equal(s"""attachment; filename="påbyggnadsundervisning_nyckeltal_rapport-2007-01-01.xlsx"""")
        response.bodyBytes.take(ENCRYPTED_XLSX_PREFIX.length) should equal(ENCRYPTED_XLSX_PREFIX)
        AuditLogTester.verifyAuditLogMessage(
          Map(
            "operation" -> "OPISKELUOIKEUS_RAPORTTI",
            "target" -> Map(
              "hakuEhto" -> s"raportti=perusopetuksenlisaopetuksenoppijamaaratraportti&oppilaitosOid=$jyväskylänNormaalikoulu&paiva=2007-01-01&lang=sv"
            )
          )
        )
      }
    }

    "Raportin sarakkeet" in {
      val rows = raportti.filter(_.oppilaitosNimi.equals("Jyväskylän normaalikoulu"))
      rows.length should be(1)
      rows.toList should equal(List(
        PerusopetuksenLisäopetusOppijamäärätRaporttiRow(
          oppilaitosNimi = "Jyväskylän normaalikoulu",
          organisaatioOid = "1.2.246.562.10.14613773812",
          opetuskieli = "ruotsi,suomi",
          oppilaita = 6 + ylimääräisetLkm,
          vieraskielisiä = 1,
          pidOppivelvollisuusEritTukiJaVaikeastiVammainen = 2 + ylimääräisetVaikeastiVammaisetLkm,
          pidOppivelvollisuusEritTukiJaMuuKuinVaikeimminVammainen = 2 + ylimääräisetMuuKuinVaikeastiVammaisetLkm,
          virheellisestiSiirrettyjaTukitietoja = 0 + rikkinäisetYlimääräisetLkm,
          erityiselläTuella = 4 + ylimääräisetErityiselläTuellaOpiskeluoikeudet,
          majoitusetu = 2,
          kuljetusetu = 2,
          sisäoppilaitosmainenMajoitus = 2,
          koulukoti = 2
        )
      ))
    }

    "Raportilla ei useita rivejä vaikka kieliä olisi useampi" in {
      val rows = raportti.filter(_.oppilaitosNimi.equals("Jyväskylän normaalikoulu"))
      rows.groupBy(it => it.organisaatioOid).values
        .foreach(rowsForOrg => rowsForOrg.map(_.opetuskieli).distinct should have length 1)
    }
  }
}
