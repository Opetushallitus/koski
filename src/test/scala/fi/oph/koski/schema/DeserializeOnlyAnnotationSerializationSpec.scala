package fi.oph.koski.schema

import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.schema.annotation.DeserializeOnly
import fi.oph.scalaschema.annotation.SyntheticProperty
import org.json4s.jackson.JsonMethods
import org.json4s.{JBool, JNothing, JString}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

case class DummyClass(
  visible: String,
  @DeserializeOnly
  hidden: Option[String]
) {
  @SyntheticProperty
  def wasSomethingThere = hidden.nonEmpty
}

class DeserializeOnlyAnnotationSerializationSpec extends AnyFreeSpec with Matchers {
  def serialize(obj: DummyClass) = JsonMethods.compact(JsonSerializer.serializeWithRoot(obj))
  private def parse(json: String) = JsonMethods.parse(json)

  "JsonSerializer" - {
    s"should omit $DeserializeOnly fields from JSON" in {
      val dummy = DummyClass("serialized", Some("not-serialized"))
      val json = serialize(dummy)

      json should include("serialized")
      json should not include "not-serialized"

      val parsed = parse(json)
      parsed \ "visible" shouldBe JString("serialized")
      parsed \ "hidden" shouldBe JNothing
    }

    s"should still include synthetic properties derived from $DeserializeOnly" - {

      "when hidden is defined" in {
        val dummy = DummyClass("I am visible", Some("I am secret"))
        val json = serialize(dummy)
        val parsed = parse(json)

        json should include("wasSomethingThere")
        parsed \ "hidden" shouldBe JNothing
        parsed \ "wasSomethingThere" shouldBe JBool(true)
      }

      "when hidden is empty" in {
        val dummy = DummyClass("I am visible", None)
        val json = serialize(dummy)
        val parsed = parse(json)

        json should include("wasSomethingThere")
        parsed \ "hidden" shouldBe JNothing
        parsed \ "wasSomethingThere" shouldBe JBool(false)
      }
    }
  }
}
