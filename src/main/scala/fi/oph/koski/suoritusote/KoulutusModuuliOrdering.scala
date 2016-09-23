package fi.oph.koski.suoritusote

import java.lang.Character._

import fi.oph.koski.schema.Koulutusmoduuli

object KoulutusModuuliOrdering {
  // Käsittelee tunnisteen numeerisen suffiksin lukuna
  lazy val orderByTunniste: Ordering[Koulutusmoduuli] = Ordering.by(järjestäSuffiksinMukaan)

  private def järjestäSuffiksinMukaan(koulutusmoduuli: Koulutusmoduuli) = {
    val koodiarvo: String = koulutusmoduuli.tunniste.koodiarvo
    val numericSuffix = koodiarvo.reverse.takeWhile(isDigit).reverse
    if (numericSuffix.isEmpty) {
      (koodiarvo, None)
    } else {
      (koodiarvo.substring(0, koodiarvo.length - numericSuffix.length), Some(numericSuffix.toInt))
    }
  }
}
