package fi.oph.tor.schema

import java.time.LocalDate

import fi.oph.tor.schema.generic.annotation.Description

trait YleissivistavaOppiaine extends Koulutusmoduuli {
  @Description("Oppiaine")
  @KoodistoUri("koskioppiaineetyleissivistava")
  @OksaUri("tmpOKSAID256", "oppiaine")
  def tunniste: Koodistokoodiviite
  def pakollinen: Boolean
  def laajuus: Option[Laajuus]
  override def toString = tunniste.nimi.getOrElse("")
}

  case class Oppiaine(
    tunniste: Koodistokoodiviite,
    pakollinen: Boolean = true,
    override val laajuus: Option[Laajuus] = None
  ) extends YleissivistavaOppiaine

  case class Uskonto(
    @KoodistoKoodiarvo("KT")
    tunniste: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "KT", koodistoUri = "koskioppiaineetyleissivistava"),
    @Description("Mikä uskonto on kyseessä")
    @KoodistoUri("oppiaineuskonto")
    uskonto: Koodistokoodiviite,
    pakollinen: Boolean = true,
    override val laajuus: Option[Laajuus] = None
  ) extends YleissivistavaOppiaine {
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
  ) extends YleissivistavaOppiaine {
    override def toString = super.toString + kieli.nimi.map(", " + _).getOrElse("")
  }

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
  ) extends YleissivistavaOppiaine {
    override def toString = super.toString + kieli.nimi.map(", " + _).getOrElse("")
  }


case class YleissivistävänkoulutuksenArviointi(
  @KoodistoUri("arvosanat")
  arvosana: Koodistokoodiviite,
  päivä: Option[LocalDate],
  arvioitsijat: Option[List[Arvioitsija]] = None
) extends Arviointi

object YleissivistävänkoulutuksenArviointi {
  def apply(arvosana: String) = new YleissivistävänkoulutuksenArviointi(arvosana = Koodistokoodiviite(koodiarvo = arvosana, koodistoUri = "arvosanat"), None)
}