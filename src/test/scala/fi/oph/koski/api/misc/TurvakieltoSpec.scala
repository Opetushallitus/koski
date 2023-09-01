package fi.oph.koski.api.misc

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat.eero
import fi.oph.koski.schema.TäydellisetHenkilötiedot
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class TurvakieltoSpec extends AnyFreeSpec with Matchers with KoskiHttpSpec with OpiskeluoikeusTestMethods {
  "Turvakielto" - {
    "Oppijan tiedoissa näkyy turvakieltotieto" in {
      oppija(eero.oid).henkilö.asInstanceOf[TäydellisetHenkilötiedot].turvakielto.get should equal(false)
      oppija(KoskiSpecificMockOppijat.turvakielto.oid).henkilö.asInstanceOf[TäydellisetHenkilötiedot].turvakielto.get should equal(true)
    }
  }
}
