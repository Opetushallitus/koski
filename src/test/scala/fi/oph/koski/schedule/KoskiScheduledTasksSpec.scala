package fi.oph.koski.schedule

import java.lang.System.currentTimeMillis

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.schema.NimitiedotJaOid
import org.json4s.jackson.JsonMethods.{parse => parseJson}
import org.scalatest.{FreeSpec, Matchers}

class KoskiScheduledTasksSpec extends FreeSpec with Matchers {
  val application = KoskiApplicationForTests
  "Päivittää muuttuneet oppijat oppijanumerorekisteristä" in {
    application.scheduledTasks.updateHenkilöt(Some(parseJson(s"""{"lastRun": ${currentTimeMillis}}""")))
    val päivitettytPerustiedot: NimitiedotJaOid = application.perustiedotRepository.findHenkilöPerustiedot(MockOppijat.eero.oid).get
    päivitettytPerustiedot.sukunimi should equal(MockOppijat.eero.sukunimi + "_muuttunut")
  }
}
