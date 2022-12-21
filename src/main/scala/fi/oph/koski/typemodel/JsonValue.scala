package fi.oph.koski.typemodel

import org.json4s.{JArray, JBool, JDouble, JField, JNothing, JObject, JString, JValue}

object JsonValue {
  def valueToJson(value: Any): JValue = value match {
    case a: Double => JDouble(a)
    case a: String => JString(a)
    case a: Boolean => JBool(a)

    case obj: Map[_, _] => JObject(
      obj.toList.flatMap {
        case (_, None) => None
        case (_, ObjectType.ObjectDefaultsProperty(None)) => None
        case (key: String, value: Any) => Some(JField(key, valueToJson(value)))
      })

    case arr: Seq[_] => JArray(arr.map(valueToJson).toList)

    case obj: ObjectType.ObjectDefaultsMap => valueToJson(obj.properties)
    case ObjectType.ObjectDefaultsProperty(default) => valueToJson(default)

    case _ => JNothing
  }
}
