package fi.oph.koski.editor

import fi.oph.koski.documentation.ExampleData
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.schema.{LaajuusVuosiviikkotunneissa, SecondaryUpperKieliOppiaine}

// TODO: TOR-1685
case class EuropeanSchoolOfHelsinkiOppiaineet(koodistoViitePalvelu: KoodistoViitePalvelu) {

  private def koodi(koodisto: String, arvo: String) = koodistoViitePalvelu.validateRequired(koodisto, arvo)
  private def muuOppiaine(koodiarvo: String) = koodi("europeanschoolofhelsinkimuuoppiaine", koodiarvo)
  private def kieliOppiaine(koodiarvo: String) = koodi("europeanschoolofhelsinkikielioppiaine", koodiarvo)

  def eshSuoritukset(luokkaAste: String) = {
    luokkaAste match {
      case "N1" | "N2" => nurseryOppiaineet(luokkaAste)
      case "P1" | "P2" | "P3" | "P4" | "P5" => primaryOppiaineet(luokkaAste)
      case "S1" | "S2" | "S3" | "S4" | "S5" => secondaryLowerOppiaineet(luokkaAste)
      case "S6" | "S7" => secondaryUpperOppiaineet(luokkaAste)
    }
  }

  private def nurseryOppiaineet(luokkaAste: String) = List()
  )

  private def primaryOppiaineet(luokkaAste: String) = List()

  private def secondaryLowerOppiaineet(luokkaAste: String) = List()

  private def secondaryUpperOppiaineet(luokkaAste: String) = List(
    SecondaryUpperKieliOppiaine(tunniste = kieliOppiaine("L1"), laajuus = LaajuusVuosiviikkotunneissa(2), kieli = ExampleData.englanti)
  )
}
