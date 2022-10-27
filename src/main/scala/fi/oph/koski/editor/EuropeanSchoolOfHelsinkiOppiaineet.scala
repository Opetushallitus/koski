package fi.oph.koski.editor

import fi.oph.koski.koodisto.KoodistoViitePalvelu

// TODO: TOR-1685
case class EuropeanSchoolOfHelsinkiOppiaineet(koodistoViitePalvelu: KoodistoViitePalvelu) {

  private def koodi(koodisto: String, arvo: String) = koodistoViitePalvelu.validateRequired(koodisto, arvo)
  private def muuOppiaine(koodiarvo: String) = koodi("europeanschoolofhelsinkimuuoppiaine", koodiarvo)
  private def kieliOppiaine(koodiarvo: String) = koodi("europeanschoolofhelsinkikielioppiaine", koodiarvo)
  // TODO: fiksaa
  //private def nurserySuoritus(luokkaAste: String)(x: Any) = NurseryVuosiluokanSuoritus(koulutusmoduuli = NurseryLuokkaAste(tunniste = Koodistokoodiviite(koodiarvo = luokkaAste, koodistoUri = "nurseryluokkaaste")))
  //private def primarySuoritus(luokkaAste: String)(x: Any) = PrimaryOppimisalueenOsasuoritus(koulutusmoduuli = PrimaryLuokkaAste(tunniste = Koodistokoodiviite(koodiarvo = luokkaAste, koodistoUri = "nurseryluokkaaste")))
  //private def secondaryLowerSuoritus(luokkaAste: String)(x: Any) = NurseryVuosiluokanSuoritus(koulutusmoduuli = NurseryLuokkaAste(tunniste = Koodistokoodiviite(koodiarvo = luokkaAste, koodistoUri = "nurseryluokkaaste")))
  //private def secondaryUpperSuoritus(luokkaAste: String)(x: Any) = NurseryVuosiluokanSuoritus(koulutusmoduuli = NurseryLuokkaAste(tunniste = Koodistokoodiviite(koodiarvo = luokkaAste, koodistoUri = "nurseryluokkaaste")))

  /*
  def eshSuoritukset(luokkaAste: String) = {
    luokkaAste match {
      case "N1" | "N2" => nurseryOppiaineet(luokkaAste).map(nurserySuoritus(luokkaAste))
      case "P1" | "P2" | "P3" | "P4" | "P5" => primaryOppiaineet(luokkaAste).map(primarySuoritus(luokkaAste))
      case "S1" | "S2" | "S3" | "S4" | "S5" => secondaryLowerOppiaineet(luokkaAste).map(secondaryLowerSuoritus(luokkaAste))
      case "S6" | "S7" => secondaryUpperOppiaineet(luokkaAste).map(secondaryUpperSuoritus(luokkaAste))
    }
  }
  */

  private def nurseryOppiaineet(luokkaAste: String) = List()

  private def primaryOppiaineet(luokkaAste: String) = List()

  private def secondaryLowerOppiaineet(luokkaAste: String) = List()

  private def secondaryUpperOppiaineet(luokkaAste: String) = List(
    // SecondaryUpperKieliOppiaine(tunniste = kieliOppiaine("L1"), laajuus = LaajuusVuosiviikkotunneissa(2), kieli = ExampleData.englanti)
  )
}
