package fi.oph.tor.arvosana

import fi.oph.tor.koodisto.KoodistoViittaus

object ArvosanaTester extends App {
  println(new KoodistoArviointiasteikkoRepository("https://testi.virkailija.opintopolku.fi/koodisto-service").getArviointiasteikko(KoodistoViittaus("ammatillisenperustutkinnonarviointiasteikko", 1)))
}
