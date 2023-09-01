package fi.oph.koski.api.misc

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.documentation.ExamplesEsiopetus.{peruskoulunEsiopetuksenTunniste, päiväkodinEsiopetuksenTunniste, suoritus}
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData.oppilaitos
import fi.oph.koski.documentation.{ExamplesEsiopetus, YleissivistavakoulutusExampleData}
import fi.oph.koski.organisaatio.MockOrganisaatiot.vironniemenPäiväkoti
import fi.oph.koski.organisaatio.{MockOrganisaatioRepository, MockOrganisaatiot}
import fi.oph.koski.schema._

import scala.reflect.runtime.universe.TypeTag

trait EsiopetusSpecification extends KoskiHttpSpec with PutOpiskeluoikeusTestMethods[EsiopetuksenOpiskeluoikeus] {
  lazy val hki = MockOrganisaatioRepository.getOrganisaatioHierarkia(MockOrganisaatiot.helsinginKaupunki).flatMap(_.toKoulutustoimija)
  lazy val tornio = MockOrganisaatioRepository.getOrganisaatioHierarkia(MockOrganisaatiot.tornionKaupunki).flatMap(_.toKoulutustoimija)
  lazy val jyväskylä = MockOrganisaatioRepository.getOrganisaatioHierarkia(MockOrganisaatiot.jyväskylänYliopisto).flatMap(_.toKoulutustoimija)

  def päiväkotiEsiopetus(toimipiste: OrganisaatioWithOid, järjestämismuoto: Option[Koodistokoodiviite] = None): EsiopetuksenOpiskeluoikeus =
    defaultOpiskeluoikeus.copy(oppilaitos = None, järjestämismuoto = järjestämismuoto, suoritukset = List(suoritus(perusteenDiaarinumero = "102/011/2014", tunniste = päiväkodinEsiopetuksenTunniste, toimipiste)))

  def peruskouluEsiopetus(toimipiste: OrganisaatioWithOid, järjestämismuoto: Option[Koodistokoodiviite] = None): EsiopetuksenOpiskeluoikeus =
    defaultOpiskeluoikeus.copy(oppilaitos = None, järjestämismuoto = järjestämismuoto, suoritukset = List(suoritus(perusteenDiaarinumero = "102/011/2014", tunniste = peruskoulunEsiopetuksenTunniste, toimipiste)))

  lazy val päiväkodinEsiopetuksenOpiskeluoikeus = defaultOpiskeluoikeus.copy(
    oppilaitos = Some(oppilaitos(vironniemenPäiväkoti)),
    suoritukset = List(päiväkodinEsiopetuksenSuoritus)
  )

  lazy val peruskoulunEsiopetuksenSuoritus = suoritus(perusteenDiaarinumero = "102/011/2014", tunniste = peruskoulunEsiopetuksenTunniste, YleissivistavakoulutusExampleData.jyväskylänNormaalikoulu)
  lazy val päiväkodinEsiopetuksenSuoritus = suoritus(perusteenDiaarinumero = "102/011/2014", tunniste = päiväkodinEsiopetuksenTunniste, oppilaitos(vironniemenPäiväkoti))

  override def tag: TypeTag[EsiopetuksenOpiskeluoikeus] = implicitly[TypeTag[EsiopetuksenOpiskeluoikeus]]
  override def defaultOpiskeluoikeus: EsiopetuksenOpiskeluoikeus = ExamplesEsiopetus.opiskeluoikeus
}
