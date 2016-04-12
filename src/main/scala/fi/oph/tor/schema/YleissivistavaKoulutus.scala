package fi.oph.tor.schema

import java.time.LocalDate

import fi.oph.tor.schema.generic.annotation.Description

trait YleissivistavaOppiaine extends KoodistostaLöytyväKoulutusmoduuli {
  @Description("Oppiaine")
  @KoodistoUri("koskioppiaineetyleissivistava")
  @OksaUri("tmpOKSAID256", "oppiaine")
  def tunniste: Koodistokoodiviite
  def pakollinen: Boolean
  def laajuus: Option[Laajuus]
  override def toString = tunniste.nimi.getOrElse("")
}

trait LukionOppiaine extends YleissivistavaOppiaine

trait PeruskoulunOppiaine extends YleissivistavaOppiaine

  case class MuuOppiaine(
    tunniste: Koodistokoodiviite,
    pakollinen: Boolean = true,
    override val laajuus: Option[Laajuus] = None
  ) extends PeruskoulunOppiaine with LukionOppiaine

  case class Uskonto(
    @KoodistoKoodiarvo("KT")
    tunniste: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "KT", koodistoUri = "koskioppiaineetyleissivistava"),
    @Description("Mikä uskonto on kyseessä")
    @KoodistoUri("oppiaineuskonto")
    uskonto: Koodistokoodiviite,
    pakollinen: Boolean = true,
    override val laajuus: Option[Laajuus] = None
  ) extends PeruskoulunOppiaine with LukionOppiaine {
    override def toString = super.toString + uskonto.nimi.map(", " + _).getOrElse("")
  }

  case class AidinkieliJaKirjallisuus(
    @KoodistoKoodiarvo("AI")
    tunniste: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "AI", koodistoUri = "koskioppiaineetyleissivistava"),
    @Description("Mikä kieli on kyseessä")
    @KoodistoUri("oppiaineaidinkielijakirjallisuus")
    kieli: Koodistokoodiviite,
    pakollinen: Boolean = true,
    override val laajuus: Option[Laajuus] = None
  ) extends PeruskoulunOppiaine with LukionOppiaine

  case class VierasTaiToinenKotimainenKieli(
    @KoodistoKoodiarvo("A1")
    @KoodistoKoodiarvo("A2")
    @KoodistoKoodiarvo("B1")
    @KoodistoKoodiarvo("B2")
    @KoodistoKoodiarvo("B3")
    tunniste: Koodistokoodiviite,
    @Description("Mikä kieli on kyseessä")
    @KoodistoUri("kielivalikoima")
    kieli: Koodistokoodiviite,
    pakollinen: Boolean = true,
    override val laajuus: Option[Laajuus] = None
  ) extends PeruskoulunOppiaine with LukionOppiaine {
    override def toString = super.toString + kieli.nimi.map(", " + _).getOrElse("")
  }

case class YleissivistävänkoulutuksenArviointi(
  @KoodistoUri("arvosanat")
  arvosana: Koodistokoodiviite,
  päivä: Option[LocalDate],
  arvioitsijat: Option[List[Arvioitsija]] = None
) extends Arviointi {
  def arvosanaNumeroin = {
    try { Some(arvosana.koodiarvo.toInt) } catch {
      case e: NumberFormatException => None
    }
  }
  def arvosanaKirjaimin(kieli: String) = arvosanaNumeroin match {
    case Some(num) if num == 4 => "hylätty" // TODO: localize
    case Some(num) if num == 5 => "välttävä"
    case Some(num) if num == 6 => "kohtalainen"
    case Some(num) if num == 7 => "tyydyttävä"
    case Some(num) if num == 8 => "hyvä"
    case Some(num) if num == 9 => "kiitettävä"
    case Some(num) if num == 10 => "erinomainen"
    case _ => arvosana.nimi.getOrElse(arvosana.koodiarvo)
  }
}

object YleissivistävänkoulutuksenArviointi {
  def apply(arvosana: String) = new YleissivistävänkoulutuksenArviointi(arvosana = Koodistokoodiviite(koodiarvo = arvosana, koodistoUri = "arvosanat"), None)
}