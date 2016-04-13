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
  override def toString = tunniste.nimi.map(_.get("fi")).getOrElse("") // TODO: remove this, localization
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
    override def toString = super.toString + uskonto.nimi.map(_.get("fi")).map(", " + _).getOrElse("") // TODO: remove this, localization
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
    override def toString = super.toString + kieli.nimi.map(_.get("fi")).map(", " + _).getOrElse("") // TODO: remove this, localization
  }

case class YleissivistävänkoulutuksenArviointi(
  @KoodistoUri("arviointiasteikkoyleissivistava")
  arvosana: Koodistokoodiviite,
  päivä: Option[LocalDate],
  arvioitsijat: Option[List[Arvioitsija]] = None
) extends Arviointi

object YleissivistävänkoulutuksenArviointi {
  def apply(arvosana: String) = new YleissivistävänkoulutuksenArviointi(arvosana = Koodistokoodiviite(koodiarvo = arvosana, koodistoUri = "arviointiasteikkoyleissivistava"), None)
}