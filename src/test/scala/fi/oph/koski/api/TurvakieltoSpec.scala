package fi.oph.koski.api

import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.henkilo.MockOppijat.eero
import fi.oph.koski.schema.TäydellisetHenkilötiedot
import org.scalatest.{FreeSpec, Matchers}

class TurvakieltoSpec extends FreeSpec with Matchers with LocalJettyHttpSpecification with OpiskeluoikeusTestMethods {
  "Turvakielto" - {
    "Oppijan tiedoissa näkyy turvakieltotieto" in {
      oppija(eero.oid).henkilö.asInstanceOf[TäydellisetHenkilötiedot].turvakielto.get should equal(false)
      oppija(MockOppijat.turvakielto.oid).henkilö.asInstanceOf[TäydellisetHenkilötiedot].turvakielto.get should equal(true)
    }
  }
}
