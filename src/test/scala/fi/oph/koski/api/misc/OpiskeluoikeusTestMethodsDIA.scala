package fi.oph.koski.api.misc

import fi.oph.koski.documentation.DIAExampleData.saksalainenKoulu
import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.{ExampleData, LukioExampleData}
import fi.oph.koski.schema._

import java.time.LocalDate.{of => date}

trait OpiskeluoikeusTestMethodsDIA extends PutOpiskeluoikeusTestMethods[DIAOpiskeluoikeus]{
  def tag = implicitly[reflect.runtime.universe.TypeTag[DIAOpiskeluoikeus]]

  override def defaultOpiskeluoikeus = OpiskeluoikeusTestMethodsDIA.opiskeluoikeus
  def valmisOpiskeluoikeus = OpiskeluoikeusTestMethodsDIA.opiskeluoikeusValmis
}

object OpiskeluoikeusTestMethodsDIA {
  def tutkintoSuoritus = DIATutkinnonSuoritus(
    toimipiste = saksalainenKoulu,
    suorituskieli = englanti,
    kokonaispistemäärä = Some(800),
    osasuoritukset = None
  )

  def opiskeluoikeus = DIAOpiskeluoikeus(
    oppilaitos = Some(saksalainenKoulu),
    tila = DIAOpiskeluoikeudenTila(
      List(
        DIAOpiskeluoikeusjakso(date(2012, 9, 1), LukioExampleData.opiskeluoikeusAktiivinen, Some(ExampleData.valtionosuusRahoitteinen))
      )
    ),
    suoritukset = List(tutkintoSuoritus)
  )

  def opiskeluoikeusValmis = opiskeluoikeus.copy(
    tila = DIAOpiskeluoikeudenTila(
      List(
        DIAOpiskeluoikeusjakso(date(2012, 9, 1), LukioExampleData.opiskeluoikeusAktiivinen, Some(ExampleData.valtionosuusRahoitteinen)),
        DIAOpiskeluoikeusjakso(date(2016, 6, 4), LukioExampleData.opiskeluoikeusPäättynyt, Some(ExampleData.valtionosuusRahoitteinen))
      )
    )
  )
}
