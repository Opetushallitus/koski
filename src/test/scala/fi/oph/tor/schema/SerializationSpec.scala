package fi.oph.tor.schema

import fi.oph.tor.documentation.{TorOppijaExampleData, TorOppijaExamples}
import fi.oph.tor.json.Json
import org.scalatest.{FunSpec, Matchers}

class SerializationSpec extends FunSpec with Matchers {
  describe("Serialization / deserialization") {
    it("Hyväksiluku") {
      val jsonString = Json.write(TorOppijaExampleData.hyväksiluku)
      val hyväksiluku = Json.read[Hyväksiluku](jsonString)
      hyväksiluku should(equal(TorOppijaExampleData.hyväksiluku))
    }
    describe("Examples") {
      TorOppijaExamples.examples.foreach { example =>
        it(example.name) {
          val jsonString = Json.write(example.data)
          val oppija = Json.read[TorOppija](jsonString)
          oppija should(equal(example.data))
          println(example.name + " ok")
        }
      }
    }
  }
}
