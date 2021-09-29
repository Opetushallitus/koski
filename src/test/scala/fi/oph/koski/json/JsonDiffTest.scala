package fi.oph.koski.json

import fi.oph.koski.TestEnvironment
import fi.oph.koski.koodisto.Koodisto
import org.json4s.JsonAST.{JInt, JObject}
import org.json4s.jackson.JsonMethods
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate

class JsonDiffTest extends AnyFreeSpec with TestEnvironment with Matchers {
  "jsonDiff" in {
    val a = JObject("a" -> JInt(1), "b" -> JInt(2))
    val b = JObject("a" -> JInt(1), "b" -> JInt(3))
    JsonMethods.compact(JsonDiff.jsonDiff(a, b)) should equal("""[{"op":"replace","path":"/b","value":3}]""")
  }
  "objectDiff" in {
    val a = Koodisto("a", 1, Nil, "group", LocalDate.MIN, "ord", None, "LUONNOS", 0)
    val b = a.copy(koodistoUri = "b")
    JsonMethods.compact(JsonDiff.objectDiff(a, b)) should equal("""[{"op":"replace","path":"/koodistoUri","value":"b"}]""")
  }
}
