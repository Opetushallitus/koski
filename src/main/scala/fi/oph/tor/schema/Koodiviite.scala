package fi.oph.tor.schema

import fi.oph.tor.localization.LocalizedString.unlocalized
import fi.oph.tor.localization.{Localizable, LocalizedString}
import fi.oph.scalaschema.annotation.{Description, ReadOnly}

trait KoodiViite {
  def koodiarvo: String
  def koodistoUri: String
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
) extends KoodiViite with Localizable {
  override def toString = koodistoUri + "/" + koodiarvo
  def description: LocalizedString = nimi.getOrElse(unlocalized(koodiarvo))
}

@Description("Paikallinen, koulutustoimijan oma kooditus koulutukselle. Käytetään kansallisen koodiston puuttuessa")
case class Paikallinenkoodi(
  @Description("Koodin tunniste koodistossa")
  koodiarvo: String,
  @Description("Koodin selväkielinen nimi")
  nimi: LocalizedString,
  @Description("Koodiston tunniste")
  koodistoUri: String
) extends KoodiViite