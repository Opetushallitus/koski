package fi.oph.koski.json

import java.time.LocalDate

import fi.oph.common.koodisto
import fi.oph.common.koodisto.Koodisto
import org.json4s.JsonAST.{JInt, JObject}
import org.json4s.jackson.JsonMethods
import org.scalatest.{FreeSpec, Matchers}

class JsonDiffTest extends FreeSpec with Matchers {
  "jsonDiff" in {
    val a = JObject("a" -> JInt(1), "b" -> JInt(2))
    val b = JObject("a" -> JInt(1), "b" -> JInt(3))
    JsonMethods.compact(JsonDiff.jsonDiff(a, b)) should equal("""[{"op":"replace","path":"/b","value":3}]""")
  }
  "objectDiff" in {
    val a = koodisto.Koodisto("a", 1, Nil, "group", LocalDate.MIN, "ord", None, "LUONNOS", 0)
    val b = a.copy(koodistoUri = "b")
    JsonMethods.compact(JsonDiff.objectDiff(a, b)) should equal("""[{"op":"replace","path":"/koodistoUri","value":"b"}]""")
  }
}
