package fi.oph.koski.api

import fi.oph.koski.documentation.{ExamplesEsiopetus, YleissivistavakoulutusExampleData}
import fi.oph.koski.documentation.ExamplesEsiopetus.{peruskoulunEsiopetuksenTunniste, päiväkodinEsiopetuksenTunniste, suoritus}
import fi.oph.koski.schema._

class OppijaValidationEsiopetusSpec extends TutkinnonPerusteetTest[EsiopetuksenOpiskeluoikeus] with LocalJettyHttpSpecification with PutOpiskeluoikeusTestMethods[EsiopetuksenOpiskeluoikeus] {
  "Peruskoulun esiopetus -> HTTP 200" in {
    putOpiskeluoikeus(defaultOpiskeluoikeus) {
      verifyResponseStatusOk()
    }
  }

  "Päiväkodin esiopetus -> HTTP 200" in {
    val opiskeluoikeus = defaultOpiskeluoikeus.copy(
      oppilaitos = Some(YleissivistavakoulutusExampleData.montessoriPäiväkoti),
      suoritukset = List(päiväkodinEsiopetuksenSuoritus)
    )
    putOpiskeluoikeus(opiskeluoikeus) {
      verifyResponseStatusOk()
    }
  }

  val peruskoulunEsiopetuksenSuoritus = suoritus(perusteenDiaarinumero = "102/011/2014", tunniste = peruskoulunEsiopetuksenTunniste, YleissivistavakoulutusExampleData.jyväskylänNormaalikoulu)
  val päiväkodinEsiopetuksenSuoritus = suoritus(perusteenDiaarinumero = "102/011/2014", tunniste = päiväkodinEsiopetuksenTunniste, YleissivistavakoulutusExampleData.montessoriPäiväkoti)

  override def tag = implicitly[reflect.runtime.universe.TypeTag[EsiopetuksenOpiskeluoikeus]]
  override def defaultOpiskeluoikeus: EsiopetuksenOpiskeluoikeus = ExamplesEsiopetus.opiskeluoikeus
  override def eperusteistaLöytymätönValidiDiaarinumero: String = "1/011/2004"
  override def opiskeluoikeusWithPerusteenDiaarinumero(diaari: Option[String]) = defaultOpiskeluoikeus.copy(suoritukset = List(
    peruskoulunEsiopetuksenSuoritus.copy(koulutusmoduuli = peruskoulunEsiopetuksenSuoritus.koulutusmoduuli.copy(perusteenDiaarinumero = diaari))
  ))
}
