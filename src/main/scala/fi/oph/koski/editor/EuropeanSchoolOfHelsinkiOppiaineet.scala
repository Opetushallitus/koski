package fi.oph.koski.editor

import fi.oph.koski.documentation.ExampleData.{englanti, sloveeni}
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.schema.{Koodistokoodiviite, LaajuusVuosiviikkotunneissa, PrimaryAlaoppimisalue, PrimaryLapsiOppimisalue, PrimaryLapsiOppimisalueenSuoritus, PrimaryOppimisalueenAlaosasuoritus, SecondaryKieliOppiaine, SecondaryLowerLuokkaAste, SecondaryLowerOppiaineenSuoritus, SecondaryMuuOppiaine, SecondaryOppiaine, SecondaryUpperOppiaineenSuoritusS6, SecondaryUpperOppiaineenSuoritusS7}


// TODO: TOR-1685 Helsingin Eurooppalainen Koulu
case class EuropeanSchoolOfHelsinkiOppiaineet(koodistoViitePalvelu: KoodistoViitePalvelu) {

  private def koodi(koodisto: String, arvo: String) = koodistoViitePalvelu.validateRequired(koodisto, arvo)

  private def muuOppiaine(koodiarvo: String) = koodi("europeanschoolofhelsinkimuuoppiaine", koodiarvo)

  private def kieliOppiaine(koodiarvo: String) = koodi("europeanschoolofhelsinkikielioppiaine", koodiarvo)

  private def secondaryUpperSuoritusS7(luokkaAste: String)(k: SecondaryOppiaine) = SecondaryUpperOppiaineenSuoritusS7(
    koulutusmoduuli = k,
    suorituskieli = englanti, osasuoritukset = None
  )

  private def secondaryUpperSuoritusS6(luokkaAste: String)(k: SecondaryOppiaine) = SecondaryUpperOppiaineenSuoritusS6(
    koulutusmoduuli = k,
    suorituskieli = englanti
  )

  def eshOsaSuoritukset(luokkaAste: String) = {
    luokkaAste match {
      case "S7" => secondaryUpperOppiaineetS7(luokkaAste).map(secondaryUpperSuoritusS7(luokkaAste))
      case "S6" => secondaryUpperOppiaineetS6(luokkaAste).map(secondaryUpperSuoritusS6(luokkaAste))
      case _ => List()
    }
  }

  private def secondaryUpperOppiaineetS6(luokkaAste: String) = List(
    SecondaryKieliOppiaine(
      kieliOppiaine("L1"),
      laajuus = LaajuusVuosiviikkotunneissa(2),
      kieli = englanti
    ),
    SecondaryMuuOppiaine(
      kieliOppiaine("L1"),
      laajuus = LaajuusVuosiviikkotunneissa(2),
    )
  )

  private def secondaryUpperOppiaineetS7(luokkaAste: String) = List(
    SecondaryKieliOppiaine(
      kieliOppiaine("L1"),
      laajuus = LaajuusVuosiviikkotunneissa(2),
      kieli = englanti
    ),
    SecondaryKieliOppiaine(
      kieliOppiaine("PE"),
      laajuus = LaajuusVuosiviikkotunneissa(2),
      kieli = englanti
    ),
    SecondaryMuuOppiaine(
      muuOppiaine("MA"),
      laajuus = LaajuusVuosiviikkotunneissa(2),
    )
  )
}
