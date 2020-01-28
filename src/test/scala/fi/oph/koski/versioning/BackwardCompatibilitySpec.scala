package fi.oph.koski.versioning

import java.io.File
import java.time.LocalDate

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.documentation.Examples
import fi.oph.koski.json.{JsonFiles, JsonSerializer}
import fi.oph.koski.koskiuser.{AccessType, KoskiSession}
import fi.oph.koski.schema.KoskiSchema.deserializationContext
import fi.oph.koski.schema.{KoskeenTallennettavaOpiskeluoikeus, Oppija}
import fi.oph.scalaschema.SchemaValidatingExtractor
import org.json4s.JValue
import org.json4s.JsonAST.{JArray, JBool, JObject}
import org.json4s.jackson.JsonMethods
import org.scalatest.{FreeSpec, Matchers}

/**
 * Tests that examples match saved JSON files. Run with -DupdateExamples=true to update saved JSON files from current examples.
 */
class BackwardCompatibilitySpec extends FreeSpec with Matchers {
  lazy val koskiValidator = KoskiApplicationForTests.validator
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
            JsonFiles.writeFile(fullName(currentFilename), example.data)
          case files =>
            files.foreach { filename =>
              filename in {

                var skipEqualityCheck = false
                var skipKoskiValidator = false

                val json: JValue = JsonFiles.readFile(fullName(filename)).removeField {
                  case ("ignoreJsonEquality", JBool(true)) =>
                    skipEqualityCheck = true
                    true
                  case ("ignoreKoskiValidator", JBool(true)) =>
                    skipKoskiValidator = true
                    true
                  case _ => false
                }

                SchemaValidatingExtractor.extract[Oppija](json) match {
                  case Right(oppija) =>
                    val afterRoundtrip: JValue = JsonSerializer.serializeWithRoot(oppija)

                    if (!skipKoskiValidator) {
                      koskiValidator.validateAsJson(oppija) match {
                        case Right(_) => //ok
                        case Left(err) => throw new IllegalStateException(err.toString)
                      }
                    }

                    if (!skipEqualityCheck) {
                      JsonMethods.compact(mangle(afterRoundtrip)) should equal(JsonMethods.compact(mangle(json)))
                    }
                  case Left(errors) =>
                    fail("Backward compatibility problem: " + errors)
                }
              }
            }
            val latest = files.last
            if (JsonSerializer.serializeWithRoot(example.data) != JsonFiles.readFile(fullName(latest))) {
              println(s"Example data differs for ${example.name} at ${latest}. Creating new version")
              JsonFiles.writeFile(fullName(currentFilename), example.data)
            }
        }
      }
    }
  }

  private def mangle(json: JValue): JValue = json match {
    case JObject(fields) => JObject(fields
      .filter { case (key, value) => key != "tila" && value.isInstanceOf[JObject]} // Ei vertailla suorituksen tiloja
      .map { case (key, value) => (key, mangle(value)) }
      .sortBy(_._1)
    )
    case JArray(elems) => JArray(elems.map(mangle))
    case _ => json
  }
}
