package fi.oph.koski.tools


import java.io.File

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.json.JsonFiles
import fi.oph.common.koskiuser.{AccessType, KoskiSession}
import fi.oph.koski.schema.KoskiSchema.deserializationContext
import fi.oph.koski.schema.Oppija
import fi.oph.scalaschema.SchemaValidatingExtractor
import org.json4s.JsonAST.{JBool, JField, JObject, JValue}

object AddIgnoreKoskiValidatorFlag extends App {

  val dirName = "src/test/resources/backwardcompatibility"
  lazy val koskiValidator = KoskiApplicationForTests.validator
  implicit val user = KoskiSession.systemUser
  implicit val accessType = AccessType.read

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

  def failsAtKoskiValidator(oppija: Oppija) = koskiValidator.validateAsJson(oppija).isLeft

  def fullName(fileName: String) = dirName + "/" + fileName

  println("Starting!")
  modifyJsons
  println("Done!")
  System.exit(0)
}
