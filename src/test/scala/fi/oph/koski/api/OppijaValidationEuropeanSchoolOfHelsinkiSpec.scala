package fi.oph.koski.api

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.documentation.{ExampleData, ExamplesEuropeanSchoolOfHelsinki}
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat.vuonna2004SyntynytPeruskouluValmis2021
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.schema._
import org.scalatest.freespec.AnyFreeSpec

import java.time.LocalDate
import java.time.LocalDate.{of => date}

class OppijaValidationEuropeanSchoolOfHelsinkiSpec
  extends AnyFreeSpec
    with KoskiHttpSpec
    with PutOpiskeluoikeusTestMethods[EuropeanSchoolOfHelsinkiOpiskeluoikeus]
{
  override def tag = implicitly[reflect.runtime.universe.TypeTag[EuropeanSchoolOfHelsinkiOpiskeluoikeus]]

  override def defaultOpiskeluoikeus = ExamplesEuropeanSchoolOfHelsinki.opiskeluoikeus

  "Example-opiskeluoikeus voidaan kirjoittaa tietokantaan" in {
    putOpiskeluoikeus(defaultOpiskeluoikeus, henkilö = vuonna2004SyntynytPeruskouluValmis2021) {
      verifyResponseStatusOk()
    }
  }

  "Opintojen rahoitus" - {

    val alkamispäivä = defaultOpiskeluoikeus.alkamispäivä.get
    val päättymispäivä = alkamispäivä.plusYears(20)

    "lasna -tilalle täydennetään opintojen rahoitus, koska vaihtoehtoja on toistaiseksi vain yksi" in {
      val oo = putAndGetOpiskeluoikeus(defaultOpiskeluoikeus.copy(tila = EuropeanSchoolOfHelsinkiOpiskeluoikeudenTila(List(EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso(alkamispäivä, ExampleData.opiskeluoikeusLäsnä, opintojenRahoitus = None)))))

      val täydennetytRahoitusmuodot = oo.tila.opiskeluoikeusjaksot.flatMap(_.opintojenRahoitus)
      täydennetytRahoitusmuodot should be(List(ExampleData.muutaKauttaRahoitettu))
    }

    "valmistunut -tilalle täydennetään opintojen rahoitus, koska vaihtoehtoja on toistaiseksi vain yksi" in {
      val tila = EuropeanSchoolOfHelsinkiOpiskeluoikeudenTila(List(
        EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso(alkamispäivä, ExampleData.opiskeluoikeusLäsnä, Some(ExampleData.muutaKauttaRahoitettu)),
        EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso(päättymispäivä, ExampleData.opiskeluoikeusValmistunut, opintojenRahoitus = None)
      ))

      val oo = putAndGetOpiskeluoikeus(defaultOpiskeluoikeus.copy(tila = tila))

      val täydennetytRahoitusmuodot = oo.tila.opiskeluoikeusjaksot.flatMap(_.opintojenRahoitus)
      täydennetytRahoitusmuodot should be(List(ExampleData.muutaKauttaRahoitettu, ExampleData.muutaKauttaRahoitettu))
    }

    "Opintojen rahoitus on kielletty muilta tiloilta" in {
      def verifyRahoitusmuotoKielletty(tila: Koodistokoodiviite) = {
        putOpiskeluoikeus(defaultOpiskeluoikeus.copy(tila = EuropeanSchoolOfHelsinkiOpiskeluoikeudenTila(List(
          EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso(alkamispäivä, ExampleData.opiskeluoikeusLäsnä, Some(ExampleData.muutaKauttaRahoitettu)),
          EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso(päättymispäivä, tila, Some(ExampleData.muutaKauttaRahoitettu))
        )))) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.tilallaEiSaaOllaRahoitusmuotoa(s"Opiskeluoikeuden tilalla ${tila.koodiarvo} ei saa olla rahoitusmuotoa"))
        }
      }

      List(
        ExampleData.opiskeluoikeusEronnut,
        ExampleData.opiskeluoikeusValiaikaisestiKeskeytynyt,
      ).foreach(verifyRahoitusmuotoKielletty)
    }
  }

  // TODO: TOR-1685 Lisää tarvittavat testit validaatioita toteutettaessa

  private def putAndGetOpiskeluoikeus(oo: EuropeanSchoolOfHelsinkiOpiskeluoikeus): EuropeanSchoolOfHelsinkiOpiskeluoikeus = putOpiskeluoikeus(oo) {
    verifyResponseStatusOk()
    getOpiskeluoikeus(readPutOppijaResponse.opiskeluoikeudet.head.oid)
  }.asInstanceOf[EuropeanSchoolOfHelsinkiOpiskeluoikeus]
}
