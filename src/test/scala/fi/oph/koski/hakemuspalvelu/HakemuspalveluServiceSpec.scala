package fi.oph.koski.hakemuspalvelu

import fi.oph.koski.api.misc.{OpiskeluoikeusTestMethods, PutOpiskeluoikeusTestMethods}
import fi.oph.koski.documentation.AmmatillinenExampleData._
import fi.oph.koski.documentation.ExampleData.{longTimeAgo, opiskeluoikeusLäsnä, valtionosuusRahoitteinen}
import fi.oph.koski.henkilo.{KoskiSpecificMockOppijat, LaajatOppijaHenkilöTiedot}
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.KoskiSpecificSession.OPH_KATSELIJA_USER
import fi.oph.koski.koskiuser.MockUsers.{hakemuspalveluKäyttäjä, vktKäyttäjä}
import fi.oph.koski.koskiuser.Rooli.OPHKATSELIJA
import fi.oph.koski.koskiuser._
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema.{LukionOppimääränSuoritus2015, LukionOppimääränSuoritus2019}
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

class HakemuspalveluServiceSpec
  extends AnyFreeSpec
    with KoskiHttpSpec
    with Matchers
    with BeforeAndAfterAll
    with BeforeAndAfterEach
    with OpiskeluoikeusTestMethods
    with PutOpiskeluoikeusTestMethods[schema.AmmatillinenOpiskeluoikeus] {
  def tag: universe.TypeTag[schema.AmmatillinenOpiskeluoikeus] = implicitly[reflect.runtime.universe.TypeTag[schema.AmmatillinenOpiskeluoikeus]]

  override def defaultOpiskeluoikeus = makeOpiskeluoikeus(alkamispäivä = longTimeAgo, suoritus = ammatillisenTutkinnonOsittainenSuoritus)

  val hakemuspalveluService = KoskiApplicationForTests.hakemuspalveluService

  implicit val koskiSession = new KoskiSpecificSession(
    AuthenticationUser(
      hakemuspalveluKäyttäjä.oid,
      OPH_KATSELIJA_USER,
      OPH_KATSELIJA_USER, None
    ),
    "fi",
    InetAddress.getLoopbackAddress,
    "",
    Set(KäyttöoikeusGlobal(List(Palvelurooli(OPHKATSELIJA))))
  )

  override def afterEach(): Unit = {
    MockYtrClient.reset()
    super.afterEach()
  }

  "Access control toimii oikein" in {
    post("/api/hakemuspalvelu/oid", JsonSerializer.writeWithRoot(OidRequest(oid = KoskiSpecificMockOppijat.dippainssi.oid)), headers = authHeaders(hakemuspalveluKäyttäjä) ++ jsonContent) {
      verifyResponseStatusOk()
      AuditLogTester.verifyLastAuditLogMessage(Map("operation" -> "HAKEMUSPALVELU_OPISKELUOIKEUS_HAKU"))
    }

    post("/api/hakemuspalvelu/oid", JsonSerializer.writeWithRoot(OidRequest(oid = KoskiSpecificMockOppijat.dippainssi.oid)), headers = authHeaders(vktKäyttäjä) ++ jsonContent) {
      verifyResponseStatus(403, KoskiErrorCategory.forbidden("Käyttäjällä ei ole oikeuksia annetun organisaation tietoihin."))
    }
  }

  "Kosken testioppijoiden tiedot voi hakea, ja ne joko palauttavat tiedot tai 404" in {
    // Tämä testi varmistaa, että mitään yllättäviä 500 tms. virheitä ei tapahdu, ja suuruusluokat on oikein, eli suurin osa testioppijoista löytyy, ja osa palauttaa 404.
    val oppijaOidit = KoskiSpecificMockOppijat.defaultOppijat
      .filter(o => o.henkilö.hetu.isEmpty || o.henkilö.hetu.exists(!KoskiApplicationForTests.virtaClient.asInstanceOf[MockVirtaClient].virheenAiheuttavaHetu(_)))
      .map(_.henkilö.oid)

    oppijaOidit.length should be > 100

    val results = oppijaOidit.map(hakemuspalveluService.findOppija)

    val (onnistuu, eiOnnistu) = results.partition(_.isRight)

    val (eiOnnistu404, _) = eiOnnistu.partition(_.left.get.statusCode == 404)

    onnistuu.length should be > 100
    eiOnnistu.length should be > 20
    eiOnnistu.length should be < 35
    eiOnnistu.length should be(eiOnnistu404.length)
  }

  "Oppija, josta ei ole tietoja Koskessa/YTR:ssä/Virrassa, palauttaa 404" in {
    val result = hakemuspalveluService.findOppija(KoskiSpecificMockOppijat.vainMitätöityjäOpiskeluoikeuksia.oid)

    result.isLeft should be(true)
    result.left.get.statusCode should be(404)
  }

  "Korkeakoulu" - {
    val oppijat = Seq(
      KoskiSpecificMockOppijat.dippainssi,
      KoskiSpecificMockOppijat.amkValmistunut,
      KoskiSpecificMockOppijat.opintojaksotSekaisin,
    )

    oppijat.foreach(oppija => {
      s"Tiedot palautetaan ${oppija.sukunimi} ${oppija.etunimet} (${oppija.hetu.getOrElse("EI HETUA")})" in {
        val expectedOoDatat = getOpiskeluoikeudet(oppija.oid)
          .filter(_.tyyppi.koodiarvo == schema.OpiskeluoikeudenTyyppi.korkeakoulutus.koodiarvo)
          .filter(_.suoritukset.collect {
            case s: schema.KorkeakoulututkinnonSuoritus => s
          }.nonEmpty)

        val result = hakemuspalveluService.findOppija(oppija.oid)

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

    Seq(
      KoskiSpecificMockOppijat.korkeakoululainen,
      KoskiSpecificMockOppijat.amkKesken,
      KoskiSpecificMockOppijat.amkKeskeytynyt
    ).foreach(oppija =>
      s"Keskeneräisen tietoja ei palauteta ${oppija.sukunimi} ${oppija.etunimet} (${oppija.hetu.getOrElse("EI HETUA")})" in {
        val result = hakemuspalveluService.findOppija(oppija.oid)

        result.isRight should be(true)

        result.foreach(o => {
          verifyOppija(oppija, o)
          o.opiskeluoikeudet.length should be(0)
        })
      }
    )
  }


  "Ylioppilastutkinto" - {
    s"Keskeneräisen tietoja ei palauteta" in {
      val oppija = KoskiSpecificMockOppijat.ylioppilasEiValmistunut

      val result = hakemuspalveluService.findOppija(oppija.oid)

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

      val result = hakemuspalveluService.findOppija(oppija.oid)

      result.isRight should be(true)

      result.foreach(o => {
        verifyOppija(oppija, o)

        o.opiskeluoikeudet should have length 1
        o.opiskeluoikeudet.head shouldBe a[HakemuspalveluYlioppilastutkinnonOpiskeluoikeus]

        val actualOo = o.opiskeluoikeudet.head
        val actualSuoritukset = actualOo.suoritukset

        verifyOpiskeluoikeusJaSuoritus(actualOo, actualSuoritukset, expectedOoData, expectedSuoritusDatat)
      })
    }

    s"Palautetaan omasta kannasta" in {
      val oppija = KoskiSpecificMockOppijat.ylioppilasUusiApi
      val result = hakemuspalveluService.findOppija(oppija.oid)

      result.isRight should be(true)

      result.foreach(o => {
        verifyOppija(oppija, o)
        o.opiskeluoikeudet.length should be(1)
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

        val result = hakemuspalveluService.findOppija(oppija.oid)

        result.isRight should be(true)

        result.map(o => {
          verifyOppija(oppija, o)

          o.opiskeluoikeudet should have length 1
          o.opiskeluoikeudet.head shouldBe a[HakemuspalveluDIAOpiskeluoikeus]

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

        val result = hakemuspalveluService.findOppija(oppija.oid)

        result.isRight should be(true)

        result.map(o => {
          verifyOppija(oppija, o)

          o.opiskeluoikeudet should have length 1

          val actualEbOo = o.opiskeluoikeudet.collectFirst { case eb: HakemuspalveluEBTutkinnonOpiskeluoikeus => eb }.get
          val actualEbSuoritukset = actualEbOo.suoritukset

          verifyOpiskeluoikeusJaSuoritus(actualEbOo, actualEbSuoritukset, expectedEbOoData, expectedEbSuoritusDatat)
        })
      }
    })
  }

  "Peruskoulu" - {
    val oppijat = Seq(
      KoskiSpecificMockOppijat.koululainen,
    )

    oppijat.foreach(oppija => {
      s"Tiedot oppijasta, jolla peruskoulun oppimäärän opiskeluoikeus, palautetaan ${oppija.sukunimi} ${oppija.etunimet} (${oppija.hetu.getOrElse("EI HETUA")})" in {
        val expectedOoData = getOpiskeluoikeus(oppija.oid, schema.OpiskeluoikeudenTyyppi.perusopetus.koodiarvo)

        val expectedSuoritusDatat = expectedOoData.suoritukset.collect {
          case s: schema.NuortenPerusopetuksenOppimääränSuoritus => s
        }

        val result = hakemuspalveluService.findOppija(oppija.oid)

        result.isRight should be(true)

        result.map(o => {
          verifyOppija(oppija, o)

          o.opiskeluoikeudet should have length 1

          val actualPeruskouluOo = o.opiskeluoikeudet.collectFirst { case pk: HakemuspalveluPerusopetuksenOppimääränOpiskeluoikeus => pk }.get
          val actualPeruskouluSuoritukset = actualPeruskouluOo.suoritukset

          verifyOpiskeluoikeusJaSuoritus(actualPeruskouluOo, actualPeruskouluSuoritukset, expectedOoData, expectedSuoritusDatat)
        })
      }
    })
  }

  "Aikuisten perusopetus" - {
    val oppijat = Seq(
      KoskiSpecificMockOppijat.aikuisOpiskelija,
    )

    oppijat.foreach(oppija => {
      s"Tiedot oppijasta, jolla aikuisten perusopetuksen oppimäärän opiskeluoikeus, palautetaan ${oppija.sukunimi} ${oppija.etunimet} (${oppija.hetu.getOrElse("EI HETUA")})" in {
        val expectedOoData = getOpiskeluoikeus(oppija.oid, schema.OpiskeluoikeudenTyyppi.aikuistenperusopetus.koodiarvo)

        val expectedSuoritusDatat = expectedOoData.suoritukset.collect {
          case s: schema.AikuistenPerusopetuksenOppimääränSuoritus => s
        }

        val result = hakemuspalveluService.findOppija(oppija.oid)

        result.isRight should be(true)

        result.map(o => {
          verifyOppija(oppija, o)

          o.opiskeluoikeudet should have length 1

          val actualOo = o.opiskeluoikeudet.collectFirst { case pk: HakemuspalveluAikuistenPerusopetuksenOppimääränOpiskeluoikeus => pk }.get
          val actualSuoritukset = actualOo.suoritukset

          verifyOpiskeluoikeusJaSuoritus(actualOo, actualSuoritukset, expectedOoData, expectedSuoritusDatat)
        })
      }
    })
  }

  "Lukio" - {
    val oppijat = Seq(
      KoskiSpecificMockOppijat.lukiolainen,
    )

    oppijat.foreach(oppija => {
      s"Tiedot oppijasta, jolla lukion oppimäärän opiskeluoikeus, palautetaan ${oppija.sukunimi} ${oppija.etunimet} (${oppija.hetu.getOrElse("EI HETUA")})" in {
        val expectedOoData = getOpiskeluoikeus(oppija.oid, schema.OpiskeluoikeudenTyyppi.lukiokoulutus.koodiarvo)

        val expectedSuoritusDatat = expectedOoData.suoritukset.collect {
          case s: schema.LukionOppimääränSuoritus => s
        }

        val result = hakemuspalveluService.findOppija(oppija.oid)

        result.isRight should be(true)

        result.map(o => {
          verifyOppija(oppija, o)

          val actualOo = o.opiskeluoikeudet.collectFirst { case pk: HakemuspalveluLukionOppimääränOpiskeluoikeus => pk }.get
          val actualSuoritukset = actualOo.suoritukset

          verifyOpiskeluoikeusJaSuoritus(actualOo, actualSuoritukset, expectedOoData, expectedSuoritusDatat)
        })
      }
    })
  }

  "Ammatillinen" - {
    val oppijat = Seq(
      KoskiSpecificMockOppijat.ammattilainen,
    )

    oppijat.foreach(oppija => {
      s"Tiedot oppijasta, jolla lukion oppimäärän opiskeluoikeus, palautetaan ${oppija.sukunimi} ${oppija.etunimet} (${oppija.hetu.getOrElse("EI HETUA")})" in {
        val expectedOoData = getOpiskeluoikeus(oppija.oid, schema.OpiskeluoikeudenTyyppi.ammatillinenkoulutus.koodiarvo)

        val expectedSuoritusDatat = expectedOoData.suoritukset.collect {
          case s: schema.AmmatillisenTutkinnonSuoritus => s
        }

        val result = hakemuspalveluService.findOppija(oppija.oid)

        result.isRight should be(true)

        result.map(o => {
          verifyOppija(oppija, o)

          val actualOo = o.opiskeluoikeudet.collectFirst { case pk: HakemuspalveluAmmatillinenTutkintoOpiskeluoikeus => pk }.get
          val actualSuoritukset = actualOo.suoritukset

          verifyOpiskeluoikeusJaSuoritus(actualOo, actualSuoritukset, expectedOoData, expectedSuoritusDatat)
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

  private def verifyOppija(expected: LaajatOppijaHenkilöTiedot, actual: HakemuspalveluOppija) = {
    actual.henkilö.oid should be(expected.oid)
    actual.henkilö.etunimet should be(expected.etunimet)
    actual.henkilö.kutsumanimi should be(expected.kutsumanimi)
    actual.henkilö.sukunimi should be(expected.sukunimi)
    actual.henkilö.syntymäaika should be(expected.syntymäaika)
  }

  private def verifyOpiskeluoikeusJaSuoritus(
    actualOo: HakemuspalveluOpiskeluoikeus,
    actualSuoritukset: Seq[HakemuspalveluSuoritus],
    expectedOoData: schema.Opiskeluoikeus,
    expectedSuoritusDatat: Seq[schema.Suoritus]
  ): Unit = {
    actualSuoritukset.length should equal(expectedSuoritusDatat.length)

    actualSuoritukset.zip(expectedSuoritusDatat).foreach {
      case (actualSuoritus, expectedSuoritusData) =>
        (actualOo, actualSuoritus, expectedOoData, expectedSuoritusData) match {
          case (
            actualOo: HakemuspalveluKorkeakoulunOpiskeluoikeus,
            actualSuoritus: HakemuspalveluKorkeakoulututkinnonSuoritus,
            expectedOoData: schema.KorkeakoulunOpiskeluoikeus,
            expectedSuoritusData: schema.KorkeakouluSuoritus
            ) => verifyKorkeakoulu(actualOo, actualSuoritus, expectedOoData, expectedSuoritusData)
          case (
            actualOo: HakemuspalveluDIAOpiskeluoikeus,
            actualSuoritus: HakemuspalveluDIATutkinnonSuoritus,
            expectedOoData: schema.DIAOpiskeluoikeus,
            expectedSuoritusData: schema.DIAPäätasonSuoritus
            ) => verifyDIA(actualOo, actualSuoritus, expectedOoData, expectedSuoritusData)
          case (
            actualOo: HakemuspalveluEBTutkinnonOpiskeluoikeus,
            actualSuoritus: HakemuspalveluEBTutkinnonPäätasonSuoritus,
            expectedOoData: schema.EBOpiskeluoikeus,
            expectedSuoritusData: schema.EBTutkinnonSuoritus,
            ) => verifyEB(actualOo, actualSuoritus, expectedOoData, expectedSuoritusData)
          case (
            actualOo: HakemuspalveluYlioppilastutkinnonOpiskeluoikeus,
            actualSuoritus: HakemuspalveluYlioppilastutkinnonPäätasonSuoritus,
            expectedOoData: schema.YlioppilastutkinnonOpiskeluoikeus,
            expectedSuoritusData: schema.YlioppilastutkinnonSuoritus
            ) => verifyYO(actualOo, actualSuoritus, expectedOoData, expectedSuoritusData)
          case (
            actualOo: HakemuspalveluPerusopetuksenOppimääränOpiskeluoikeus,
            actualSuoritus: HakemuspalveluPerusopetuksenOppimääränSuoritus,
            expectedOoData: schema.PerusopetuksenOpiskeluoikeus,
            expectedSuoritusData: schema.PerusopetuksenOppimääränSuoritus
            ) => verifyPeruskoulu(actualOo, actualSuoritus, expectedOoData, expectedSuoritusData)
          case (
            actualOo: HakemuspalveluAikuistenPerusopetuksenOppimääränOpiskeluoikeus,
            actualSuoritus: HakemuspalveluAikuistenPerusopetuksenOppimääränSuoritus,
            expectedOoData: schema.AikuistenPerusopetuksenOpiskeluoikeus,
            expectedSuoritusData: schema.AikuistenPerusopetuksenOppimääränSuoritus
            ) => verifyAikuistenPerusopetus(actualOo, actualSuoritus, expectedOoData, expectedSuoritusData)
          case (
            actualOo: HakemuspalveluLukionOppimääränOpiskeluoikeus,
            actualSuoritus: HakemuspalveluLukionOppimääränSuoritus,
            expectedOoData: schema.LukionOpiskeluoikeus,
            expectedSuoritusData: schema.LukionOppimääränSuoritus
            ) => verifyLukio(actualOo, actualSuoritus, expectedOoData, expectedSuoritusData)
          case (
            actualOo: HakemuspalveluAmmatillinenTutkintoOpiskeluoikeus,
            actualSuoritus: HakemuspalveluAmmatillisenTutkinnonSuoritus,
            expectedOoData: schema.AmmatillinenOpiskeluoikeus,
            expectedSuoritusData: schema.AmmatillisenTutkinnonSuoritus
            ) => verifyAmmatillinen(actualOo, actualSuoritus, expectedOoData, expectedSuoritusData)
          case _ => fail(s"Palautettiin tunnistamattoman tyyppistä dataa actual: (${actualOo.getClass.getName},${actualSuoritus.getClass.getName}), expected:(${expectedOoData.getClass.getName},${expectedSuoritusData.getClass.getName})")
        }
    }
  }

  private def verifyDIA(
    actualOo: HakemuspalveluDIAOpiskeluoikeus,
    actualSuoritus: HakemuspalveluDIATutkinnonSuoritus,
    expectedOoData: schema.DIAOpiskeluoikeus,
    expectedSuoritusData: schema.DIAPäätasonSuoritus
  ): Unit = {
    verifyKoskiOpiskeluoikeudenKentät(actualOo, expectedOoData)
    actualSuoritus.tyyppi.koodiarvo should equal(expectedSuoritusData.tyyppi.koodiarvo)
    actualSuoritus.koulutusmoduuli.tunniste.koodiarvo should equal(expectedSuoritusData.koulutusmoduuli.tunniste.koodiarvo)
  }

  private def verifyEB(
    actualOo: HakemuspalveluEBTutkinnonOpiskeluoikeus,
    actualSuoritus: HakemuspalveluEBTutkinnonPäätasonSuoritus,
    expectedOoData: schema.EBOpiskeluoikeus,
    expectedSuoritusData: schema.EBTutkinnonSuoritus
  ): Unit = {
    verifyKoskiOpiskeluoikeudenKentät(actualOo, expectedOoData)

    actualSuoritus.tyyppi.koodiarvo should equal(expectedSuoritusData.tyyppi.koodiarvo)
    actualSuoritus.koulutusmoduuli.tunniste.koodiarvo should equal(expectedSuoritusData.koulutusmoduuli.tunniste.koodiarvo)
  }

  private def verifyYO(
    actualOo: HakemuspalveluYlioppilastutkinnonOpiskeluoikeus,
    actualSuoritus: HakemuspalveluYlioppilastutkinnonPäätasonSuoritus,
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
    actualOo: HakemuspalveluKorkeakoulunOpiskeluoikeus,
    actualSuoritus: HakemuspalveluKorkeakoulututkinnonSuoritus,
    expectedOoData: schema.KorkeakoulunOpiskeluoikeus,
    expectedSuoritusData: schema.KorkeakouluSuoritus
  ): Unit = {
    verifyKorkeakouluOpiskeluoikeudenKentät(actualOo, expectedOoData)

    (actualSuoritus, expectedSuoritusData) match {
      case (actualSuoritus: HakemuspalveluKorkeakoulututkinnonSuoritus, expectedSuoritusData: schema.KorkeakoulututkinnonSuoritus) =>
        actualSuoritus.koulutusmoduuli.koulutustyyppi should equal(expectedSuoritusData.koulutusmoduuli.koulutustyyppi)
        actualSuoritus.koulutusmoduuli.virtaNimi should equal(expectedSuoritusData.koulutusmoduuli.virtaNimi)
      case _ => fail(s"Palautettiin tunnistamattoman tyyppistä suoritusdataa actual: (${actualSuoritus.getClass.getName}), expected:(${expectedSuoritusData.getClass.getName})")
    }
  }

  private def verifyPeruskoulu(
    actualOo: HakemuspalveluPerusopetuksenOppimääränOpiskeluoikeus,
    actualSuoritus: HakemuspalveluPerusopetuksenOppimääränSuoritus,
    expectedOoData: schema.PerusopetuksenOpiskeluoikeus,
    expectedSuoritusData: schema.PerusopetuksenOppimääränSuoritus
  ): Unit = {
    verifyPeruskouluOpiskeluoikeudenKentät(actualOo, expectedOoData)

    (actualSuoritus, expectedSuoritusData) match {
      case (actualSuoritus: HakemuspalveluPerusopetuksenOppimääränSuoritus, expectedSuoritusData: schema.PerusopetuksenOppimääränSuoritus) =>
        actualSuoritus.tyyppi.koodiarvo should equal(expectedSuoritusData.tyyppi.koodiarvo)
        actualSuoritus.koulutusmoduuli.tunniste.koodiarvo should equal(expectedSuoritusData.koulutusmoduuli.tunniste.koodiarvo)
      case _ => fail(s"Palautettiin tunnistamattoman tyyppistä suoritusdataa actual: (${actualSuoritus.getClass.getName}), expected:(${expectedSuoritusData.getClass.getName})")
    }
  }


  private def verifyAikuistenPerusopetus(
    actualOo: HakemuspalveluAikuistenPerusopetuksenOppimääränOpiskeluoikeus,
    actualSuoritus: HakemuspalveluAikuistenPerusopetuksenOppimääränSuoritus,
    expectedOoData: schema.AikuistenPerusopetuksenOpiskeluoikeus,
    expectedSuoritusData: schema.AikuistenPerusopetuksenOppimääränSuoritus
  ): Unit = {
    verifyAikuistenPeruskouluOpiskeluoikeudenKentät(actualOo, expectedOoData)

    (actualSuoritus, expectedSuoritusData) match {
      case (actualSuoritus: HakemuspalveluAikuistenPerusopetuksenOppimääränSuoritus, expectedSuoritusData: schema.PerusopetuksenOppimääränSuoritus) =>
        actualSuoritus.tyyppi.koodiarvo should equal(expectedSuoritusData.tyyppi.koodiarvo)
        actualSuoritus.koulutusmoduuli.tunniste.koodiarvo should equal(expectedSuoritusData.koulutusmoduuli.tunniste.koodiarvo)
      case _ => fail(s"Palautettiin tunnistamattoman tyyppistä suoritusdataa actual: (${actualSuoritus.getClass.getName}), expected:(${expectedSuoritusData.getClass.getName})")
    }
  }

  private def verifyLukio(
    actualOo: HakemuspalveluLukionOppimääränOpiskeluoikeus,
    actualSuoritus: HakemuspalveluLukionOppimääränSuoritus,
    expectedOoData: schema.LukionOpiskeluoikeus,
    expectedSuoritusData: schema.LukionOppimääränSuoritus
  ): Unit = {
    verifyLukionOpiskeluoikeudenKentät(actualOo, expectedOoData)

    (actualSuoritus, expectedSuoritusData) match {
      case (actualSuoritus: HakemuspalveluLukionOppimääränSuoritus, expectedSuoritusData: schema.LukionOppimääränSuoritus2015) =>
        actualSuoritus.tyyppi.koodiarvo shouldEqual expectedSuoritusData.tyyppi.koodiarvo
        actualSuoritus.koulutusmoduuli.tunniste.koodiarvo should equal(expectedSuoritusData.koulutusmoduuli.tunniste.koodiarvo)
      case (actualSuoritus: HakemuspalveluLukionOppimääränSuoritus, expectedSuoritusData: schema.LukionOppimääränSuoritus2019) =>
        actualSuoritus.tyyppi.koodiarvo should equal(expectedSuoritusData.tyyppi.koodiarvo)
        actualSuoritus.koulutusmoduuli.tunniste.koodiarvo should equal(expectedSuoritusData.koulutusmoduuli.tunniste.koodiarvo)
      case _ => fail(s"Palautettiin tunnistamattoman tyyppistä suoritusdataa actual: (${actualSuoritus.getClass.getName}), expected:(${expectedSuoritusData.getClass.getName})")
    }
  }

  private def verifyAmmatillinen(
    actualOo: HakemuspalveluAmmatillinenTutkintoOpiskeluoikeus,
    actualSuoritus: HakemuspalveluAmmatillisenTutkinnonSuoritus,
    expectedOoData: schema.AmmatillinenOpiskeluoikeus,
    expectedSuoritusData: schema.AmmatillisenTutkinnonSuoritus
  ): Unit = {
    verifyAmmatillisenOpiskeluoikeudenKentät(actualOo, expectedOoData)

    (actualSuoritus, expectedSuoritusData) match {
      case (actualSuoritus: HakemuspalveluAmmatillisenTutkinnonSuoritus, expectedSuoritusData: schema.AmmatillisenTutkinnonSuoritus) =>
        actualSuoritus.koulutusmoduuli.tunniste.koodiarvo should equal(expectedSuoritusData.koulutusmoduuli.tunniste.koodiarvo)
        actualSuoritus.koulutusmoduuli.koulutustyyppi should equal(expectedSuoritusData.koulutusmoduuli.koulutustyyppi)
      case _ => fail(s"Palautettiin tunnistamattoman tyyppistä suoritusdataa actual: (${actualSuoritus.getClass.getName}), expected:(${expectedSuoritusData.getClass.getName})")
    }
  }

  private def verifyPeruskouluOpiskeluoikeudenKentät(
    actualOo: HakemuspalveluPerusopetuksenOppimääränOpiskeluoikeus,
    expectedOoData: schema.PerusopetuksenOpiskeluoikeus
  ): Unit = {
    verifyOpiskeluoikeudenKentät(actualOo, expectedOoData)
  }

  private def verifyAikuistenPeruskouluOpiskeluoikeudenKentät(
    actualOo: HakemuspalveluAikuistenPerusopetuksenOppimääränOpiskeluoikeus,
    expectedOoData: schema.AikuistenPerusopetuksenOpiskeluoikeus
  ): Unit = {
    verifyOpiskeluoikeudenKentät(actualOo, expectedOoData)
  }

  private def verifyLukionOpiskeluoikeudenKentät(
    actualOo: HakemuspalveluLukionOppimääränOpiskeluoikeus,
    expectedOoData: schema.LukionOpiskeluoikeus
  ): Unit = {
    verifyOpiskeluoikeudenKentät(actualOo, expectedOoData)
  }

  private def verifyAmmatillisenOpiskeluoikeudenKentät(
    actualOo: HakemuspalveluAmmatillinenTutkintoOpiskeluoikeus,
    expectedOoData: schema.AmmatillinenOpiskeluoikeus
  ): Unit = {
    verifyOpiskeluoikeudenKentät(actualOo, expectedOoData)
  }

  private def verifyKorkeakouluOpiskeluoikeudenKentät(
    actualOo: HakemuspalveluKorkeakoulunOpiskeluoikeus,
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
    actualOo: HakemuspalveluKoskeenTallennettavaOpiskeluoikeus,
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
    actualOo: HakemuspalveluOpiskeluoikeus,
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
    val result = hakemuspalveluService.findOppija(oppija.oid)

    result.isRight should be(true)

    result.map(o => {
      verifyOppija(oppija, o)

      o.opiskeluoikeudet should have length 0
    })
  }

  private def verifyEiLöydyTaiEiKäyttöoikeuksia(oppijaOid: String)(implicit user: KoskiSpecificSession): Unit = {
    val result = hakemuspalveluService.findOppija(oppijaOid)(user)

    result.isLeft should be(true)
    result should equal(Left(KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia("Oppijaa ei löydy tai käyttäjällä ei ole oikeuksia tietojen katseluun.")))
  }
}
