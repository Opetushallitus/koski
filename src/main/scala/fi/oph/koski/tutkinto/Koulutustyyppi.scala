package fi.oph.koski.tutkinto

import fi.oph.koski.koodisto.{MockKoodistoPalvelu, MockKoodistoViitePalvelu}
import fi.oph.koski.schema.Koodistokoodiviite

object Koulutustyyppi {
  type Koulutustyyppi = Koodistokoodiviite

  // https://testi.virkailija.opintopolku.fi/koodisto-ui/html/index.html#/koodisto/koulutustyyppi/2
  val ammatillinenPerustutkinto = apply(1)
  val lukiokoulutus = apply(2)
  val korkeakoulutus = apply(3)
  val ammatillinenPerustutkintoErityisopetuksena = apply(4)
  val ammattitutkinto = apply(11)
  val erikoisammattitutkinto = apply(12)
  val ammatillinenPerustutkintoNäyttötutkintona = apply(13)
  val aikuistenLukiokoulutus = apply(14)
  val perusopetus = apply(16)
  val aikuistenPerusopetus = apply(17)
  val valma = apply(18)
  val valmaErityisopetuksena = apply(19)

  def apply(numero: Int) = MockKoodistoViitePalvelu.validate(Koodistokoodiviite(numero.toString, "koulutustyyppi")).get
  def describe(koulutustyyppi: Koulutustyyppi) = koulutustyyppi.koodiarvo + koulutustyyppi.nimi.map(nimi => s"(${nimi.get("fi")})").getOrElse("")

  val ammatillisetKoulutustyypit = List(ammatillinenPerustutkinto, ammatillinenPerustutkintoErityisopetuksena, ammatillinenPerustutkintoNäyttötutkintona, ammattitutkinto, erikoisammattitutkinto)
  val ammatillisenPerustutkinnonTyypit = List(ammatillinenPerustutkinto, ammatillinenPerustutkintoErityisopetuksena, ammatillinenPerustutkintoNäyttötutkintona)
  val perusopetuksenKoulutustyypit = List(perusopetus, aikuistenPerusopetus)
  val lukionKoulutustyypit = List(lukiokoulutus, aikuistenLukiokoulutus)
}
