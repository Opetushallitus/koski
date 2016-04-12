package fi.oph.tor.documentation

import fi.oph.tor.organisaatio.MockOrganisaatiot
import fi.oph.tor.schema._

object YleissivistavakoulutusExampleData {
  implicit def int2String(int: Int) = int.toString

  def oppiaine(aine: String) = MuuOppiaine(tunniste = Koodistokoodiviite(koodistoUri = "koskioppiaineetyleissivistava", koodiarvo = aine))
  def äidinkieli(kieli: String) = AidinkieliJaKirjallisuus(kieli = Koodistokoodiviite(koodiarvo = kieli, koodistoUri = "oppiaineaidinkielijakirjallisuus"))
  def kieli(oppiaine: String, kieli: String) = VierasTaiToinenKotimainenKieli(
    tunniste = Koodistokoodiviite(koodiarvo = oppiaine, koodistoUri = "koskioppiaineetyleissivistava"),
    kieli = Koodistokoodiviite(koodiarvo = kieli, koodistoUri = "kielivalikoima"))
  def uskonto(uskonto: String) = Uskonto(uskonto = Koodistokoodiviite(koodiarvo = uskonto, koodistoUri = "oppiaineuskonto"))

  def arviointi(arvosana: String): Some[List[YleissivistävänkoulutuksenArviointi]] = {
    Some(List(YleissivistävänkoulutuksenArviointi(arvosana.toString)))
  }

  val hyväksytty = Some(List(YleissivistävänkoulutuksenArviointi("S")))
  lazy val jyväskylänNormaalikoulu: Oppilaitos = Oppilaitos(MockOrganisaatiot.jyväskylänNormaalikoulu, Some(Koodistokoodiviite("00204", None, "oppilaitosnumero", None)), Some("Jyväskylän normaalikoulu"))
}
