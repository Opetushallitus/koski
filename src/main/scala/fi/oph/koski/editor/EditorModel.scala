package fi.oph.koski.editor

import java.time.LocalDate

sealed trait EditorModel

case class ObjectModel(classes: List[String], properties: List[EditorProperty], title: Option[String], editable: Boolean, prototypes: Map[String, EditorModel], props: Map[String, Any]) extends EditorModel

case class PrototypeModel(key: String, props: Map[String, Any]) extends EditorModel

case class EditorProperty(key: String, title: String, model: EditorModel, props: Map[String, Any])

case class ListModel(items: List[EditorModel], prototype: Option[EditorModel], props: Map[String, Any]) extends EditorModel

case class EnumeratedModel(value: Option[EnumValue], alternatives: Option[List[EnumValue]], alternativesPath: Option[String], props: Map[String, Any]) extends EditorModel
case class EnumValue(value: String, title: String, data: Any)

case class NumberModel(value: ValueWithData[Number], props: Map[String, Any]) extends EditorModel
case class BooleanModel(value: ValueWithData[Boolean], props: Map[String, Any]) extends EditorModel
case class DateModel(value: ValueWithData[LocalDate], props: Map[String, Any]) extends EditorModel
case class StringModel(value: ValueWithData[String], props: Map[String, Any]) extends EditorModel
case class ValueWithData[T](data: T)

case class OptionalModel(model: Option[EditorModel], prototype: Option[EditorModel], props: Map[String, Any]) extends EditorModel

case class OneOfModel(`class`: String, model: EditorModel, prototypes: List[EditorModel], props: Map[String, Any]) extends EditorModel