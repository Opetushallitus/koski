package fi.oph.koski.editor

import java.time.LocalDate

import fi.oph.koski.util.FinnishDateFormat.finnishDateFormat
import org.json4s.jackson.Serialization
import org.json4s.{Extraction, _}

sealed trait EditorModel {
  def empty = false
}

case class ObjectModel(`class`: String, properties: List[EditorProperty], data: Option[AnyRef], title: Option[String], editable: Boolean) extends EditorModel {
  override def empty = !properties.exists(!_.model.empty)
}
case class EditorProperty(key: String, title: String, model: EditorModel, hidden: Boolean, representative: Boolean)

case class ListModel(items: List[EditorModel], prototype: Option[EditorModel]) extends EditorModel { // need to add a prototype for adding new item
  override def empty = !items.exists(!_.empty)
}

case class EnumeratedModel(value: Option[EnumValue], alternatives: Option[List[EnumValue]]) extends EditorModel
case class EnumValue(value: String, title: String, data: Any)

case class NumberModel(data: Number) extends EditorModel
case class BooleanModel(data: Boolean) extends EditorModel
case class DateModel(data: LocalDate) extends EditorModel
case class StringModel(data: String) extends EditorModel

case class OptionalModel(model: Option[EditorModel], prototype: Option[EditorModel]) extends EditorModel { // need a prototype for editing
  override def empty = !model.exists(!_.empty)
}

case class OneOfModel(`class`: String, model: Option[EditorModel]) extends EditorModel { // need to add option prototypes for editing
  override def empty = model.isEmpty || model.get.empty
}

object EditorModelSerializer extends Serializer[EditorModel] {
  override def deserialize(implicit format: Formats) = PartialFunction.empty

  override def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case (model: EditorModel) => {
      val json: JValue = model match {
        case (ObjectModel(c, properties, data, title, editable)) => d("object", "class" -> c, "properties" -> properties, "data" -> data, "title" -> title, "editable" -> editable)
        case (OptionalModel(model, prototype)) => model.map(Extraction.decompose).getOrElse(j()).merge(j("optional" -> true, "prototype" -> Extraction.decompose(prototype)))
        case (ListModel(items, prototype)) => d("array", "items" -> items, "prototype" -> Extraction.decompose(prototype))
        case (EnumeratedModel(Some(EnumValue(value, title, data)), alternatives)) => d("enum", "simple" -> true, "data" -> data, "value" -> value, "title" -> title, "alternatives" -> alternatives)
        case (EnumeratedModel(None, alternatives)) => d("enum", "simple" -> true, "title" -> "", "alternatives" -> alternatives)
        case (OneOfModel(c, None)) => j("one-of-class" -> c)
        case (OneOfModel(c, Some(model))) => Extraction.decompose(model).merge(j("one-of-class" -> c))
        case (NumberModel(data)) => d("number", "simple" -> true, "data" -> data)
        case (BooleanModel(data)) => d("boolean", "simple" -> true, "data" -> data, "title" -> (if (data) { "kyllä" } else { "ei" })) // TODO: localization
        case (DateModel(data)) => d("date", "simple" -> true, "data" -> data, "title" -> finnishDateFormat.format(data))
        case (StringModel(data)) => d("string", "simple" -> true, "data" -> data)
        case _ => throw new RuntimeException("No match : " + model)
      }
      model.empty match {
        case true => json merge(j("empty" -> true))
        case _ => json
      }
    }
  }

  private def d(tyep: String, props: (String, Any)*)(implicit format: Formats) = {
    val elems: List[(String, Any)] = ("type" -> tyep) :: props.toList
    j(elems: _*)
  }

  private def j(props: (String, Any)*)(implicit format: Formats): JValue = Extraction.decompose(Map(props : _*))
}