package fi.oph.koski.koski

import com.github.fge.jsonschema.core.report.LogLevel.{ERROR, FATAL}
import com.github.fge.jsonschema.core.report.{ListReportProvider, ProcessingMessage, ProcessingReport}
import com.github.fge.jsonschema.main.JsonSchemaFactory
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.schema._
import fi.oph.scalaschema.{Schema, SchemaToJson}
import org.json4s._
import org.json4s.jackson.JsonMethods._

import scala.collection.JavaConversions._

object KoskiJsonSchemaValidator {
  private val jsonSchemaFactory = JsonSchemaFactory.newBuilder.setReportProvider(new ListReportProvider(ERROR, FATAL)).freeze()

  val henkilöSchema = createSchema(classOf[Henkilö])
  val opiskeluoikeusSchemas = Map(
    "ammatillinenkoulutus" -> createSchema(classOf[AmmatillinenOpiskeluoikeus]),
    "perusopetus" -> createSchema(classOf[PerusopetuksenOpiskeluoikeus]),
    "perusopetuksenlisaopetus" -> createSchema(classOf[PerusopetuksenLisäopetuksenOpiskeluoikeus]),
    "perusopetukseenvalmistavaopetus" -> createSchema(classOf[PerusopetukseenValmistavanOpetuksenOpiskeluoikeus]),
    "luva" -> createSchema(classOf[LukioonValmistavanKoulutuksenOpiskeluoikeus]),
    "lukiokoulutus" -> createSchema(classOf[LukionOpiskeluoikeus]),
    "ibtutkinto" -> createSchema(classOf[IBOpiskeluoikeus]),
    "korkeakoulutus" -> createSchema(classOf[KorkeakoulunOpiskeluoikeus]),
    "ylioppilastutkinto" -> createSchema(classOf[YlioppilastutkinnonOpiskeluoikeus])
  )

  private def createSchema(clazz: Class[_]) = toJsonSchema(KoskiSchema.createSchema(clazz))
  private def toJsonSchema(schema: Schema) = jsonSchemaFactory.getJsonSchema(asJsonNode(SchemaToJson.toJsonSchema(schema)))

  def jsonSchemaValidate(node: JValue): HttpStatus = {
    node match {
      case JObject(List(("henkilö", henkilö: JObject), ("opiskeluoikeudet", JArray(opiskeluoikeudet)))) =>
        val henkilöStatus: HttpStatus = toHttpStatus(henkilöSchema.validate(asJsonNode(henkilö)))
        val opiskeluoikeusStatii: List[HttpStatus] = opiskeluoikeudet.map(validateOpiskeluoikeus)
        HttpStatus.fold(henkilöStatus :: opiskeluoikeusStatii)

      case jobject => KoskiErrorCategory.badRequest.validation.jsonSchema("Oppijan on oltava muotoa { henkilö: {...}, opiskeluoikeudet: [...]}")
    }
  }

  private def validateOpiskeluoikeus(node: JValue): HttpStatus = {
    node \ "tyyppi" \ "koodiarvo" match {
      case JString(koodiarvo) => opiskeluoikeusSchemas.get(koodiarvo) match {
        case Some(schema) => toHttpStatus(schema.validate(asJsonNode(node)))
        case None => KoskiErrorCategory.badRequest.validation.jsonSchema(s"Tuntematon opiskeluoikeuden tyyppi: $koodiarvo")
      }
      case _ => KoskiErrorCategory.badRequest.validation.jsonSchema("Opiskeluoikeudesta puuttuu { tyyppi: { koodiarvo : _ }}")
    }
  }

  private def toHttpStatus(report: ProcessingReport): HttpStatus = {
    toHttpStatus(report.filter(message => message.getLogLevel == ERROR))
  }

  private def toHttpStatus(errorMessages: Iterable[ProcessingMessage]): HttpStatus = {
    if (!errorMessages.isEmpty) {
      val errors = errorMessages
        .map(_.asJson)
        .map(fromJsonNode)
      HttpStatus.fold(errors.map((error: JValue) => KoskiErrorCategory.badRequest.validation.jsonSchema.apply(error)))
    } else {
      HttpStatus.ok
    }
  }
}
