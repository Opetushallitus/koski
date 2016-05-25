package fi.oph.koski.schema

import fi.oph.koski.documentation.{ExamplesKorkeakoulu, AmmatillinenExampleData, Examples}
import fi.oph.koski.json.Json
import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.log.Logging
import org.scalatest.{FunSpec, Matchers}

class SerializationSpec extends FunSpec with Matchers with Logging {
  val examples = Examples.examples

  describe("Serialization / deserialization") {
    it("Hyväksiluku") {
      val jsonString = Json.write(AmmatillinenExampleData.hyväksiluku)
      val hyväksiluku = Json.read[Hyväksiluku](jsonString)
      hyväksiluku should(equal(AmmatillinenExampleData.hyväksiluku))
    }

    describe("Examples") {
      examples.foreach { example =>
        it(example.name) {
          val jsonString = Json.write(example.data)
          val oppija = Json.read[Oppija](jsonString)
          oppija should(equal(example.data))
          logger.info(example.name + " ok")
        }
      }
    }

    describe("LocalizedString") {
      it("Serialized/deserializes cleanly") {
        val string: LocalizedString = LocalizedString.finnish("rölli")
        string.values.foreach{x: AnyRef => {}} // <- force lazy val to evaluate
        val jsonString = Json.write(string)
        jsonString should equal("""{"fi":"rölli"}""")
      }
    }
  }
}
