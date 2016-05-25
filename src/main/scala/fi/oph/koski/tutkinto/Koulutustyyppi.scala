package fi.oph.koski.tutkinto

object Koulutustyyppi {
  type Koulutustyyppi = Int

  // https://testi.virkailija.opintopolku.fi/koodisto-ui/html/index.html#/koodisto/koulutustyyppi/2
  val ammatillinenPerustutkinto = 1
  val lukiokoulutus = 2
  val korkeakoulutus = 3
  val ammatillinenPerustutkintoErityisopetuksena = 4
  val ammattitutkinto = 11
  val erikoisammattitutkinto = 12
  val ammatillinenPerustutkintoNäyttötutkintona = 13
  val aikuistenLukiokoulutus = 14
  val perusopetus = 16
  val aikuistenPerusopetus = 17

  val ammatillisetKoulutustyypit = List(ammatillinenPerustutkinto, ammatillinenPerustutkintoErityisopetuksena, ammatillinenPerustutkintoNäyttötutkintona, ammattitutkinto, erikoisammattitutkinto)
  val perusopetuksenKoulutustyypit = List(perusopetus, aikuistenPerusopetus)
  val lukionKoulutustyypit = List(lukiokoulutus, aikuistenLukiokoulutus)
}
