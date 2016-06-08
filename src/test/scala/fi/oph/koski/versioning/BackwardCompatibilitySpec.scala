package fi.oph.koski.versioning

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.documentation.Examples
import fi.oph.koski.json.Json
import fi.oph.koski.koskiuser.{AccessType, KoskiUser}
import fi.oph.koski.schema.{KoskeenTallennettavaOpiskeluoikeus, Oppija}
import org.scalatest.{FreeSpec, Matchers}

/**
 * Tests that examples match saved JSON files. Run with -DupdateExamples=true to update saved JSON files from current examples.
 */
class BackwardCompatibilitySpec extends FreeSpec with Matchers {
  val validator = KoskiApplication().validator
  implicit val user = KoskiUser.systemUser
  implicit val accessType = AccessType.read

  "backward compatibility with stored JSON examples" - {
    val updateAll = System.getProperty("updateExamples", "false").toBoolean
    Examples.examples.filter(example => example.data.opiskeluoikeudet.head.isInstanceOf[KoskeenTallennettavaOpiskeluoikeus]).foreach { example =>
      example.name in {
        val filename = "src/test/resources/backwardcompatibility/" + example.name.replaceAll(" ", "").replaceAll("ä", "a").replaceAll("ö", "o") + ".json"
        (updateAll, Json.readFileIfExists(filename)) match {
          case (false, Some(json)) =>
            println("Checking backward compatibility: " + filename)
            val oppija: Oppija = Json.fromJValue[Oppija](json)
            val afterRoundtrip = Json.toJValue(oppija)
            afterRoundtrip should equal(json)
            validator.validateAsJson(oppija) match {
              case Right(validated) => // Valid
              case Left(err) => throw new IllegalStateException(err.toString)
            }
          case _ =>
            println("Updating/creating " + filename)
            new java.io.File(filename).getParentFile().mkdirs()
            Json.writeFile(filename, example.data)
        }
      }
    }
  }
}
