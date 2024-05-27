package fi.oph.koski.vkt

import fi.oph.koski.api.misc.{OpiskeluoikeusTestMethods, PutOpiskeluoikeusTestMethods}
import fi.oph.koski.documentation.AmmatillinenExampleData._
import fi.oph.koski.documentation.ExampleData.{longTimeAgo, opiskeluoikeusLäsnä, valtionosuusRahoitteinen}
import fi.oph.koski.henkilo.{KoskiSpecificMockOppijat, LaajatOppijaHenkilöTiedot}
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.Rooli.{OPHKATSELIJA, VKT}
import fi.oph.koski.koskiuser._
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.virta.MockVirtaClient
import fi.oph.koski.ytr.MockYtrClient
import fi.oph.koski.{KoskiApplicationForTests, KoskiHttpSpec, schema}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}

import java.net.InetAddress
import java.time.LocalDate
import java.time.LocalDate.{of => date}
import scala.reflect.runtime.universe

class VktServiceSpec
  extends AnyFreeSpec
    with KoskiHttpSpec
    with Matchers
    with BeforeAndAfterAll
    with BeforeAndAfterEach
    with OpiskeluoikeusTestMethods
    with PutOpiskeluoikeusTestMethods[schema.AmmatillinenOpiskeluoikeus] {
  def tag: universe.TypeTag[schema.AmmatillinenOpiskeluoikeus] = implicitly[reflect.runtime.universe.TypeTag[schema.AmmatillinenOpiskeluoikeus]]

  override def defaultOpiskeluoikeus = makeOpiskeluoikeus(alkamispäivä = longTimeAgo, suoritus = ammatillisenTutkinnonOsittainenSuoritus)

  val vktService = KoskiApplicationForTests.vktService

  private val vktTestUser = new KoskiSpecificSession(
    AuthenticationUser(
      VKT,
      VKT,
      VKT, None
    ),
    "fi",
    InetAddress.getLoopbackAddress,
    "",
    Set(KäyttöoikeusGlobal(List(Palvelurooli(OPHKATSELIJA))))
  )

  implicit val koskiSession = vktTestUser

  override def afterEach(): Unit = {
    MockYtrClient.reset()
    super.afterEach()
  }

  "Kosken testioppijoiden tiedot voi hakea ilman virheitä" in {
    val oppijaOidit = KoskiSpecificMockOppijat.defaultOppijat
      .filter(o => o.henkilö.hetu.isEmpty || o.henkilö.hetu.exists(!KoskiApplicationForTests.virtaClient.asInstanceOf[MockVirtaClient].virheenAiheuttavaHetu(_)))
      .map(_.henkilö.oid)

    oppijaOidit.length should be > 100

    oppijaOidit.foreach(oppijaOid => {
      val result = vktService.findOppija(oppijaOid)
      result.isRight should be(true)
    })
  }

  "Korkeakoulu" - {
    val oppijat = Seq(
      KoskiSpecificMockOppijat.dippainssi,
      KoskiSpecificMockOppijat.korkeakoululainen,
      KoskiSpecificMockOppijat.amkValmistunut,
      KoskiSpecificMockOppijat.opintojaksotSekaisin,
      KoskiSpecificMockOppijat.amkKesken,
      KoskiSpecificMockOppijat.amkKeskeytynyt
    )

    oppijat.foreach(oppija => {
      s"Tiedot palautetaan ${oppija.sukunimi} ${oppija.etunimet} (${oppija.hetu.getOrElse("EI HETUA")})" in {
        val expectedOoDatat = getOpiskeluoikeudet(oppija.oid)
          .filter(_.tyyppi.koodiarvo == schema.OpiskeluoikeudenTyyppi.korkeakoulutus.koodiarvo)
          .filter(_.suoritukset.collect {
            case s: schema.KorkeakoulututkinnonSuoritus => s
          }.nonEmpty)

        val result = vktService.findOppija(oppija.oid)

        result.isRight should be(true)

        result.foreach(o => {
          verifyOppija(oppija, o)
          o.opiskeluoikeudet.length should be(expectedOoDatat.length)

          val actualOotSorted = o.opiskeluoikeudet.sortBy(_.suoritukset.head.tyyppi.koodiarvo)
          val expectedOoDatatSorted = expectedOoDatat.sortBy(_.suoritukset.head.tyyppi.koodiarvo)

          actualOotSorted.zip(expectedOoDatatSorted).foreach {
            case (actualOo, expectedOoData) =>
              val expectedSuoritusDatat = expectedOoData.suoritukset.collect {
                case s: schema.KorkeakoulututkinnonSuoritus => s
              }
              val actualSuoritukset = actualOo.suoritukset

              verifyOpiskeluoikeusJaSuoritus(actualOo, actualSuoritukset, expectedOoData, expectedSuoritusDatat)
          }
        })
      }
    })
  }

  "Ylioppilastutkinto" - {
    s"Keskeneräisen tietoja ei palauteta" in {
      val oppija = KoskiSpecificMockOppijat.ylioppilasEiValmistunut

      val result = vktService.findOppija(oppija.oid)

      result.isRight should be(true)

      result.foreach(o => {
        verifyOppija(oppija, o)
        o.opiskeluoikeudet.length should be(0)
      })
    }

    s"Valmistuneen tiedot palautetaan" in {
      val oppija = KoskiSpecificMockOppijat.ylioppilas

      val expectedOoData = getOpiskeluoikeus(oppija.oid, schema.OpiskeluoikeudenTyyppi.ylioppilastutkinto.koodiarvo)
      val expectedSuoritusDatat = expectedOoData.suoritukset

      val result = vktService.findOppija(oppija.oid)

      result.isRight should be(true)

      result.foreach(o => {
        verifyOppija(oppija, o)

        o.opiskeluoikeudet should have length 1
        o.opiskeluoikeudet.head shouldBe a[VktYlioppilastutkinnonOpiskeluoikeus]

        val actualOo = o.opiskeluoikeudet.head
        val actualSuoritukset = actualOo.suoritukset

        verifyOpiskeluoikeusJaSuoritus(actualOo, actualSuoritukset, expectedOoData, expectedSuoritusDatat)
      })
    }
  }

  "DIA" - {
    val oppijat = Seq(
      KoskiSpecificMockOppijat.dia,
    )

    oppijat.foreach(oppija => {
      s"Tiedot palautetaan ${oppija.sukunimi} ${oppija.etunimet} (${oppija.hetu.getOrElse("EI HETUA")})" in {
        val expectedOoData = getOpiskeluoikeus(oppija.oid, schema.OpiskeluoikeudenTyyppi.diatutkinto.koodiarvo)
        val expectedSuoritusDatat = expectedOoData.suoritukset.collect {
          case s: schema.DIATutkinnonSuoritus => s
        }

        val result = vktService.findOppija(oppija.oid)

        result.isRight should be(true)

        result.map(o => {
          verifyOppija(oppija, o)

          o.opiskeluoikeudet should have length 1
          o.opiskeluoikeudet.head shouldBe a[VktDIAOpiskeluoikeus]

          val actualOo = o.opiskeluoikeudet.head
          val actualSuoritukset = actualOo.suoritukset

          verifyOpiskeluoikeusJaSuoritus(actualOo, actualSuoritukset, expectedOoData, expectedSuoritusDatat)
        })
      }
    })
  }

  "EB" - {
    val oppijat = Seq(
      KoskiSpecificMockOppijat.europeanSchoolOfHelsinki,
    )

    oppijat.foreach(oppija => {
      s"Tiedot oppijasta, jolla secondary upper -vuosiluokan suorituksia, palautetaan ${oppija.sukunimi} ${oppija.etunimet} (${oppija.hetu.getOrElse("EI HETUA")})" in {
        val expectedEbOoData = getOpiskeluoikeus(oppija.oid, schema.OpiskeluoikeudenTyyppi.ebtutkinto.koodiarvo)

        val expectedEbSuoritusDatat = expectedEbOoData.suoritukset.collect {
          case s: schema.EBTutkinnonSuoritus => s
        }

        val result = vktService.findOppija(oppija.oid)

        result.isRight should be(true)

        result.map(o => {
          verifyOppija(oppija, o)

          o.opiskeluoikeudet should have length 1

          val actualEbOo = o.opiskeluoikeudet.collectFirst { case eb: VktEBTutkinnonOpiskeluoikeus => eb }.get
          val actualEbSuoritukset = actualEbOo.suoritukset

          verifyOpiskeluoikeusJaSuoritus(actualEbOo, actualEbSuoritukset, expectedEbOoData, expectedEbSuoritusDatat)
        })
      }
    })
  }

  private def makeOpiskeluoikeus(
    alkamispäivä: LocalDate = longTimeAgo,
    oppilaitos: schema.Oppilaitos = schema.Oppilaitos(MockOrganisaatiot.stadinAmmattiopisto),
    suoritus: schema.AmmatillinenPäätasonSuoritus,
    tila: Option[schema.Koodistokoodiviite] = None
  ) = schema.AmmatillinenOpiskeluoikeus(
    tila = schema.AmmatillinenOpiskeluoikeudenTila(
      List(
        schema.AmmatillinenOpiskeluoikeusjakso(alkamispäivä, opiskeluoikeusLäsnä, Some(valtionosuusRahoitteinen))
      ) ++ tila.map(t => schema.AmmatillinenOpiskeluoikeusjakso(date(2023, 12, 31), t, Some(valtionosuusRahoitteinen))).toList
    ),
    oppilaitos = Some(oppilaitos),
    suoritukset = List(suoritus)
  )

  private def verifyOppija(expected: LaajatOppijaHenkilöTiedot, actual: VktOppija) = {
    actual.henkilö.oid should be(expected.oid)
    actual.henkilö.etunimet should be(expected.etunimet)
    actual.henkilö.kutsumanimi should be(expected.kutsumanimi)
    actual.henkilö.sukunimi should be(expected.sukunimi)
    actual.henkilö.syntymäaika should be(expected.syntymäaika)
  }

  private def verifyOpiskeluoikeusJaSuoritus(
    actualOo: VktOpiskeluoikeus,
    actualSuoritukset: Seq[Suoritus],
    expectedOoData: schema.Opiskeluoikeus,
    expectedSuoritusDatat: Seq[schema.Suoritus]
  ): Unit = {
    actualSuoritukset.length should equal(expectedSuoritusDatat.length)

    actualSuoritukset.zip(expectedSuoritusDatat).foreach {
      case (actualSuoritus, expectedSuoritusData) =>
        (actualOo, actualSuoritus, expectedOoData, expectedSuoritusData) match {
          case (
            actualOo: VktKorkeakoulunOpiskeluoikeus,
            actualSuoritus: VktKorkeakoulututkinnonSuoritus,
            expectedOoData: schema.KorkeakoulunOpiskeluoikeus,
            expectedSuoritusData: schema.KorkeakouluSuoritus
            ) => verifyKorkeakoulu(actualOo, actualSuoritus, expectedOoData, expectedSuoritusData)
          case (
            actualOo: VktDIAOpiskeluoikeus,
            actualSuoritus: VktDIATutkinnonSuoritus,
            expectedOoData: schema.DIAOpiskeluoikeus,
            expectedSuoritusData: schema.DIAPäätasonSuoritus
            ) => verifyDIA(actualOo, actualSuoritus, expectedOoData, expectedSuoritusData)
          case (
            actualOo: VktEBTutkinnonOpiskeluoikeus,
            actualSuoritus: VktEBTutkinnonPäätasonSuoritus,
            expectedOoData: schema.EBOpiskeluoikeus,
            expectedSuoritusData: schema.EBTutkinnonSuoritus,
            ) => verifyEB(actualOo, actualSuoritus, expectedOoData, expectedSuoritusData)
          case (
            actualOo: VktYlioppilastutkinnonOpiskeluoikeus,
            actualSuoritus: VktYlioppilastutkinnonPäätasonSuoritus,
            expectedOoData: schema.YlioppilastutkinnonOpiskeluoikeus,
            expectedSuoritusData: schema.YlioppilastutkinnonSuoritus
            ) => verifyYO(actualOo, actualSuoritus, expectedOoData, expectedSuoritusData)
          case _ => fail(s"Palautettiin tunnistamattoman tyyppistä dataa actual: (${actualOo.getClass.getName},${actualSuoritus.getClass.getName}), expected:(${expectedOoData.getClass.getName},${expectedSuoritusData.getClass.getName})")
        }
    }
  }

  private def verifyDIA(
    actualOo: VktDIAOpiskeluoikeus,
    actualSuoritus: VktDIATutkinnonSuoritus,
    expectedOoData: schema.DIAOpiskeluoikeus,
    expectedSuoritusData: schema.DIAPäätasonSuoritus
  ): Unit = {
    verifyKoskiOpiskeluoikeudenKentät(actualOo, expectedOoData)
    actualSuoritus.tyyppi.koodiarvo should equal(expectedSuoritusData.tyyppi.koodiarvo)
    actualSuoritus.koulutusmoduuli.tunniste.koodiarvo should equal(expectedSuoritusData.koulutusmoduuli.tunniste.koodiarvo)
  }

  private def verifyEB(
    actualOo: VktEBTutkinnonOpiskeluoikeus,
    actualSuoritus: VktEBTutkinnonPäätasonSuoritus,
    expectedOoData: schema.EBOpiskeluoikeus,
    expectedSuoritusData: schema.EBTutkinnonSuoritus
  ): Unit = {
    verifyKoskiOpiskeluoikeudenKentät(actualOo, expectedOoData)

    actualSuoritus.tyyppi.koodiarvo should equal(expectedSuoritusData.tyyppi.koodiarvo)
    actualSuoritus.koulutusmoduuli.tunniste.koodiarvo should equal(expectedSuoritusData.koulutusmoduuli.tunniste.koodiarvo)
  }

  private def verifyYO(
    actualOo: VktYlioppilastutkinnonOpiskeluoikeus,
    actualSuoritus: VktYlioppilastutkinnonPäätasonSuoritus,
    expectedOoData: schema.YlioppilastutkinnonOpiskeluoikeus,
    expectedSuoritusData: schema.YlioppilastutkinnonSuoritus
  ): Unit = {
    verifyOpiskeluoikeudenKentät(actualOo, expectedOoData)

    actualSuoritus.tyyppi.koodiarvo should equal(expectedSuoritusData.tyyppi.koodiarvo)
    actualSuoritus.koulutusmoduuli.tunniste.koodiarvo should equal(expectedSuoritusData.koulutusmoduuli.tunniste.koodiarvo)
    actualSuoritus.vahvistus.map(_.päivä) should equal(expectedSuoritusData.vahvistus.map(_.päivä))
    actualSuoritus.vahvistus.isDefined should be(true)
  }

  private def verifyKorkeakoulu(
    actualOo: VktKorkeakoulunOpiskeluoikeus,
    actualSuoritus: VktKorkeakoulututkinnonSuoritus,
    expectedOoData: schema.KorkeakoulunOpiskeluoikeus,
    expectedSuoritusData: schema.KorkeakouluSuoritus
  ): Unit = {
    verifyKorkeakouluOpiskeluoikeudenKentät(actualOo, expectedOoData)

    actualSuoritus.tyyppi.koodiarvo should equal(expectedSuoritusData.tyyppi.koodiarvo)
    actualSuoritus.koulutusmoduuli.tunniste.koodiarvo should equal(expectedSuoritusData.koulutusmoduuli.tunniste.koodiarvo)

    (actualSuoritus, expectedSuoritusData) match {
      case (actualSuoritus: VktKorkeakoulututkinnonSuoritus, expectedSuoritusData: schema.KorkeakoulututkinnonSuoritus) =>
        actualSuoritus.koulutusmoduuli.koulutustyyppi should equal(expectedSuoritusData.koulutusmoduuli.koulutustyyppi)
        actualSuoritus.koulutusmoduuli.virtaNimi should equal(expectedSuoritusData.koulutusmoduuli.virtaNimi)
      case _ => fail(s"Palautettiin tunnistamattoman tyyppistä suoritusdataa actual: (${actualSuoritus.getClass.getName}), expected:(${expectedSuoritusData.getClass.getName})")
    }
  }

  private def verifyKorkeakouluOpiskeluoikeudenKentät(
    actualOo: VktKorkeakoulunOpiskeluoikeus,
    expectedOoData: schema.KorkeakoulunOpiskeluoikeus
  ): Unit = {
    verifyOpiskeluoikeudenKentät(actualOo, expectedOoData)

    actualOo.luokittelu.map(_.length) should equal(expectedOoData.luokittelu.map(_.length))
    actualOo.luokittelu.map(_.map(_.koodiarvo)) should equal(expectedOoData.luokittelu.map(_.map(_.koodiarvo)))

    actualOo.lisätiedot.map(_.virtaOpiskeluoikeudenTyyppi.map(_.koodiarvo)) should equal(expectedOoData.lisätiedot.map(_.virtaOpiskeluoikeudenTyyppi.map(_.koodiarvo)))
    actualOo.lisätiedot.map(_.lukukausiIlmoittautuminen.map(_.ilmoittautumisjaksot.length)) should equal(expectedOoData.lisätiedot.map(_.lukukausiIlmoittautuminen.map(_.ilmoittautumisjaksot.length)))
    actualOo.lisätiedot.map(_.lukukausiIlmoittautuminen.map(_.ilmoittautumisjaksot.map(_.alku))) should equal(expectedOoData.lisätiedot.map(_.lukukausiIlmoittautuminen.map(_.ilmoittautumisjaksot.map(_.alku))))
    actualOo.lisätiedot.map(_.lukukausiIlmoittautuminen.map(_.ilmoittautumisjaksot.map(_.loppu))) should equal(expectedOoData.lisätiedot.map(_.lukukausiIlmoittautuminen.map(_.ilmoittautumisjaksot.map(_.loppu))))
    actualOo.lisätiedot.map(_.lukukausiIlmoittautuminen.map(_.ilmoittautumisjaksot.map(_.tila.koodiarvo))) should equal(expectedOoData.lisätiedot.map(_.lukukausiIlmoittautuminen.map(_.ilmoittautumisjaksot.map(_.tila.koodiarvo))))
    actualOo.lisätiedot.map(_.lukukausiIlmoittautuminen.map(_.ilmoittautumisjaksot.map(_.maksetutLukuvuosimaksut.map(_.maksettu)))) should equal(expectedOoData.lisätiedot.map(_.lukukausiIlmoittautuminen.map(_.ilmoittautumisjaksot.map(_.maksetutLukuvuosimaksut.map(_.maksettu)))))
    actualOo.lisätiedot.map(_.lukukausiIlmoittautuminen.map(_.ilmoittautumisjaksot.map(_.maksetutLukuvuosimaksut.map(_.summa)))) should equal(expectedOoData.lisätiedot.map(_.lukukausiIlmoittautuminen.map(_.ilmoittautumisjaksot.map(_.maksetutLukuvuosimaksut.map(_.summa)))))
    actualOo.lisätiedot.map(_.lukukausiIlmoittautuminen.map(_.ilmoittautumisjaksot.map(_.maksetutLukuvuosimaksut.map(_.apuraha)))) should equal(expectedOoData.lisätiedot.map(_.lukukausiIlmoittautuminen.map(_.ilmoittautumisjaksot.map(_.maksetutLukuvuosimaksut.map(_.apuraha)))))
    actualOo.lisätiedot.map(_.lukukausiIlmoittautuminen.map(_.ilmoittautumisjaksot.map(_.ylioppilaskunnanJäsen))) should equal(expectedOoData.lisätiedot.map(_.lukukausiIlmoittautuminen.map(_.ilmoittautumisjaksot.map(_.ylioppilaskunnanJäsen))))
  }

  private def verifyKoskiOpiskeluoikeudenKentät(
    actualOo: VktKoskeenTallennettavaOpiskeluoikeus,
    expectedOoData: schema.KoskeenTallennettavaOpiskeluoikeus
  ): Unit = {
    verifyOpiskeluoikeudenKentät(actualOo, expectedOoData)

    actualOo.oid should be(expectedOoData.oid)
    actualOo.versionumero should be(expectedOoData.versionumero)

    actualOo.tila.opiskeluoikeusjaksot.zip(expectedOoData.tila.opiskeluoikeusjaksot).foreach {
      case (actual, expected: schema.KoskiOpiskeluoikeusjakso) =>
        actual.opintojenRahoitus.map(_.koodiarvo) should equal(expected.opintojenRahoitus.map(_.koodiarvo))
      case (actual, _) =>
        actual.opintojenRahoitus should equal(None)
    }
  }

  private def verifyOpiskeluoikeudenKentät(
    actualOo: VktOpiskeluoikeus,
    expectedOoData: schema.Opiskeluoikeus
  ): Unit = {
    actualOo.oppilaitos.map(_.oid) should equal(expectedOoData.oppilaitos.map(_.oid))
    actualOo.koulutustoimija.map(_.oid) should equal(expectedOoData.koulutustoimija.map(_.oid))
    actualOo.tyyppi.koodiarvo should equal(expectedOoData.tyyppi.koodiarvo)

    actualOo.alkamispäivä should equal(expectedOoData.alkamispäivä)
    actualOo.päättymispäivä should equal(expectedOoData.päättymispäivä)

    actualOo.tila.opiskeluoikeusjaksot.length should equal(expectedOoData.tila.opiskeluoikeusjaksot.length)
    actualOo.tila.opiskeluoikeusjaksot.map(_.tila.koodiarvo) should equal(expectedOoData.tila.opiskeluoikeusjaksot.map(_.tila.koodiarvo))
    actualOo.tila.opiskeluoikeusjaksot.map(_.alku) should equal(expectedOoData.tila.opiskeluoikeusjaksot.map(_.alku))
  }

  private def verifyEiOpiskeluoikeuksia(oppija: LaajatOppijaHenkilöTiedot) = {
    val result = vktService.findOppija(oppija.oid)

    result.isRight should be(true)

    result.map(o => {
      verifyOppija(oppija, o)

      o.opiskeluoikeudet should have length 0
    })
  }

  private def verifyEiLöydyTaiEiKäyttöoikeuksia(oppijaOid: String)(implicit user: KoskiSpecificSession): Unit = {
    val result = vktService.findOppija(oppijaOid)(user)

    result.isLeft should be(true)
    result should equal(Left(KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia("Oppijaa ei löydy tai käyttäjällä ei ole oikeuksia tietojen katseluun.")))
  }
}
