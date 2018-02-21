package fi.oph.koski.schema

import fi.oph.koski.localization.LocalizedString.unlocalized
import fi.oph.koski.localization.{Localized, LocalizedString}
import fi.oph.koski.schema.annotation.{ReadOnly, Representative}
import fi.oph.scalaschema.annotation.{Description, Discriminator, Title}

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

  override def toString = koodistoUri + "/" + koodiarvo
  def description: LocalizedString = koodistoUri match {
    case "arviointiasteikkoyleissivistava" | "arviointiasteikkoib" | "arviointiasteikkocorerequirementsib" => unlocalized(koodiarvo)
    case _ => nimi.getOrElse(unlocalized(koodiarvo))
  }
  def getNimi = nimi
}

@Description("Paikallinen, koulutustoimijan oma kooditus. Käytetään kansallisen koodiston puuttuessa")
case class PaikallinenKoodi(
  @Description("Koodin yksilöivä tunniste käytetyssä koodistossa")
  @Title("Tunniste")
  @Discriminator
  koodiarvo: String,
  @Description("Koodin selväkielinen nimi")
  @Representative
  @Discriminator
  nimi: LocalizedString,
  @Description("Koodiston tunniste. Esimerkiksi Virta-järjestelmästä saatavissa arvioinneissa käytetään virta/x, missä x on arviointiasteikon tunniste. Jos koodistolla ei ole tunnistetta, voidaan kenttä jättää tyhjäksi")
  @Title("Koodisto-URI")
  koodistoUri: Option[String] = None
) extends KoodiViite {
  override def toString = s"$koodiarvo (${nimi.get("fi")})"
  def getNimi = Some(nimi)
  def description: LocalizedString = nimi
}
