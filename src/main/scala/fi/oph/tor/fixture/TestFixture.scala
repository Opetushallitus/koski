package fi.oph.tor.fixture

import fi.oph.tor.TodennetunOsaamisenRekisteri

object TestFixture {
  def apply(rekisteri: TodennetunOsaamisenRekisteri): Unit = {
    rekisteri.insertTutkintosuoritus(TutkintoSuoritusTestData.tutkintosuoritus1)
  }
}
