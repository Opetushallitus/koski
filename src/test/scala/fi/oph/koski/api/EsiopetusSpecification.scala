package fi.oph.koski.api

import fi.oph.koski.documentation.ExamplesEsiopetus.{peruskoulunEsiopetuksenTunniste, päiväkodinEsiopetuksenTunniste, suoritus}
import fi.oph.koski.documentation.{ExamplesEsiopetus, YleissivistavakoulutusExampleData}
import fi.oph.koski.schema._

import scala.reflect.runtime.universe.TypeTag

trait EsiopetusSpecification extends LocalJettyHttpSpecification with PutOpiskeluoikeusTestMethods[EsiopetuksenOpiskeluoikeus] {
  val päiväkodinEsiopetuksenOpiskeluoikeus = defaultOpiskeluoikeus.copy(
    oppilaitos = Some(YleissivistavakoulutusExampleData.montessoriPäiväkoti),
    suoritukset = List(päiväkodinEsiopetuksenSuoritus)
  )

  val peruskoulunEsiopetuksenSuoritus = suoritus(perusteenDiaarinumero = "102/011/2014", tunniste = peruskoulunEsiopetuksenTunniste, YleissivistavakoulutusExampleData.jyväskylänNormaalikoulu)
  val päiväkodinEsiopetuksenSuoritus = suoritus(perusteenDiaarinumero = "102/011/2014", tunniste = päiväkodinEsiopetuksenTunniste, YleissivistavakoulutusExampleData.montessoriPäiväkoti)

  override def tag: TypeTag[EsiopetuksenOpiskeluoikeus] = implicitly[TypeTag[EsiopetuksenOpiskeluoikeus]]
  override def defaultOpiskeluoikeus: EsiopetuksenOpiskeluoikeus = ExamplesEsiopetus.opiskeluoikeus
}
