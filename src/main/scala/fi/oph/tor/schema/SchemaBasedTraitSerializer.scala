package fi.oph.tor.schema

import fi.oph.tor.schema.generic.{SchemaWithClassName, ClassSchema, OneOfSchema}
import org.json4s._
import org.json4s.reflect.TypeInfo

class SchemaBasedTraitSerializer(schema: SchemaWithClassName) extends Serializer[AnyRef] {
  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), AnyRef] = {
    case (TypeInfo(t, _), json) if (t.isInterface && schema.getSchema(t.getTypeName).isDefined) => { // <- todo: recognize trait type correctly
      val fullName: String = t.getTypeName
      schema.getSchema(fullName) match {
        case Some(schema: OneOfSchema) =>
          val empty: List[(Class[_], AnyRef)] = Nil
          val found = schema.alternatives.foldLeft(empty) {
            case (found, alternative) =>
              found ++ (try {
                val klass: Class[_] = Class.forName(alternative.fullClassName)
                List((klass, json.extract[AnyRef](format, Manifest.classType(klass))))
              } catch {
                case e: Exception => Nil
              })
          }
          try {
            found.sortBy {
              _._1.getConstructors.toList(0).getParameterTypes.length // choose longest constructor if multiple matches
            }.reverse.map(_._2).headOption match {
              case Some(matching) => matching
              case None => throw new RuntimeException("No matching implementation of " + t.getSimpleName + " found for data " + json)
            }
          } catch {
            case e: Exception =>
              throw e
          }
      }
    }
  }

  def serialize(implicit format: Formats): PartialFunction[Any, JValue] = PartialFunction.empty
}
