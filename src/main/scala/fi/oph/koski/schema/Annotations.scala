package fi.oph.koski.schema

import fi.oph.scalaschema.Metadata
import org.json4s.JsonAST
import org.json4s.JsonAST.JObject

trait RepresentationalMetadata extends Metadata {
  override def appendMetadataToJsonSchema(obj: JObject) = obj // Does not affect JSON schema
}

/* This property can be used to represent the whole entity */
case class Representative() extends RepresentationalMetadata

/* This property contains complex nested structure and should be rendered as a section in the UI */
case class ComplexObject() extends RepresentationalMetadata

/* This property should be flattened in the UI */
case class Flatten() extends RepresentationalMetadata

/* This property contains a list of items that should be represented in a table */
case class Tabular() extends RepresentationalMetadata

case class ReadOnly(why: String) extends Metadata {
  override def appendMetadataToJsonSchema(obj: JsonAST.JObject) = appendToDescription(obj, why)
}

case class ClassName(classname: String) extends RepresentationalMetadata

/* This is a multi-line string that should be edited with a textare instead of a string input */
case class MultiLineString(lineCount: Int) extends RepresentationalMetadata