package fi.oph.koski.editor

import java.time.{LocalDate, LocalDateTime}

import fi.oph.scalaschema.Metadata
import org.json4s.JValue

sealed trait EditorModel {
  def metadata: List[Metadata]
}

case class ObjectModel(classes: List[String], properties: List[EditorProperty], title: Option[String], editable: Boolean, invalidatable: Boolean, prototypes: Map[String, EditorModel], metadata: List[Metadata]) extends EditorModel

case class PrototypeModel(key: String, metadata: List[Metadata]) extends EditorModel

case class EditorProperty(key: String, title: String, description: List[String], model: EditorModel, flags: Map[String, JValue])

case class ListModel(items: List[EditorModel], prototype: Option[EditorModel], metadata: List[Metadata]) extends EditorModel

case class EnumeratedModel(value: Option[EnumValue], alternatives: Option[List[EnumValue]], alternativesPath: Option[String], metadata: List[Metadata]) extends EditorModel
case class EnumValue(value: String, title: String, data: JValue, groupName: Option[String])

case class NumberModel(value: ValueWithData[Number], metadata: List[Metadata]) extends EditorModel
case class BooleanModel(value: ValueWithData[Boolean], metadata: List[Metadata]) extends EditorModel
case class DateModel(value: ValueWithData[LocalDate], metadata: List[Metadata]) extends EditorModel
case class DateTimeModel(value: ValueWithData[LocalDateTime], metadata: List[Metadata]) extends EditorModel
case class StringModel(value: ValueWithData[String], metadata: List[Metadata]) extends EditorModel
case class ValueWithData[T](data: T, classes: Option[List[String]])

case class OptionalModel(model: Option[EditorModel], prototype: Option[EditorModel], metadata: List[Metadata]) extends EditorModel

case class OneOfModel(`class`: String, model: EditorModel, prototypes: List[EditorModel], metadata: List[Metadata]) extends EditorModel
