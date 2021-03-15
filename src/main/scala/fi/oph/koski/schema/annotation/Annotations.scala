package fi.oph.koski.schema.annotation

import fi.oph.koski.koskiuser.Rooli.Role
import fi.oph.scalaschema._
import org.json4s.JsonAST

/* This property can be used to represent the whole entity */
case class Representative() extends RepresentationalMetadata

/* This property contains complex nested structure and should be rendered as a section in the UI */
case class ComplexObject() extends RepresentationalMetadata

/* This property should be flattened in the UI */
case class FlattenInUI() extends RepresentationalMetadata

/* This property contains a list of items that should be represented in a table */
case class Tabular() extends RepresentationalMetadata

case class ReadOnly(why: String) extends Metadata {
  override def appendMetadataToJsonSchema(obj: JsonAST.JObject) = appendToDescription(obj, why)
}

case class ClassName(classname: String) extends RepresentationalMetadata

/* This is a multi-line string that should be edited with a textare instead of a string input */
case class MultiLineString(lineCount: Int) extends RepresentationalMetadata

/* Tags a numeric field with a unit of measure */
case class UnitOfMeasure(unit: String) extends RepresentationalMetadata

/* An example of the data */
case class Example(text: String) extends RepresentationalMetadata

case class SensitiveData(roles: Set[Role]) extends RepresentationalMetadata

case class Tooltip(text: String) extends RepresentationalMetadata

/* Numeric field should be rendered using this scale */
case class Scale(numberOfDigits: Int) extends RepresentationalMetadata

// TODO: Toteuta siistimmin suoraan scala-schemaan
case class EnumValues(values: Set[String]) extends Metadata {
  override def applyMetadata(x: ObjectWithMetadata[_], schemaFactory: SchemaFactory): ObjectWithMetadata[_] = {
    val metadata = x match {
      case p: Property => p.schema match {
        case s: StringSchema => p.copy(schema = s.copy(enumValues = Option(s.enumValues.toList.flatten ++ values)))
        case other: Any => throw new UnsupportedOperationException("EnumValues not supported for " + other)
      }
      case other: Any => throw new UnsupportedOperationException("EnumValues not supported for " + other)
    }
    super.applyMetadata(metadata, schemaFactory)
  }

  override def appendMetadataToJsonSchema(obj: JsonAST.JObject): JsonAST.JObject = obj
}
