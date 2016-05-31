package fi.oph.koski.api

import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.ExamplesYlioppilastutkinto
import fi.oph.koski.schema._

trait OpiskeluoikeusTestMethodsYlioppilastutkinto extends OpiskeluOikeusTestMethods[YlioppilastutkinnonOpiskeluoikeus]{
  override def defaultOpiskeluoikeus = ExamplesYlioppilastutkinto.opiskeluOikeus
}
