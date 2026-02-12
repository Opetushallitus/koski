package fi.oph.koski.todistus

import fi.oph.koski.organisaatio.MockOrganisaatiot.YleinenKielitutkintoOrg
import fi.oph.koski.organisaatio.Opetushallitus
import fi.oph.koski.schema._
import fi.oph.koski.todistus.yleinenkielitutkinto.{YleinenKielitutkintoSuoritusJaArvosana, YleinenKielitutkintoTodistusData}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate

class TodistusDataValidationSpec extends AnyFreeSpec with Matchers {

  val missingString = LocalizedString.missingString

  "TodistusDataValidation" - {
    "validateYleinenKielitutkintoData" - {
      "hyväksyy validin datan" in {
        val validData = createValidTodistusData()
        val result = TodistusDataValidation.validateYleinenKielitutkintoData(validData, "test-id")
        result shouldBe Right(())
      }

      "Oppijan nimi" - {
        "hylkää tyhjän nimen" in {
          val data = createValidTodistusData().copy(oppijaNimi = "")
          val result = TodistusDataValidation.validateYleinenKielitutkintoData(data, "test-id")
          result match {
            case Left(error) => error.errorString.get should include("Oppijan nimi")
            case Right(_) => fail("Odotettiin virhettä")
          }
        }

        "hylkää pelkästään whitespacea sisältävän nimen" in {
          val data = createValidTodistusData().copy(oppijaNimi = "   ")
          val result = TodistusDataValidation.validateYleinenKielitutkintoData(data, "test-id")
          result.isLeft shouldBe true
        }

        "hylkää '???' -merkkijonon (LocalizedString:n default-arvo)" in {
          val data = createValidTodistusData().copy(oppijaNimi = missingString)
          val result = TodistusDataValidation.validateYleinenKielitutkintoData(data, "test-id")
          result match {
            case Left(error) =>
              error.errorString.get should include(missingString)
              error.errorString.get should include("lokalisaatio puuttuu")
            case Right(_) => fail("Odotettiin virhettä")
          }
        }

        "hylkää lokalisaatioavaimen" in {
          val data = createValidTodistusData().copy(oppijaNimi = "todistus:kielitutkinto_yleinenkielitutkinto_tutkinnon_nimi_fi_kt")
          val result = TodistusDataValidation.validateYleinenKielitutkintoData(data, "test-id")
          result match {
            case Left(error) => error.errorString.get should include("lokalisaatioavain")
            case Right(_) => fail("Odotettiin virhettä")
          }
        }

        "hylkää liian pitkän nimen" in {
          val data = createValidTodistusData().copy(oppijaNimi = "A" * 401)
          val result = TodistusDataValidation.validateYleinenKielitutkintoData(data, "test-id")
          result match {
            case Left(error) => error.errorString.get should include("liian pitkä")
            case Right(_) => fail("Odotettiin virhettä")
          }
        }
      }

      "Oppijan syntymäaika" - {
        "hylkää tyhjän syntymäajan" in {
          val data = createValidTodistusData().copy(oppijaSyntymäaika = "")
          val result = TodistusDataValidation.validateYleinenKielitutkintoData(data, "test-id")
          result.isLeft shouldBe true
        }

        "hylkää '???' -merkkijonon" in {
          val data = createValidTodistusData().copy(oppijaSyntymäaika = missingString)
          val result = TodistusDataValidation.validateYleinenKielitutkintoData(data, "test-id")
          result.isLeft shouldBe true
        }

        "hylkää lokalisaatioavaimen" in {
          val data = createValidTodistusData().copy(oppijaSyntymäaika = "todistus:some_key")
          val result = TodistusDataValidation.validateYleinenKielitutkintoData(data, "test-id")
          result.isLeft shouldBe true
        }
      }

      "Tutkinnon nimi" - {
        "hylkää tyhjän tutkinnon nimen" in {
          val data = createValidTodistusData().copy(tutkinnonNimi = "")
          val result = TodistusDataValidation.validateYleinenKielitutkintoData(data, "test-id")
          result.isLeft shouldBe true
        }

        "hylkää '???' -merkkijonon" in {
          val data = createValidTodistusData().copy(tutkinnonNimi = missingString)
          val result = TodistusDataValidation.validateYleinenKielitutkintoData(data, "test-id")
          result.isLeft shouldBe true
        }

        "hylkää lokalisaatioavaimen" in {
          val data = createValidTodistusData().copy(tutkinnonNimi = "todistus:kielitutkinto_yleinenkielitutkinto_tutkinnon_nimi_fi_kt")
          val result = TodistusDataValidation.validateYleinenKielitutkintoData(data, "test-id")
          result.isLeft shouldBe true
        }
      }

      "Suoritukset ja arvosanat" - {
        "hylkää tyhjän listan" in {
          val data = createValidTodistusData().copy(suorituksetJaArvosanat = List.empty)
          val result = TodistusDataValidation.validateYleinenKielitutkintoData(data, "test-id")
          result match {
            case Left(error) => error.errorString.get should include("tyhjä")
            case Right(_) => fail("Odotettiin virhettä")
          }
        }

        "hylkää suorituksen jossa on '???' -arvosana" in {
          val data = createValidTodistusData().copy(
            suorituksetJaArvosanat = List(
              YleinenKielitutkintoSuoritusJaArvosana("Puhuminen", missingString)
            )
          )
          val result = TodistusDataValidation.validateYleinenKielitutkintoData(data, "test-id")
          result match {
            case Left(error) => error.errorString.get should include(missingString)
            case Right(_) => fail("Odotettiin virhettä")
          }
        }

        "hylkää suorituksen jossa suorituksen nimi on lokalisaatioavain" in {
          val data = createValidTodistusData().copy(
            suorituksetJaArvosanat = List(
              YleinenKielitutkintoSuoritusJaArvosana("todistus:some_key", "3, perustaso")
            )
          )
          val result = TodistusDataValidation.validateYleinenKielitutkintoData(data, "test-id")
          result match {
            case Left(error) => error.errorString.get should include("lokalisaatioavain")
            case Right(_) => fail("Odotettiin virhettä")
          }
        }

        "hylkää suorituksen jossa arvosana on lokalisaatioavain" in {
          val data = createValidTodistusData().copy(
            suorituksetJaArvosanat = List(
              YleinenKielitutkintoSuoritusJaArvosana("Puhuminen", "todistus:some_key")
            )
          )
          val result = TodistusDataValidation.validateYleinenKielitutkintoData(data, "test-id")
          result match {
            case Left(error) => error.errorString.get should include("lokalisaatioavain")
            case Right(_) => fail("Odotettiin virhettä")
          }
        }

        "hylkää suorituksen jossa suorituksen nimi on tyhjä" in {
          val data = createValidTodistusData().copy(
            suorituksetJaArvosanat = List(
              YleinenKielitutkintoSuoritusJaArvosana("", "3, perustaso")
            )
          )
          val result = TodistusDataValidation.validateYleinenKielitutkintoData(data, "test-id")
          result.isLeft shouldBe true
        }

        "hylkää suorituksen jossa arvosana on tyhjä" in {
          val data = createValidTodistusData().copy(
            suorituksetJaArvosanat = List(
              YleinenKielitutkintoSuoritusJaArvosana("Puhuminen", "")
            )
          )
          val result = TodistusDataValidation.validateYleinenKielitutkintoData(data, "test-id")
          result.isLeft shouldBe true
        }
      }

      "Tason arvosanarajat" - {
        "hylkää tyhjän arvosanarajat-tekstin" in {
          val data = createValidTodistusData().copy(tasonArvosanarajat = "")
          val result = TodistusDataValidation.validateYleinenKielitutkintoData(data, "test-id")
          result.isLeft shouldBe true
        }

        "hylkää '???' -merkkijonon" in {
          val data = createValidTodistusData().copy(tasonArvosanarajat = missingString)
          val result = TodistusDataValidation.validateYleinenKielitutkintoData(data, "test-id")
          result.isLeft shouldBe true
        }

        "hylkää lokalisaatioavaimen" in {
          val data = createValidTodistusData().copy(tasonArvosanarajat = "todistus:kielitutkinto_yleinenkielitutkinto_tason_arvosanarajat_kt")
          val result = TodistusDataValidation.validateYleinenKielitutkintoData(data, "test-id")
          result.isLeft shouldBe true
        }
      }

      "Järjestäjän nimi" - {
        "hylkää tyhjän nimen" in {
          val data = createValidTodistusData().copy(järjestäjäNimi = "")
          val result = TodistusDataValidation.validateYleinenKielitutkintoData(data, "test-id")
          result.isLeft shouldBe true
        }

        "hylkää '???' -merkkijonon" in {
          val data = createValidTodistusData().copy(järjestäjäNimi = missingString)
          val result = TodistusDataValidation.validateYleinenKielitutkintoData(data, "test-id")
          result.isLeft shouldBe true
        }
      }

      "Allekirjoituspäivämäärä" - {
        "hylkää tyhjän päivämäärän" in {
          val data = createValidTodistusData().copy(allekirjoitusPäivämäärä = "")
          val result = TodistusDataValidation.validateYleinenKielitutkintoData(data, "test-id")
          result.isLeft shouldBe true
        }

        "hylkää '???' -merkkijonon" in {
          val data = createValidTodistusData().copy(allekirjoitusPäivämäärä = missingString)
          val result = TodistusDataValidation.validateYleinenKielitutkintoData(data, "test-id")
          result.isLeft shouldBe true
        }
      }

      "Vahvistuksen viimeinen päivämäärä" - {
        "hylkää tyhjän päivämäärän" in {
          val data = createValidTodistusData().copy(vahvistusViimeinenPäivämäärä = "")
          val result = TodistusDataValidation.validateYleinenKielitutkintoData(data, "test-id")
          result.isLeft shouldBe true
        }

        "hylkää '???' -merkkijonon" in {
          val data = createValidTodistusData().copy(vahvistusViimeinenPäivämäärä = missingString)
          val result = TodistusDataValidation.validateYleinenKielitutkintoData(data, "test-id")
          result.isLeft shouldBe true
        }
      }
    }
  }

  private def createValidTodistusData(): YleinenKielitutkintoTodistusData = {
    // Luo minimaalinen KielitutkinnonOpiskeluoikeus joka on tarpeen
    val minimalOpiskeluoikeus = KielitutkinnonOpiskeluoikeus(
      oid = Some("1.2.246.562.15.00000000001"),
      tila = KielitutkinnonOpiskeluoikeudenTila(
        opiskeluoikeusjaksot = List(
          KielitutkinnonOpiskeluoikeudenOpiskeluoikeusjakso(
            alku = LocalDate.of(2020, 1, 1),
            tila = Koodistokoodiviite("lasna", "koskiopiskeluoikeudentila")
          )
        )
      ),
      suoritukset = List(
        YleisenKielitutkinnonSuoritus(
          koulutusmoduuli = YleinenKielitutkinto(
            tunniste = Koodistokoodiviite("kt", "ykitutkintotaso"),
            kieli = Koodistokoodiviite("FI", "kieli")
          ),
          toimipiste = OidOrganisaatio(YleinenKielitutkintoOrg.organisaatio),
          testinJärjestäjä = OidOrganisaatio("1.2.246.562.10.00000000001"),
          vahvistus = Some(Päivämäärävahvistus(
            päivä = LocalDate.of(2020, 6, 1),
            myöntäjäOrganisaatio = OidOrganisaatio("1.2.246.562.10.00000000001")
          )),
          osasuoritukset = None,
          yleisarvosana = None
        )
      )
    )

    YleinenKielitutkintoTodistusData(
      templateName = "kielitutkinto_yleinenkielitutkinto_fi",
      oppijaNimi = "Testi Testinen",
      oppijaSyntymäaika = "1.1.2000",
      tutkinnonNimi = "suomen kielen ylimmän tason",
      suorituksetJaArvosanat = List(
        YleinenKielitutkintoSuoritusJaArvosana("Puheen ymmärtäminen", "3, perustaso"),
        YleinenKielitutkintoSuoritusJaArvosana("Puhuminen", "3, perustaso"),
        YleinenKielitutkintoSuoritusJaArvosana("Tekstin ymmärtäminen", "3, perustaso"),
        YleinenKielitutkintoSuoritusJaArvosana("Kirjoittaminen", "3, perustaso")
      ),
      tasonArvosanarajat = "3-4",
      järjestäjäNimi = "Testioppilaitos",
      allekirjoitusPäivämäärä = "1. kesäkuuta vuonna 2020",
      vahvistusViimeinenPäivämäärä = "1.6.2025",
      siistittyOo = minimalOpiskeluoikeus
    )
  }
}
