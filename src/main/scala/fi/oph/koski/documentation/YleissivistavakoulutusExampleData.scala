package fi.oph.koski.documentation

import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema._

object YleissivistavakoulutusExampleData {
  implicit def int2String(int: Int) = int.toString

  lazy val jyväskylänNormaalikoulu: Oppilaitos = Oppilaitos(MockOrganisaatiot.jyväskylänNormaalikoulu, Some(Koodistokoodiviite("00204", None, "oppilaitosnumero", None)), Some("Jyväskylän normaalikoulu"))
  lazy val kulosaarenAlaAste: Oppilaitos = Oppilaitos(MockOrganisaatiot.kulosaarenAlaAste, Some(Koodistokoodiviite("03016", None, "oppilaitosnumero", None)), Some("Kulosaaren ala-aste"))
}
