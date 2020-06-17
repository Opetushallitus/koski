package fi.oph.koski.schema

import java.time.LocalDate

import fi.oph.koski.log.Logging
import fi.oph.scalaschema._
import fi.oph.scalaschema.extraction.{CustomDeserializer, OtherViolation, ValidationError}

case class JaksoCustomDeserializer(customDeserializers : scala.List[CustomDeserializer]) extends CustomDeserializer with Logging {
  override def extract(cursor: JsonCursor, schema: SchemaWithClassName, metadata: List[Metadata])(implicit context: ExtractionContext): Either[List[ValidationError], Any] = {
    SchemaValidatingExtractor.extract(cursor, schema, metadata)(context.copy(customDeserializers = customDeserializers)) match {
      case Right(j: Jakso) if j.loppu.forall(loppu => loppu.isAfter(j.alku) || loppu.equals(j.alku)) => Right(j)
      case Right(j: MahdollisestiAlkupäivällinenJakso) if j.loppu.forall(loppu => j.alku.exists(alku => loppu.isAfter(alku) || loppu.equals(alku))) => Right(j) // käytetään 'exists' koska jos syötetään loppu vaaditaan myös alku
      case Right(j: Jakso) => error(cursor, Some(j.alku), j.loppu)
      case Right(j: MahdollisestiAlkupäivällinenJakso) => error(cursor, j.alku, j.loppu)
      case errors => errors
    }
  }

  private def error(cursor: JsonCursor, alku: Option[LocalDate], loppu: Option[LocalDate]) = {
    Left(List(ValidationError(cursor.path, cursor.json, OtherViolation(s"Jakson alku ${alku.mkString} on jakson lopun ${loppu.mkString} jälkeen", "jaksonLoppuEnnenAlkua"))))
  }

  def isApplicable(schema: SchemaWithClassName): Boolean =
    classOf[Jakso].isAssignableFrom(Class.forName(schema.fullClassName)) ||
    classOf[MahdollisestiAlkupäivällinenJakso].isAssignableFrom(Class.forName(schema.fullClassName))
}
