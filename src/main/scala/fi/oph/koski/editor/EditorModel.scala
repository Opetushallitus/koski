package fi.oph.koski.editor

import java.time.LocalDate

sealed trait EditorModel

case class ObjectModel(classes: List[String], properties: List[EditorProperty], title: Option[String], editable: Boolean, prototypes: Map[String, EditorModel]) extends EditorModel

case class PrototypeModel(key: String) extends EditorModel

case class EditorProperty(key: String, title: String, model: EditorModel, props: Map[String, Any])

case class ListModel(items: List[EditorModel], prototype: Option[EditorModel]) extends EditorModel

case class EnumeratedModel(value: Option[EnumValue], alternatives: Option[List[EnumValue]], alternativesPath: Option[String]) extends EditorModel
case class EnumValue(value: String, title: String, data: Any)

case class NumberModel(value: ValueWithData[Number]) extends EditorModel
case class BooleanModel(value: ValueWithData[Boolean]) extends EditorModel
case class DateModel(value: ValueWithData[LocalDate]) extends EditorModel
case class StringModel(value: ValueWithData[String]) extends EditorModel
case class ValueWithData[T](data: T)

case class OptionalModel(model: Option[EditorModel], prototype: Option[EditorModel]) extends EditorModel

case class OneOfModel(`class`: String, model: EditorModel, prototypes: List[EditorModel]) extends EditorModel