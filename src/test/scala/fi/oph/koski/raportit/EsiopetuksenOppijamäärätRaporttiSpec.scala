package fi.oph.koski.raportit

import fi.oph.koski.documentation.ExamplesEsiopetus
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat.vuonna2005SyntynytEiOpiskeluoikeuksiaFikstuurissa
import fi.oph.koski.henkilo.VerifiedHenkilöOid
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.{DirtiesFixtures, KoskiApplicationForTests}
import fi.oph.koski.koskiuser.MockUsers.{helsinkiTallentaja, tornioTallentaja}
import fi.oph.koski.koskiuser.{KoskiMockUser, KoskiSpecificSession, MockUsers}
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.organisaatio.MockOrganisaatiot.{helsinginKaupunki, päiväkotiTouhula, tornionKaupunki}
import fi.oph.koski.raportit.esiopetus.{EsiopetuksenOppijamäärätAikajaksovirheetRaportti, EsiopetuksenOppijamäärätAikajaksovirheetRaporttiRow, EsiopetuksenOppijamäärätRaportti, EsiopetuksenOppijamäärätRaporttiRow}
import fi.oph.koski.raportointikanta.RaportointikantaTestMethods
import fi.oph.koski.schema.{Aikajakso, ErityisenTuenPäätös, EsiopetuksenOpiskeluoikeudenLisätiedot, EsiopetuksenOpiskeluoikeus, Opiskeluoikeus}
import fi.oph.koski.schema.Organisaatio.Oid
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate.{of => date}

class EsiopetuksenOppijamäärätRaporttiSpec
  extends AnyFreeSpec
    with Matchers
    with RaportointikantaTestMethods
    with BeforeAndAfterAll
    with DirtiesFixtures {

  private val oppilaitosOid = MockOrganisaatiot.jyväskylänNormaalikoulu

  private val raportointipäivä = date(2015, 1, 1)

  var rikkinäisetOpiskeluoikeusOidit: Seq[Opiskeluoikeus.Oid] = Seq()

  override protected def alterFixture(): Unit = {
    // Lisää validointien osalta rikkinäisiä opiskeluoikeuksia suoraan tietokantaan, koska raportti kertoo
    // rikkinäisyyksistä.

    def create(oo: EsiopetuksenOpiskeluoikeus): Either[HttpStatus, Opiskeluoikeus.Oid] = {
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

  private val ylimääräisetLkm = ehjätTestiopiskeluoikeudet.length + rikkinäisetTestiopiskeluoikeudet.length
  private val ylimääräisetErityiselläTuellaOpiskeluoikeudet = 6
  private val ylimääräisetVaikeastiVammaisetLkm = 1
  private val ylimääräisetMuuKuinVaikeastiVammaisetLkm = 2
  private val rikkinäisetYlimääräisetLkm = 6

  private def ehjäPidennettyOppivelvollisuusTarvittavienTietojenKanssaOpiskeluoikeus: EsiopetuksenOpiskeluoikeus =
    raportilleOsuvaOpiskeluoikeus(
      EsiopetuksenOpiskeluoikeudenLisätiedot(
        pidennettyOppivelvollisuus = raportointipäiväänOsuvaPidennettyOppivelvollisuus,
        vammainen = raportointipäiväänOsuvaVammaisuustieto,
        erityisenTuenPäätökset = raportointipäiväänOsuvaErityisenTuenPäätös
      )
    )

  private def rikkinäinenPelkkäPidennettyOppivelvollisuusOpiskeluoikeus: EsiopetuksenOpiskeluoikeus =
    raportilleOsuvaOpiskeluoikeus(
      EsiopetuksenOpiskeluoikeudenLisätiedot(
        pidennettyOppivelvollisuus = raportointipäiväänOsuvaPidennettyOppivelvollisuus
      )
    )

  private def rikkinäinenPidennettyOppivelvollisuusIlmanVammaisuuttaOpiskeluoikeus: EsiopetuksenOpiskeluoikeus =
    raportilleOsuvaOpiskeluoikeus(
      EsiopetuksenOpiskeluoikeudenLisätiedot(
        pidennettyOppivelvollisuus = raportointipäiväänOsuvaPidennettyOppivelvollisuus,
        erityisenTuenPäätökset = raportointipäiväänOsuvaErityisenTuenPäätös
      )
    )

  private def rikkinäinenPidennettyOppivelvollisuusIlmanErityisenTuenPäätöstäOpiskeluoikeus: EsiopetuksenOpiskeluoikeus =
    raportilleOsuvaOpiskeluoikeus(
      EsiopetuksenOpiskeluoikeudenLisätiedot(
        pidennettyOppivelvollisuus = raportointipäiväänOsuvaPidennettyOppivelvollisuus,
        vaikeastiVammainen = raportointipäiväänOsuvaVammaisuustieto
      )
    )

  private def rikkinäinenPelkkäVaikeastiVammaisuusOpiskeluoikeus: EsiopetuksenOpiskeluoikeus =
    raportilleOsuvaOpiskeluoikeus(
      EsiopetuksenOpiskeluoikeudenLisätiedot(
        vaikeastiVammainen = raportointipäiväänOsuvaVammaisuustieto
      )
    )

  private def rikkinäinenPelkkäVammaisuusOpiskeluoikeus: EsiopetuksenOpiskeluoikeus =
    raportilleOsuvaOpiskeluoikeus(
      EsiopetuksenOpiskeluoikeudenLisätiedot(
        vammainen = raportointipäiväänOsuvaVammaisuustieto
      )
    )

  private def rikkinäinenPäällekäisetVammaisuudetOpiskeluoikeus: EsiopetuksenOpiskeluoikeus =
    raportilleOsuvaOpiskeluoikeus(
      EsiopetuksenOpiskeluoikeudenLisätiedot(
        pidennettyOppivelvollisuus = raportointipäiväänOsuvaPidennettyOppivelvollisuus,
        vammainen = raportointipäiväänOsuvaVammaisuustieto,
        vaikeastiVammainen = raportointipäiväänOsuvaVammaisuustieto,
        erityisenTuenPäätökset = raportointipäiväänOsuvaErityisenTuenPäätös
      )
    )

  private def ehjäPelkkäErityinenTukiOpiskeluoikeus: EsiopetuksenOpiskeluoikeus =
    raportilleOsuvaOpiskeluoikeus(
      EsiopetuksenOpiskeluoikeudenLisätiedot(
        erityisenTuenPäätökset = raportointipäiväänOsuvaErityisenTuenPäätös
      )
    )

  private def raportilleOsuvaOpiskeluoikeus(
    lisätiedot: EsiopetuksenOpiskeluoikeudenLisätiedot
  ): EsiopetuksenOpiskeluoikeus =
    raportilleOsuvaOpiskeluoikeus(Some(lisätiedot))

  private def raportilleOsuvaOpiskeluoikeus(
    lisätiedot: Option[EsiopetuksenOpiskeluoikeudenLisätiedot] = None
  ): EsiopetuksenOpiskeluoikeus = {
    ExamplesEsiopetus.opiskeluoikeus.copy(
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

  private val application = KoskiApplicationForTests
  private val t = new LocalizationReader(application.koskiLocalizationRepository, "fi")

  "Esiopetuksen oppijamäärien raportti" - {
    "Raportti voidaan ladata ja lataaminen tuottaa auditlogin" in {
      authGet(s"api/raportit/esiopetuksenoppijamaaratraportti?oppilaitosOid=$oppilaitosOid&paiva=2007-01-01&password=salasana&lang=fi") {
        verifyResponseStatusOk()
        response.headers("Content-Disposition").head should equal(s"""attachment; filename="esiopetuksen_oppijamäärät_raportti-2007-01-01.xlsx"""")
        response.bodyBytes.take(ENCRYPTED_XLSX_PREFIX.length) should equal(ENCRYPTED_XLSX_PREFIX)
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_RAPORTTI", "target" -> Map("hakuEhto" -> s"raportti=esiopetuksenoppijamaaratraportti&oppilaitosOid=$oppilaitosOid&paiva=2007-01-01&lang=fi")))
      }
    }

    "Raportti voidaan ladata eri lokalisaatiolla ja lataaminen tuottaa auditlogin" in {
      authGet(s"api/raportit/esiopetuksenoppijamaaratraportti?oppilaitosOid=$oppilaitosOid&paiva=2007-01-01&password=salasana&lang=sv") {
        verifyResponseStatusOk()
        response.headers("Content-Disposition").head should equal(s"""attachment; filename="antal_elever_förskoleundervisningens_rapport-2007-01-01.xlsx"""")
        response.bodyBytes.take(ENCRYPTED_XLSX_PREFIX.length) should equal(ENCRYPTED_XLSX_PREFIX)
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_RAPORTTI", "target" -> Map("hakuEhto" -> s"raportti=esiopetuksenoppijamaaratraportti&oppilaitosOid=$oppilaitosOid&paiva=2007-01-01&lang=sv")))
      }
    }
  }

  "Esiopetuksen oppijamäärien raportti - päävälilehti" - {

    "Raportin kolumnit" in {
      lazy val r = findSingle(esiopetuksenOppijamäärätRaportti)

      r.oppilaitosNimi should equal("Jyväskylän normaalikoulu")
      r.opetuskieli should equal("ruotsi,suomi")
      r.esiopetusoppilaidenMäärä should equal(4 + ylimääräisetLkm)
      r.vieraskielisiä should equal(0)
      r.koulunesiopetuksessa should equal(4 + ylimääräisetLkm)
      r.päiväkodinesiopetuksessa should equal(0)
      r.viisivuotiaita should equal(0)
      r.viisivuotiaitaEiPidennettyäOppivelvollisuutta should equal(0)
      r.pidOppivelvollisuusEritTukiJaVaikeastiVammainen should equal(0 + ylimääräisetVaikeastiVammaisetLkm)
      r.pidOppivelvollisuusEritTukiJaMuuKuinVaikeimminVammainen should equal(2 + ylimääräisetMuuKuinVaikeastiVammaisetLkm)
      r.virheellisestiSiirrettyjaTukitietoja should equal(0 + rikkinäisetYlimääräisetLkm)
      r.erityiselläTuella should equal(2 + ylimääräisetErityiselläTuellaOpiskeluoikeudet)
      r.majoitusetu should equal(3)
      r.kuljetusetu should equal(3)
      r.sisäoppilaitosmainenMajoitus should equal(3)
    }

    "Haettu vuodelle, jona ei oppilaita" in {
      esiopetuksenOppijamäärätTyhjäVuosiRaportti.length should be(0)
    }

    "Ei näe muiden organisaatioiden raporttia" in {
      esiopetuksenOppijamäärätIlmanOikeuksiaRaportti.length should be(0)
    }

    "Varhaiskasvatuksen järjestäjä" - {
      "näkee vain omat opiskeluoikeutensa" in {
        val tornionTekemäRaportti = buildEsiopetuksenOppijamäärätRaportti(tornioTallentaja, päiväkotiTouhula)
        getOppilaitokset(tornionTekemäRaportti) should be(empty)

        val helsinginTekemäRaportti = buildEsiopetuksenOppijamäärätRaportti(helsinkiTallentaja, päiväkotiTouhula)
        getOppilaitokset(helsinginTekemäRaportti) should equal(List("Päiväkoti Touhula"))
      }

      "voi hakea kaikki koulutustoimijan alla olevat tiedot" in {
        val raportti = buildEsiopetuksenOppijamäärätRaportti(helsinkiTallentaja, helsinginKaupunki)
        getOppilaitokset(raportti) should equal(List("Kulosaaren ala-aste", "Päiväkoti Majakka", "Päiväkoti Touhula"))
      }

      "ei näe muiden ostopalvelu/palveluseteli-tietoja" in {
        val raportti = buildEsiopetuksenOppijamäärätRaportti(tornioTallentaja, tornionKaupunki)
        getOppilaitokset(raportti) should be(empty)
      }

      "globaaleilla käyttöoikeuksilla voi tehdä raportin" in {
        val raportti = buildEsiopetuksenOppijamäärätRaportti(MockUsers.paakayttaja, helsinginKaupunki)
        getOppilaitokset(raportti) should equal(List("Kulosaaren ala-aste", "Päiväkoti Majakka", "Päiväkoti Touhula"))
      }
    }

    "Raportilla ei useita rivejä vaikka kieliä olisi useampi" in {
      val raportti = buildEsiopetuksenOppijamäärätRaportti(MockUsers.paakayttaja, helsinginKaupunki)
      getRows(raportti).groupBy(it => it.oppilaitosNimi).values
        .foreach(rowsForOrg => rowsForOrg.map(_.opetuskieli).distinct should have length 1)
    }
  }

  private val esiopetuksenOppijamäärätRaporttiBuilder = EsiopetuksenOppijamäärätRaportti(application.raportointiDatabase.db, application.organisaatioService)
  private lazy val esiopetuksenOppijamäärätRaportti =
    esiopetuksenOppijamäärätRaporttiBuilder.build(List(oppilaitosOid), raportointipäivä, t)(session(defaultUser)).rows.map(_.asInstanceOf[EsiopetuksenOppijamäärätRaporttiRow])
  private lazy val esiopetuksenOppijamäärätIlmanOikeuksiaRaportti =
    esiopetuksenOppijamäärätRaporttiBuilder.build(List(oppilaitosOid), raportointipäivä, t)(session(tornioTallentaja)).rows.map(_.asInstanceOf[EsiopetuksenOppijamäärätRaporttiRow])
  private lazy val esiopetuksenOppijamäärätTyhjäVuosiRaportti =
    esiopetuksenOppijamäärätRaporttiBuilder.build(List(oppilaitosOid), date(2012, 1, 1), t)(session(defaultUser)).rows.map(_.asInstanceOf[EsiopetuksenOppijamäärätRaporttiRow])
  private val esiopetuksenOppijamäärätRaporttiService = EsiopetuksenOppijamäärätRaportti(application.raportointiDatabase.db, application.organisaatioService)

  private def findSingle(rows: Seq[EsiopetuksenOppijamäärätRaporttiRow]) = {
    val found = rows.filter(_.oppilaitosNimi.equals("Jyväskylän normaalikoulu"))
    found.length should be(1)
    found.head
  }

  private def buildEsiopetuksenOppijamäärätRaportti(user: KoskiMockUser, organisaatio: Oid) =
    esiopetuksenOppijamäärätRaporttiService.build(List(organisaatio), raportointipäivä, t)(session(user))

  private def getOppilaitokset(raportti: DataSheet) = {
    getRows(raportti).map(_.oppilaitosNimi).sorted
  }

  private def getRows(raportti: DataSheet): List[EsiopetuksenOppijamäärätRaporttiRow] = {
    raportti.rows.collect {
      case r: EsiopetuksenOppijamäärätRaporttiRow => r
    }.toList
  }

  "Esiopetuksen oppijamäärien raportti - aikajaksovirheet" - {

    "Raportin kolumnit" in {
      val r = findRows(esiopetuksenOppijamäärätAikajaksovirheetRaportti)
      r.length should be(rikkinäisetYlimääräisetLkm)

      val expectedRows: Seq[EsiopetuksenOppijamäärätAikajaksovirheetRaporttiRow] =
        rikkinäisetOpiskeluoikeusOidit.map(opiskeluoikeusOid =>
          EsiopetuksenOppijamäärätAikajaksovirheetRaporttiRow(
            oppilaitosNimi = "Jyväskylän normaalikoulu",
            oppijaOid = vuonna2005SyntynytEiOpiskeluoikeuksiaFikstuurissa.oid,
            opiskeluoikeusOid = opiskeluoikeusOid
          )
      ).sortBy(_.opiskeluoikeusOid)

      r should be(expectedRows)
    }
  }

  private def findRows(rows: Seq[EsiopetuksenOppijamäärätAikajaksovirheetRaporttiRow]) = {
    val found = rows.filter(_.oppilaitosNimi.equals("Jyväskylän normaalikoulu"))
    found
  }

  private val esiopetuksenOppijamäärätAikajaksovirheetRaporttiBuilder = EsiopetuksenOppijamäärätAikajaksovirheetRaportti(application.raportointiDatabase.db, application.organisaatioService)
  private lazy val esiopetuksenOppijamäärätAikajaksovirheetRaportti =
    esiopetuksenOppijamäärätAikajaksovirheetRaporttiBuilder.build(List(oppilaitosOid), raportointipäivä, t)(session(defaultUser)).rows.map(_.asInstanceOf[EsiopetuksenOppijamäärätAikajaksovirheetRaporttiRow])

  private def session(user: KoskiMockUser): KoskiSpecificSession = user.toKoskiSpecificSession(application.käyttöoikeusRepository)
}
