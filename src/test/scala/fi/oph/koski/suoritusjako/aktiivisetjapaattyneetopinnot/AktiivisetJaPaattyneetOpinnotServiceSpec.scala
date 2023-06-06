package fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot

import fi.oph.koski.api.{OpiskeluoikeusTestMethods, PutOpiskeluoikeusTestMethods}
import fi.oph.koski.documentation.AmmatillinenExampleData._
import fi.oph.koski.documentation.ExampleData.{longTimeAgo, opiskeluoikeusLäsnä, opiskeluoikeusMitätöity, valtionosuusRahoitteinen}
import fi.oph.koski.henkilo.{KoskiSpecificMockOppijat, LaajatOppijaHenkilöTiedot}
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.KoskiSpecificSession.SUORITUSJAKO_KATSOMINEN_USER
import fi.oph.koski.koskiuser.Rooli.OPHKATSELIJA
import fi.oph.koski.koskiuser._
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema.MYPVuosiluokanSuoritus
import fi.oph.koski.virta.MockVirtaClient
import fi.oph.koski.ytr.MockYrtClient
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
{
  def tag: universe.TypeTag[schema.AmmatillinenOpiskeluoikeus] = implicitly[reflect.runtime.universe.TypeTag[schema.AmmatillinenOpiskeluoikeus]]
  override def defaultOpiskeluoikeus = makeOpiskeluoikeus(alkamispäivä = longTimeAgo, suoritus = ammatillisenTutkinnonOsittainenSuoritus)

  val aktiivisetJaPäättyneetOpinnotService = KoskiApplicationForTests.aktiivisetJaPäättyneetOpinnotService

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
    MockYrtClient.reset()
    super.afterEach()
  }

  "Kosken testioppijoiden tiedot voi hakea ilman virheitä" in {
    val oppijaOidit = KoskiSpecificMockOppijat.defaultOppijat
      .filter(o => o.henkilö.hetu.isEmpty || o.henkilö.hetu.exists(!MockVirtaClient.virheenAiheuttavaHetu(_)))
      .map(_.henkilö.oid)

    oppijaOidit.length should be > 100

    oppijaOidit.foreach(oppijaOid => {
      val result = aktiivisetJaPäättyneetOpinnotService.findOppija(oppijaOid)
      result.isRight should be(true)
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
      KoskiSpecificMockOppijat.erikoisammattitutkinto,
      KoskiSpecificMockOppijat.reformitutkinto,
      KoskiSpecificMockOppijat.osittainenammattitutkinto,
      KoskiSpecificMockOppijat.telma,
      KoskiSpecificMockOppijat.valma
    )

    oppijat.foreach(oppija => {
      s"Tiedot palautetaan ${oppija.sukunimi} ${oppija.etunimet} (${oppija.hetu.getOrElse("EI HETUA")})" in {
        val expectedOoData = getOpiskeluoikeus(oppija.oid, schema.OpiskeluoikeudenTyyppi.ammatillinenkoulutus.koodiarvo)
        val expectedSuoritusDatat = expectedOoData.suoritukset

        val result = aktiivisetJaPäättyneetOpinnotService.findOppija(oppija.oid)

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

        val result = aktiivisetJaPäättyneetOpinnotService.findOppija(oppija.oid)

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

        val result = aktiivisetJaPäättyneetOpinnotService.findOppija(oppija.oid)

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

        val result = aktiivisetJaPäättyneetOpinnotService.findOppija(oppija.oid)

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

        val result = aktiivisetJaPäättyneetOpinnotService.findOppija(oppija.oid)

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

  "ESH" - {
    val oppijat = Seq(
      KoskiSpecificMockOppijat.europeanSchoolOfHelsinki,
    )

    oppijat.foreach(oppija => {
      s"Tiedot oppijasta, jolla secondary upper -vuosiluokan suorituksia, palautetaan ${oppija.sukunimi} ${oppija.etunimet} (${oppija.hetu.getOrElse("EI HETUA")})" in {
        val expectedOoData = getOpiskeluoikeus(oppija.oid, schema.OpiskeluoikeudenTyyppi.europeanschoolofhelsinki.koodiarvo)
        val expectedSuoritusDatat = expectedOoData.suoritukset.collect {
          case s: schema.SecondaryUpperVuosiluokanSuoritus => s
          case s: schema.EBTutkinnonSuoritus => s
        }

        val result = aktiivisetJaPäättyneetOpinnotService.findOppija(oppija.oid)

        result.isRight should be(true)

        result.map(o => {
          verifyOppija(oppija, o)

          o.opiskeluoikeudet should have length 1
          o.opiskeluoikeudet.head shouldBe a[AktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiOpiskeluoikeus]

          val actualOo = o.opiskeluoikeudet.head
          val actualSuoritukset = actualOo.suoritukset

          verifyOpiskeluoikeusJaSuoritus(actualOo, actualSuoritukset, expectedOoData, expectedSuoritusDatat)
        })
      }
    })

    "Ei palauteta opiskeluoikeutta, jolla ei ole secondary upper -vuosiluokan tai EB-tutkinnon suorituksia" in {
      val oppija = KoskiSpecificMockOppijat.europeanSchoolOfHelsinki

      val alkuperäinenOo = getOpiskeluoikeus(oppija.oid, schema.OpiskeluoikeudenTyyppi.europeanschoolofhelsinki.koodiarvo).asInstanceOf[schema.EuropeanSchoolOfHelsinkiOpiskeluoikeus]

      val ooIlmanSecondaryUpperSuorituksia = alkuperäinenOo
        .withSuoritukset(
        alkuperäinenOo.suoritukset.collect {
          case s if !s.isInstanceOf[schema.SecondaryUpperVuosiluokanSuoritus] && !s.isInstanceOf[schema.EBTutkinnonSuoritus] => s
        }
      )

      // Korvaa oppijan opiskeluoikeus sellaisella, mistä secondary upper -vuosiluokat on poistettu
      putOpiskeluoikeus(ooIlmanSecondaryUpperSuorituksia, oppija) {
        verifyResponseStatusOk()
      }

      verifyEiOpiskeluoikeuksia(oppija)

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

        val result = aktiivisetJaPäättyneetOpinnotService.findOppija(oppija.oid)

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

    "Palautetaan opiskeluioikeus, jossa on 10. vuosiluokan suoritus mutta ei uudempia" in {
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

      val result = aktiivisetJaPäättyneetOpinnotService.findOppija(oppija.oid)

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

        val result = aktiivisetJaPäättyneetOpinnotService.findOppija(oppija.oid)

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

        val result = aktiivisetJaPäättyneetOpinnotService.findOppija(oppija.oid)

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

        val result = aktiivisetJaPäättyneetOpinnotService.findOppija(oppija.oid)

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
      KoskiSpecificMockOppijat.vapaaSivistystyöLukutaitoKotoutus,
    )

    oppijat.foreach(oppija => {
      s"Tiedot oppijasta, jolla VST:n ei-vapaatavoitteisia, palautetaan ${oppija.sukunimi} ${oppija.etunimet} (${oppija.hetu.getOrElse("EI HETUA")})" in {
        val expectedOoData = getOpiskeluoikeus(oppija.oid, schema.OpiskeluoikeudenTyyppi.vapaansivistystyonkoulutus.koodiarvo)
        val expectedSuoritusDatat = expectedOoData.suoritukset

        val result = aktiivisetJaPäättyneetOpinnotService.findOppija(oppija.oid)

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

  private def verifyOppija(expected: LaajatOppijaHenkilöTiedot, actual: AktiivisetJaPäättyneetOpinnotOppija) = {
    actual.henkilö.oid should be(expected.oid)
    actual.henkilö.etunimet should be(expected.etunimet)
    actual.henkilö.kutsumanimi should be(expected.kutsumanimi)
    actual.henkilö.sukunimi should be(expected.sukunimi)
    actual.henkilö.syntymäaika should be(expected.syntymäaika)
  }

  private def verifyOpiskeluoikeusJaSuoritus(
    actualOo: AktiivisetJaPäättyneetOpinnotOpiskeluoikeus,
    actualSuoritukset: Seq[Suoritus],
    expectedOoData: schema.Opiskeluoikeus,
    expectedSuoritusDatat: Seq[schema.Suoritus]
  ): Unit = {
    actualSuoritukset.length should equal(expectedSuoritusDatat.length)

    actualSuoritukset.zip(expectedSuoritusDatat).foreach {
      case (actualSuoritus, expectedSuoritusData) =>
        (actualOo, actualSuoritus, expectedOoData, expectedSuoritusData) match {
          case (
            actualOo: AktiivisetJaPäättyneetOpinnotAmmatillinenOpiskeluoikeus,
            actualSuoritus: AktiivisetJaPäättyneetOpinnotAmmatillinenPäätasonSuoritus,
            expectedOoData: schema.AmmatillinenOpiskeluoikeus,
            expectedSuoritusData: schema.AmmatillinenPäätasonSuoritus
            ) => verifyAmmatillinen(actualOo, actualSuoritus, expectedOoData, expectedSuoritusData)
          case (
            actualOo: AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeus,
            actualSuoritus: AktiivisetJaPäättyneetOpinnotKorkeakouluSuoritus,
            expectedOoData: schema.KorkeakoulunOpiskeluoikeus,
            expectedSuoritusData: schema.KorkeakouluSuoritus
            ) => verifyKorkeakoulu(actualOo, actualSuoritus, expectedOoData, expectedSuoritusData)
          case (
            actualOo: AktiivisetJaPäättyneetOpinnotAikuistenPerusopetuksenOpiskeluoikeus,
            actualSuoritus: AktiivisetJaPäättyneetOpinnotPäätasonSuoritus,
            expectedOoData: schema.AikuistenPerusopetuksenOpiskeluoikeus,
            expectedSuoritusData: schema.AikuistenPerusopetuksenPäätasonSuoritus
            ) => verifyAikuistenPerusopetus(actualOo, actualSuoritus, expectedOoData, expectedSuoritusData)
          case (
            actualOo: AktiivisetJaPäättyneetOpinnotDIAOpiskeluoikeus,
            actualSuoritus: AktiivisetJaPäättyneetOpinnotPäätasonSuoritus,
            expectedOoData: schema.DIAOpiskeluoikeus,
            expectedSuoritusData: schema.DIAPäätasonSuoritus
            ) => verifyDIA(actualOo, actualSuoritus, expectedOoData, expectedSuoritusData)
          case (
            actualOo: AktiivisetJaPäättyneetOpinnotIBOpiskeluoikeus,
            actualSuoritus: AktiivisetJaPäättyneetOpinnotPäätasonSuoritus,
            expectedOoData: schema.IBOpiskeluoikeus,
            expectedSuoritusData: schema.IBPäätasonSuoritus
            ) => verifyIB(actualOo, actualSuoritus, expectedOoData, expectedSuoritusData)
          case (
            actualOo: AktiivisetJaPäättyneetOpinnotLukionOpiskeluoikeus,
            actualSuoritus: AktiivisetJaPäättyneetOpinnotPäätasonSuoritus,
            expectedOoData: schema.LukionOpiskeluoikeus,
            expectedSuoritusData: schema.LukionPäätasonSuoritus
            ) => verifyLukio(actualOo, actualSuoritus, expectedOoData, expectedSuoritusData)
          case (
            actualOo: AktiivisetJaPäättyneetOpinnotTutkintokoulutukseenValmentavanOpiskeluoikeus,
            actualSuoritus: AktiivisetJaPäättyneetOpinnotPäätasonSuoritus,
            expectedOoData: schema.TutkintokoulutukseenValmentavanOpiskeluoikeus,
            expectedSuoritusData: schema.TutkintokoulutukseenValmentavanKoulutuksenSuoritus
            ) => verifyTuva(actualOo, actualSuoritus, expectedOoData, expectedSuoritusData)
          case (
            actualOo: AktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiOpiskeluoikeus,
            actualSuoritus: AktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiPäätasonSuoritus,
            expectedOoData: schema.EuropeanSchoolOfHelsinkiOpiskeluoikeus,
            expectedSuoritusData: schema.EuropeanSchoolOfHelsinkiPäätasonSuoritus
            ) => verifyESH(actualOo, actualSuoritus, expectedOoData, expectedSuoritusData)
          case (
            actualOo: AktiivisetJaPäättyneetOpinnotInternationalSchoolOpiskeluoikeus,
            actualSuoritus: AktiivisetJaPäättyneetOpinnotInternationalSchoolVuosiluokanSuoritus,
            expectedOoData: schema.InternationalSchoolOpiskeluoikeus,
            expectedSuoritusData: schema.InternationalSchoolVuosiluokanSuoritus
            ) => verifyISH(actualOo, actualSuoritus, expectedOoData, expectedSuoritusData)
          case (
            actualOo: AktiivisetJaPäättyneetOpinnotMuunKuinSäännellynKoulutuksenOpiskeluoikeus,
            actualSuoritus: AktiivisetJaPäättyneetOpinnotMuunKuinSäännellynKoulutuksenPäätasonSuoritus,
            expectedOoData: schema.MuunKuinSäännellynKoulutuksenOpiskeluoikeus,
            expectedSuoritusData: schema.MuunKuinSäännellynKoulutuksenPäätasonSuoritus
            ) => verifyMUKS(actualOo, actualSuoritus, expectedOoData, expectedSuoritusData)
          case (
            actualOo: AktiivisetJaPäättyneetOpinnotVapaanSivistystyönOpiskeluoikeus,
            actualSuoritus: AktiivisetJaPäättyneetOpinnotVapaanSivistystyönPäätasonSuoritus,
            expectedOoData: schema.VapaanSivistystyönOpiskeluoikeus,
            expectedSuoritusData: schema.VapaanSivistystyönPäätasonSuoritus
            ) => verifyVST(actualOo, actualSuoritus, expectedOoData, expectedSuoritusData)
          case _ => fail(s"Palautettiin tunnistamattoman tyyppistä dataa actual: (${actualOo.getClass.getName},${actualSuoritus.getClass.getName}), expected:(${expectedOoData.getClass.getName},${expectedSuoritusData.getClass.getName})")
        }
    }

  }

  private def verifyAikuistenPerusopetus(
    actualOo: AktiivisetJaPäättyneetOpinnotAikuistenPerusopetuksenOpiskeluoikeus,
    actualSuoritus: AktiivisetJaPäättyneetOpinnotPäätasonSuoritus,
    expectedOoData: schema.AikuistenPerusopetuksenOpiskeluoikeus,
    expectedSuoritusData: schema.AikuistenPerusopetuksenPäätasonSuoritus
  ): Unit = {
    verifyKoskiOpiskeluoikeudenKentät(actualOo, expectedOoData)

    verifyPäätasonSuoritus(actualSuoritus, expectedSuoritusData)
  }

  private def verifyDIA(
    actualOo: AktiivisetJaPäättyneetOpinnotDIAOpiskeluoikeus,
    actualSuoritus: AktiivisetJaPäättyneetOpinnotPäätasonSuoritus,
    expectedOoData: schema.DIAOpiskeluoikeus,
    expectedSuoritusData: schema.DIAPäätasonSuoritus
  ): Unit = {
    verifyKoskiOpiskeluoikeudenKentät(actualOo, expectedOoData)

    verifyPäätasonSuoritus(actualSuoritus, expectedSuoritusData)
  }

  private def verifyIB(
    actualOo: AktiivisetJaPäättyneetOpinnotIBOpiskeluoikeus,
    actualSuoritus: AktiivisetJaPäättyneetOpinnotPäätasonSuoritus,
    expectedOoData: schema.IBOpiskeluoikeus,
    expectedSuoritusData: schema.IBPäätasonSuoritus
  ): Unit = {
    verifyKoskiOpiskeluoikeudenKentät(actualOo, expectedOoData)

    verifyPäätasonSuoritus(actualSuoritus, expectedSuoritusData)
  }

  private def verifyESH(
    actualOo: AktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiOpiskeluoikeus,
    actualSuoritus: AktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiPäätasonSuoritus,
    expectedOoData: schema.EuropeanSchoolOfHelsinkiOpiskeluoikeus,
    expectedSuoritusData: schema.EuropeanSchoolOfHelsinkiPäätasonSuoritus
  ): Unit = {
    verifyKoskiOpiskeluoikeudenKentät(actualOo, expectedOoData)

    actualSuoritus.tyyppi.koodiarvo should equal(expectedSuoritusData.tyyppi.koodiarvo)
    actualSuoritus.koulutusmoduuli.tunniste.koodiarvo should equal(expectedSuoritusData.koulutusmoduuli.tunniste.koodiarvo)
  }

  private def verifyISH(
    actualOo: AktiivisetJaPäättyneetOpinnotInternationalSchoolOpiskeluoikeus,
    actualSuoritus: AktiivisetJaPäättyneetOpinnotInternationalSchoolVuosiluokanSuoritus,
    expectedOoData: schema.InternationalSchoolOpiskeluoikeus,
    expectedSuoritusData: schema.InternationalSchoolVuosiluokanSuoritus
  ): Unit = {
    verifyKoskiOpiskeluoikeudenKentät(actualOo, expectedOoData)

    actualSuoritus.tyyppi.koodiarvo should equal(expectedSuoritusData.tyyppi.koodiarvo)
    actualSuoritus.koulutusmoduuli.tunniste.koodiarvo should equal(expectedSuoritusData.koulutusmoduuli.tunniste.koodiarvo)
  }

  private def verifyMUKS(
    actualOo: AktiivisetJaPäättyneetOpinnotMuunKuinSäännellynKoulutuksenOpiskeluoikeus,
    actualSuoritus: AktiivisetJaPäättyneetOpinnotMuunKuinSäännellynKoulutuksenPäätasonSuoritus,
    expectedOoData: schema.MuunKuinSäännellynKoulutuksenOpiskeluoikeus,
    expectedSuoritusData: schema.MuunKuinSäännellynKoulutuksenPäätasonSuoritus
  ): Unit = {
    verifyKoskiOpiskeluoikeudenKentät(actualOo, expectedOoData)

    actualSuoritus.tyyppi.koodiarvo should equal(expectedSuoritusData.tyyppi.koodiarvo)
    actualSuoritus.koulutusmoduuli.tunniste.koodiarvo should equal(expectedSuoritusData.koulutusmoduuli.tunniste.koodiarvo)
    actualSuoritus.koulutusmoduuli.koulutustyyppi.map(_.koodiarvo) should equal(expectedSuoritusData.koulutusmoduuli.koulutustyyppi.map(_.koodiarvo))
    actualSuoritus.koulutusmoduuli.opintokokonaisuus.koodiarvo should equal(expectedSuoritusData.koulutusmoduuli.opintokokonaisuus.koodiarvo)
    actualSuoritus.koulutusmoduuli.laajuus.map(_.yksikkö.koodiarvo) should equal(expectedSuoritusData.koulutusmoduuli.laajuus.map(_.yksikkö.koodiarvo))
    actualSuoritus.koulutusmoduuli.laajuus.map(_.arvo) should equal(expectedSuoritusData.koulutusmoduuli.laajuus.map(_.arvo))
  }

  private def verifyVST(
    actualOo: AktiivisetJaPäättyneetOpinnotVapaanSivistystyönOpiskeluoikeus,
    actualSuoritus: AktiivisetJaPäättyneetOpinnotVapaanSivistystyönPäätasonSuoritus,
    expectedOoData: schema.VapaanSivistystyönOpiskeluoikeus,
    expectedSuoritusData: schema.VapaanSivistystyönPäätasonSuoritus
  ): Unit = {
    verifyKoskiOpiskeluoikeudenKentät(actualOo, expectedOoData)

    actualSuoritus.tyyppi.koodiarvo should equal(expectedSuoritusData.tyyppi.koodiarvo)
    actualSuoritus.koulutusmoduuli.tunniste.koodiarvo should equal(expectedSuoritusData.koulutusmoduuli.tunniste.koodiarvo)
  }

  private def verifyLukio(
    actualOo: AktiivisetJaPäättyneetOpinnotLukionOpiskeluoikeus,
    actualSuoritus: AktiivisetJaPäättyneetOpinnotPäätasonSuoritus,
    expectedOoData: schema.LukionOpiskeluoikeus,
    expectedSuoritusData: schema.LukionPäätasonSuoritus
  ): Unit = {
    verifyKoskiOpiskeluoikeudenKentät(actualOo, expectedOoData)

    verifyPäätasonSuoritus(actualSuoritus, expectedSuoritusData)
  }

  private def verifyTuva(
    actualOo: AktiivisetJaPäättyneetOpinnotTutkintokoulutukseenValmentavanOpiskeluoikeus,
    actualSuoritus: AktiivisetJaPäättyneetOpinnotPäätasonSuoritus,
    expectedOoData: schema.TutkintokoulutukseenValmentavanOpiskeluoikeus,
    expectedSuoritusData: schema.TutkintokoulutukseenValmentavanKoulutuksenSuoritus
  ): Unit = {
    verifyKoskiOpiskeluoikeudenKentät(actualOo, expectedOoData)

    actualOo.järjestämislupa.koodiarvo should be(expectedOoData.järjestämislupa.koodiarvo)

    expectedOoData.lisätiedot match {
      case Some(lt: schema.TutkintokoulutukseenValmentavanOpiskeluoikeudenAmmatillisenLuvanLisätiedot) =>
        actualOo.lisätiedot.map(_.osaAikaisuusjaksot.map(_.length)) should equal(Some(lt.osaAikaisuusjaksot.map(_.length)))
        actualOo.lisätiedot.map(_.osaAikaisuusjaksot.map(_.map(_.alku))) should equal(Some(lt.osaAikaisuusjaksot.map(_.map(_.alku))))
        actualOo.lisätiedot.map(_.osaAikaisuusjaksot.map(_.map(_.loppu))) should equal(Some(lt.osaAikaisuusjaksot.map(_.map(_.loppu))))
        actualOo.lisätiedot.map(_.osaAikaisuusjaksot.map(_.map(_.osaAikaisuus))) should equal(Some(lt.osaAikaisuusjaksot.map(_.map(_.osaAikaisuus))))
      case _ =>
        actualOo.lisätiedot.flatMap(_.osaAikaisuusjaksot) should be(None)
    }

    verifyPäätasonSuoritus(actualSuoritus, expectedSuoritusData)
    actualSuoritus.koulutusmoduuli.koulutustyyppi.map(_.koodiarvo) should equal(expectedSuoritusData.koulutusmoduuli.koulutustyyppi.map(_.koodiarvo))
    actualSuoritus.koulutusmoduuli.perusteenDiaarinumero should equal(expectedSuoritusData.koulutusmoduuli.perusteenDiaarinumero)
  }

  private def verifyAmmatillinen(
    actualOo: AktiivisetJaPäättyneetOpinnotAmmatillinenOpiskeluoikeus,
    actualSuoritus: AktiivisetJaPäättyneetOpinnotAmmatillinenPäätasonSuoritus,
    expectedOoData: schema.AmmatillinenOpiskeluoikeus,
    expectedSuoritusData: schema.AmmatillinenPäätasonSuoritus
  ): Unit = {
    verifyAmmatillinenOpiskeluoikeudenKentät(actualOo, expectedOoData)

    actualSuoritus.tyyppi.koodiarvo should equal(expectedSuoritusData.tyyppi.koodiarvo)
    expectedSuoritusData match {
      case expectedSuoritusData: schema.AmmatillisenTutkinnonSuoritus =>
        verifyAmmatillisenTutkinnonSuoritus(actualSuoritus, expectedSuoritusData)
      case expectedSuoritusData: schema.AmmatillisenTutkinnonOsittainenSuoritus =>
        verifyAmmatillisenTutkinnonOsittainenSuoritus(actualSuoritus, expectedSuoritusData)
      case expectedSuoritusData: schema.TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus =>
        verifyTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus(actualSuoritus, expectedSuoritusData)
      case expectedSuoritusData: schema.MuunAmmatillisenKoulutuksenSuoritus =>
        verifyMuunAmmatillisenKoulutuksenSuoritus(actualSuoritus, expectedSuoritusData)
      case expectedSuoritusData: schema.NäyttötutkintoonValmistavanKoulutuksenSuoritus =>
        verifyNäyttötutkintoonValmistavanKoulutuksenSuoritus(actualSuoritus, expectedSuoritusData)
      case expectedSuoritusData: schema.TelmaKoulutuksenSuoritus =>
        verifyTelmaKoulutuksenSuoritus(actualSuoritus, expectedSuoritusData)
      case expectedSuoritusData: schema.ValmaKoulutuksenSuoritus =>
        verifyValmaKoulutuksenSuoritus(actualSuoritus, expectedSuoritusData)
      case _ => fail(s"Palautettiin tunnistamattoman tyyppistä suoritusdataa actual: (${actualSuoritus.getClass.getName}), expected:(${expectedSuoritusData.getClass.getName})")
    }
  }

  private def verifyAmmatillisenTutkinnonSuoritus(
    actualSuoritus: AktiivisetJaPäättyneetOpinnotAmmatillinenPäätasonSuoritus,
    expectedSuoritusData: schema.AmmatillisenTutkinnonSuoritus
  ): Unit = {
    val expectedKoulutusmoduuli = AktiivisetJaPäättyneetOpinnotAmmatillisenSuorituksenKoulutusmoduuli(
      tunniste = AktiivisetJaPäättyneetOpinnotKoodistokoodiviite.fromKoskiSchema(expectedSuoritusData.koulutusmoduuli.tunniste),
      perusteenDiaarinumero = expectedSuoritusData.koulutusmoduuli.perusteenDiaarinumero,
      perusteenNimi = expectedSuoritusData.koulutusmoduuli.perusteenNimi,
      koulutustyyppi = expectedSuoritusData.koulutusmoduuli.koulutustyyppi.map(AktiivisetJaPäättyneetOpinnotKoodistokoodiviite.fromKoskiSchema),
      kuvaus = None,
      laajuus = expectedSuoritusData.koulutusmoduuli.laajuus.map(AktiivisetJaPäättyneetOpinnotLaajuus.fromKoskiSchema)
    )
    actualSuoritus.koulutusmoduuli should be(expectedKoulutusmoduuli)

    verifyOsaamisenHankkimistavallinen(actualSuoritus, expectedSuoritusData)
    verifyKoulutussopimuksellinen(actualSuoritus, expectedSuoritusData)
  }

  private def verifyAmmatillisenTutkinnonOsittainenSuoritus(
    actualSuoritus: AktiivisetJaPäättyneetOpinnotAmmatillinenPäätasonSuoritus,
    expectedSuoritusData: schema.AmmatillisenTutkinnonOsittainenSuoritus
  ): Unit = {
    val expectedKoulutusmoduuli = AktiivisetJaPäättyneetOpinnotAmmatillisenSuorituksenKoulutusmoduuli(
      tunniste = AktiivisetJaPäättyneetOpinnotKoodistokoodiviite.fromKoskiSchema(expectedSuoritusData.koulutusmoduuli.tunniste),
      perusteenDiaarinumero = expectedSuoritusData.koulutusmoduuli.perusteenDiaarinumero,
      perusteenNimi = expectedSuoritusData.koulutusmoduuli.perusteenNimi,
      koulutustyyppi = expectedSuoritusData.koulutusmoduuli.koulutustyyppi.map(AktiivisetJaPäättyneetOpinnotKoodistokoodiviite.fromKoskiSchema),
      kuvaus = None,
      laajuus = expectedSuoritusData.koulutusmoduuli.laajuus.map(AktiivisetJaPäättyneetOpinnotLaajuus.fromKoskiSchema)
    )
    actualSuoritus.koulutusmoduuli should be(expectedKoulutusmoduuli)

    verifyOsaamisenHankkimistavallinen(actualSuoritus, expectedSuoritusData)
    verifyKoulutussopimuksellinen(actualSuoritus, expectedSuoritusData)
  }

  private def verifyTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus(
    actualSuoritus: AktiivisetJaPäättyneetOpinnotAmmatillinenPäätasonSuoritus,
    expectedSuoritusData: schema.TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus
  ): Unit = {
    val expectedKoulutusmoduuli = AktiivisetJaPäättyneetOpinnotAmmatillisenSuorituksenKoulutusmoduuli(
      tunniste = AktiivisetJaPäättyneetOpinnotPaikallinenKoodi.fromKoskiSchema(expectedSuoritusData.koulutusmoduuli.tunniste),
      perusteenDiaarinumero = None,
      perusteenNimi = None,
      koulutustyyppi = None,
      kuvaus = Some(expectedSuoritusData.koulutusmoduuli.kuvaus),
      laajuus = expectedSuoritusData.koulutusmoduuli.laajuus.map(AktiivisetJaPäättyneetOpinnotLaajuus.fromKoskiSchema)
    )
    actualSuoritus.koulutusmoduuli should be(expectedKoulutusmoduuli)

    verifyOsaamisenHankkimistavallinen(actualSuoritus, expectedSuoritusData)
    verifyKoulutussopimuksellinen(actualSuoritus, expectedSuoritusData)
  }

  private def verifyMuunAmmatillisenKoulutuksenSuoritus(
    actualSuoritus: AktiivisetJaPäättyneetOpinnotAmmatillinenPäätasonSuoritus,
    expectedSuoritusData: schema.MuunAmmatillisenKoulutuksenSuoritus
  ): Unit = {

    val expectedKoulutusmoduuli = expectedSuoritusData.koulutusmoduuli match {
      case expectedKoulutusmoduuliData: schema.AmmatilliseenTehtäväänValmistavaKoulutus =>
        AktiivisetJaPäättyneetOpinnotAmmatillisenSuorituksenKoulutusmoduuli(
          tunniste = AktiivisetJaPäättyneetOpinnotKoodistokoodiviite.fromKoskiSchema(expectedKoulutusmoduuliData.tunniste),
          perusteenDiaarinumero = None,
          perusteenNimi = None,
          koulutustyyppi = None,
          kuvaus = expectedKoulutusmoduuliData.kuvaus,
          laajuus = expectedSuoritusData.koulutusmoduuli.laajuus.map(AktiivisetJaPäättyneetOpinnotLaajuus.fromKoskiSchema)
        )
      case expectedKoulutusmoduuliData: schema.PaikallinenMuuAmmatillinenKoulutus =>
        AktiivisetJaPäättyneetOpinnotAmmatillisenSuorituksenKoulutusmoduuli(
          tunniste = AktiivisetJaPäättyneetOpinnotPaikallinenKoodi.fromKoskiSchema(expectedKoulutusmoduuliData.tunniste),
          perusteenDiaarinumero = None,
          perusteenNimi = None,
          koulutustyyppi = None,
          kuvaus = Some(expectedKoulutusmoduuliData.kuvaus),
          laajuus = expectedSuoritusData.koulutusmoduuli.laajuus.map(AktiivisetJaPäättyneetOpinnotLaajuus.fromKoskiSchema)
        )
    }
    actualSuoritus.koulutusmoduuli should be(expectedKoulutusmoduuli)

    verifyOsaamisenHankkimistavallinen(actualSuoritus, expectedSuoritusData)
    verifyKoulutussopimuksellinen(actualSuoritus, expectedSuoritusData)
  }

  private def verifyNäyttötutkintoonValmistavanKoulutuksenSuoritus(
    actualSuoritus: AktiivisetJaPäättyneetOpinnotAmmatillinenPäätasonSuoritus,
    expectedSuoritusData: schema.NäyttötutkintoonValmistavanKoulutuksenSuoritus
  ): Unit = {
    val expectedKoulutusmoduuli = AktiivisetJaPäättyneetOpinnotAmmatillisenSuorituksenKoulutusmoduuli(
      tunniste = AktiivisetJaPäättyneetOpinnotKoodistokoodiviite.fromKoskiSchema(expectedSuoritusData.koulutusmoduuli.tunniste),
      perusteenDiaarinumero = None,
      perusteenNimi = None,
      koulutustyyppi = expectedSuoritusData.koulutusmoduuli.koulutustyyppi.map(AktiivisetJaPäättyneetOpinnotKoodistokoodiviite.fromKoskiSchema),
      kuvaus = None,
      laajuus = expectedSuoritusData.koulutusmoduuli.laajuus.map(AktiivisetJaPäättyneetOpinnotLaajuus.fromKoskiSchema)
    )
    actualSuoritus.koulutusmoduuli should be(expectedKoulutusmoduuli)

    verifyOsaamisenHankkimistavallinen(actualSuoritus, expectedSuoritusData)
    verifyKoulutussopimuksellinen(actualSuoritus, expectedSuoritusData)
  }

  private def verifyTelmaKoulutuksenSuoritus(
    actualSuoritus: AktiivisetJaPäättyneetOpinnotAmmatillinenPäätasonSuoritus,
    expectedSuoritusData: schema.TelmaKoulutuksenSuoritus
  ): Unit = {
    val expectedKoulutusmoduuli = AktiivisetJaPäättyneetOpinnotAmmatillisenSuorituksenKoulutusmoduuli(
      tunniste = AktiivisetJaPäättyneetOpinnotKoodistokoodiviite.fromKoskiSchema(expectedSuoritusData.koulutusmoduuli.tunniste),
      perusteenDiaarinumero = expectedSuoritusData.koulutusmoduuli.perusteenDiaarinumero,
      perusteenNimi = None,
      koulutustyyppi = expectedSuoritusData.koulutusmoduuli.koulutustyyppi.map(AktiivisetJaPäättyneetOpinnotKoodistokoodiviite.fromKoskiSchema),
      kuvaus = None,
      laajuus = expectedSuoritusData.koulutusmoduuli.laajuus.map(AktiivisetJaPäättyneetOpinnotLaajuus.fromKoskiSchema)
    )
    actualSuoritus.koulutusmoduuli should be(expectedKoulutusmoduuli)

    actualSuoritus.osaamisenHankkimistavat should equal(None)
    verifyKoulutussopimuksellinen(actualSuoritus, expectedSuoritusData)
  }

  private def verifyValmaKoulutuksenSuoritus(
    actualSuoritus: AktiivisetJaPäättyneetOpinnotAmmatillinenPäätasonSuoritus,
    expectedSuoritusData: schema.ValmaKoulutuksenSuoritus
  ): Unit = {
    val expectedKoulutusmoduuli = AktiivisetJaPäättyneetOpinnotAmmatillisenSuorituksenKoulutusmoduuli(
      tunniste = AktiivisetJaPäättyneetOpinnotKoodistokoodiviite.fromKoskiSchema(expectedSuoritusData.koulutusmoduuli.tunniste),
      perusteenDiaarinumero = expectedSuoritusData.koulutusmoduuli.perusteenDiaarinumero,
      perusteenNimi = None,
      koulutustyyppi = expectedSuoritusData.koulutusmoduuli.koulutustyyppi.map(AktiivisetJaPäättyneetOpinnotKoodistokoodiviite.fromKoskiSchema),
      kuvaus = None,
      laajuus = expectedSuoritusData.koulutusmoduuli.laajuus.map(AktiivisetJaPäättyneetOpinnotLaajuus.fromKoskiSchema)
    )
    actualSuoritus.koulutusmoduuli should be(expectedKoulutusmoduuli)

    actualSuoritus.osaamisenHankkimistavat should equal(None)
    verifyKoulutussopimuksellinen(actualSuoritus, expectedSuoritusData)
  }

  private def verifyOsaamisenHankkimistavallinen(
    actualSuoritus: AktiivisetJaPäättyneetOpinnotAmmatillinenPäätasonSuoritus,
    expectedSuoritusData: schema.OsaamisenHankkimistavallinen
  ): Unit = {
    actualSuoritus.osaamisenHankkimistavat.map(_.length) should equal(expectedSuoritusData.osaamisenHankkimistavat.map(_.length))
    actualSuoritus.osaamisenHankkimistavat.map(_.map(_.osaamisenHankkimistapa.tunniste.koodiarvo)) should equal(expectedSuoritusData.osaamisenHankkimistavat.map(_.map(_.osaamisenHankkimistapa.tunniste.koodiarvo)))
    actualSuoritus.osaamisenHankkimistavat.map(_.map(_.alku)) should equal(expectedSuoritusData.osaamisenHankkimistavat.map(_.map(_.alku)))
    actualSuoritus.osaamisenHankkimistavat.map(_.map(_.loppu)) should equal(expectedSuoritusData.osaamisenHankkimistavat.map(_.map(_.loppu)))
  }

  private def verifyKoulutussopimuksellinen(
    actualSuoritus: AktiivisetJaPäättyneetOpinnotAmmatillinenPäätasonSuoritus,
    expectedSuoritusData: schema.Koulutussopimuksellinen
  ): Unit = {
    actualSuoritus.koulutussopimukset.map(_.length) should equal(expectedSuoritusData.koulutussopimukset.map(_.length))
    actualSuoritus.koulutussopimukset.map(_.map(_.alku)) should equal(expectedSuoritusData.koulutussopimukset.map(_.map(_.alku)))
    actualSuoritus.koulutussopimukset.map(_.map(_.loppu)) should equal(expectedSuoritusData.koulutussopimukset.map(_.map(_.loppu)))
    actualSuoritus.koulutussopimukset.map(_.map(_.maa.koodiarvo)) should equal(expectedSuoritusData.koulutussopimukset.map(_.map(_.maa.koodiarvo)))
    actualSuoritus.koulutussopimukset.map(_.map(_.paikkakunta.koodiarvo)) should equal(expectedSuoritusData.koulutussopimukset.map(_.map(_.paikkakunta.koodiarvo)))
  }

  private def verifyAmmatillinenOpiskeluoikeudenKentät(
    actualOo: AktiivisetJaPäättyneetOpinnotAmmatillinenOpiskeluoikeus,
    expectedOoData: schema.AmmatillinenOpiskeluoikeus
  ): Unit = {
    verifyKoskiOpiskeluoikeudenKentät(actualOo, expectedOoData)
    actualOo.suoritukset.length should equal(expectedOoData.suoritukset.length)

    actualOo.lisätiedot.map(_.osaAikaisuusjaksot.map(_.length)) should equal(expectedOoData.lisätiedot.map(_.osaAikaisuusjaksot.map(_.length)))
    actualOo.lisätiedot.map(_.osaAikaisuusjaksot.map(_.map(_.alku))) should equal(expectedOoData.lisätiedot.map(_.osaAikaisuusjaksot.map(_.map(_.alku))))
    actualOo.lisätiedot.map(_.osaAikaisuusjaksot.map(_.map(_.loppu))) should equal(expectedOoData.lisätiedot.map(_.osaAikaisuusjaksot.map(_.map(_.loppu))))
    actualOo.lisätiedot.map(_.osaAikaisuusjaksot.map(_.map(_.osaAikaisuus))) should equal(expectedOoData.lisätiedot.map(_.osaAikaisuusjaksot.map(_.map(_.osaAikaisuus))))
  }

  private def verifyKorkeakoulu(
    actualOo: AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeus,
    actualSuoritus: AktiivisetJaPäättyneetOpinnotKorkeakouluSuoritus,
    expectedOoData: schema.KorkeakoulunOpiskeluoikeus,
    expectedSuoritusData: schema.KorkeakouluSuoritus
  ): Unit = {
    verifyKorkeakouluOpiskeluoikeudenKentät(actualOo, expectedOoData)

    actualSuoritus.tyyppi.koodiarvo should equal(expectedSuoritusData.tyyppi.koodiarvo)
    actualSuoritus.koulutusmoduuli.tunniste.koodiarvo should equal(expectedSuoritusData.koulutusmoduuli.tunniste.koodiarvo)

    (actualSuoritus, expectedSuoritusData) match {
      case (actualSuoritus: AktiivisetJaPäättyneetOpinnotKorkeakoulututkinnonSuoritus, expectedSuoritusData: schema.KorkeakoulututkinnonSuoritus) =>
        actualSuoritus.koulutusmoduuli.koulutustyyppi should equal(expectedSuoritusData.koulutusmoduuli.koulutustyyppi)
        actualSuoritus.koulutusmoduuli.virtaNimi should equal(expectedSuoritusData.koulutusmoduuli.virtaNimi)
      case (actualSuoritus: AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunSuoritus, expectedSuoritusData: schema.MuuKorkeakoulunSuoritus) =>
        actualSuoritus.koulutusmoduuli.nimi should equal(expectedSuoritusData.koulutusmoduuli.nimi)
      case (actualSuoritus: AktiivisetJaPäättyneetOpinnotKorkeakoulunOpintojaksonSuoritus, expectedSuoritusData: schema.KorkeakoulunOpintojaksonSuoritus) =>
        actualSuoritus.koulutusmoduuli.nimi should equal(expectedSuoritusData.koulutusmoduuli.nimi)
      case _ => fail(s"Palautettiin tunnistamattoman tyyppistä suoritusdataa actual: (${actualSuoritus.getClass.getName}), expected:(${expectedSuoritusData.getClass.getName})")
    }
  }

  private def verifyPäätasonSuoritus(actualSuoritus: AktiivisetJaPäättyneetOpinnotPäätasonSuoritus, expectedSuoritusData: schema.PäätasonSuoritus) = {
    actualSuoritus.tyyppi.koodiarvo should equal(expectedSuoritusData.tyyppi.koodiarvo)
    actualSuoritus.koulutusmoduuli.tunniste.koodiarvo should equal(expectedSuoritusData.koulutusmoduuli.tunniste.koodiarvo)
  }

  private def verifyKorkeakouluOpiskeluoikeudenKentät(
    actualOo: AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeus,
    expectedOoData: schema.KorkeakoulunOpiskeluoikeus
  ): Unit = {
    verifyOpiskeluoikeudenKentät(actualOo, expectedOoData)

    actualOo.luokittelu.map(_.length) should equal(expectedOoData.luokittelu.map(_.length))
    actualOo.luokittelu.map(_.map(_.koodiarvo)) should equal(expectedOoData.luokittelu.map(_.map(_.koodiarvo)))

    actualOo.suoritukset.length should equal(expectedOoData.suoritukset.length)

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
    actualOo: AktiivisetJaPäättyneetOpinnotOpiskeluoikeus,
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
    actualOo: AktiivisetJaPäättyneetOpinnotOpiskeluoikeus,
    expectedOoData: schema.Opiskeluoikeus
  ): Unit = {
    actualOo.oppilaitos.map(_.oid) should equal(expectedOoData.oppilaitos.map(_.oid))
    actualOo.koulutustoimija.map(_.oid) should equal(expectedOoData.koulutustoimija.map(_.oid))
    actualOo.sisältyyOpiskeluoikeuteen.map(_.oid) should equal(None)
    actualOo.tyyppi.koodiarvo should equal(expectedOoData.tyyppi.koodiarvo)

    actualOo.alkamispäivä should equal(expectedOoData.alkamispäivä)
    actualOo.päättymispäivä should equal(expectedOoData.päättymispäivä)

    actualOo.tila.opiskeluoikeusjaksot.length should equal(expectedOoData.tila.opiskeluoikeusjaksot.length)
    actualOo.tila.opiskeluoikeusjaksot.map(_.tila.koodiarvo) should equal(expectedOoData.tila.opiskeluoikeusjaksot.map(_.tila.koodiarvo))
    actualOo.tila.opiskeluoikeusjaksot.map(_.alku) should equal(expectedOoData.tila.opiskeluoikeusjaksot.map(_.alku))
  }

  private def verifyEiOpiskeluoikeuksia(oppija: LaajatOppijaHenkilöTiedot) = {
    val result = aktiivisetJaPäättyneetOpinnotService.findOppija(oppija.oid)

    result.isRight should be(true)

    result.map(o => {
      verifyOppija(oppija, o)

      o.opiskeluoikeudet should have length 0
    })
  }

  private def verifyEiLöydyTaiEiKäyttöoikeuksia(oppijaOid: String)(implicit user: KoskiSpecificSession): Unit = {
    val result = aktiivisetJaPäättyneetOpinnotService.findOppija(oppijaOid)(user)

    result.isLeft should be(true)
    result should equal(Left(KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia("Oppijaa ei löydy tai käyttäjällä ei ole oikeuksia tietojen katseluun.")))
  }
}
