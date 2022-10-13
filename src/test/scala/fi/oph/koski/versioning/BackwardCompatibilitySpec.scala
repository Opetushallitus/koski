package fi.oph.koski.versioning

import java.io.File
import java.time.LocalDate
import fi.oph.koski.{KoskiApplicationForTests, TestEnvironment}
import fi.oph.koski.documentation.Example
import fi.oph.koski.documentation.Examples.{oppijaExamples}
import fi.oph.koski.json.{JsonFiles, JsonSerializer}
import fi.oph.koski.koskiuser.{AccessType, KoskiSpecificSession}
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.KoskiSchema.strictDeserialization
import fi.oph.koski.schema.{KoskeenTallennettavaOpiskeluoikeus, Oppija}
import fi.oph.koski.util.EnvVariables
import fi.oph.scalaschema.{ExtractionContext, SchemaValidatingExtractor}
import org.json4s.JValue
import org.json4s.JsonAST.{JArray, JBool, JObject}
import org.json4s.jackson.JsonMethods
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

/**
 * Tests that examples match saved JSON files.
 */
class BackwardCompatibilitySpec extends AnyFreeSpec with TestEnvironment with Matchers with Logging with EnvVariables {
  private lazy val koskiValidator = KoskiApplicationForTests.validator
  private lazy val runningOnCI = optEnv("CI").isDefined
  private implicit val user: KoskiSpecificSession = KoskiSpecificSession.systemUser
  private implicit val accessType: AccessType.Value = AccessType.read

  private val backwardsCompatibilityFilesDir: String = "src/test/resources/backwardcompatibility"

  private val allBackwardsCompatibilityFiles = new File(backwardsCompatibilityFilesDir).list()

  private def backwardsCompatibilityFilesForExample(exampleName: String): Seq[String] =
    allBackwardsCompatibilityFiles
      .filter(filename => filename.matches(sanitize(exampleName) + raw"_\d\d\d\d-\d\d-\d\d\.json"))
      .toSeq.sorted

  private def sanitize(exampleName: String): String =
    exampleName
      .replaceAll(" ", "")
      .replaceAll("ä", "a")
      .replaceAll("ö", "o")

  private def fullPath(fileName: String) = backwardsCompatibilityFilesDir + "/" + fileName

  private def filenameWithDate(exampleName: String): String = sanitize(exampleName) + "_" + LocalDate.now.toString + ".json"

  private def writeNewVersion(example: Example): Unit = {
    if (runningOnCI) {
      throw new IllegalStateException(
        s"Backwards compatibility data missing for example '${example.name}' while running on CI. " +
          "Run BackwardCompatibilitySpec locally and commit the files first."
      )
    }
    JsonFiles.writeFile(fullPath(filenameWithDate(example.name)), example.data)
  }

  private def writeInitialVersion(example: Example): Unit = {
    logger.info(s"No example data found for ${example.name}. Creating initial version.")
    new File(backwardsCompatibilityFilesDir).mkdirs()
    writeNewVersion(example)
  }

  private def readFile(filename: String): (JValue, Boolean, Boolean) = {
    var skipEqualityCheck = false
    var skipKoskiValidator = false
    val json: JValue = JsonFiles.readFile(fullPath(filename)).removeField {
      case ("ignoreJsonEquality", JBool(true)) =>
        skipEqualityCheck = true
        true
      case ("ignoreKoskiValidator", JBool(true)) =>
        skipKoskiValidator = true
        true
      case _ => false
    }
    (json, skipEqualityCheck, skipKoskiValidator)
  }

  "backwards compatibility with stored JSON examples" - {
    val storedExamples = oppijaExamples.filter(
      _.data.opiskeluoikeudet.head.isInstanceOf[KoskeenTallennettavaOpiskeluoikeus]
    )
    storedExamples.foreach(makeTestForExample)
  }

  private def makeTestForExample(example: Example): Unit =
    example.name - {
      val existingFiles = backwardsCompatibilityFilesForExample(example.name)
      if (existingFiles.isEmpty) {
        writeInitialVersion(example)
      } else {
        existingFiles.foreach(makeTestForExistingFile)
        val latest = existingFiles.last
        if (JsonSerializer.serializeWithRoot(example.data) != JsonFiles.readFile(fullPath(latest))) {
          logger.info(s"Example data differs for ${example.name} at ${latest}. Creating new version.")
          writeNewVersion(example)
        }
      }
    }

  private def makeTestForExistingFile(filename: String): Unit =
    filename in {
      val (json, skipRoundtripCheck, skipKoskiValidator) = readFile(filename)

      implicit val context: ExtractionContext = strictDeserialization
      SchemaValidatingExtractor.extract[Oppija](json) match {
        case Left(errors) => fail("Backwards compatibility problem: " + errors)
        case Right(oppija) =>
          if (!skipKoskiValidator) {
            koskiValidator.updateFieldsAndValidateAsJson(oppija) match {
              case Right(_) => //ok
              case Left(err) => fail("Validation error: " + err.toString)
            }
          }

          val afterRoundtrip: JValue = JsonSerializer.serializeWithRoot(oppija)
          if (!skipRoundtripCheck) {
            JsonMethods.compact(mangle(afterRoundtrip)) should equal(JsonMethods.compact(mangle(json)))
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

