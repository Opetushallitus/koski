package fi.oph.koski.api.oppijavalidation

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.api.misc.PutOpiskeluoikeusTestMethods
import fi.oph.koski.documentation.{ExamplesIB, ExamplesIBDiploma, ExamplesInternationalSchool}
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.http.{ErrorMatcher, KoskiErrorCategory}
import fi.oph.koski.schema._
import org.scalatest.freespec.AnyFreeSpec

class OppijaValidationIBDiplomaSpec
  extends AnyFreeSpec
    with KoskiHttpSpec
    with PutOpiskeluoikeusTestMethods[IBDiplomaOpiskeluoikeus]
{
  override def tag = implicitly[reflect.runtime.universe.TypeTag[IBDiplomaOpiskeluoikeus]]

  override def defaultOpiskeluoikeus = ExamplesIBDiploma.opiskeluoikeus

  private val henkilö = KoskiSpecificMockOppijat.ibDiploma

  "Example-opiskeluoikeus voidaan kirjoittaa tietokantaan, kun oppijalla on IB-koulutus samalla iboTunnisteella" in {
    setupOppijaWithOpiskeluoikeus(ExamplesIB.opiskeluoikeus, henkilö = henkilö) {
      verifyResponseStatusOk()
    }

    putOpiskeluoikeus(defaultOpiskeluoikeus, henkilö = henkilö) {
      verifyResponseStatusOk()
    }
  }

  "Voidaan kirjoittaa International schoolin opiskeluoikeuden perusteella" in {
    setupOppijaWithOpiskeluoikeus(ExamplesInternationalSchool.opiskeluoikeus.copy(iboTunniste = Some(ExamplesIBDiploma.iboTunniste)), henkilö = henkilö) {
      verifyResponseStatusOk()
    }

    putOpiskeluoikeus(defaultOpiskeluoikeus, henkilö = henkilö) {
      verifyResponseStatusOk()
    }
  }

  "Ei voi kirjoittaa, jos oppijalla ei ole IB-koulutuksen eikä International schoolin opiskeluoikeutta" in {
    setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus, henkilö = KoskiSpecificMockOppijat.tyhjä) {
      verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.ibdiploma.puuttuvaKoulutus())
    }
  }

  "Ei voi kirjoittaa, jos koulutuksen opiskeluoikeudella on eri iboTunniste" in {
    setupOppijaWithOpiskeluoikeus(ExamplesIB.opiskeluoikeus.copy(iboTunniste = Some("XYZ999")), henkilö = henkilö) {
      verifyResponseStatusOk()
    }

    putOpiskeluoikeus(defaultOpiskeluoikeus, henkilö = henkilö) {
      verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.ibdiploma.puuttuvaKoulutus())
    }
  }

  "Ei voi kirjoittaa, jos koulutuksen opiskeluoikeudella ei ole iboTunnistetta" in {
    setupOppijaWithOpiskeluoikeus(ExamplesIB.opiskeluoikeus.copy(iboTunniste = None), henkilö = henkilö) {
      verifyResponseStatusOk()
    }

    putOpiskeluoikeus(defaultOpiskeluoikeus, henkilö = henkilö) {
      verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.ibdiploma.puuttuvaKoulutus())
    }
  }

  "Skeema" - {
    "kokonaispistemäärä ei voi ylittää 45" in {
      setupOppijaWithOpiskeluoikeus(ExamplesIB.opiskeluoikeus, henkilö = henkilö) {
        verifyResponseStatusOk()
      }

      val oo = defaultOpiskeluoikeus.copy(suoritukset = List(ExamplesIBDiploma.suoritus.copy(kokonaispistemäärä = Some(46))))
      putOpiskeluoikeus(oo, henkilö = henkilö) {
        verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*kokonaispistemäärä.*".r))
      }
    }

    "tila ei voi olla läsnä" in {
      setupOppijaWithOpiskeluoikeus(ExamplesIB.opiskeluoikeus, henkilö = henkilö) {
        verifyResponseStatusOk()
      }

      val oo = defaultOpiskeluoikeus.copy(tila = IBDiplomaOpiskeluoikeudenTila(List(
        IBDiplomaOpiskeluoikeusjakso(ExamplesIBDiploma.vahvistuspäivä, Koodistokoodiviite("lasna", "koskiopiskeluoikeudentila"))
      )))
      putOpiskeluoikeus(oo, henkilö = henkilö) {
        verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*lasna.*".r))
      }
    }
  }
}
