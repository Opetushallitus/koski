package fi.oph.koski.schema

import fi.oph.koski.schema.annotation.{KoodistoKoodiarvo, KoodistoUri, Tooltip}
import fi.oph.scalaschema.annotation.{Description, MinValueExclusive}

@Description("Tutkinnon tai tutkinnon osan laajuus. Koostuu opintojen laajuuden arvosta ja yksiköstä")
trait Laajuus {
  @Description("Opintojen laajuuden arvo")
  @Tooltip("Opintojen laajuus.")
  @MinValueExclusive(0)
  def arvo: Double
  @Description("Opintojen laajuuden yksikkö")
  @KoodistoUri("opintojenlaajuusyksikko")
  def yksikkö: Koodistokoodiviite

  final def withArvo(uusiArvo: BigDecimal): Laajuus = {
    import mojave._
    shapeless.lens[Laajuus].field[Double]("arvo").set(this)(uusiArvo.toDouble)
  }
}

case class LaajuusKaikkiYksiköt(
  arvo: Double,
  yksikkö: Koodistokoodiviite
) extends Laajuus

case class LaajuusOpintopisteissä(
  arvo: Double,
  @KoodistoKoodiarvo("2")
  yksikkö: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "2", koodistoUri = "opintojenlaajuusyksikko")
) extends LaajuusOpintopisteissäTaiKursseissa

case class LaajuusVuosiviikkotunneissa(
  arvo: Double,
  @KoodistoKoodiarvo("3")
  yksikkö: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "3", koodistoUri = "opintojenlaajuusyksikko")
) extends LaajuusVuosiviikkotunneissaTaiKursseissa

case class LaajuusKursseissa(
  arvo: Double,
  @KoodistoKoodiarvo("4")
  yksikkö: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "4", koodistoUri = "opintojenlaajuusyksikko")
) extends LaajuusVuosiviikkotunneissaTaiKursseissa with LaajuusOpintopisteissäTaiKursseissa

trait LaajuusOpintopisteissäTaiKursseissa extends Laajuus

// TODO: tarvitaan aikuisten perusopetuksessa jotta voidaan siirtymäaikana käyttää useita laajuusyksiköitä, poistetaan siirtymäajan jälkeen
trait LaajuusVuosiviikkotunneissaTaiKursseissa extends Laajuus

case class LaajuusTunneissa(
  arvo: Double,
  @KoodistoKoodiarvo("5")
  yksikkö: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "5", koodistoUri = "opintojenlaajuusyksikko")
) extends Laajuus

case class LaajuusOsaamispisteissä(
  arvo: Double,
  @KoodistoKoodiarvo("6")
  yksikkö: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "6", koodistoUri = "opintojenlaajuusyksikko")
) extends Laajuus
