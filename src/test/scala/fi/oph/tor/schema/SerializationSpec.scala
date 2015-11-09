package fi.oph.tor.schema

import fi.oph.tor.json.Json
import org.scalatest.{FreeSpec, Matchers}

class SerializationSpec extends FreeSpec with Matchers {
  "Serialization / deserialization" - {
    TorOppijaExamples.examples.foreach { example =>
      val jsonString = Json.write(example.oppija)
      val oppija = Json.read[TorOppija](jsonString)
      oppija should(equal(example.oppija))
    }
  }
}
