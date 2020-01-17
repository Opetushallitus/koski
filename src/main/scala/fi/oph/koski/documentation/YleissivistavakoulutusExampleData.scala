package fi.oph.koski.documentation

import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.organisaatio.{MockOrganisaatioRepository, MockOrganisaatiot}
import fi.oph.koski.schema._

object YleissivistavakoulutusExampleData {
  lazy val montessoriPäiväkoti: Oppilaitos = Oppilaitos(MockOrganisaatiot.montessoriPäiväkoti12241, None, Some("Helsingin kaupunki toimipaikka 12241"))
  lazy val jyväskylänNormaalikoulu: Oppilaitos = Oppilaitos(MockOrganisaatiot.jyväskylänNormaalikoulu, Some(Koodistokoodiviite("00204", None, "oppilaitosnumero", None)), Some("Jyväskylän normaalikoulu"))
  lazy val ressunLukio: Oppilaitos = Oppilaitos(MockOrganisaatiot.ressunLukio, Some(Koodistokoodiviite("00082", None, "oppilaitosnumero", None)), Some("Ressun lukio"))
  lazy val kulosaarenAlaAste: Oppilaitos = Oppilaitos(MockOrganisaatiot.kulosaarenAlaAste, Some(Koodistokoodiviite("03016", None, "oppilaitosnumero", None)), Some("Kulosaaren ala-aste"))
  lazy val päiväkotiTouhula: OidOrganisaatio = MockOrganisaatioRepository.getOrganisaatioHierarkia(MockOrganisaatiot.päiväkotiTouhula).map(o => OidOrganisaatio(o.oid, Some(o.nimi), o.kotipaikka)).get
  lazy val helsinki: Koulutustoimija = MockOrganisaatioRepository.getOrganisaatio(MockOrganisaatiot.helsinginKaupunki).flatMap(_.toKoulutustoimija).get
  lazy val tornio: Koulutustoimija = MockOrganisaatioRepository.getOrganisaatio(MockOrganisaatiot.tornionKaupunki).flatMap(_.toKoulutustoimija).get
}
