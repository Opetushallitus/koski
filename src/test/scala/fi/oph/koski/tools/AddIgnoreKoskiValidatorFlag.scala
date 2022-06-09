package fi.oph.koski.tools


import java.io.File
import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.json.JsonFiles
import fi.oph.koski.koskiuser.{AccessType, KoskiSpecificSession}
import fi.oph.koski.schema.KoskiSchema.strictDeserialization
import fi.oph.koski.schema.Oppija
import fi.oph.scalaschema.{ExtractionContext, SchemaValidatingExtractor}
import org.json4s.JsonAST.{JBool, JField, JObject, JValue}

object AddIgnoreKoskiValidatorFlag extends App {
  private val dirName = "src/test/resources/backwardcompatibility"
  private lazy val koskiValidator = KoskiApplicationForTests.validator
  private implicit val user = KoskiSpecificSession.systemUser
  private implicit val accessType = AccessType.read
  private implicit val context: ExtractionContext = strictDeserialization

  def modifyJsons = {
    val existingFiles = new File(dirName).list().filter(_.endsWith(".json")).map(fullName)

    existingFiles.foreach { filename => {
      val json = JsonFiles.readFile(filename)
      val oppija = deserializeToOppija(json)

      if (failsAtKoskiValidator(oppija) && !flagExists(json)) {
        val withFlag = json.merge(JObject("ignoreKoskiValidator" -> JBool(true)))
        JsonFiles.writeFile(filename, withFlag)
        println(s"Added ignoreKoskiValidator flag to $filename")
      }
    }}
  }

  def deserializeToOppija(json: JValue) = {
    val cleanedJson = json.removeField {
      case ("ignoreJsonEquality", JBool(true)) => true
      case ("ignoreKoskiValidator", JBool(true)) => true
      case _ => false
    }
    SchemaValidatingExtractor.extract[Oppija](cleanedJson) match {
      case Right(oppija) => oppija
      case Left(err) => throw new IllegalStateException(err.toString)
    }
  }

  def flagExists(json: JValue) = json.findField {
    case JField("ignoreKoskiValidator", JBool(true)) => true
    case _ => false
  }.isDefined

  def failsAtKoskiValidator(oppija: Oppija) = koskiValidator.updateFieldsAndValidateAsJson(oppija).isLeft

  def fullName(fileName: String) = dirName + "/" + fileName

  println("Starting!")
  modifyJsons
  println("Done!")
  System.exit(0)
}
