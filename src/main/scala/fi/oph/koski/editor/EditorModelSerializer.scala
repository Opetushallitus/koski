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

  private def propsToFields(props: Map[String, Any])(implicit format: Formats) = props.toList.map{ case (key, value) => JField(key, Extraction.decompose(value)) }

  override def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case (model: EditorModel) => {
      model match {
        case (ObjectModel(c, properties, title, editable, prototypes, props)) =>
          val protos = if (prototypes.nonEmpty) { JObject(prototypes.toList.map { case (key, model) => JField(key, serialize(format)(model)) }) } else { JNothing }
          JObject(List(
            JField("type", JString("object")),
            JField("value", JObject(
              JField("classes", JArray(c.map(JString(_)))),
              JField("title", title.map(JString(_)).getOrElse(JNothing)),
              JField("properties", JArray(properties.map{ case EditorProperty(key, title, model, props) =>
                JObject(List(
                  JField("key", JString(key)),
                  JField("title", JString(title)),
                  JField("model", serialize(format)(model))
                ) ++ propsToFields(props))
              }))
            )),
            JField("editable", JBool(editable)),
            JField("prototypes", protos)
          ) ++ propsToFields(props))
        case (PrototypeModel(key, props)) =>
          JObject(
            List(
              JField("type", JString("prototype")),
              JField("key", JString(key))
            ) ++ propsToFields(props)
          )
        case (OptionalModel(model, prototype, props)) =>
          val optionalInfo: JValue = JObject(
            JField("optional", JBool(true)),
            JField("optionalPrototype", prototype.map(p => serialize(format)(p)).getOrElse(JNothing))
          )

          val typeAndValue = modelOrEmptyObject(model)
          typeAndValue.merge(optionalInfo).merge(JObject(propsToFields(props)))

        case (ListModel(items, prototype, props)) =>
          JObject(List(
            JField("type", JString("array")),
            JField("value", JArray(items.map(item => serialize(format)(item)))),
            JField("arrayPrototype", prototype.map(p => serialize(format)(p)).getOrElse(JNothing))
          ) ++ propsToFields(props))

        case (EnumeratedModel(value, alternatives, path, props)) =>
          json("enum", "alternatives" -> alternatives, "alternativesPath" -> path, "value" -> value).merge(json(props))
        case (OneOfModel(c, model, prototypes, props)) =>
          val oneOfInfo = JObject(
            JField("oneOfClass", JString(c)),
            JField("oneOfPrototypes", JArray(prototypes.map(p => serialize(format)(p))))
          )
          serialize(format)(model).merge(oneOfInfo).merge(JObject(propsToFields(props)))
        case (NumberModel(value, props)) => json("number", "value" -> value).merge(json(props))
        case (BooleanModel(value, props)) => json("boolean", "value" -> value).merge(json(props))
        case (DateModel(value, props)) => json("date", "value" -> value).merge(json(props))
        case (StringModel(value, props)) => json("string", "value" -> value).merge(json(props))
        case _ => throw new RuntimeException("No match : " + model)
      }
    }
  }

  private def modelOrEmptyObject(model: Option[EditorModel])(implicit format: Formats) = model.map(serialize(format)(_)).getOrElse(emptyObject)

  private def json(tyep: String, props: (String, Any)*)(implicit format: Formats): JValue = {
    val elems: List[(String, Any)] = ("type" -> tyep) :: props.toList
    json(elems: _*)
  }

  private def json(props: (String, Any)*)(implicit format: Formats): JValue = json(Map(props : _*))
  private def json(props: Map[String, Any])(implicit format: Formats): JValue = Extraction.decompose(props)
  private def emptyObject = JObject()
}
