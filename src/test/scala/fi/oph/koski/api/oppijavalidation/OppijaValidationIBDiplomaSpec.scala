package fi.oph.koski.api.oppijavalidation

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.api.misc.PutOpiskeluoikeusTestMethods
import fi.oph.koski.documentation.{ExamplesIBDiploma, ExamplesInternationalSchool}
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.http.{ErrorMatcher, KoskiErrorCategory}
import fi.oph.koski.ibis.IBO
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.schema._
import org.scalatest.freespec.AnyFreeSpec

class OppijaValidationIBDiplomaSpec
  extends AnyFreeSpec
    with KoskiHttpSpec
    with PutOpiskeluoikeusTestMethods[IBDiplomaOpiskeluoikeus]
{
  override def tag = implicitly[reflect.runtime.universe.TypeTag[IBDiplomaOpiskeluoikeus]]

  // IB Diplomien oppilaitos on IBO, johon oletuskäyttäjällä kallella ei ole oikeuksia.
  override def defaultUser = MockUsers.paakayttaja

  override def defaultOpiskeluoikeus = ExamplesIBDiploma.opiskeluoikeus

  private val henkilö = KoskiSpecificMockOppijat.ibDiploma
  private val ibKoulutus = ExamplesIBDiploma.ibKoulutuksenOpiskeluoikeus

  "Example-opiskeluoikeus voidaan kirjoittaa tietokantaan, kun oppijalla on IB-koulutus samalla iboTunnisteella" in {
    setupOppijaWithOpiskeluoikeus(ibKoulutus, henkilö = henkilö) {
      verifyResponseStatusOk()
    }

    putOpiskeluoikeus(defaultOpiskeluoikeus, henkilö = henkilö) {
      verifyResponseStatusOk()
    }
  }

  "iboTunnisteen kirjainkoolla ei ole väliä" in {
    setupOppijaWithOpiskeluoikeus(ibKoulutus.copy(iboTunniste = Some(ExamplesIBDiploma.iboTunniste.toUpperCase)), henkilö = henkilö) {
      verifyResponseStatusOk()
    }

    putOpiskeluoikeus(defaultOpiskeluoikeus, henkilö = henkilö) {
      verifyResponseStatusOk()
    }
  }

  "Oppilaitos ja koulutustoimija ovat IBO" in {
    setupOppijaWithOpiskeluoikeus(ibKoulutus, henkilö = henkilö) {
      verifyResponseStatusOk()
    }

    putOpiskeluoikeus(defaultOpiskeluoikeus, henkilö = henkilö) {
      verifyResponseStatusOk()
    }
    getOpiskeluoikeudet(henkilö.oid).collect {
      case diploma: IBDiplomaOpiskeluoikeus => (diploma.oppilaitos.map(_.oid), diploma.koulutustoimija.map(_.oid))
    } should be(List((Some(IBO.oid), Some(IBO.oid))))
  }

  "Voi kirjoittaa International schoolin opiskeluoikeuden perusteella" in {
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

  "Ei voi kirjoittaa, jos IB-koulutuksen opiskeluoikeudella on eri iboTunniste" in {
    setupOppijaWithOpiskeluoikeus(ibKoulutus.copy(iboTunniste = Some("XYZ999")), henkilö = henkilö) {
      verifyResponseStatusOk()
    }

    putOpiskeluoikeus(defaultOpiskeluoikeus, henkilö = henkilö) {
      verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.ibdiploma.puuttuvaKoulutus())
    }
  }

  "Ei voi kirjoittaa, jos IB-koulutuksen opiskeluoikeudella ei ole iboTunnistetta" in {
    setupOppijaWithOpiskeluoikeus(ibKoulutus.copy(iboTunniste = None), henkilö = henkilö) {
      verifyResponseStatusOk()
    }

    putOpiskeluoikeus(defaultOpiskeluoikeus, henkilö = henkilö) {
      verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.ibdiploma.puuttuvaKoulutus())
    }
  }

  "Skeema" - {
    "totalPoints ei voi ylittää 45" in {
      setupOppijaWithOpiskeluoikeus(ibKoulutus, henkilö = henkilö) {
        verifyResponseStatusOk()
      }

      val oo = defaultOpiskeluoikeus.copy(suoritukset = List(ExamplesIBDiploma.suoritus.copy(totalPoints = Some(46))))
      putOpiskeluoikeus(oo, henkilö = henkilö) {
        verifyResponseStatus(400, ErrorMatcher.regex(KoskiErrorCategory.badRequest.validation.jsonSchema, ".*totalPoints.*".r))
      }
    }

    "tila ei voi olla läsnä" in {
      setupOppijaWithOpiskeluoikeus(ibKoulutus, henkilö = henkilö) {
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
