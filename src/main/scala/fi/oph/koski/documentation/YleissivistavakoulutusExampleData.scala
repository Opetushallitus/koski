package fi.oph.koski.documentation

import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema._
import fi.oph.koski.localization.LocalizedStringImplicits._

object YleissivistavakoulutusExampleData {
  implicit def int2String(int: Int) = int.toString

  def arviointi(arvosana: String): Some[List[YleissivistävänkoulutuksenArviointi]] = {
    Some(List(YleissivistävänkoulutuksenArviointi(arvosana)))
  }

  val hyväksytty = Some(List(YleissivistävänkoulutuksenArviointi("S")))
  lazy val jyväskylänNormaalikoulu: Oppilaitos = Oppilaitos(MockOrganisaatiot.jyväskylänNormaalikoulu, Some(Koodistokoodiviite("00204", None, "oppilaitosnumero", None)), Some("Jyväskylän normaalikoulu"))
}
