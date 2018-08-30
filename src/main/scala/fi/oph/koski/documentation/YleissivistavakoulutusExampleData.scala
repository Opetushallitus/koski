package fi.oph.koski.documentation

import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema._

object YleissivistavakoulutusExampleData {
  lazy val jyväskylänNormaalikoulu: Oppilaitos = Oppilaitos(MockOrganisaatiot.jyväskylänNormaalikoulu, Some(Koodistokoodiviite("00204", None, "oppilaitosnumero", None)), Some("Jyväskylän normaalikoulu"))
  lazy val ressunLukio: Oppilaitos = Oppilaitos(MockOrganisaatiot.ressunLukio, Some(Koodistokoodiviite("00082", None, "oppilaitosnumero", None)), Some("Ressun lukio"))
  lazy val kulosaarenAlaAste: Oppilaitos = Oppilaitos(MockOrganisaatiot.kulosaarenAlaAste, Some(Koodistokoodiviite("03016", None, "oppilaitosnumero", None)), Some("Kulosaaren ala-aste"))
}
