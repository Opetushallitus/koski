package fi.oph.koski.raportit

import fi.oph.koski.documentation.ExamplesTutkintokoulutukseenValmentavaKoulutus.tuvaOpiskeluOikeusjakso
import fi.oph.koski.documentation.{ExamplesTutkintokoulutukseenValmentavaKoulutus, YleissivistavakoulutusExampleData}
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat.vuonna2005SyntynytEiOpiskeluoikeuksiaFikstuurissa
import fi.oph.koski.henkilo.VerifiedHenkilöOid
import fi.oph.koski.http.HttpStatus

import java.time.LocalDate.{of => date}
import fi.oph.koski.{DirtiesFixtures, KoskiApplicationForTests}
import fi.oph.koski.koskiuser.KoskiMockUser
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.organisaatio.{MockOrganisaatioRepository, MockOrganisaatiot}
import fi.oph.koski.organisaatio.MockOrganisaatiot.{jyväskylänNormaalikoulu, stadinAmmattiopisto}
import fi.oph.koski.raportit.tuva.{TuvaPerusopetuksenOppijamäärätAikajaksovirheetRaportti, TuvaPerusopetuksenOppijamäärätAikajaksovirheetRaporttiRow, TuvaPerusopetuksenOppijamäärätRaportti, TuvaPerusopetuksenOppijamäärätRaporttiRow}
import fi.oph.koski.raportointikanta.RaportointikantaTestMethods
import fi.oph.koski.schema.{Aikajakso, Opiskeluoikeus, TutkintokoulutukseenValmentavanKoulutuksenSuoritus, TutkintokoulutukseenValmentavanOpiskeluoikeudenPerusopetuksenLuvanLisätiedot, TutkintokoulutukseenValmentavanOpiskeluoikeudenTila, TutkintokoulutukseenValmentavanOpiskeluoikeus, TuvaErityisenTuenPäätös}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class TuvaPerusopetuksenOppijamäärätRaporttiSpec extends AnyFreeSpec with Matchers with RaportointikantaTestMethods with BeforeAndAfterAll with DirtiesFixtures
{
  private val raportointipäivä = date(2021, 10, 1)

  var rikkinäisetOpiskeluoikeusOidit: Seq[Opiskeluoikeus.Oid] = Seq()

  override protected def alterFixture(): Unit = {
    // Lisää validointien osalta rikkinäisiä opiskeluoikeuksia suoraan tietokantaan, koska raportti kertoo
    // rikkinäisyyksistä.

    def create(oo: TutkintokoulutukseenValmentavanOpiskeluoikeus): Either[HttpStatus, Opiskeluoikeus.Oid] = {
      val createResult = application.opiskeluoikeusRepository.createOrUpdate(
        oppijaOid = VerifiedHenkilöOid(vuonna2005SyntynytEiOpiskeluoikeuksiaFikstuurissa),
        opiskeluoikeus = oo,
        allowUpdate = false
      )(session(defaultUser))
      createResult.map(_.created) should be(Right(true))
      createResult.map(_.oid)
    }

    ehjätTestiopiskeluoikeudet.map(create)
    rikkinäisetOpiskeluoikeusOidit = rikkinäisetTestiopiskeluoikeudet.map(create).map(_.getOrElse(throw new Error))

    application.perustiedotIndexer.sync(refresh = true)
    reloadRaportointikanta
  }

  private val ehjätTestiopiskeluoikeudet =
    List(
      ehjäErityinenTukiTarvittavienTietojenKanssaOpiskeluoikeus,
      ehjäPelkkäErityinenTukiOpiskeluoikeus
    )

  private val rikkinäisetTestiopiskeluoikeudet =
    List(
      rikkinäinenPelkkäVaikeastiVammaisuusOpiskeluoikeus,
      rikkinäinenPelkkäVammaisuusOpiskeluoikeus,
      rikkinäinenPäällekäisetVammaisuudetOpiskeluoikeus,
    )

  private val ylimääräisetLkm = ehjätTestiopiskeluoikeudet.length + rikkinäisetTestiopiskeluoikeudet.length
  private val ylimääräisetErityiselläTuellaOpiskeluoikeudet = 3
  private val ylimääräisetVaikeastiVammaisetLkm = 0
  private val ylimääräisetMuuKuinVaikeastiVammaisetLkm = 1
  private val rikkinäisetYlimääräisetLkm = 3

  private def ehjäErityinenTukiTarvittavienTietojenKanssaOpiskeluoikeus: TutkintokoulutukseenValmentavanOpiskeluoikeus =
    raportinRivilleOsuvaOpiskeluoikeus(
      TutkintokoulutukseenValmentavanOpiskeluoikeudenPerusopetuksenLuvanLisätiedot(
        vammainen = raportointipäiväänOsuvaVammaisuustieto,
        erityisenTuenPäätökset = raportointipäiväänOsuvaErityisenTuenPäätös
      )
    )

  private def rikkinäinenPelkkäVaikeastiVammaisuusOpiskeluoikeus: TutkintokoulutukseenValmentavanOpiskeluoikeus =
    raportinRivilleOsuvaOpiskeluoikeus(
      TutkintokoulutukseenValmentavanOpiskeluoikeudenPerusopetuksenLuvanLisätiedot(
        vaikeastiVammainen = raportointipäiväänOsuvaVammaisuustieto
      )
    )

  private def rikkinäinenPelkkäVammaisuusOpiskeluoikeus: TutkintokoulutukseenValmentavanOpiskeluoikeus =
    raportinRivilleOsuvaOpiskeluoikeus(
      TutkintokoulutukseenValmentavanOpiskeluoikeudenPerusopetuksenLuvanLisätiedot(
        vammainen = raportointipäiväänOsuvaVammaisuustieto
      )
    )

  private def rikkinäinenPäällekäisetVammaisuudetOpiskeluoikeus: TutkintokoulutukseenValmentavanOpiskeluoikeus =
    raportinRivilleOsuvaOpiskeluoikeus(
      TutkintokoulutukseenValmentavanOpiskeluoikeudenPerusopetuksenLuvanLisätiedot(
        vammainen = raportointipäiväänOsuvaVammaisuustieto,
        vaikeastiVammainen = raportointipäiväänOsuvaVammaisuustieto,
        erityisenTuenPäätökset = raportointipäiväänOsuvaErityisenTuenPäätös
      )
    )

  private def ehjäPelkkäErityinenTukiOpiskeluoikeus: TutkintokoulutukseenValmentavanOpiskeluoikeus =
    raportinRivilleOsuvaOpiskeluoikeus(
      TutkintokoulutukseenValmentavanOpiskeluoikeudenPerusopetuksenLuvanLisätiedot(
        erityisenTuenPäätökset = raportointipäiväänOsuvaErityisenTuenPäätös
      )
    )

  private def raportinRivilleOsuvaOpiskeluoikeus(
    lisätiedot: TutkintokoulutukseenValmentavanOpiskeluoikeudenPerusopetuksenLuvanLisätiedot
  ): TutkintokoulutukseenValmentavanOpiskeluoikeus =
    raportinRivilleOsuvaOpiskeluoikeus(Some(lisätiedot))

  private def raportinRivilleOsuvaOpiskeluoikeus(
    lisätiedot: Option[TutkintokoulutukseenValmentavanOpiskeluoikeudenPerusopetuksenLuvanLisätiedot] = None
  ): TutkintokoulutukseenValmentavanOpiskeluoikeus = {
    val koulutustoimija = MockOrganisaatioRepository.getOrganisaatioHierarkia(MockOrganisaatiot.jyväskylänYliopisto).flatMap(_.toKoulutustoimija).get
    val oppilaitos = YleissivistavakoulutusExampleData.jyväskylänNormaalikoulu
    val toimipiste = YleissivistavakoulutusExampleData.jyväskylänNormaalikoulu
    val alkamispäivä = date(2021, 8, 15)
    val päättymispäivä = date(2022, 6, 4)

    ExamplesTutkintokoulutukseenValmentavaKoulutus.tuvaOpiskeluOikeusEiValmistunut.copy(
      tila = TutkintokoulutukseenValmentavanOpiskeluoikeudenTila(
        opiskeluoikeusjaksot = List(
          tuvaOpiskeluOikeusjakso(alkamispäivä, "lasna"),
          tuvaOpiskeluOikeusjakso(päättymispäivä, "valmistunut")
        )
      ),
      oppilaitos = Some(oppilaitos),
      koulutustoimija = Some(koulutustoimija),
      suoritukset = ExamplesTutkintokoulutukseenValmentavaKoulutus.tuvaOpiskeluOikeusEiValmistunut.suoritukset.map {
        case t: TutkintokoulutukseenValmentavanKoulutuksenSuoritus => t.copy(toimipiste = toimipiste)
      }
    ).copy(
      lisätiedot = lisätiedot
    )
  }

  private def raportointipäiväänOsuvaVammaisuustieto = Some(List(raportointipäiväänOsuvaAikajakso))
  private def raportointipäiväänOsuvaPidennettyOppivelvollisuus = Some(raportointipäiväänOsuvaAikajakso)
  private def raportointipäiväänOsuvaErityisenTuenPäätös =  Some(List(
    TuvaErityisenTuenPäätös(
      alku = Some(raportointipäiväänOsuvaAikajakso.alku),
      loppu = raportointipäiväänOsuvaAikajakso.loppu
    )
  ))

  private def raportointipäiväänOsuvaAikajakso =
    Aikajakso(raportointipäivä.minusDays(10), Some(raportointipäivä.plusDays(10)))

  private def session(user: KoskiMockUser) = user.toKoskiSpecificSession(application.käyttöoikeusRepository)

  private val application = KoskiApplicationForTests
  private val t = new LocalizationReader(KoskiApplicationForTests.koskiLocalizationRepository, "fi")

  "Tuva perusopetuksen oppijamäärien raportti" - {
    "Raportti voidaan ladata ja lataaminen tuottaa auditlogin" in {
      authGet(s"api/raportit/tuvaperusopetuksenoppijamaaratraportti?oppilaitosOid=$jyväskylänNormaalikoulu&paiva=2021-10-01&lang=fi&password=salasana") {
        verifyResponseStatusOk()
        response.headers("Content-Disposition").head should equal(s"""attachment; filename="tuva_perusopetus_vos_raportti-2021-10-01.xlsx"""")
        response.bodyBytes.take(ENCRYPTED_XLSX_PREFIX.length) should equal(ENCRYPTED_XLSX_PREFIX)
        AuditLogTester.verifyAuditLogMessage(
          Map(
            "operation" -> "OPISKELUOIKEUS_RAPORTTI",
            "target" -> Map(
              "hakuEhto" -> s"raportti=tuvaperusopetuksenoppijamaaratraportti&oppilaitosOid=$jyväskylänNormaalikoulu&paiva=2021-10-01&lang=fi"
            )
          )
        )
      }
    }

    "Raportti voidaan ladata eri lokalisaatiolla ja lataaminen tuottaa auditlogin" in {
      authGet(s"api/raportit/tuvaperusopetuksenoppijamaaratraportti?oppilaitosOid=$jyväskylänNormaalikoulu&paiva=2021-10-01&lang=sv&password=salasana") {
        verifyResponseStatusOk()
        response.headers("Content-Disposition").head should equal(s"""attachment; filename="tuva_perusopetus_vos_raportti-2021-10-01.xlsx"""")
        response.bodyBytes.take(ENCRYPTED_XLSX_PREFIX.length) should equal(ENCRYPTED_XLSX_PREFIX)
        AuditLogTester.verifyAuditLogMessage(
          Map(
            "operation" -> "OPISKELUOIKEUS_RAPORTTI",
            "target" -> Map(
              "hakuEhto" -> s"raportti=tuvaperusopetuksenoppijamaaratraportti&oppilaitosOid=$jyväskylänNormaalikoulu&paiva=2021-10-01&lang=sv"
            )
          )
        )
      }
    }
  }

  "Tuva perusopetuksen oppijamäärien raportti - päävälilehti" in {
    val rows = tuvaPerusopetuksenOppijamäärätRaportti
    rows.length should be(2)
    rows.toList should equal(List(
      TuvaPerusopetuksenOppijamäärätRaporttiRow(
        oppilaitosNimi = "Jyväskylän normaalikoulu",
        organisaatioOid = "1.2.246.562.10.14613773812",
        opetuskieli = "suomi",
        oppilaita = ylimääräisetLkm,
        eritTukiJaVaikeastiVammainen = ylimääräisetVaikeastiVammaisetLkm,
        erityinenTukiJaMuuKuinVaikeimminVammainen = ylimääräisetMuuKuinVaikeastiVammaisetLkm,
        tuvaPerusVirheellisestiSiirrettyjaTukitietoja = rikkinäisetYlimääräisetLkm,
        erityiselläTuella = ylimääräisetErityiselläTuellaOpiskeluoikeudet,
        majoitusetu = 0,
        kuljetusetu = 0,
        sisäoppilaitosmainenMajoitus = 0,
        koulukoti = 0,
      ),
      TuvaPerusopetuksenOppijamäärätRaporttiRow(
        oppilaitosNimi = "Stadin ammatti- ja aikuisopisto",
        organisaatioOid = "1.2.246.562.10.52251087186",
        opetuskieli = "suomi",
        oppilaita = 1,
        eritTukiJaVaikeastiVammainen = 0,
        erityinenTukiJaMuuKuinVaikeimminVammainen = 1,
        tuvaPerusVirheellisestiSiirrettyjaTukitietoja = 0,
        erityiselläTuella = 1,
        majoitusetu = 1,
        kuljetusetu = 1,
        sisäoppilaitosmainenMajoitus = 1,
        koulukoti = 1,
      )
    ))
  }

  private val tuvaPerusopetuksenOppijamäärätRaporttiBuilder = TuvaPerusopetuksenOppijamäärätRaportti(application.raportointiDatabase.db, application.organisaatioService)
  private lazy val tuvaPerusopetuksenOppijamäärätRaportti = tuvaPerusopetuksenOppijamäärätRaporttiBuilder
    .build(Seq(stadinAmmattiopisto, jyväskylänNormaalikoulu), raportointipäivä, t)(session(defaultUser))
    .rows.map(_.asInstanceOf[TuvaPerusopetuksenOppijamäärätRaporttiRow])

  "Perusopetuksen oppijamäärien raportti - aikajaksovirheet" in {
    val rows = tuvaPerusopetuksenOppijamäärätAikajaksovirheetRaportti.filter(_.oppilaitosNimi.equals("Jyväskylän normaalikoulu"))
    rows.length should be(rikkinäisetYlimääräisetLkm)

    val expectedRows: Seq[TuvaPerusopetuksenOppijamäärätAikajaksovirheetRaporttiRow] =
      rikkinäisetOpiskeluoikeusOidit.toList.map(opiskeluoikeusOid =>
        TuvaPerusopetuksenOppijamäärätAikajaksovirheetRaporttiRow(
          oppilaitosNimi = "Jyväskylän normaalikoulu",
          organisaatioOid = "1.2.246.562.10.14613773812",
          oppijaOid = vuonna2005SyntynytEiOpiskeluoikeuksiaFikstuurissa.oid,
          opiskeluoikeusOid = opiskeluoikeusOid
        )
      ).sortBy(_.opiskeluoikeusOid)

    rows.toList should equal(expectedRows)
  }

  private val tuvaPerusopetuksenOppijamäärätAikajaksovirheetRaporttiBuilder = TuvaPerusopetuksenOppijamäärätAikajaksovirheetRaportti(application.raportointiDatabase.db, application.organisaatioService)
  private lazy val tuvaPerusopetuksenOppijamäärätAikajaksovirheetRaportti = tuvaPerusopetuksenOppijamäärätAikajaksovirheetRaporttiBuilder
    .build(Seq(stadinAmmattiopisto, jyväskylänNormaalikoulu), raportointipäivä, t)(session(defaultUser))
    .rows.map(_.asInstanceOf[TuvaPerusopetuksenOppijamäärätAikajaksovirheetRaporttiRow])


}
