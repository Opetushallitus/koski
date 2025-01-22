package fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot

import fi.oph.koski.aktiivisetjapaattyneetopinnot.{AktiivisetJaPäättyneetOpinnotAikuistenPerusopetuksenOpiskeluoikeus, AktiivisetJaPäättyneetOpinnotAmmatillinenOpiskeluoikeus, AktiivisetJaPäättyneetOpinnotDIAOpiskeluoikeus, AktiivisetJaPäättyneetOpinnotEBTutkinnonOpiskeluoikeus, AktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiOpiskeluoikeus, AktiivisetJaPäättyneetOpinnotIBOpiskeluoikeus, AktiivisetJaPäättyneetOpinnotInternationalSchoolOpiskeluoikeus, AktiivisetJaPäättyneetOpinnotKoskeenTallennettavaOpiskeluoikeus, AktiivisetJaPäättyneetOpinnotLukionOpiskeluoikeus, AktiivisetJaPäättyneetOpinnotMuunKuinSäännellynKoulutuksenOpiskeluoikeus, AktiivisetJaPäättyneetOpinnotTutkintokoulutukseenValmentavanOpiskeluoikeus, AktiivisetJaPäättyneetOpinnotVapaanSivistystyönOpiskeluoikeus, AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonOpiskeluoikeus, ViitekehyksellisenTutkintoSuorituksenKoulutusmoduuli}
import fi.oph.koski.api.misc.{OpiskeluoikeusTestMethods, PutOpiskeluoikeusTestMethods}
import fi.oph.koski.documentation.AmmatillinenExampleData._
import fi.oph.koski.documentation.ExampleData.{longTimeAgo, opiskeluoikeusLäsnä, opiskeluoikeusMitätöity, valtionosuusRahoitteinen}
import fi.oph.koski.henkilo.{KoskiSpecificMockOppijat, LaajatOppijaHenkilöTiedot}
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.KoskiSpecificSession.SUORITUSJAKO_KATSOMINEN_USER
import fi.oph.koski.koskiuser.Rooli.OPHKATSELIJA
import fi.oph.koski.koskiuser._
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema.{Koodistokoodiviite, MYPVuosiluokanSuoritus}
import fi.oph.koski.suoritusjako.AktiivisetJaPäättyneetOpinnotOppijaJakolinkillä
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

class AktiivisetJaPäättyneetOpinnotServiceSpec
  extends AnyFreeSpec
  with KoskiHttpSpec
  with Matchers
  with BeforeAndAfterAll
  with BeforeAndAfterEach
  with OpiskeluoikeusTestMethods
  with PutOpiskeluoikeusTestMethods[schema.AmmatillinenOpiskeluoikeus]
  with AktiivisetJaPäättyneetOpinnotVerifiers
{
  def tag: universe.TypeTag[schema.AmmatillinenOpiskeluoikeus] = implicitly[reflect.runtime.universe.TypeTag[schema.AmmatillinenOpiskeluoikeus]]
  override def defaultOpiskeluoikeus = makeOpiskeluoikeus(alkamispäivä = longTimeAgo, suoritus = ammatillisenTutkinnonOsittainenSuoritus)

  val suoritusjakoService = KoskiApplicationForTests.suoritusjakoService

  private val suoritusjakoKatsominenTestUser = new KoskiSpecificSession(
    AuthenticationUser(
      SUORITUSJAKO_KATSOMINEN_USER,
      SUORITUSJAKO_KATSOMINEN_USER,
      SUORITUSJAKO_KATSOMINEN_USER, None
    ),
    "fi",
    InetAddress.getLoopbackAddress,
    "",
    Set(KäyttöoikeusGlobal(List(Palvelurooli(OPHKATSELIJA))))
  )

  implicit val koskiSession = suoritusjakoKatsominenTestUser

  override def afterEach(): Unit = {
    MockYtrClient.reset()
    super.afterEach()
  }

  "Kosken testioppijoiden tiedot voi hakea, ja ne joko palauttavat tiedot tai 404" in {
    // Tämä testi varmistaa, että mitään yllättäviä 500 tms. virheitä ei tapahdu, ja suuruusluokat on oikein, eli suurin osa testioppijoista löytyy, ja osa palauttaa 404.
    val oppijaOidit = KoskiSpecificMockOppijat.defaultOppijat
      .filter(o => o.henkilö.hetu.isEmpty || o.henkilö.hetu.exists(!KoskiApplicationForTests.virtaClient.asInstanceOf[MockVirtaClient].virheenAiheuttavaHetu(_)))
      .map(_.henkilö.oid)

    oppijaOidit.length should be > 100

    val result = oppijaOidit.map(suoritusjakoService.findAktiivisetJaPäättyneetOpinnotOppija)

    val (onnistuu, eiOnnistu) = result.partition(_.isRight)

    val (eiOnnistu404, _) = eiOnnistu.partition(_.left.get.statusCode == 404)

    onnistuu.length should be > 100
    eiOnnistu.length should be > 20
    eiOnnistu.length should be < 35
    eiOnnistu.length should be(eiOnnistu404.length)
  }

  "Oppija, josta ei ole tietoja Koskessa/YTR:ssä/Virrassa, palauttaa 404" in {
    val result = suoritusjakoService.findAktiivisetJaPäättyneetOpinnotOppija(KoskiSpecificMockOppijat.vainMitätöityjäOpiskeluoikeuksia.oid)

    result.isLeft should be(true)
    result.left.get.statusCode should be(404)
  }

  "Palautetaan EQF- ja NQF-tietoja" in {
    val oppija = KoskiSpecificMockOppijat.ammattilainen

    val expectedOoData = getOpiskeluoikeus(oppija.oid, schema.OpiskeluoikeudenTyyppi.ammatillinenkoulutus.koodiarvo)

    val result = suoritusjakoService.findAktiivisetJaPäättyneetOpinnotOppija(oppija.oid)

    result.isRight should be(true)

    result.map(o => {
      verifyOppija(oppija, o)

      val actualOo = o.opiskeluoikeudet.head
      val actualSuoritukset = actualOo.suoritukset

      actualSuoritukset.foreach(_.koulutusmoduuli.asInstanceOf[ViitekehyksellisenTutkintoSuorituksenKoulutusmoduuli].eurooppalainenTutkintojenViitekehysEQF should equal(Some(Koodistokoodiviite("4", "eqf"))))
      actualSuoritukset.foreach(_.koulutusmoduuli.asInstanceOf[ViitekehyksellisenTutkintoSuorituksenKoulutusmoduuli].kansallinenTutkintojenViitekehysNQF should equal(Some(Koodistokoodiviite("4", "nqf"))))
    })
  }

  "Ammatillinen" - {

    val oppijat = Seq(
      KoskiSpecificMockOppijat.ammattilainen,
      KoskiSpecificMockOppijat.tutkinnonOsaaPienempiKokonaisuus,
      KoskiSpecificMockOppijat.muuAmmatillinen,
      KoskiSpecificMockOppijat.muuAmmatillinenKokonaisuuksilla,
      KoskiSpecificMockOppijat.ammatilliseenTetäväänValmistavaMuuAmmatillinen,
      KoskiSpecificMockOppijat.erkkiEiperusteissa,
      KoskiSpecificMockOppijat.amis,
      KoskiSpecificMockOppijat.ammatillisenOsittainenRapsa,
      KoskiSpecificMockOppijat.paikallinenTunnustettu,
      KoskiSpecificMockOppijat.reformitutkinto,
      KoskiSpecificMockOppijat.osittainenammattitutkinto,
      KoskiSpecificMockOppijat.telma,
      KoskiSpecificMockOppijat.valma
    )

    oppijat.foreach(oppija => {
      s"Tiedot palautetaan ${oppija.sukunimi} ${oppija.etunimet} (${oppija.hetu.getOrElse("EI HETUA")})" in {
        val expectedOoData = getOpiskeluoikeus(oppija.oid, schema.OpiskeluoikeudenTyyppi.ammatillinenkoulutus.koodiarvo)
        val expectedSuoritusDatat = expectedOoData.suoritukset

        val result = suoritusjakoService.findAktiivisetJaPäättyneetOpinnotOppija(oppija.oid)

        result.isRight should be(true)

        result.map(o => {
          verifyOppija(oppija, o)

          o.opiskeluoikeudet should have length 1
          o.opiskeluoikeudet.head shouldBe a[AktiivisetJaPäättyneetOpinnotAmmatillinenOpiskeluoikeus]

          val actualOo = o.opiskeluoikeudet.head
          val actualSuoritukset = actualOo.suoritukset

          verifyOpiskeluoikeusJaSuoritus(actualOo, actualSuoritukset, expectedOoData, expectedSuoritusDatat)
        })
      }
    })

    "Näyttötutkintoon valmistavaa päätason suoritusta ei palauteta" in {
      val oppija = KoskiSpecificMockOppijat.erikoisammattitutkinto

      val expectedOoData = getOpiskeluoikeus(oppija.oid, schema.OpiskeluoikeudenTyyppi.ammatillinenkoulutus.koodiarvo)
      val expectedSuoritusDatat = expectedOoData.suoritukset.collect { case s if !s.isInstanceOf[schema.NäyttötutkintoonValmistavanKoulutuksenSuoritus] => s}

      val result = suoritusjakoService.findAktiivisetJaPäättyneetOpinnotOppija(oppija.oid)

      result.isRight should be(true)

      result.map(o => {
        verifyOppija(oppija, o)

        o.opiskeluoikeudet should have length 1
        o.opiskeluoikeudet.head shouldBe a[AktiivisetJaPäättyneetOpinnotAmmatillinenOpiskeluoikeus]

        val actualOo = o.opiskeluoikeudet.head
        val actualSuoritukset = actualOo.suoritukset

        verifyOpiskeluoikeusJaSuoritus(actualOo, actualSuoritukset, expectedOoData, expectedSuoritusDatat)
      })
    }
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
        val expectedOoDatat = getOpiskeluoikeudet(oppija.oid).filter(_.tyyppi.koodiarvo == schema.OpiskeluoikeudenTyyppi.korkeakoulutus.koodiarvo)

        val result = suoritusjakoService.findAktiivisetJaPäättyneetOpinnotOppija(oppija.oid)

        result.isRight should be(true)

        result.foreach(o => {
          verifyOppija(oppija, o)
          o.opiskeluoikeudet.length should be(expectedOoDatat.length)

          val actualOotSorted = o.opiskeluoikeudet.sortBy(_.suoritukset.head.tyyppi.koodiarvo)
          val expectedOoDatatSorted = expectedOoDatat.sortBy(_.suoritukset.head.tyyppi.koodiarvo)

          actualOotSorted.zip(expectedOoDatatSorted).foreach {
            case (actualOo, expectedOoData) =>
              val expectedSuoritusDatat = expectedOoData.suoritukset
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

      val result = suoritusjakoService.findAktiivisetJaPäättyneetOpinnotOppija(oppija.oid)

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

      val result = suoritusjakoService.findAktiivisetJaPäättyneetOpinnotOppija(oppija.oid)

      result.isRight should be(true)

      result.foreach(o => {
        verifyOppija(oppija, o)

        o.opiskeluoikeudet should have length 1
        o.opiskeluoikeudet.head shouldBe a[AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonOpiskeluoikeus]

        val actualOo = o.opiskeluoikeudet.head
        val actualSuoritukset = actualOo.suoritukset

        verifyOpiskeluoikeusJaSuoritus(actualOo, actualSuoritukset, expectedOoData, expectedSuoritusDatat)
      })
    }
  }

  "Aikuisten perusopetus" - {
    val oppijat = Seq(
      KoskiSpecificMockOppijat.oppiaineenKorottaja,
      KoskiSpecificMockOppijat.montaOppiaineenOppimäärääOpiskeluoikeudessa,
      KoskiSpecificMockOppijat.aikuisOpiskelija,
      KoskiSpecificMockOppijat.aikuisOpiskelijaMuuRahoitus,
    )

    oppijat.foreach(oppija => {
      s"Tiedot palautetaan ${oppija.sukunimi} ${oppija.etunimet} (${oppija.hetu.getOrElse("EI HETUA")})" in {
        val expectedOoData = getOpiskeluoikeus(oppija.oid, schema.OpiskeluoikeudenTyyppi.aikuistenperusopetus.koodiarvo)
        val expectedSuoritusDatat = expectedOoData.suoritukset

        val result = suoritusjakoService.findAktiivisetJaPäättyneetOpinnotOppija(oppija.oid)

        result.isRight should be(true)

        result.map(o => {
          verifyOppija(oppija, o)

          o.opiskeluoikeudet should have length 1
          o.opiskeluoikeudet.head shouldBe a[AktiivisetJaPäättyneetOpinnotAikuistenPerusopetuksenOpiskeluoikeus]

          val actualOo = o.opiskeluoikeudet.head
          val actualSuoritukset = actualOo.suoritukset

          verifyOpiskeluoikeusJaSuoritus(actualOo, actualSuoritukset, expectedOoData, expectedSuoritusDatat)
        })
      }
    })
  }

  "DIA" - {
    val oppijat = Seq(
      KoskiSpecificMockOppijat.dia,
    )

    oppijat.foreach(oppija => {
      s"Tiedot palautetaan ${oppija.sukunimi} ${oppija.etunimet} (${oppija.hetu.getOrElse("EI HETUA")})" in {
        val expectedOoData = getOpiskeluoikeus(oppija.oid, schema.OpiskeluoikeudenTyyppi.diatutkinto.koodiarvo)
        val expectedSuoritusDatat = expectedOoData.suoritukset

        val result = suoritusjakoService.findAktiivisetJaPäättyneetOpinnotOppija(oppija.oid)

        result.isRight should be(true)

        result.map(o => {
          verifyOppija(oppija, o)

          o.opiskeluoikeudet should have length 1
          o.opiskeluoikeudet.head shouldBe a[AktiivisetJaPäättyneetOpinnotDIAOpiskeluoikeus]

          val actualOo = o.opiskeluoikeudet.head
          val actualSuoritukset = actualOo.suoritukset

          verifyOpiskeluoikeusJaSuoritus(actualOo, actualSuoritukset, expectedOoData, expectedSuoritusDatat)
        })
      }
    })
  }

  "IB" - {
    val oppijat = Seq(
      KoskiSpecificMockOppijat.ibFinal,
      KoskiSpecificMockOppijat.ibPredicted,
      KoskiSpecificMockOppijat.ibPreIB2019,
      KoskiSpecificMockOppijat.vanhanMallinenIBOppija,
    )

    oppijat.foreach(oppija => {
      s"Tiedot palautetaan ${oppija.sukunimi} ${oppija.etunimet} (${oppija.hetu.getOrElse("EI HETUA")})" in {
        val expectedOoData = getOpiskeluoikeus(oppija.oid, schema.OpiskeluoikeudenTyyppi.ibtutkinto.koodiarvo)
        val expectedSuoritusDatat = expectedOoData.suoritukset

        val result = suoritusjakoService.findAktiivisetJaPäättyneetOpinnotOppija(oppija.oid)

        result.isRight should be(true)

        result.map(o => {
          verifyOppija(oppija, o)

          o.opiskeluoikeudet should have length 1
          o.opiskeluoikeudet.head shouldBe a[AktiivisetJaPäättyneetOpinnotIBOpiskeluoikeus]

          val actualOo = o.opiskeluoikeudet.head
          val actualSuoritukset = actualOo.suoritukset

          verifyOpiskeluoikeusJaSuoritus(actualOo, actualSuoritukset, expectedOoData, expectedSuoritusDatat)
        })
      }
    })
  }

  "ESH ja EB" - {
    val oppijat = Seq(
      KoskiSpecificMockOppijat.europeanSchoolOfHelsinki,
    )

    oppijat.foreach(oppija => {
      s"Tiedot oppijasta, jolla secondary upper -vuosiluokan suorituksia, palautetaan ${oppija.sukunimi} ${oppija.etunimet} (${oppija.hetu.getOrElse("EI HETUA")})" in {
        val expectedEshOoData = getOpiskeluoikeus(oppija.oid, schema.OpiskeluoikeudenTyyppi.europeanschoolofhelsinki.koodiarvo)
        val expectedEbOoData = getOpiskeluoikeus(oppija.oid, schema.OpiskeluoikeudenTyyppi.ebtutkinto.koodiarvo)
        val expectedEshSuoritusDatat = expectedEshOoData.suoritukset.collect {
          case s: schema.SecondaryUpperVuosiluokanSuoritus => s
          case s: schema.SecondaryLowerVuosiluokanSuoritus if s.koulutusmoduuli.tunniste.koodiarvo == "S5" => s
        }
        val expectedEbSuoritusDatat = expectedEbOoData.suoritukset.collect {
          case s: schema.EBTutkinnonSuoritus => s
        }

        val result = suoritusjakoService.findAktiivisetJaPäättyneetOpinnotOppija(oppija.oid)

        result.isRight should be(true)

        result.map(o => {
          verifyOppija(oppija, o)

          o.opiskeluoikeudet should have length 2

          val actualEshOo = o.opiskeluoikeudet.collectFirst { case esh: AktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiOpiskeluoikeus => esh}.get
          val actualEbOo = o.opiskeluoikeudet.collectFirst { case eb: AktiivisetJaPäättyneetOpinnotEBTutkinnonOpiskeluoikeus => eb}.get
          val actualEshSuoritukset = actualEshOo.suoritukset
          val actualEbSuoritukset = actualEbOo.suoritukset

          verifyOpiskeluoikeusJaSuoritus(actualEshOo, actualEshSuoritukset, expectedEshOoData, expectedEshSuoritusDatat)
          verifyOpiskeluoikeusJaSuoritus(actualEbOo, actualEbSuoritukset, expectedEbOoData, expectedEbSuoritusDatat)
        })
      }
    })

    "Palautetaan S5-vuosiluokan suoritus" in {
      val oppija = KoskiSpecificMockOppijat.europeanSchoolOfHelsinki

      val expectedEshOo = getOpiskeluoikeus(oppija.oid, schema.OpiskeluoikeudenTyyppi.europeanschoolofhelsinki.koodiarvo).asInstanceOf[schema.EuropeanSchoolOfHelsinkiOpiskeluoikeus]

      val expectedEshSuoritukset = expectedEshOo.suoritukset.collect {
        case s: schema.SecondaryLowerVuosiluokanSuoritus if s.koulutusmoduuli.tunniste.koodiarvo == "S5" => s
      }

      expectedEshSuoritukset should have length 1

      val result = suoritusjakoService.findAktiivisetJaPäättyneetOpinnotOppija(oppija.oid)

      result.isRight should be(true)

      result.map(o => {
        verifyOppija(oppija, o)

        o.opiskeluoikeudet should have length 2

        val actualEshOo = o.opiskeluoikeudet.collectFirst { case esh: AktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiOpiskeluoikeus => esh }.get

        actualEshOo.suoritukset.exists(s =>
          s.tyyppi.koodiarvo == "europeanschoolofhelsinkivuosiluokkasecondarylower" && s.koulutusmoduuli.tunniste.koodiarvo == "S5"
        ) should be(true)
      })
    }

    "Ei palauteta ESH opiskeluoikeutta, jolla ei ole S5-suoritusta eikä secondary upper -vuosiluokan suorituksia" in {
      val oppija = KoskiSpecificMockOppijat.europeanSchoolOfHelsinki

      val alkuperäinenOo = getOpiskeluoikeus(oppija.oid, schema.OpiskeluoikeudenTyyppi.europeanschoolofhelsinki.koodiarvo).asInstanceOf[schema.EuropeanSchoolOfHelsinkiOpiskeluoikeus]

      val ooIlmanSecondaryUpperSuorituksia = alkuperäinenOo
        .withSuoritukset(
        alkuperäinenOo.suoritukset.collect {
          case s if !s.isInstanceOf[schema.SecondaryUpperVuosiluokanSuoritus] && s.koulutusmoduuli.tunniste.koodiarvo != "S5" => s
        }
      )

      // Korvaa oppijan ESH opiskeluoikeus sellaisella, mistä secondary upper -vuosiluokat on poistettu
      putOpiskeluoikeus(ooIlmanSecondaryUpperSuorituksia, oppija) {
        verifyResponseStatusOk()
      }

      // Tarkista, että pelkkä EB-tutkinnon opiskeluoikeus palautetaan
      val result = suoritusjakoService.findAktiivisetJaPäättyneetOpinnotOppija(oppija.oid)

      result.isRight should be(true)
      result.map(o => {
        verifyOppija(oppija, o)

        o.opiskeluoikeudet should have length 1
        o.opiskeluoikeudet.head shouldBe a[AktiivisetJaPäättyneetOpinnotEBTutkinnonOpiskeluoikeus]
      })

      // Palauta vanha data
      putOpiskeluoikeus(
        alkuperäinenOo.copy(versionumero = ooIlmanSecondaryUpperSuorituksia.versionumero.map(_ + 1)), oppija
      ) {
        verifyResponseStatusOk()
      }
    }
  }

  "ISH" - {
    val oppijat = Seq(
      KoskiSpecificMockOppijat.internationalschool,
    )

    oppijat.foreach(oppija => {
      s"Tiedot oppijasta, jolla diploma -vuosiluokan suorituksia, palautetaan ${oppija.sukunimi} ${oppija.etunimet} (${oppija.hetu.getOrElse("EI HETUA")})" in {
        val expectedOoData = getOpiskeluoikeus(oppija.oid, schema.OpiskeluoikeudenTyyppi.internationalschool.koodiarvo)
        val expectedSuoritusDatat = expectedOoData.suoritukset.collect {
          case s if schema.InternationalSchoolOpiskeluoikeus.onLukiotaVastaavaInternationalSchoolinSuoritus(s.tyyppi.koodiarvo, s.koulutusmoduuli.tunniste.koodiarvo) => s
        }

        val result = suoritusjakoService.findAktiivisetJaPäättyneetOpinnotOppija(oppija.oid)

        result.isRight should be(true)

        result.map(o => {
          verifyOppija(oppija, o)

          o.opiskeluoikeudet should have length 1
          o.opiskeluoikeudet.head shouldBe a[AktiivisetJaPäättyneetOpinnotInternationalSchoolOpiskeluoikeus]

          val actualOo = o.opiskeluoikeudet.head
          val actualSuoritukset = actualOo.suoritukset

          verifyOpiskeluoikeusJaSuoritus(actualOo, actualSuoritukset, expectedOoData, expectedSuoritusDatat)
        })
      }
    })

    "Ei palauteta opiskeluoikeutta, jolla on vain alle 10. vuosiluokan suorituksia" in {
      val oppija = KoskiSpecificMockOppijat.internationalschool

      val alkuperäinenOo = getOpiskeluoikeus(oppija.oid, schema.OpiskeluoikeudenTyyppi.internationalschool.koodiarvo).asInstanceOf[schema.InternationalSchoolOpiskeluoikeus]

      // Mitätöi alkuperäinen oo, koska ilman tätä siitä poistetut päätason suoritukset lisätään siihen muutoksen yhteydessä uudestaan.
      putOpiskeluoikeus(mitätöityOpiskeluoikeus(alkuperäinenOo), oppija) {
        verifyResponseStatusOk()
      }

      // Korvaa oppijan opiskeluoikeus sellaisella, mistä lukiota vastaavat vuosiluokat on poistettu
      val ooIlmanLukiotaVastaaviaSuorituksia = alkuperäinenOo
        .copy(
          oid = None,
          versionumero = None
        )
        .withSuoritukset(
          alkuperäinenOo.suoritukset.collect {
            case s if !schema.InternationalSchoolOpiskeluoikeus.onLukiotaVastaavaInternationalSchoolinSuoritus(s.tyyppi.koodiarvo, s.koulutusmoduuli.tunniste.koodiarvo) => s
          }
        )
      postOpiskeluoikeus(ooIlmanLukiotaVastaaviaSuorituksia, oppija) {
        verifyResponseStatusOk()
      }

      verifyEiOpiskeluoikeuksia(oppija)

      // Mitätöi korvaaja-opiskeluoikeus ja palauta vanha
      val korvaajaOo = getOpiskeluoikeus(oppija.oid, schema.OpiskeluoikeudenTyyppi.internationalschool.koodiarvo).asInstanceOf[schema.InternationalSchoolOpiskeluoikeus]
      putOpiskeluoikeus(mitätöityOpiskeluoikeus(korvaajaOo), oppija) {
        verifyResponseStatusOk()
      }
      postOpiskeluoikeus(alkuperäinenOo.copy(
        versionumero = None,
        oid = None
      ), oppija) {
        verifyResponseStatusOk()
      }
    }

    "Palautetaan opiskeluoikeus, jossa on 10. vuosiluokan suoritus mutta ei uudempia" in {
      val oppija = KoskiSpecificMockOppijat.internationalschool

      val alkuperäinenOo = getOpiskeluoikeus(oppija.oid, schema.OpiskeluoikeudenTyyppi.internationalschool.koodiarvo).asInstanceOf[schema.InternationalSchoolOpiskeluoikeus]

      // Mitätöi alkuperäinen oo, koska ilman tätä siitä poistetut päätason suoritukset lisätään siihen muutoksen yhteydessä uudestaan.
      putOpiskeluoikeus(mitätöityOpiskeluoikeus(alkuperäinenOo), oppija) {
        verifyResponseStatusOk()
      }

      // Korvaa oppijan opiskeluoikeus sellaisella, mistä lukiota vastaavat vuosiluokat on poistettu
      val ooIlmanLukiotaVastaaviaSuorituksia = alkuperäinenOo
        .copy(
          oid = None,
          versionumero = None
        )
        .withSuoritukset(
          alkuperäinenOo.suoritukset.flatMap {
            case _: schema.DiplomaVuosiluokanSuoritus => None
            case s => Some(s)
          }
        ).asInstanceOf[schema.InternationalSchoolOpiskeluoikeus]

      val expectedOoData = putAndGetOpiskeluoikeus(ooIlmanLukiotaVastaaviaSuorituksia, oppija)
      val expectedSuoritusDatat = expectedOoData.suoritukset.collect { case s: MYPVuosiluokanSuoritus if s.koulutusmoduuli.tunniste.koodiarvo == "10" => s }

      val result = suoritusjakoService.findAktiivisetJaPäättyneetOpinnotOppija(oppija.oid)

      result.isRight should be(true)

      result.map(o => {
        verifyOppija(oppija, o)

        o.opiskeluoikeudet should have length 1
        o.opiskeluoikeudet.head shouldBe a[AktiivisetJaPäättyneetOpinnotInternationalSchoolOpiskeluoikeus]

        val actualOo = o.opiskeluoikeudet.head
        val actualSuoritukset = actualOo.suoritukset

        verifyOpiskeluoikeusJaSuoritus(actualOo, actualSuoritukset, expectedOoData, expectedSuoritusDatat)
      })

      // Mitätöi korvaaja-opiskeluoikeus ja palauta vanha
      val korvaajaOo = getOpiskeluoikeus(oppija.oid, schema.OpiskeluoikeudenTyyppi.internationalschool.koodiarvo).asInstanceOf[schema.InternationalSchoolOpiskeluoikeus]
      putOpiskeluoikeus(mitätöityOpiskeluoikeus(korvaajaOo), oppija) {
        verifyResponseStatusOk()
      }
      postOpiskeluoikeus(alkuperäinenOo.copy(
        versionumero = None,
        oid = None
      ), oppija) {
        verifyResponseStatusOk()
      }
    }

    def mitätöityOpiskeluoikeus(oo: schema.InternationalSchoolOpiskeluoikeus): schema.InternationalSchoolOpiskeluoikeus = {
      oo.copy(
        tila = schema.InternationalSchoolOpiskeluoikeudenTila(
          oo.tila.opiskeluoikeusjaksot ++
            List(
              schema.InternationalSchoolOpiskeluoikeusjakso(date(2022, 5, 31), opiskeluoikeusMitätöity)
            )
        )
      )
    }

    def putAndGetOpiskeluoikeus(oo: schema.InternationalSchoolOpiskeluoikeus, henkilö: schema.Henkilö): schema.InternationalSchoolOpiskeluoikeus =
      putOpiskeluoikeus(oo, henkilö) {
        verifyResponseStatusOk()
        getOpiskeluoikeus(readPutOppijaResponse.opiskeluoikeudet.head.oid)
      }.asInstanceOf[schema.InternationalSchoolOpiskeluoikeus]
  }

  "Lukio" - {
    val oppijat = Seq(
      KoskiSpecificMockOppijat.lukiolainen,
      KoskiSpecificMockOppijat.lukioKesken,
      KoskiSpecificMockOppijat.lukionAineopiskelija,
      KoskiSpecificMockOppijat.lukionAineopiskelijaAktiivinen,
      KoskiSpecificMockOppijat.uusiLukio,
      KoskiSpecificMockOppijat.uusiLukionAineopiskelija,
    )

    oppijat.foreach(oppija => {
      s"Tiedot palautetaan ${oppija.sukunimi} ${oppija.etunimet} (${oppija.hetu.getOrElse("EI HETUA")})" in {
        val expectedOoData = getOpiskeluoikeus(oppija.oid, schema.OpiskeluoikeudenTyyppi.lukiokoulutus.koodiarvo)
        val expectedSuoritusDatat = expectedOoData.suoritukset

        val result = suoritusjakoService.findAktiivisetJaPäättyneetOpinnotOppija(oppija.oid)

        result.isRight should be(true)

        result.map(o => {
          verifyOppija(oppija, o)

          o.opiskeluoikeudet should have length 1
          o.opiskeluoikeudet.head shouldBe a[AktiivisetJaPäättyneetOpinnotLukionOpiskeluoikeus]

          val actualOo = o.opiskeluoikeudet.head
          val actualSuoritukset = actualOo.suoritukset

          verifyOpiskeluoikeusJaSuoritus(actualOo, actualSuoritukset, expectedOoData, expectedSuoritusDatat)
        })
      }
    })
  }

  "Muu kuin säännelty" - {
    val oppijat = Seq(
      KoskiSpecificMockOppijat.jotpaMuuKuinSäännelty,
    )

    oppijat.foreach(oppija => {
      s"Tiedot palautetaan ${oppija.sukunimi} ${oppija.etunimet} (${oppija.hetu.getOrElse("EI HETUA")})" in {
        val expectedOoData = getOpiskeluoikeus(oppija.oid, schema.OpiskeluoikeudenTyyppi.muukuinsaanneltykoulutus.koodiarvo)
        val expectedSuoritusDatat = expectedOoData.suoritukset

        val result = suoritusjakoService.findAktiivisetJaPäättyneetOpinnotOppija(oppija.oid)

        result.isRight should be(true)

        result.map(o => {
          verifyOppija(oppija, o)

          o.opiskeluoikeudet should have length 1
          o.opiskeluoikeudet.head shouldBe a[AktiivisetJaPäättyneetOpinnotMuunKuinSäännellynKoulutuksenOpiskeluoikeus]

          val actualOo = o.opiskeluoikeudet.head
          val actualSuoritukset = actualOo.suoritukset

          verifyOpiskeluoikeusJaSuoritus(actualOo, actualSuoritukset, expectedOoData, expectedSuoritusDatat)
        })
      }
    })
  }

  "TUVA" - {
    val oppijat = Seq(
      KoskiSpecificMockOppijat.tuva,
      KoskiSpecificMockOppijat.tuvaPerus
    )

    oppijat.foreach(oppija => {
      s"Tiedot palautetaan ${oppija.sukunimi} ${oppija.etunimet} (${oppija.hetu.getOrElse("EI HETUA")})" in {
        val expectedOoData = getOpiskeluoikeus(oppija.oid, schema.OpiskeluoikeudenTyyppi.tuva.koodiarvo)
        val expectedSuoritusDatat = expectedOoData.suoritukset

        val result = suoritusjakoService.findAktiivisetJaPäättyneetOpinnotOppija(oppija.oid)

        result.isRight should be(true)

        result.map(o => {
          verifyOppija(oppija, o)

          o.opiskeluoikeudet should have length 1
          o.opiskeluoikeudet.head shouldBe a[AktiivisetJaPäättyneetOpinnotTutkintokoulutukseenValmentavanOpiskeluoikeus]

          val actualOo = o.opiskeluoikeudet.head
          val actualSuoritukset = actualOo.suoritukset

          verifyOpiskeluoikeusJaSuoritus(actualOo, actualSuoritukset, expectedOoData, expectedSuoritusDatat)
        })
      }
    })

  }

  "VST" - {
    val oppijat = Seq(
      KoskiSpecificMockOppijat.vstKoto2022Aloittaja,
      KoskiSpecificMockOppijat.vstKoto2022Kesken,
      KoskiSpecificMockOppijat.vstKoto2022Suorittanut,
      KoskiSpecificMockOppijat.vstJotpaKeskenOppija,
      KoskiSpecificMockOppijat.vapaaSivistystyöOppivelvollinen,
      KoskiSpecificMockOppijat.vapaaSivistystyöMaahanmuuttajienKotoutus,
      KoskiSpecificMockOppijat.vapaaSivistystyöLukutaitoKoulutus,
    )

    oppijat.foreach(oppija => {
      s"Tiedot oppijasta, jolla VST:n ei-vapaatavoitteisia, palautetaan ${oppija.sukunimi} ${oppija.etunimet} (${oppija.hetu.getOrElse("EI HETUA")})" in {
        val expectedOoData = getOpiskeluoikeus(oppija.oid, schema.OpiskeluoikeudenTyyppi.vapaansivistystyonkoulutus.koodiarvo)
        val expectedSuoritusDatat = expectedOoData.suoritukset

        val result = suoritusjakoService.findAktiivisetJaPäättyneetOpinnotOppija(oppija.oid)

        result.isRight should be(true)

        result.map(o => {
          verifyOppija(oppija, o)

          o.opiskeluoikeudet should have length 1
          o.opiskeluoikeudet.head shouldBe a[AktiivisetJaPäättyneetOpinnotVapaanSivistystyönOpiskeluoikeus]

          val actualOo = o.opiskeluoikeudet.head
          val actualSuoritukset = actualOo.suoritukset

          verifyOpiskeluoikeusJaSuoritus(actualOo, actualSuoritukset, expectedOoData, expectedSuoritusDatat)
        })
      }
    })

    "Ei palauteta VST:n vapaatavoitteisten opintojen opiskeluoikeutta" in {
      val oppija = KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus

      verifyEiOpiskeluoikeuksia(oppija)
    }
  }

  "Älä palauta kuori-opiskeluoikeuksia, ainoastaan sisältyvät" in {
    val oppija = KoskiSpecificMockOppijat.eskari

    val kuori: schema.AmmatillinenOpiskeluoikeus = createOpiskeluoikeus(oppija, defaultOpiskeluoikeus, user = MockUsers.stadinAmmattiopistoJaOppisopimuskeskusTallentaja)

    val sisältyväInput: schema.AmmatillinenOpiskeluoikeus = defaultOpiskeluoikeus.copy(
      oppilaitos = Some(schema.Oppilaitos(MockOrganisaatiot.omnia)),
      sisältyyOpiskeluoikeuteen = Some(schema.SisältäväOpiskeluoikeus(kuori.oppilaitos.get, kuori.oid.get)),
      suoritukset = List(
        defaultOpiskeluoikeus.suoritukset.head.asInstanceOf[schema.AmmatillisenTutkinnonOsittainenSuoritus].copy(
          toimipiste = schema.OidOrganisaatio(MockOrganisaatiot.omnia)
        )
      )
    )

    val sisältyvä = createOpiskeluoikeus(oppija, sisältyväInput, user = MockUsers.omniaTallentaja)

    val result = suoritusjakoService.findAktiivisetJaPäättyneetOpinnotOppija(oppija.oid)

    result.isRight should be(true)

    result.map(o => {
      verifyOppija(oppija, o)
      o.opiskeluoikeudet should have length 1

      o.opiskeluoikeudet.head.oppilaitos.map(_.oid) should equal(Some(MockOrganisaatiot.omnia))
      o.opiskeluoikeudet.head match {
        case koo: AktiivisetJaPäättyneetOpinnotKoskeenTallennettavaOpiskeluoikeus => koo.oid should equal(Some(sisältyvä.oid.get))
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

  private def verifyOppija(expected: LaajatOppijaHenkilöTiedot, actual: AktiivisetJaPäättyneetOpinnotOppijaJakolinkillä) = {
    actual.henkilö.oid should be(expected.oid)
    actual.henkilö.etunimet should be(expected.etunimet)
    actual.henkilö.kutsumanimi should be(expected.kutsumanimi)
    actual.henkilö.sukunimi should be(expected.sukunimi)
    actual.henkilö.syntymäaika should be(expected.syntymäaika)
  }

  private def verifyEiOpiskeluoikeuksia(oppija: LaajatOppijaHenkilöTiedot) = {
    val result = suoritusjakoService.findAktiivisetJaPäättyneetOpinnotOppija(oppija.oid)

    result.isRight should be(true)

    result.map(o => {
      verifyOppija(oppija, o)

      o.opiskeluoikeudet should have length 0
    })
  }

  private def verifyEiLöydyTaiEiKäyttöoikeuksia(oppijaOid: String)(implicit user: KoskiSpecificSession): Unit = {
    val result = suoritusjakoService.findAktiivisetJaPäättyneetOpinnotOppija(oppijaOid)(user)

    result.isLeft should be(true)
    result should equal(Left(KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia("Oppijaa ei löydy tai käyttäjällä ei ole oikeuksia tietojen katseluun.")))
  }
}
