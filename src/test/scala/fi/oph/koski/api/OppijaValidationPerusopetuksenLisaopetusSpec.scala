package fi.oph.koski.api

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.documentation.ExampleData.{opiskeluoikeusEronnut, opiskeluoikeusLäsnä}
import fi.oph.koski.documentation.ExamplesPerusopetuksenLisaopetus
import fi.oph.koski.documentation.ExamplesPerusopetuksenLisaopetus.lisäopetuksenSuoritus
import fi.oph.koski.documentation.OsaAikainenErityisopetusExampleData._
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.schema._

import java.time.LocalDate
import scala.reflect.runtime.universe.TypeTag

class OppijaValidationPerusopetuksenLisäopetusSpec extends TutkinnonPerusteetTest[PerusopetuksenLisäopetuksenOpiskeluoikeus] with KoskiHttpSpec {
  def opiskeluoikeusWithPerusteenDiaarinumero(diaari: Option[String]) = defaultOpiskeluoikeus.copy(suoritukset = List(
    lisäopetuksenSuoritus.copy(koulutusmoduuli = lisäopetuksenSuoritus.koulutusmoduuli.copy(perusteenDiaarinumero = diaari))
  ))
  def eperusteistaLöytymätönValidiDiaarinumero: String = "1/011/2004"
  override def tag: TypeTag[PerusopetuksenLisäopetuksenOpiskeluoikeus] = implicitly[TypeTag[PerusopetuksenLisäopetuksenOpiskeluoikeus]]
  override def defaultOpiskeluoikeus: PerusopetuksenLisäopetuksenOpiskeluoikeus = ExamplesPerusopetuksenLisaopetus.lisäopetuksenOpiskeluoikeus
  def defaultLisäopetuksenSuoritus = ExamplesPerusopetuksenLisaopetus.lisäopetuksenSuoritus

  "Osa-aikainen erityisopetus" - {
    "Opiskeluoikeudella on erityisen tuen päätös muusta kuin osa-aikaisesta erityisopetuksesta, muttei tietoa suorituksessa -> HTTP 200" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(
        lisätiedot = perusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedotJoissaErityisenTuenPäätösIlmanOsaAikaistaErityisopetusta
      )) {
        verifyResponseStatusOk()
      }
    }

    "Opiskeluoikeudella on tehostetun tuen päätös muusta kuin osa-aikaisesta erityisopetuksesta, muttei tietoa suorituksessa -> HTTP 200" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(
        lisätiedot = perusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedotJoissaTehostetunTuenPäätösIlmanOsaAikaistaErityisopetusta
      )) {
        verifyResponseStatusOk()
      }
    }

    "Opiskeluoikeudella on erityisen tuen päätös osa-aikaisesta erityisopetuksesta ja tieto suorituksessa -> HTTP 200" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(
        lisätiedot = perusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedotJoissaOsaAikainenErityisopetusErityisenTuenPäätöksessä,
        suoritukset = List(defaultLisäopetuksenSuoritus.copy(osaAikainenErityisopetus = true))
      )) {
        verifyResponseStatusOk()
      }
    }

    "Opiskeluoikeudella on erityisen tuen päätös osa-aikaisesta erityisopetuksesta, muttei tietoa suorituksessa -> HTTP 400" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(
        lisätiedot = perusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedotJoissaOsaAikainenErityisopetusErityisenTuenPäätöksessä
      )) {
        verifyResponseStatus(400,
          KoskiErrorCategory.badRequest.validation.osaAikainenErityisopetus.kirjausPuuttuuSuorituksesta(
            "Jos osa-aikaisesta erityisopetuksesta on päätös opiskeluoikeuden lisätiedoissa, se pitää kirjata myös suoritukseen")
        )
      }
    }

    "Opiskeluoikeudella on tehostetun tuen päätös osa-aikaisesta erityisopetuksesta, muttei tietoa suorituksessa -> HTTP 400" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(
        lisätiedot = perusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedotJoissaOsaAikainenErityisopetusTehostetunTuenPäätöksessä
      )) {
        verifyResponseStatus(400,
          KoskiErrorCategory.badRequest.validation.osaAikainenErityisopetus.kirjausPuuttuuSuorituksesta(
            "Jos osa-aikaisesta erityisopetuksesta on päätös opiskeluoikeuden lisätiedoissa, se pitää kirjata myös suoritukseen")
        )
      }
    }
  }

  "Kun suorituksen tila 'vahvistettu', opiskeluoikeuden tila ei voi olla 'eronnut' tai 'katsotaan eronneeksi'" in {
    val opiskeluoikeus = defaultOpiskeluoikeus.copy(
      tila = NuortenPerusopetuksenOpiskeluoikeudenTila(List(
        NuortenPerusopetuksenOpiskeluoikeusjakso(LocalDate.of(2016, 1, 1), opiskeluoikeusLäsnä),
        NuortenPerusopetuksenOpiskeluoikeusjakso(LocalDate.of(2017, 1, 1), opiskeluoikeusEronnut)
      )))
    putOpiskeluoikeus(opiskeluoikeus) {
      verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.tilaEronnutTaiKatsotaanEronneeksiVaikkaVahvistettuPäätasonSuoritus())
    }
  }
}
