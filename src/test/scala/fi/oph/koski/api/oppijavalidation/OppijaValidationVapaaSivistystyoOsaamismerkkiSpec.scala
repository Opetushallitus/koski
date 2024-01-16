package fi.oph.koski.api.oppijavalidation

import com.typesafe.config.{Config, ConfigFactory}
import fi.oph.koski.{KoskiApplicationForTests, KoskiHttpSpec}
import fi.oph.koski.api.misc.PutOpiskeluoikeusTestMethods
import fi.oph.koski.documentation.AmmatillinenExampleData.{lähdePrimus}
import fi.oph.koski.documentation.ExampleData
import fi.oph.koski.documentation.VapaaSivistystyöExample._
import fi.oph.koski.eperusteetvalidation.{EPerusteetFiller, EPerusteetLops2019Validator, EPerusteisiinPerustuvaValidator}
import fi.oph.koski.http.{KoskiErrorCategory}
import fi.oph.koski.koskiuser.{AccessType, KoskiSpecificSession}
import fi.oph.koski.koskiuser.MockUsers.varsinaisSuomiPalvelukäyttäjä
import fi.oph.koski.schema._
import fi.oph.koski.util.FinnishDateFormat.finnishDateFormat
import fi.oph.koski.validation.KoskiValidator
import org.scalatest.freespec.AnyFreeSpec

import java.time.LocalDate
import java.time.LocalDate.{of => date}

class OppijaValidationVapaaSivistystyoOsaamismerkkiSpec extends AnyFreeSpec with PutOpiskeluoikeusTestMethods[VapaanSivistystyönOpiskeluoikeus] with KoskiHttpSpec {
  def tag = implicitly[reflect.runtime.universe.TypeTag[VapaanSivistystyönOpiskeluoikeus]]

  "Osaamismerkki" - {
    "Useita osaamismerkkejä samasta oppilaitoksesta" - {
      "ei voi olla samana päivänä, jos osaamismerkki on sama" in {
        val oo = defaultOpiskeluoikeus.copy(
          lähdejärjestelmänId = Some(LähdejärjestelmäId(Some("yksi"), lähdePrimus))
        )

        val oo2 = defaultOpiskeluoikeus.copy(
          lähdejärjestelmänId = Some(LähdejärjestelmäId(Some("kaksi"), lähdePrimus))
        )

        setupOppijaWithOpiskeluoikeus(oo, headers = authHeaders(varsinaisSuomiPalvelukäyttäjä) ++ jsonContent) {
          verifyResponseStatusOk()
        }

        postOpiskeluoikeus(oo2, headers = authHeaders(varsinaisSuomiPalvelukäyttäjä) ++ jsonContent) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.vapaaSivistystyö.duplikaattiOsaamismerkki())
        }
      }

      "voi olla eri päivinä, jos osaamismerkki on sama" in {
        val oo = opiskeluoikeusOsaamismerkki()
        val eriPäiväOo = opiskeluoikeusOsaamismerkki(oo.tila.opiskeluoikeusjaksot.head.alku.plusDays(1))

        val toinenOo = postAndGetOpiskeluoikeus(eriPäiväOo)

        toinenOo.oid should not equal(oo.oid)
      }

      "voi olla samana päivänä, jos osaamismerkki on eri" in {
        val oo = setupOppijaWithAndGetOpiskeluoikeus(defaultOpiskeluoikeus)

        val eriOsaamismerkkiOo =
          defaultOpiskeluoikeus.copy(
            suoritukset = List(
              suoritusOsaamismerkki(koodiarvo = "1002")
            )
          )

        val toinenOo = postAndGetOpiskeluoikeus(eriOsaamismerkkiOo)

        toinenOo.oid should not equal(oo.oid)
      }
    }

    "Opiskeluoikeuden tallentaminen" - {
      "ei voi tehdä ennen aikaisinta tallennuspäivää" in {
        mitätöiOppijanKaikkiOpiskeluoikeudet()

        val aikaisinTallennuspäivä = LocalDate.now.plusDays(2)

        implicit val session: KoskiSpecificSession = KoskiSpecificSession.systemUser
        implicit val accessType = AccessType.read
        val mockConfig = ConfigFactory.parseString(
          s"""
            validaatiot.vstOsaamismerkkiAikaisinSallittuTallennuspaiva = "${aikaisinTallennuspäivä.toString}"
          """.stripMargin)
        val config = KoskiApplicationForTests.config.withoutPath("validaatiot.vstOsaamismerkkiAikaisinSallittuTallennuspaiva").withFallback(mockConfig)
        mockKoskiValidator(config).updateFieldsAndValidateAsJson(
          Oppija(defaultHenkilö, List(defaultOpiskeluoikeus))
        ).left.get should equal (KoskiErrorCategory.badRequest.validation.vapaaSivistystyö.tallennuspäivä(s"Osaamismerkkejä voi alkaa tallentaa vasta ${finnishDateFormat.format(aikaisinTallennuspäivä)} alkaen"))
      }

      "voi tehdä ensimmäisenä tallennuspäivänä" in {
        mitätöiOppijanKaikkiOpiskeluoikeudet()

        val aikaisinTallennuspäivä = LocalDate.now

        implicit val session: KoskiSpecificSession = KoskiSpecificSession.systemUser
        implicit val accessType = AccessType.read
        val mockConfig = ConfigFactory.parseString(
          s"""
            validaatiot.vstOsaamismerkkiAikaisinSallittuTallennuspaiva = "${aikaisinTallennuspäivä.toString}"
          """.stripMargin)
        val config = KoskiApplicationForTests.config.withoutPath("validaatiot.vstOsaamismerkkiAikaisinSallittuTallennuspaiva").withFallback(mockConfig)
        mockKoskiValidator(config).updateFieldsAndValidateAsJson(
          Oppija(defaultHenkilö, List(defaultOpiskeluoikeus))
        ).isRight shouldBe(true)
      }
    }

    "Opiskeluoikeuden päättymispäivä" - {
      "ei voi olla ennen lain voimaantuloa" in {
        val oo = opiskeluoikeusOsaamismerkki(date(2023, 12, 31))

        setupOppijaWithOpiskeluoikeus(oo) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.vapaaSivistystyö.päättymispäivä(s"Osaamismerkin sisältävä opiskeluoikeus ei voi olla päättynyt ennen lain voimaantuloa 1.1.2024"))
        }
      }
    }

    "Arviointi ja vahvistus" - {
      val päättymispäivä = date(2025, 2, 2)

      val ooIlmanArviointiaJaVahvistusta = opiskeluoikeusOsaamismerkki(päättymispäivä).copy(
        suoritukset = List(suoritusOsaamismerkki().copy(
          vahvistus = None,
          arviointi = None
        ))
      )

      "Voi tallentaa käyttöliittymästä ensimmäisen version ilman arviointia ja vahvistusta" in {
        setupOppijaWithOpiskeluoikeus(ooIlmanArviointiaJaVahvistusta) {
          verifyResponseStatusOk()
        }
      }

      "Ei voi tallentaa käyttöliittymästä toista versiota ilman arviointia ja vahvistusta" in {
        val ooTallennettu = setupOppijaWithAndGetOpiskeluoikeus(ooIlmanArviointiaJaVahvistusta)

        putOpiskeluoikeus(ooTallennettu) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.vapaaSivistystyö.osaamismerkkiPäivät(s"Osaamismerkin pitää olla arvioitu, vahvistettu ja päättynyt"))
        }
      }

      "Ei voi tallentaa lähdejärjestelmä-id:llä ilman arviointia ja vahvistusta" in {
        val oo = ooIlmanArviointiaJaVahvistusta.copy(
          lähdejärjestelmänId = Some(LähdejärjestelmäId(Some("yksikaksi"), lähdePrimus))
        )

        setupOppijaWithOpiskeluoikeus(oo, headers = authHeaders(varsinaisSuomiPalvelukäyttäjä) ++ jsonContent) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.vapaaSivistystyö.osaamismerkkiPäivät(s"Osaamismerkin pitää olla arvioitu, vahvistettu ja päättynyt"))
        }
      }
    }


    "Opiskeluoikeuden arviointi" - {
      "päivä ei voi olla eri kuin päättymispäivä" in {
        val päättymispäivä = date(2025, 2, 2)
        val vahvistuspäivä = päättymispäivä
        val arviointipäivä = date(2024, 1, 1)
        val oo = opiskeluoikeusOsaamismerkki(päättymispäivä).copy(
          suoritukset = List(suoritusOsaamismerkki(arviointipäivä).copy(
            vahvistus = ExampleData.vahvistus(päivä = vahvistuspäivä)
          ))
        )

        setupOppijaWithOpiskeluoikeus(oo) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.vapaaSivistystyö.osaamismerkkiPäivät(s"Osaamismerkin pitää olla arvioitu, vahvistettu ja päättynyt samana päivänä"))
        }
      }

      "päivä ei voi olla eri kuin vahvistuspäivä" in {
        val päättymispäivä = date(2026, 3, 3)
        val vahvistuspäivä = date(2025, 2, 2)
        val arviointipäivä = date(2024, 1, 1)
        val oo = opiskeluoikeusOsaamismerkki(päättymispäivä).copy(
          suoritukset = List(suoritusOsaamismerkki(arviointipäivä).copy(
            vahvistus = ExampleData.vahvistus(päivä = vahvistuspäivä)
          ))
        )

        setupOppijaWithOpiskeluoikeus(oo) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.vapaaSivistystyö.osaamismerkkiPäivät(s"Osaamismerkin pitää olla arvioitu, vahvistettu ja päättynyt samana päivänä"))
        }
      }

      "ei voi puuttua" in {
        val oo = opiskeluoikeusOsaamismerkki().copy(
          suoritukset = List(suoritusOsaamismerkki().copy(
            arviointi = None
          ))
        )

        setupOppijaWithOpiskeluoikeus(oo) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.vahvistusIlmanArviointia(s"Suorituksella osaamismerkit/1001 on vahvistus, vaikka arviointi puuttuu"))
        }
      }
    }

    "Opiskeluoikeuden vahvistus" - {
      "päivä ei voi olla eri kuin päättymispäivä" in {
        val päättymispäivä = date(2026, 3, 3)
        val vahvistuspäivä = date(2025, 2, 2)
        val arviointipäivä = vahvistuspäivä
        val oo = opiskeluoikeusOsaamismerkki(päättymispäivä).copy(
          suoritukset = List(suoritusOsaamismerkki(arviointipäivä).copy(
            vahvistus = ExampleData.vahvistus(päivä = vahvistuspäivä)
          ))
        )

        setupOppijaWithOpiskeluoikeus(oo) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.vapaaSivistystyö.osaamismerkkiPäivät(s"Osaamismerkin pitää olla arvioitu, vahvistettu ja päättynyt samana päivänä"))
        }
      }

      "ei voi puuttua, jos opiskeluoikeus on arvioitu" in {
        val päättymispäivä = date(2026, 3, 3)
        val vahvistuspäivä = date(2025, 2, 2)
        val arviointipäivä = vahvistuspäivä
        val oo = opiskeluoikeusOsaamismerkki(päättymispäivä).copy(
          suoritukset = List(suoritusOsaamismerkki(arviointipäivä).copy(
            vahvistus = None
          ))
        )

        setupOppijaWithOpiskeluoikeus(oo) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.vapaaSivistystyö.osaamismerkkiPäivät(s"Osaamismerkin pitää olla arvioitu, vahvistettu ja päättynyt samana päivänä"))
        }
      }
    }

  }

  private def setupOppijaWithAndGetOpiskeluoikeus(oo: VapaanSivistystyönOpiskeluoikeus): VapaanSivistystyönOpiskeluoikeus =
    setupOppijaWithOpiskeluoikeus(oo) {
      verifyResponseStatusOk()
      getOpiskeluoikeus(readPutOppijaResponse.opiskeluoikeudet.head.oid)
    }.asInstanceOf[VapaanSivistystyönOpiskeluoikeus]

  private def mockKoskiValidator(config: Config) = {
    new KoskiValidator(
      KoskiApplicationForTests.organisaatioRepository,
      KoskiApplicationForTests.possu,
      KoskiApplicationForTests.henkilöRepository,
      new EPerusteisiinPerustuvaValidator(
        KoskiApplicationForTests.ePerusteet,
        KoskiApplicationForTests.tutkintoRepository,
        KoskiApplicationForTests.koodistoViitePalvelu
      ),
      new EPerusteetLops2019Validator(KoskiApplicationForTests.config, KoskiApplicationForTests.ePerusteet),
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
  }

  override def defaultOpiskeluoikeus: VapaanSivistystyönOpiskeluoikeus = opiskeluoikeusOsaamismerkki()
}
