package fi.oph.koski.schema

import fi.oph.koski.schema.LocalizedString.unlocalized
import fi.oph.koski.schema.annotation.{ReadOnly, Representative}
import fi.oph.scalaschema.annotation.{Description, Discriminator, SyntheticProperty, Title}

trait KoodiViite extends Localized {
  def koodiarvo: String
  def getNimi: Option[LocalizedString]
}

object Koodistokoodiviite {
  def apply(koodiarvo: String, koodistoUri: String): Koodistokoodiviite = Koodistokoodiviite(koodiarvo, None, None, koodistoUri, None)
  def apply(koodiarvo: String, nimi: Option[LocalizedString], koodistoUri: String, koodistoVersio: Option[Int] = None): Koodistokoodiviite = Koodistokoodiviite(koodiarvo, nimi, None, koodistoUri, koodistoVersio)
}

case class Koodistokoodiviite(
  @Description("Koodin tunniste koodistossa")
  @Discriminator
  koodiarvo: String,
  @Description("Koodin selväkielinen, kielistetty nimi")
  @ReadOnly("Tiedon syötössä kuvausta ei tarvita; kuvaus haetaan Koodistopalvelusta")
  nimi: Option[LocalizedString],
  @Description("Koodin selväkielinen, kielistetty lyhennetty nimi")
  @ReadOnly("Tiedon syötössä kuvausta ei tarvita; kuvaus haetaan Koodistopalvelusta")
  lyhytNimi: Option[LocalizedString],
  @Description("Käytetyn koodiston tunniste")
  @Discriminator
  @Title("Koodisto-URI")
  koodistoUri: String,
  @Description("Käytetyn koodiston versio. Jos versiota ei määritellä, käytetään uusinta versiota")
  @Title("Koodistoversio")
  koodistoVersio: Option[Int]
) extends KoodiViite with Equals {

  override def canEqual(that: Any): Boolean = that.isInstanceOf[Koodistokoodiviite]
  override def equals(that: Any): Boolean = that match {
    case that: Koodistokoodiviite =>
      that.canEqual(this) &&
        (this.koodiarvo == that.koodiarvo) &&
        (this.koodistoUri == that.koodistoUri)
    case _ => false
  }
  override def hashCode(): Int = this.koodiarvo.hashCode

  override def toString: String = koodistoUri + "/" + koodiarvo
  def description: LocalizedString = koodistoUri match {
    case "arviointiasteikkoyleissivistava" | "arviointiasteikkoib" | "arviointiasteikkocorerequirementsib" => unlocalized(koodiarvo)
    case _ => nimi.getOrElse(unlocalized(koodiarvo))
  }
  override def getNimi: Option[LocalizedString] = nimi
}

trait PaikallinenKoodiviite extends KoodiViite {
  @Representative
  @Discriminator
  def nimi: LocalizedString
  def description: LocalizedString = nimi
  override def getNimi = Some(nimi)
  override def toString = s"$koodiarvo (${nimi.get("fi")})"
}

trait SynteettinenKoodiviite extends KoodiViite with Equals {
  @Description("Koodin tunniste")
  @Discriminator
  def koodiarvo: String
  @SyntheticProperty
  @Description("Koodin selväkielinen, kielistetty nimi")
  @ReadOnly("Tiedon syötössä kuvausta ei tarvita; täydennetään automaattisesti tähän")
  def nimi: Option[LocalizedString]
  @SyntheticProperty
  @Description("Koodin selväkielinen, kielistetty lyhennetty nimi")
  @ReadOnly("Tiedon syötössä kuvausta ei tarvita; täydennetään automaattisesti tähän")
  def lyhytNimi: Option[LocalizedString]
  @Description("Käytetyn koodiston tunniste.")
  @Discriminator
  @Title("Koodisto-URI")
  def koodistoUri: Option[String]

  override def canEqual(that: Any): Boolean = that.isInstanceOf[SynteettinenKoodiviite]
  override def equals(that: Any): Boolean = that match {
    case that: SynteettinenKoodiviite =>
      that.canEqual(this) &&
        (this.koodiarvo == that.koodiarvo) &&
        (this.koodistoUri == that.koodistoUri)
    case _ => false
  }
  override def hashCode(): Int = this.koodiarvo.hashCode

  override def toString: String = koodistoUri + "/" + koodiarvo
  def description: LocalizedString = nimi.getOrElse(unlocalized(koodiarvo))
  override def getNimi: Option[LocalizedString] = nimi
}

@Description("Paikallinen, koulutustoimijan oma kooditus. Käytetään kansallisen koodiston puuttuessa")
case class PaikallinenKoodi(
  @Description("Koodin yksilöivä tunniste käytetyssä koodistossa")
  @Title("Tunniste")
  @Discriminator
  koodiarvo: String,
  @Description("Koodin selväkielinen nimi")
  nimi: LocalizedString,
  @Description("Koodiston tunniste. Esimerkiksi Virta-järjestelmästä saatavissa arvioinneissa käytetään virta/x, missä x on arviointiasteikon tunniste. Jos koodistolla ei ole tunnistetta, voidaan kenttä jättää tyhjäksi")
  @Title("Koodisto-URI")
  koodistoUri: Option[String] = None
) extends PaikallinenKoodiviite
