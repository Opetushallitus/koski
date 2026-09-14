package fi.oph.koski.api.oppijavalidation

import com.typesafe.config.ConfigFactory
import fi.oph.koski.{KoskiApplicationForTests, KoskiHttpSpec}
import fi.oph.koski.api.misc.PutOpiskeluoikeusTestMethods
import fi.oph.koski.documentation.AhvenanmaanPerusopetusExampleData
import fi.oph.koski.documentation.ExampleData.{opiskeluoikeusLäsnä, ruotsinKieli}
import fi.oph.koski.documentation.PerusopetusExampleData.suoritustapaErityinenTutkinto
import fi.oph.koski.eperusteetvalidation.{EPerusteetFiller, EPerusteisiinPerustuvaValidator}
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.{AccessType, KoskiSpecificSession}
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema._
import fi.oph.koski.validation.KoskiValidator
import org.scalatest.freespec.AnyFreeSpec

import java.time.LocalDate.{of => date}

class OppijaValidationAhvenanmaanPerusopetusSpec
  extends AnyFreeSpec
  with KoskiHttpSpec
  with PutOpiskeluoikeusTestMethods[AhvenanmaanPerusopetuksenOpiskeluoikeus] {

  def tag = implicitly[reflect.runtime.universe.TypeTag[AhvenanmaanPerusopetuksenOpiskeluoikeus]]

  override def defaultOpiskeluoikeus = AhvenanmaanPerusopetusExampleData.opiskeluoikeus

  "Ahvenanmaan perusopetuksen opiskeluoikeus" - {
    "voidaan tallentaa paikallisesti" in {
      setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus) {
        verifyResponseStatusOk()
      }
    }

    // Vastaa muodoltaan sitä, minkä uuden opiskeluoikeuden luontidialogi tuottaa:
    // pelkkä oppimäärän suoritus ahvenanmaalaisessa oppilaitoksessa, ei vahvistusta.
    // Ahvenanmaalainen oppilaitos on olennainen, koska tallennus meni läpi validoinnista
    // ja kaatui vasta perustietojen serialisoinnissa, kun kunnan (koulutustoimijan)
    // ytunnus oli mockdatassa tyhjä merkkijono.
    "voidaan tallentaa ahvenanmaalaiseen oppilaitokseen" in {
      val toimipiste = Oppilaitos(MockOrganisaatiot.övernäsSkola)
      val opiskeluoikeus = AhvenanmaanPerusopetuksenOpiskeluoikeus(
        oppilaitos = Some(toimipiste),
        tila = AhvenanmaanPerusopetuksenOpiskeluoikeudenTila(
          List(AhvenanmaanPerusopetuksenOpiskeluoikeusjakso(date(2026, 8, 15), opiskeluoikeusLäsnä))
        ),
        suoritukset = List(AhvenanmaanPerusopetuksenOppimääränSuoritus(
          koulutusmoduuli = AhvenanmaanPerusopetus(
            perusteenDiaarinumero = Some(AhvenanmaanPerusopetusExampleData.ahvenanmaanDiaarinumero)
          ),
          toimipiste = toimipiste,
          suoritustapa = Koodistokoodiviite("koulutus", "perusopetuksensuoritustapa"),
          suorituskieli = ruotsinKieli
        ))
      )
      setupOppijaWithOpiskeluoikeus(opiskeluoikeus) {
        verifyResponseStatusOk()
      }
    }

    "tuotantokonfiguraatio estää tallennuksen disabledPäätasonSuoritusLuokat-asetuksella" in {
      implicit val session: KoskiSpecificSession = KoskiSpecificSession.systemUser
      implicit val accessType: AccessType.Value = AccessType.write
      val mockConfig = ConfigFactory.parseString(
        """
          features = {
            disabledPäätasonSuoritusTyypit = []
            disabledPäätasonSuoritusLuokat = [
              AhvenanmaanPerusopetuksenVuosiluokanSuoritus
              AhvenanmaanPerusopetuksenOppimääränSuoritus
              AhvenanmaanAikuistenPerusopetuksenOppimääränSuoritus
            ]
            disabledOsasuoritusTyypit = []
          }
        """.stripMargin)
      val config = KoskiApplicationForTests.config.withoutPath("features").withFallback(mockConfig)
      val validator = new KoskiValidator(
        KoskiApplicationForTests.organisaatioRepository,
        KoskiApplicationForTests.possu,
        KoskiApplicationForTests.henkilöRepository,
        new EPerusteisiinPerustuvaValidator(
          KoskiApplicationForTests.ePerusteet,
          KoskiApplicationForTests.tutkintoRepository,
          KoskiApplicationForTests.koodistoViitePalvelu,
          config
        ),
        KoskiApplicationForTests.ePerusteetLops2019Validator,
        new EPerusteetFiller(
          KoskiApplicationForTests.ePerusteet,
          KoskiApplicationForTests.tutkintoRepository,
          KoskiApplicationForTests.koodistoViitePalvelu
        ),
        KoskiApplicationForTests.validatingAndResolvingExtractor,
        KoskiApplicationForTests.suostumuksenPeruutusService,
        KoskiApplicationForTests.koodistoViitePalvelu,
        config,
        KoskiApplicationForTests.validationContext,
      )

      val opiskelija = oppija(KoskiSpecificMockOppijat.ahvenanmaanPerusoppilas.oid)
      val result = validator.updateFieldsAndValidateAsJson(opiskelija)
      result.swap.toOption.get should equal(
        KoskiErrorCategory.notImplemented("Päätason suorituksen luokka AhvenanmaanPerusopetuksenVuosiluokanSuoritus ei ole käytössä tässä ympäristössä")
      )

      val aikuisopiskelija = oppija(KoskiSpecificMockOppijat.ahvenanmaanAikuisopiskelija.oid)
      val aikuistenResult = validator.updateFieldsAndValidateAsJson(aikuisopiskelija)
      aikuistenResult.swap.toOption.get should equal(
        KoskiErrorCategory.notImplemented("Päätason suorituksen luokka AhvenanmaanAikuistenPerusopetuksenOppimääränSuoritus ei ole käytössä tässä ympäristössä")
      )
    }
  }

  "Vahvistettu oppimäärän suoritus vaatii vahvistetun 9. vuosiluokan suorituksen" - {
    val ilmanYsiluokkaa = defaultOpiskeluoikeus.copy(
      suoritukset = List(
        AhvenanmaanPerusopetusExampleData.kahdeksannenLuokanSuoritus,
        AhvenanmaanPerusopetusExampleData.päättötodistuksenSuoritus
      )
    )

    "9. vuosiluokan suoritus puuttuu" in {
      setupOppijaWithOpiskeluoikeus(ilmanYsiluokkaa) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.ahvenanmaanPerusopetuksenOppimääräIlmanYsiluokanSuoritusta())
      }
    }

    // Opiskeluoikeus jätetään läsnä-tilaan, jotta vahvistamattomasta 9. vuosiluokasta ei
    // tule päällekkäin myös vahvistusPuuttuu-virhettä.
    "9. vuosiluokan suoritusta ei ole vahvistettu" in {
      val opiskeluoikeus = defaultOpiskeluoikeus.copy(
        tila = AhvenanmaanPerusopetuksenOpiskeluoikeudenTila(
          List(AhvenanmaanPerusopetuksenOpiskeluoikeusjakso(date(2017, 8, 15), opiskeluoikeusLäsnä))
        ),
        suoritukset = List(
          AhvenanmaanPerusopetusExampleData.kahdeksannenLuokanSuoritus,
          AhvenanmaanPerusopetusExampleData.ysiluokanSuoritus.copy(vahvistus = None),
          AhvenanmaanPerusopetusExampleData.päättötodistuksenSuoritus
        )
      )
      setupOppijaWithOpiskeluoikeus(opiskeluoikeus) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.ahvenanmaanPerusopetuksenOppimääräIlmanYsiluokanSuoritusta())
      }
    }

    "oppimäärän suoritusta ei ole vahvistettu" in {
      val opiskeluoikeus = ilmanYsiluokkaa.copy(
        tila = AhvenanmaanPerusopetuksenOpiskeluoikeudenTila(
          List(AhvenanmaanPerusopetuksenOpiskeluoikeusjakso(date(2017, 8, 15), opiskeluoikeusLäsnä))
        ),
        suoritukset = ilmanYsiluokkaa.suoritukset.map {
          case s: AhvenanmaanPerusopetuksenOppimääränSuoritus => s.copy(vahvistus = None)
          case s => s
        }
      )
      setupOppijaWithOpiskeluoikeus(opiskeluoikeus) {
        verifyResponseStatusOk()
      }
    }

    "oppilas on kotiopetuksessa oppimäärän vahvistuspäivänä" in {
      val opiskeluoikeus = ilmanYsiluokkaa.copy(
        lisätiedot = Some(AhvenanmaanPerusopetuksenOpiskeluoikeudenLisätiedot(
          kotiopetusjaksot = Some(List(Aikajakso(date(2025, 8, 15), Some(date(2026, 6, 4)))))
        ))
      )
      setupOppijaWithOpiskeluoikeus(opiskeluoikeus) {
        verifyResponseStatusOk()
      }
    }

    "kotiopetusjakso on päättynyt ennen oppimäärän vahvistuspäivää" in {
      val opiskeluoikeus = ilmanYsiluokkaa.copy(
        lisätiedot = Some(AhvenanmaanPerusopetuksenOpiskeluoikeudenLisätiedot(
          kotiopetusjaksot = Some(List(Aikajakso(date(2025, 8, 15), Some(date(2026, 6, 3)))))
        ))
      )
      setupOppijaWithOpiskeluoikeus(opiskeluoikeus) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.ahvenanmaanPerusopetuksenOppimääräIlmanYsiluokanSuoritusta())
      }
    }

    "oppimäärän suoritustapa on erityinen tutkinto" in {
      val opiskeluoikeus = ilmanYsiluokkaa.copy(
        suoritukset = ilmanYsiluokkaa.suoritukset.map {
          case s: AhvenanmaanPerusopetuksenOppimääränSuoritus =>
            s.copy(suoritustapa = suoritustapaErityinenTutkinto)
          case s => s
        }
      )
      setupOppijaWithOpiskeluoikeus(opiskeluoikeus) {
        verifyResponseStatusOk()
      }
    }
  }

  "Ahvenanmaan perusopetuksen oppimäärä muille kuin oppivelvollisille" - {
    val aikuistenOpiskeluoikeus = AhvenanmaanPerusopetusExampleData.aikuistenOpiskeluoikeus

    "voidaan tallentaa paikallisesti" in {
      setupOppijaWithOpiskeluoikeus(aikuistenOpiskeluoikeus) {
        verifyResponseStatusOk()
      }
    }

    "opiskeluoikeudella ei saa olla vuosiluokkasuorituksia" in {
      val opiskeluoikeus = aikuistenOpiskeluoikeus.copy(
        suoritukset = List(
          AhvenanmaanPerusopetusExampleData.aikuistenPäättötodistuksenSuoritus,
          AhvenanmaanPerusopetusExampleData.kahdeksannenLuokanSuoritus
        )
      )
      setupOppijaWithOpiskeluoikeus(opiskeluoikeus) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaSuorituksia(
          "Ahvenanmaan perusopetuksen opiskeluoikeudella, jolla on muiden kuin oppivelvollisten oppimäärän suoritus, ei voi olla vuosiluokan suorituksia"
        ))
      }
    }

    // Vahvistettu oppimäärän suoritus vaatii vahvistetun 9. vuosiluokan suorituksen vain
    // oppivelvollisilta; muilla kuin oppivelvollisilla ei ole vuosiluokkasuorituksia lainkaan.
    "ei vaadi vahvistettua 9. vuosiluokan suoritusta" in {
      setupOppijaWithOpiskeluoikeus(aikuistenOpiskeluoikeus) {
        verifyResponseStatusOk()
      }
    }

    "suoritukselta vaaditaan alkamispäivä" in {
      val opiskeluoikeus = aikuistenOpiskeluoikeus.copy(
        suoritukset = List(
          AhvenanmaanPerusopetusExampleData.aikuistenPäättötodistuksenSuoritus.copy(alkamispäivä = None)
        )
      )
      setupOppijaWithOpiskeluoikeus(opiskeluoikeus) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.alkamispäiväPuuttuu(
          "Suoritukselle koulutus/201101 ei ole merkitty alkamispäivää"
        ))
      }
    }

    "alkuvaihe voidaan jättää pois" in {
      setupOppijaWithOpiskeluoikeus(aikuistenOpiskeluoikeus.copy(lisätiedot = None)) {
        verifyResponseStatusOk()
      }
    }

    "alkuvaiheella ei tarvitse olla päättymispäivää" in {
      val opiskeluoikeus = aikuistenOpiskeluoikeus.copy(
        lisätiedot = Some(AhvenanmaanPerusopetuksenOpiskeluoikeudenLisätiedot(
          alkuvaihe = Some(Aikajakso(date(2024, 8, 15), None))
        ))
      )
      setupOppijaWithOpiskeluoikeus(opiskeluoikeus) {
        verifyResponseStatusOk()
      }
    }
  }

  "Alkuvaihe" - {
    "ei ole sallittu oppivelvollisen oppimäärän suorituksen opiskeluoikeudella" in {
      val opiskeluoikeus = defaultOpiskeluoikeus.copy(
        lisätiedot = Some(AhvenanmaanPerusopetuksenOpiskeluoikeudenLisätiedot(
          alkuvaihe = Some(Aikajakso(date(2017, 8, 15), Some(date(2018, 6, 4))))
        ))
      )
      setupOppijaWithOpiskeluoikeus(opiskeluoikeus) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.ahvenanmaanAlkuvaiheVainMuilleKuinOppivelvollisille())
      }
    }
  }
}
