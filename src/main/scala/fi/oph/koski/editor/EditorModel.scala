package fi.oph.koski.editor

import java.time.LocalDate

import fi.oph.koski.util.FinnishDateFormat.finnishDateFormat
import org.json4s.JsonAST.JValue
import org.json4s.{Extraction, _}

sealed trait EditorModel

case class ObjectModel(`class`: String, properties: List[EditorProperty], data: Option[AnyRef], title: Option[String], editable: Boolean, prototypes: Map[String, EditorModel]) extends EditorModel

case class PrototypeModel(`class`: String) extends EditorModel

case class EditorProperty(key: String, title: String, model: EditorModel, hidden: Boolean, representative: Boolean)

case class ListModel(items: List[EditorModel], prototype: Option[EditorModel]) extends EditorModel

case class EnumeratedModel(value: Option[EnumValue], alternatives: Option[List[EnumValue]], alternativesPath: Option[String]) extends EditorModel
case class EnumValue(value: String, title: String, data: Any)

case class NumberModel(data: Number) extends EditorModel
case class BooleanModel(data: Boolean) extends EditorModel
case class DateModel(data: LocalDate) extends EditorModel
case class StringModel(data: String) extends EditorModel

case class OptionalModel(model: Option[EditorModel], prototype: Option[EditorModel]) extends EditorModel

case class OneOfModel(`class`: String, model: Option[EditorModel], prototypes: List[EditorModel]) extends EditorModel

object EditorModelSerializer extends Serializer[EditorModel] {
  override def deserialize(implicit format: Formats) = PartialFunction.empty

  override def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case (model: EditorModel) => {
      model match {
        case (ObjectModel(c, properties, data, title, editable, prototypes)) =>
          json("object", "value" -> Map("class" -> c, "data" -> data, "title" -> title, "properties" -> properties), "editable" -> editable, "prototypes" -> prototypes)
        case (PrototypeModel(c)) => json("prototype", "class" -> c)
        case (OptionalModel(model, prototype)) =>
          val optionalInfo: JValue = json("optional" -> true, "prototype" -> prototype)
          val typeAndValue = valueOrPrototypeWithoutData(model, prototype)
          typeAndValue.merge(optionalInfo)

        case (ListModel(items, prototype)) =>
          json("array", "value" -> items, "prototype" -> prototype)
        case (EnumeratedModel(value, alternatives, path)) =>
          json("enum", "simple" -> true, "alternatives" -> alternatives, "alternativesPath" -> path, "value" -> value)
        case (OneOfModel(c, model, prototypes)) =>
          val oneOfInfo: JValue = json("one-of-class" -> c, "one-of-prototypes" -> prototypes)
          valueOrPrototypeWithoutData(model, prototypes.headOption).merge(oneOfInfo)

        case (NumberModel(data)) => json("number", "simple" -> true, "value" -> Map("data" -> data))
        case (BooleanModel(data)) => json("boolean", "simple" -> true, "value" -> Map("data" -> data, "title" -> (if (data) { "kyllä" } else { "ei" }))) // TODO: localization
        case (DateModel(data)) => json("date", "simple" -> true, "value" -> Map("data" -> data, "title" -> finnishDateFormat.format(data)))
        case (StringModel(data)) => json("string", "simple" -> true, "value" -> Map("data" -> data))
        case _ => throw new RuntimeException("No match : " + model)
      }
    }
  }

  private def valueOrPrototypeWithoutData(model: Option[EditorModel], prototype: Option[EditorModel])(implicit format: Formats) = (model, prototype) match {
    case (Some(innerModel), _) => Extraction.decompose(innerModel)
    case (None, Some(p:PrototypeModel)) =>
      val fields = Extraction.decompose(p).filterField{case (key, value) => key != "value"} // get structure from prototype, but remove value
      JObject(fields: _*)
    case _ => emptyObject
  }

  private def json(tyep: String, props: (String, Any)*)(implicit format: Formats): JValue = {
    val elems: List[(String, Any)] = ("type" -> tyep) :: props.toList
    json(elems: _*)
  }

  private def json(props: (String, Any)*)(implicit format: Formats): JValue = Extraction.decompose(Map(props : _*))
  private def emptyObject = JObject()
}