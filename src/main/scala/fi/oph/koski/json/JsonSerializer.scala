package fi.oph.koski.json

import fi.oph.koski.schema.KoskiSchema.schemaFactory
import fi.oph.scalaschema.extraction.ValidationError
import fi.oph.scalaschema.{SchemaValidatingExtractor, _}
import org.json4s.JValue
import org.json4s.jackson.JsonMethods

import scala.reflect.runtime.universe.TypeTag

/**
  * JSON (de)serialization using scala-schema mechanisms
  */
object JsonSerializer {
  def writeWithRoot[T: TypeTag](x: T, pretty: Boolean = false): String = {
    implicit val u = SensitiveDataAllowed.SystemUser
    write(x, pretty)
  }

  def serializeWithRoot[T: TypeTag](obj: T): JValue = serializeWithUser(SensitiveDataAllowed.SystemUser)(obj)

  def serializeWithUser[T: TypeTag](user: SensitiveDataAllowed)(obj: T): JValue = {
    implicit val u = user
    serialize(obj)
  }

  def write[T: TypeTag](x: T, pretty: Boolean = false, includeClassReferences: Boolean = false)(implicit user: SensitiveDataAllowed): String = {
    if (pretty) {
      JsonMethods.pretty(serialize(x, includeClassReferences))
    } else {
      JsonMethods.compact(serialize(x, includeClassReferences))
    }
  }

  def serialize[T: TypeTag](obj: T, includeClassReferences: Boolean = false)(implicit user: SensitiveDataAllowed): JValue = {
    Serializer.serialize(obj, SensitiveAndRedundantDataFilter(user).serializationContext.copy(includeClassReferences = includeClassReferences))
  }

  def serialize(obj: Any, schema: Schema)(implicit user: SensitiveDataAllowed): JValue = {
    Serializer.serialize(obj, schema, SensitiveAndRedundantDataFilter(user).serializationContext)
  }

  def parse[T: TypeTag](j: String, ignoreExtras: Boolean = false): T = {
    extract(JsonMethods.parse(j), ignoreExtras)
  }

  def extract[T: TypeTag](j: JValue, ignoreExtras: Boolean = false): T = {
    implicit val c = ExtractionContext(schemaFactory).copy(ignoreUnexpectedProperties = ignoreExtras)
    SchemaValidatingExtractor.extract(j) match {
      case Right(x) => x
      case Left(error) =>
        throw new RuntimeException(s"Validation error while de-serializing as ${implicitly[TypeTag[T]].tpe.toString}: " + error)
    }
  }

  def validateAndExtract[T: TypeTag](j: JValue, ignoreExtras: Boolean = false): Either[List[ValidationError], T] = {
    implicit val c = ExtractionContext(schemaFactory).copy(ignoreUnexpectedProperties = ignoreExtras)
    SchemaValidatingExtractor.extract(j)
  }
}

