package fi.oph.koski.versioning

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.documentation.Examples
import fi.oph.koski.json.Json
import fi.oph.koski.koskiuser.{AccessType, KoskiSession}
import fi.oph.koski.schema.{KoskeenTallennettavaOpiskeluoikeus, Oppija}
import org.scalatest.{FreeSpec, Matchers}
import java.io.File
import java.time.LocalDate

import fi.oph.koski.schema.KoskiSchema.deserializationContext
import fi.oph.scalaschema.SchemaValidatingExtractor
import org.json4s.JsonAST.JBool

/**
 * Tests that examples match saved JSON files. Run with -DupdateExamples=true to update saved JSON files from current examples.
 */
class BackwardCompatibilitySpec extends FreeSpec with Matchers {
  lazy val validator = KoskiApplicationForTests.validator
  implicit val user = KoskiSession.systemUser
  implicit val accessType = AccessType.read

  "backward compatibility with stored JSON examples" - {
    Examples.examples.filter(example => example.data.opiskeluoikeudet.head.isInstanceOf[KoskeenTallennettavaOpiskeluoikeus]).foreach { example =>
      example.name - {
        val basename = example.name.replaceAll(" ", "").replaceAll("ä", "a").replaceAll("ö", "o")
        val currentFilename = basename + "_" + LocalDate.now.toString + ".json"
        val dirName: String = "src/test/resources/backwardcompatibility"
        def fullName(fileName: String) = dirName + "/" + fileName
        val existingFiles = new File(dirName).list().filter(fn => fn == basename + ".json" || fn.startsWith(basename + "_")).toList.sorted
        existingFiles match {
          case Nil =>
            println("Creating " + currentFilename)
            new File(fullName(currentFilename)).getParentFile().mkdirs()
            Json.writeFile(fullName(currentFilename), example.data)
          case files =>
            files.foreach { filename =>
              filename in {
                var skipEqualityCheck = false
                val json = Json.readFile(fullName(filename)).removeField {
                  case ("ignoreJsonEquality", JBool(true)) =>
                    skipEqualityCheck = true
                    true
                  case _ => false
                }
                SchemaValidatingExtractor.extract[Oppija](json) match {
                  case Right(oppija) =>
                    val afterRoundtrip = Json.toJValue(oppija)
                    validator.validateAsJson(oppija) match {
                      case Right(validated) =>
                        // Valid, now check for JSON equality after roundtrip (not strictly necessary, but it's good to know if this breaks)
                        if (!skipEqualityCheck) {
                          Json.write(afterRoundtrip) should equal(Json.write(json))
                        }
                      case Left(err) =>
                        throw new IllegalStateException(err.toString)
                    }
                  case Left(errors) =>
                    fail("Backward compatibility problem: " + errors)
                }
              }
            }
            val latest = files.last
            if (Json.toJValue(example.data) != Json.readFile(fullName(latest))) {
              println(s"Example data differs for ${example.name} at ${latest}. Creating new version")
              Json.writeFile(fullName(currentFilename), example.data)
            }
        }
      }
    }
  }
}
