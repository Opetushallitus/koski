package fi.oph.koski.editor

import fi.oph.koski.log.Logging
import org.json4s.JsonAST.{JObject, JValue}
import org.json4s.{Extraction, _}

object EditorModelSerializer extends Serializer[EditorModel] with Logging {
  override def deserialize(implicit format: Formats) = PartialFunction.empty

  override def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case (model: EditorModel) => {
      model match {
        case (ObjectModel(c, properties, title, editable, prototypes)) =>
          json("object", "value" -> Map("classes" -> c, "title" -> title, "properties" -> properties), "editable" -> editable, "prototypes" -> (if (prototypes.nonEmpty) { prototypes} else {JNothing}))
        case (PrototypeModel(key)) => json("prototype", "key" -> key)
        case (OptionalModel(model, prototype)) =>
          val optionalInfo: JValue = json("optional" -> true, "optionalPrototype" -> prototype)
          val typeAndValue = modelOrEmptyObject(model)
          typeAndValue.merge(optionalInfo)

        case (ListModel(items, prototype)) =>
          json("array", "value" -> items, "arrayPrototype" -> prototype)
        case (EnumeratedModel(value, alternatives, path)) =>
          json("enum", "simple" -> true, "alternatives" -> alternatives, "alternativesPath" -> path, "value" -> value)
        case (OneOfModel(c, model, prototypes)) =>
          val oneOfInfo: JValue = json("oneOfClass" -> c, "oneOfPrototypes" -> prototypes)
          modelOrEmptyObject(model).merge(oneOfInfo)

        case (NumberModel(value)) => json("number", "value" -> value)
        case (BooleanModel(value)) => json("boolean", "value" -> value)
        case (DateModel(value)) => json("date", "value" -> value)
        case (StringModel(value)) => json("string", "value" -> value)
        case _ => throw new RuntimeException("No match : " + model)
      }
    }
  }

  private def modelOrEmptyObject(model: Option[EditorModel])(implicit format: Formats) = model.map(Extraction.decompose).getOrElse(emptyObject)

  private def json(tyep: String, props: (String, Any)*)(implicit format: Formats): JValue = {
    val elems: List[(String, Any)] = ("type" -> tyep) :: props.toList
    json(elems: _*)
  }

  private def json(props: (String, Any)*)(implicit format: Formats): JValue = Extraction.decompose(Map(props : _*))
  private def emptyObject = JObject()
}
