package fi.oph.koski.documentation

import fi.oph.koski.organisaatio.{MockOrganisaatioRepository, MockOrganisaatiot}
import fi.oph.koski.schema._

object YleissivistavakoulutusExampleData {
  lazy val jyväskylänNormaalikoulu: Oppilaitos = oppilaitos(MockOrganisaatiot.jyväskylänNormaalikoulu)
  lazy val ressunLukio: Oppilaitos = oppilaitos(MockOrganisaatiot.ressunLukio)
  lazy val helsinginMedialukio: Oppilaitos = oppilaitos(MockOrganisaatiot.helsinginMedialukio)
  lazy val kulosaarenAlaAste: Oppilaitos = oppilaitos(MockOrganisaatiot.kulosaarenAlaAste)
  lazy val päiväkotiTouhula: OidOrganisaatio = oidOrganisaatio(MockOrganisaatiot.päiväkotiTouhula)
  lazy val päiväkotiMajakka: OidOrganisaatio = oidOrganisaatio(MockOrganisaatiot.päiväkotiMajakka)
  lazy val päiväkotiVironniemi: OidOrganisaatio = oidOrganisaatio(MockOrganisaatiot.vironniemenPäiväkoti)
  lazy val helsinki: Koulutustoimija = MockOrganisaatioRepository.getOrganisaatio(MockOrganisaatiot.helsinginKaupunki).flatMap(_.toKoulutustoimija).get
  lazy val tornio: Koulutustoimija = MockOrganisaatioRepository.getOrganisaatio(MockOrganisaatiot.tornionKaupunki).flatMap(_.toKoulutustoimija).get

  def oidOrganisaatio(oid: String): OidOrganisaatio = MockOrganisaatioRepository.getOrganisaatioHierarkia(oid).map(o => OidOrganisaatio(o.oid, Some(o.nimi), o.kotipaikka)).get
  def oppilaitos(oid: String): Oppilaitos = MockOrganisaatioRepository.getOrganisaatioHierarkia(oid).flatMap(_.toOppilaitos).get
}
