package fi.oph.koski.schema

import fi.oph.koski.localization.LocalizedString.unlocalized
import fi.oph.koski.localization.{Localizable, LocalizedString}
import fi.oph.scalaschema.annotation.Description

trait KoodiViite extends Localizable {
  def koodiarvo: String
  def getNimi: Option[LocalizedString]
}

object Koodistokoodiviite {
  def apply(koodiarvo: String, koodistoUri: String): Koodistokoodiviite = Koodistokoodiviite(koodiarvo, None, None, koodistoUri, None)
  def apply(koodiarvo: String, nimi: Option[LocalizedString], koodistoUri: String, koodistoVersio: Option[Int] = None): Koodistokoodiviite = Koodistokoodiviite(koodiarvo, nimi, None, koodistoUri, koodistoVersio)
}

case class Koodistokoodiviite(
  @Description("Koodin tunniste koodistossa")
  koodiarvo: String,
  @Description("Koodin selväkielinen, kielistetty nimi")
  @ReadOnly("Tiedon syötössä kuvausta ei tarvita; kuvaus haetaan Koodistopalvelusta")
  nimi: Option[LocalizedString],
  @Description("Koodin selväkielinen, kielistetty lyhennetty nimi")
  @ReadOnly("Tiedon syötössä kuvausta ei tarvita; kuvaus haetaan Koodistopalvelusta")
  lyhytNimi: Option[LocalizedString],
  @Description("Käytetyn koodiston tunniste")
  koodistoUri: String,
  @Description("Käytetyn koodiston versio. Jos versiota ei määritellä, käytetään uusinta versiota")
  koodistoVersio: Option[Int]
) extends KoodiViite {
  override def toString = koodistoUri + "/" + koodiarvo
  def description: LocalizedString = koodistoUri match {
    case "arviointiasteikkoyleissivistava" => unlocalized(koodiarvo)
    case _ => nimi.getOrElse(unlocalized(koodiarvo))
  }
  def getNimi = nimi
}

@Description("Paikallinen, koulutustoimijan oma kooditus. Käytetään kansallisen koodiston puuttuessa")
case class PaikallinenKoodi(
  @Description("Koodin yksilöivä tunniste käytetyssä koodistossa")
  koodiarvo: String,
  @Description("Koodin selväkielinen nimi")
  @Representative
  nimi: LocalizedString,
  @Description("Koodiston tunniste. Esimerkiksi Virta-järjestelmästä saatavissa arvioinneissa käytetään virta/x, missä x on arviointiasteikon tunniste. Jos koodistolla ei ole tunnistetta, voidaan kenttä jättää tyhjäksi.")
  koodistoUri: Option[String] = None
) extends KoodiViite {
  def getNimi = Some(nimi)
  def description: LocalizedString = nimi
}