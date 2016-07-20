package fi.oph.koski.editor

import java.time.LocalDate

import fi.oph.koski.util.FinnishDateFormat.finnishDateFormat
import org.json4s.{Extraction, _}

sealed trait EditorModel {
  def empty = false
}

case class ObjectModel(`class`: String, properties: List[EditorProperty], data: Option[AnyRef], title: Option[String], editable: Boolean, prototypes: Map[String, EditorModel]) extends EditorModel {
  override def empty = !properties.exists(!_.model.empty)
}

case class PrototypeModel(`class`: String) extends EditorModel

case class EditorProperty(key: String, title: String, model: EditorModel, hidden: Boolean, representative: Boolean)

case class ListModel(items: List[EditorModel], prototype: Option[EditorModel]) extends EditorModel {
  override def empty = !items.exists(!_.empty)
}

case class EnumeratedModel(value: Option[EnumValue], alternatives: Option[List[EnumValue]], alternativesPath: Option[String]) extends EditorModel
case class EnumValue(value: String, title: String, data: Any)

case class NumberModel(data: Number) extends EditorModel
case class BooleanModel(data: Boolean) extends EditorModel
case class DateModel(data: LocalDate) extends EditorModel
case class StringModel(data: String) extends EditorModel

case class OptionalModel(model: Option[EditorModel], prototype: Option[EditorModel]) extends EditorModel {
  override def empty = !model.exists(!_.empty)
}

case class OneOfModel(`class`: String, model: Option[EditorModel], prototypes: List[EditorModel]) extends EditorModel {
  override def empty = model.isEmpty || model.get.empty
}

object EditorModelSerializer extends Serializer[EditorModel] {
  override def deserialize(implicit format: Formats) = PartialFunction.empty

  override def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case (model: EditorModel) => {
      val jvalue: JValue = model match {
        case (ObjectModel(c, properties, data, title, editable, prototypes)) =>
          json("object", "class" -> c, "properties" -> properties, "value" -> Map("data" -> data, "title" -> title), "editable" -> editable, "prototypes" -> prototypes)
        case (PrototypeModel(c)) => json("prototype", "class" -> c)
        case (OptionalModel(model, prototype)) =>
          model.map(Extraction.decompose).getOrElse(emptyObject).merge(json("optional" -> true, "prototype" -> prototype))
        case (ListModel(items, prototype)) =>
          json("array", "items" -> items, "prototype" -> prototype)
        case (EnumeratedModel(value, alternatives, path)) =>
          json("enum", "simple" -> true, "alternatives" -> alternatives, "alternativesPath" -> path, "value" -> value)
        case (OneOfModel(c, model, prototypes)) =>
          model.map(Extraction.decompose).getOrElse(emptyObject).merge(json("one-of-class" -> c, "prototypes" -> prototypes))
        case (NumberModel(data)) => json("number", "simple" -> true, "value" -> Map("data" -> data))
        case (BooleanModel(data)) => json("boolean", "simple" -> true, "value" -> Map("data" -> data, "title" -> (if (data) { "kyllä" } else { "ei" }))) // TODO: localization
        case (DateModel(data)) => json("date", "simple" -> true, "value" -> Map("data" -> data, "title" -> finnishDateFormat.format(data)))
        case (StringModel(data)) => json("string", "simple" -> true, "value" -> Map("data" -> data))
        case _ => throw new RuntimeException("No match : " + model)
      }
      model.empty match {
        case true => jvalue merge(json("empty" -> true)) // TODO: move to value
        case _ => jvalue
      }
    }
  }

  private def json(tyep: String, props: (String, Any)*)(implicit format: Formats): JValue = {
    val elems: List[(String, Any)] = ("type" -> tyep) :: props.toList
    json(elems: _*)
  }

  private def json(props: (String, Any)*)(implicit format: Formats): JValue = Extraction.decompose(Map(props : _*))
  private def emptyObject = JObject()
}