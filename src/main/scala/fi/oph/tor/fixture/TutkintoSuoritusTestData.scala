package fi.oph.tor.fixture

import fi.oph.tor.model.{Arviointi, Tutkintosuoritus}

object TutkintoSuoritusTestData {
  val tutkintosuoritus1: Tutkintosuoritus = Tutkintosuoritus(None, "org1", "person1", "komo1", "kesken", Some(Arviointi(None, "1-10", 7, Some("Ihan perus ok"))), List())
}
