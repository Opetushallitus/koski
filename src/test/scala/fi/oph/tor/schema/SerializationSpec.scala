package fi.oph.tor.schema

import fi.oph.tor.json.Json
import org.scalatest.{FreeSpec, Matchers}

class SerializationSpec extends FreeSpec with Matchers {
  "Serialization / deserialization" - {
    "Hyväksiluku" - {
      val jsonString = Json.write(TorOppijaExamples.hyväksiluku)
      val hyväksiluku = Json.read[Hyväksiluku](jsonString)
      hyväksiluku should(equal(TorOppijaExamples.hyväksiluku))
    }
    "Examples" - {
      TorOppijaExamples.examples.foreach { example =>
        val jsonString = Json.write(example.data)
        val oppija = Json.read[TorOppija](jsonString)
        oppija should(equal(example.data))
        println(example.name + " ok")
      }
    }
  }
}
