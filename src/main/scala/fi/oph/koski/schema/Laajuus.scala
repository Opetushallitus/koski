package fi.oph.koski.schema

import fi.oph.koski.schema.LocalizedString.{finnish, english}
import fi.oph.koski.schema.annotation.{KoodistoKoodiarvo, KoodistoUri, Tooltip}
import fi.oph.scalaschema.annotation.{Description, MinValueExclusive}

@Description("Tutkinnon tai tutkinnon osan laajuus. Koostuu opintojen laajuuden arvosta ja yksiköstä")
trait Laajuus {
  @Description("Opintojen laajuuden arvo")
  @Tooltip("Opintojen laajuus.")
  @MinValueExclusive(0)
  def arvo: Float
  @Description("Opintojen laajuuden yksikkö")
  @KoodistoUri("opintojenlaajuusyksikko")
  def yksikkö: Koodistokoodiviite
}

case class LaajuusOpintopisteissä(
  arvo: Float,
  @KoodistoKoodiarvo("2")
  yksikkö: Koodistokoodiviite = Koodistokoodiviite("2", Some(finnish("opintopistettä")), "opintojenlaajuusyksikko")
) extends LaajuusOpintopisteissäTaiKursseissa

case class LaajuusVuosiviikkotunneissa(
  arvo: Float,
  @KoodistoKoodiarvo("3")
  yksikkö: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "3", nimi = Some(finnish("Vuosiviikkotuntia")), koodistoUri = "opintojenlaajuusyksikko")
) extends LaajuusVuosiviikkotunneissaTaiKursseissa

case class LaajuusKursseissa(
  arvo: Float,
  @KoodistoKoodiarvo("4")
  yksikkö: Koodistokoodiviite = Koodistokoodiviite(koodistoUri = "opintojenlaajuusyksikko", koodiarvo = "4", nimi = Some(finnish("kurssia")))
) extends LaajuusVuosiviikkotunneissaTaiKursseissa with LaajuusOpintopisteissäTaiKursseissa

trait LaajuusOpintopisteissäTaiKursseissa extends Laajuus

// TODO: tarvitaan aikuisten perusopetuksessa jotta voidaan siirtymäaikana käyttää useita laajuusyksiköitä, poistetaan siirtymäajan jälkeen
trait LaajuusVuosiviikkotunneissaTaiKursseissa extends Laajuus

case class LaajuusTunneissa(
  arvo: Float,
  @KoodistoKoodiarvo("5")
  yksikkö: Koodistokoodiviite = Koodistokoodiviite(koodistoUri = "opintojenlaajuusyksikko", koodiarvo = "5", nimi = Some(english("hours")))
) extends Laajuus

case class LaajuusOsaamispisteissä(
  arvo: Float,
  @KoodistoKoodiarvo("6")
  yksikkö: Koodistokoodiviite = Koodistokoodiviite("6", Some(finnish("Osaamispistettä")), "opintojenlaajuusyksikko")
) extends Laajuus
