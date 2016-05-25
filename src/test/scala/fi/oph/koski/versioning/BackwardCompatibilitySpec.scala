package fi.oph.koski.versioning

import fi.oph.koski.documentation.Examples
import fi.oph.koski.json.Json
import fi.oph.koski.schema.Oppija
import org.scalatest.{FreeSpec, Matchers}

/**
 * Tests that examples match saved JSON files. Run with -DupdateExamples=true to update saved JSON files from current examples.
 */
class BackwardCompatibilitySpec extends FreeSpec with Matchers {
  "backward compatibility with stored JSON examples" in {
    val updateAll = System.getProperty("updateExamples", "false").toBoolean
    Examples.examples.foreach { example =>
      val filename = "src/test/resources/backwardcompatibility/" + example.name.replaceAll(" ", "").replaceAll("ä", "a").replaceAll("ö", "o") + ".json"
      (updateAll, Json.readFileIfExists(filename)) match {
        case (false, Some(json)) =>
          println("Checking backward compatibility: " + filename)
          val afterRoundtrip = Json.toJValue(Json.fromJValue[Oppija](json))
          afterRoundtrip should equal(json)
        case _ =>
          println("Updating/creating " + filename)
          new java.io.File(filename).getParentFile().mkdirs()
          Json.writeFile(filename, example.data)
      }
    }
  }
}
