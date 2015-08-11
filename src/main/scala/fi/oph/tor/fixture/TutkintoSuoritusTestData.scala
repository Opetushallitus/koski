package fi.oph.tor.fixture

import fi.oph.tor.model.{Kurssisuoritus, Tutkinnonosasuoritus, Arviointi, Tutkintosuoritus}

object TutkintoSuoritusTestData {
  val tutkintosuoritus1: Tutkintosuoritus = Tutkintosuoritus(None, "org1", "person1", "tutkinto-1", "kesken",
    Some(Arviointi(None, "1-10", 7, Some("Ihan perus ok"))),
    List(
      Tutkinnonosasuoritus(None, "tutkinnonosa-1.1", "kesken", Some(Arviointi(None, "1-10", 9, Some("Well done"))), List(
        Kurssisuoritus(None, "kurssi-1.1.1", "suoritettu", Some(Arviointi(None, "1-10", 9, None))),
        Kurssisuoritus(None, "kurssi-1.1.2", "kesken", None)
      )),
      Tutkinnonosasuoritus(None, "tutkinnonosa-1.2", "kesken", None, List(
        Kurssisuoritus(None, "kurssi-1.2.2", "kesken", None)
      ))
    ))
}
