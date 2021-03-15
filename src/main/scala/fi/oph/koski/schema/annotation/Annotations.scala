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
