package fi.oph.tor.versioning

import fi.oph.tor.documentation.Examples
import fi.oph.tor.json.Json
import fi.oph.tor.schema.Oppija
import org.scalatest.{FreeSpec, Matchers}

/**
 * Tests that examples match saved JSON files. Run with -DupdateExamples=true to update saved JSON files from current examples.
 */
class BackwardCompatibilitySpec extends FreeSpec with Matchers {
  val updateAll = System.getProperty("updateExamples", "false").toBoolean
  Examples.examples.foreach { example =>
    val filename = "src/test/resources/backwardcompatibility/" + example.name.replaceAll(" ", "").replaceAll("ä", "a").replaceAll("ö", "o") + ".json"
    (updateAll, Json.readFileIfExists(filename)) match {
      case (false, Some(json)) =>
        println("Checking backward compatibility: " + filename)
        Json.fromJValue[Oppija](json) should equal(example.data)
      case _ =>
        println("Updating " + filename)
        new java.io.File(filename).getParentFile().mkdirs()
        Json.writeFile(filename, example.data)
    }
  }
}
