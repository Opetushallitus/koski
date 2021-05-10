package fi.oph.koski.api

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat.eero
import fi.oph.koski.schema.TäydellisetHenkilötiedot
import org.scalatest.{FreeSpec, Matchers}

class TurvakieltoSpec extends FreeSpec with Matchers with KoskiHttpSpec with OpiskeluoikeusTestMethods {
  "Turvakielto" - {
    "Oppijan tiedoissa näkyy turvakieltotieto" in {
      oppija(eero.oid).henkilö.asInstanceOf[TäydellisetHenkilötiedot].turvakielto.get should equal(false)
      oppija(KoskiSpecificMockOppijat.turvakielto.oid).henkilö.asInstanceOf[TäydellisetHenkilötiedot].turvakielto.get should equal(true)
    }
  }
}
