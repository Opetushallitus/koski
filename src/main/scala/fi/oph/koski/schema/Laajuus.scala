package fi.oph.koski.schema

import fi.oph.koski.documentation.ExampleData._
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
}

case class LaajuusKaikkiYksiköt(
  arvo: Double,
  yksikkö: Koodistokoodiviite
) extends Laajuus

case class LaajuusOpintoviikoissa(
  arvo: Double,
  @KoodistoKoodiarvo("1")
  yksikkö: Koodistokoodiviite = laajuusOpintoviikoissa
) extends Laajuus

case class LaajuusOpintopisteissä(
  arvo: Double,
  @KoodistoKoodiarvo("2")
  yksikkö: Koodistokoodiviite = laajuusOpintopisteissä
) extends LaajuusOpintopisteissäTaiKursseissa

case class LaajuusVuosiviikkotunneissa(
  arvo: Double,
  @KoodistoKoodiarvo("3")
  yksikkö: Koodistokoodiviite = laajuusVuosiviikkotunneissa
) extends LaajuusVuosiviikkotunneissaTaiKursseissa with LaajuusVuosiviikkotunneissaTaiTunneissa

case class LaajuusKursseissa(
  arvo: Double,
  @KoodistoKoodiarvo("4")
  yksikkö: Koodistokoodiviite = laajuusKursseissa
) extends LaajuusVuosiviikkotunneissaTaiKursseissa with LaajuusOpintopisteissäTaiKursseissa

trait LaajuusOpintopisteissäTaiKursseissa extends Laajuus

// TODO: tarvitaan aikuisten perusopetuksessa jotta voidaan siirtymäaikana käyttää useita laajuusyksiköitä, poistetaan siirtymäajan jälkeen
trait LaajuusVuosiviikkotunneissaTaiKursseissa extends Laajuus

trait LaajuusVuosiviikkotunneissaTaiTunneissa extends Laajuus

case class LaajuusTunneissa(
  arvo: Double,
  @KoodistoKoodiarvo("5")
  yksikkö: Koodistokoodiviite = laajuusTunneissa
) extends LaajuusVuosiviikkotunneissaTaiTunneissa

case class LaajuusOsaamispisteissä(
  arvo: Double,
  @KoodistoKoodiarvo("6")
  yksikkö: Koodistokoodiviite = laajuusOsaamispisteissä
) extends Laajuus

case class LaajuusViikoissa(
  arvo: Double,
  @KoodistoKoodiarvo("8")
  yksikkö: Koodistokoodiviite = laajuusViikoissa
) extends Laajuus
