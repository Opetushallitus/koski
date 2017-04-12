package fi.oph.koski.editor

import fi.oph.koski.log.Logging
import org.json4s.JsonAST.{JObject, JValue}
import org.json4s.{Extraction, _}

object EditorPropertySerializer extends Serializer[EditorProperty] {
  override def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case (property: EditorProperty) => {
      Extraction.decompose(Map(
        "key" -> property.key,
        "title" -> property.title,
        "model" -> property.model
      ) ++ property.props)
    }
  }

  override def deserialize(implicit format: Formats) = PartialFunction.empty
}

object EditorModelSerializer extends Serializer[EditorModel] with Logging {
  override def deserialize(implicit format: Formats) = PartialFunction.empty

  override def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case (model: EditorModel) => {
      model match {
        case (ObjectModel(c, properties, title, editable, prototypes, props)) =>
          json("object", "value" -> Map("classes" -> c, "title" -> title, "properties" -> properties), "editable" -> editable, "prototypes" -> (if (prototypes.nonEmpty) { prototypes} else {JNothing})).merge(json(props))
        case (PrototypeModel(key, props)) => json("prototype", "key" -> key).merge(json(props))
        case (OptionalModel(model, prototype, props)) =>
          val optionalInfo: JValue = json("optional" -> true, "optionalPrototype" -> prototype)
          val typeAndValue = modelOrEmptyObject(model)
          typeAndValue.merge(optionalInfo).merge(json(props))

        case (ListModel(items, prototype, props)) =>
          json("array", "value" -> items, "arrayPrototype" -> prototype).merge(json(props))
        case (EnumeratedModel(value, alternatives, path, props)) =>
          json("enum", "alternatives" -> alternatives, "alternativesPath" -> path, "value" -> value).merge(json(props))
        case (OneOfModel(c, model, prototypes, props)) =>
          val oneOfInfo: JValue = json("oneOfClass" -> c, "oneOfPrototypes" -> prototypes)
          Extraction.decompose(model).merge(oneOfInfo).merge(json(props))

        case (NumberModel(value, props)) => json("number", "value" -> value).merge(json(props))
        case (BooleanModel(value, props)) => json("boolean", "value" -> value).merge(json(props))
        case (DateModel(value, props)) => json("date", "value" -> value).merge(json(props))
        case (StringModel(value, props)) => json("string", "value" -> value).merge(json(props))
        case _ => throw new RuntimeException("No match : " + model)
      }
    }
  }

  private def modelOrEmptyObject(model: Option[EditorModel])(implicit format: Formats) = model.map(Extraction.decompose).getOrElse(emptyObject)

  private def json(tyep: String, props: (String, Any)*)(implicit format: Formats): JValue = {
    val elems: List[(String, Any)] = ("type" -> tyep) :: props.toList
    json(elems: _*)
  }

  private def json(props: (String, Any)*)(implicit format: Formats): JValue = json(Map(props : _*))
  private def json(props: Map[String, Any])(implicit format: Formats): JValue = Extraction.decompose(props)
  private def emptyObject = JObject()
}
