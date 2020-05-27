package fi.oph.koski.schema

import fi.oph.koski.log.Logging
import fi.oph.scalaschema._
import fi.oph.scalaschema.extraction.{CustomDeserializer, OtherViolation, ValidationError}

case class JaksoCustomDeserializer(customDeserializers : scala.List[CustomDeserializer]) extends CustomDeserializer with Logging {
  override def extract(cursor: JsonCursor, schema: SchemaWithClassName, metadata: List[Metadata])(implicit context: ExtractionContext): Either[List[ValidationError], Any] = {
    SchemaValidatingExtractor.extract(cursor, schema, metadata)(context.copy(customDeserializers = customDeserializers)) match {
      case Right(j: Jakso) if j.loppu.forall(loppu => loppu.isAfter(j.alku) || loppu.equals(j.alku)) => Right(j)
      case Right(j: Jakso) =>
        Left(List(ValidationError(cursor.path, cursor.json, OtherViolation(s"Jakson alku ${j.alku} on jakson lopun ${j.loppu.mkString} jälkeen", "jaksonLoppuEnnenAlkua"))))
      case errors => errors
    }
  }

  def isApplicable(schema: SchemaWithClassName): Boolean = classOf[Jakso].isAssignableFrom(Class.forName(schema.fullClassName))
}
