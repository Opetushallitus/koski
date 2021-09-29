package fi.oph.koski.environment

import fi.oph.koski.{KoskiApplicationForTests, KoskiHttpSpec}
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api.actionBasedSQLInterpolation
import fi.oph.koski.db.QueryMethods
import fi.oph.koski.json.JsonSerializer
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.util.TimeZone

class TimeZoneSpec extends AnyFreeSpec with Matchers with QueryMethods with KoskiHttpSpec {
  protected val db = KoskiApplicationForTests.masterDatabase.db

  private val expectedTimeZone = "Europe/Helsinki"

  "Aikavyöhyke" - {
    "JVM on oikealla aikavyöhykkeellä" in {
      TimeZone.getDefault should be(TimeZone.getTimeZone(expectedTimeZone))
    }

    "Palvelin on oikealla aikavyöhykkeellä" in {
      get("api/status/") {
        val status = JsonSerializer.parse[Map[String, String]](body)
        status("server time") should include(expectedTimeZone)
      }
    }

    "Tietokanta on oikealla aikavyöhykkeellä" in {
      runDbSync(sql"select current_setting('TIMEZONE')".as[String])(0) should be(expectedTimeZone)
    }
  }
}
