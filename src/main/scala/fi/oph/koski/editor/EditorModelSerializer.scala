package fi.oph.koski.editor

import fi.oph.koski.json.{JsonSerializer, LegacyJsonSerialization}
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.KoskiSchema
import fi.oph.koski.schema.annotation.{Example, InfoDescription, InfoLinkTitle, InfoLinkUrl, MultiLineString, Scale, UnitOfMeasure}
import fi.oph.scalaschema.annotation._
import fi.oph.scalaschema.{Metadata, SerializationContext, Serializer}
import org.json4s.JsonAST.{JObject, JString, JValue}
import org.json4s.{Extraction, _}

object EditorModelSerializer extends Serializer[EditorModel] with Logging {
  def serializeOnlyWhen(o: OnlyWhen) = Serializer.serialize(o.serializableForm, SerializationContext(KoskiSchema.schemaFactory))
  def serializeModel(model: EditorModel) = serialize(LegacyJsonSerialization.jsonFormats)(model)
  def serializeEnum(enum: EnumValue) = serializeEnumValue(enum)(LegacyJsonSerialization.jsonFormats)

  override def deserialize(implicit format: Formats) = PartialFunction.empty

  override def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case (model: EditorModel) => {
      model match {
        case (ObjectModel(c, properties, title, editable, invalidatable, prototypes, metadata)) =>
          val protos = if (prototypes.nonEmpty) { JObject(prototypes.toList.map { case (key, model) => JField(key, serialize(format)(model)) }) } else { JNothing }
          JObject(List(
            JField("type", JString("object")),
            JField("value", JObject(
              JField("classes", JArray(c.map(JString(_)))),
              JField("title", title.map(JString(_)).getOrElse(JNothing)),
              JField("properties", JArray(properties.map{ case EditorProperty(key, title, description, model, flags) =>
                JObject(List(
                  JField("key", JString(key)),
                  JField("title", JString(title)),
                  JField("model", serialize(format)(model))
                ) ++ flagsToFields(flags) ++ descriptionToField(description))
              }))
            )),
            JField("editable", JBool(editable)),
            JField("invalidatable", JBool(invalidatable)),
            JField("prototypes", protos)
          ) ++ metadataToFields(metadata))
        case (PrototypeModel(key, metadata)) =>
          JObject(
            List(
              JField("type", JString("prototype")),
              JField("key", JString(key))
            ) ++ metadataToFields(metadata)
          )
        case (OptionalModel(model, prototype, metadata)) =>
          val optionalInfo: JValue = JObject(
            JField("optional", JBool(true)),
            JField("optionalPrototype", prototype.map(p => serialize(format)(p)).getOrElse(JNothing))
          )

          val typeAndValue = model.map(serialize(format)(_)).getOrElse(emptyObject)
          typeAndValue.merge(optionalInfo).merge(JObject(metadataToFields(metadata)))

        case (ListModel(items, prototype, metadata)) =>
          JObject(List(
            JField("type", JString("array")),
            JField("value", JArray(items.map(item => serialize(format)(item)))),
            JField("arrayPrototype", prototype.map(p => serialize(format)(p)).getOrElse(JNothing))
          ) ++ metadataToFields(metadata))

        case (EnumeratedModel(value, alternatives, path, metadata)) =>
          JObject(List(
            JField("type", JString("enum")),
            JField("alternatives", alternatives.map(alts => JArray(alts.map(serializeEnumValue))).getOrElse(JNothing)),
            JField("alternativesPath", path.map(JString(_)).getOrElse(JNothing)),
            JField("value", value.map(serializeEnumValue).getOrElse(JNothing))
          ) ++ metadataToFields(metadata))
        case (OneOfModel(c, model, prototypes, metadata)) =>
          val oneOfInfo = JObject(
            JField("oneOfClass", JString(c)),
            JField("oneOfPrototypes", JArray(prototypes.map(p => serialize(format)(p))))
          )
          serialize(format)(model).merge(oneOfInfo).merge(JObject(metadataToFields(metadata)))
        case (NumberModel(value, metadata)) => serializeValueModel("number", value, metadata)
        case (BooleanModel(value, metadata)) => serializeValueModel("boolean", value, metadata)
        case (DateModel(value, metadata)) => serializeValueModel("date", value, metadata)
        case (DateTimeModel(value, metadata)) => serializeValueModel("date", value, metadata)
        case (StringModel(value, metadata)) => serializeValueModel("string", value, metadata)
        case _ => throw new RuntimeException("No match : " + model)
      }
    }
  }

  private def metadataToFields(metadata: List[Metadata]): List[JField] = {
    val onlyWhen = metadata.collect {
      case o: OnlyWhen => serializeOnlyWhen(o)
    } match {
      case Nil => Nil
      case conditions => List(JField("onlyWhen", JArray(conditions)))
    }

    metadata.collect {
      case MinItems(x) => JField("minItems", JInt(x))
      case MaxItems(x) => JField("maxItems", JInt(x))
      case MinValue(x) => JField("minValue", JDouble(x))
      case MaxValue(x) => JField("maxValue", JDouble(x))
      case MinValueExclusive(x) => JField("minValueExclusive", JDouble(x))
      case MaxValueExclusive(x) => JField("maxValueExclusive", JDouble(x))
      case MultiLineString(x) => JField("maxLines", JInt(x))
      case Scale(x) => JField("scale", JInt(x))
      case UnitOfMeasure(x) => JField("unitOfMeasure", JString(x))
      case InfoDescription(x) => JField("infoDescription", JString(x))
      case InfoLinkUrl(x) => JField("infoLinkUrl", JString(x))
      case InfoLinkTitle(x) => JField("infoLinkTitle", JString(x))
      case RegularExpression(x) => JField("regularExpression", JString(x))
      case Example(x) => JField("example", JString(x))
    } ++ onlyWhen
  }

  private def metadataToObject(metadata: List[Metadata]) = JObject(metadataToFields(metadata))

  private def flagsToFields(props: Map[String, JValue]) = props.toList.map{ case (key, value) => JField(key, value) }

  private def descriptionToField(description: List[String]): List[JField] = description match {
    case Nil =>
      Nil
    case descriptions =>
      List(JField("description", JArray(descriptions.map(JString(_)))))
  }

  private def serializeEnumValue(enumValue: EnumValue)(implicit format: Formats): JValue = JsonSerializer.serializeWithRoot(enumValue)

  private def serializeValueModel(tyep: String, value: ValueWithData[_], metadata: List[Metadata])(implicit format: Formats) = JObject(List(
    JField("type", JString(tyep)),
    JField("value", Extraction.decompose(value))
  ) ++ metadataToFields(metadata))

  private def emptyObject = JObject()
}
