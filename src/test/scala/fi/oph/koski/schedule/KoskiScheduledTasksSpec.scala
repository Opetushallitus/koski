package fi.oph.koski.schedule

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.schema.NimitiedotJaOid
import org.json4s.jackson.JsonMethods.{parse => parseJson}
import org.scalatest.{FreeSpec, Matchers}

class KoskiScheduledTasksSpec extends FreeSpec with Matchers {
  val application = KoskiApplicationForTests
  "Päivittää muuttuneet oppijat oppijanumerorekisteristä" in {
    application.scheduledTasks.updateHenkilöt(Some(parseJson("""{"lastRun": "2017-01-20T14:54:31.251"}""")))
    val päivitettytPerustiedot: NimitiedotJaOid = application.perustiedotRepository.findHenkilöPerustiedot(MockOppijat.eero.oid).get
    päivitettytPerustiedot.sukunimi should equal(MockOppijat.eero.sukunimi + "_muuttunut")
  }
}
