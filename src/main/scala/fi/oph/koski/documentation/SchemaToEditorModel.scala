package fi.oph.koski.documentation

import java.time.LocalDate

import fi.oph.koski.editor._
import fi.oph.koski.koskiuser.KoskiUser
import fi.oph.koski.localization.Localizable
import fi.oph.koski.schema.Hidden
import fi.oph.koski.todistus.LocalizedHtml
import fi.oph.scalaschema._

class SchemaToEditorModel(implicit val user: KoskiUser) extends LocalizedHtml {
  def buildModel(schema: ClassSchema, value: AnyRef): EditorModel = {
    buildObjectModel(schema, value, schema, root = true)
  }

  private def buildModel(obj: Any, schema: Schema, mainSchema: ClassSchema): EditorModel = (obj, schema) match {
    case (o: Localizable, t:ClassSchema)  => EnumeratedModel(EnumValue(i(o), o))
    case (o: AnyRef, t:ClassSchema) => buildObjectModel(t, o, mainSchema)
    case (xs: Iterable[_], t:ListSchema) => ListModel(xs.toList.map(item => buildModel(item, t.itemSchema, mainSchema)))
    case (x: Number, t:NumberSchema) => NumberModel(x)
    case (x: Boolean, t:BooleanSchema) => BooleanModel(x)
    case (x: LocalDate, t:DateSchema) => DateModel(x)
    case (x: String, t:StringSchema) => StringModel(x)
    case (x: Option[_], t:OptionalSchema) => OptionalModel(x.map(value => buildModel(value, t.itemSchema, mainSchema)))
    case (x: AnyRef, t:OptionalSchema) => OptionalModel(Some(buildModel(x, t.itemSchema, mainSchema)))
    case (x: AnyRef, t:AnyOfSchema) => OneOfModel(t.simpleName, buildModel(x, findOneOfSchema(t, x), mainSchema))
    case (x: AnyRef, t:ClassRefSchema) => buildModel(x, mainSchema.getSchema(t.fullClassName).get, mainSchema)
    case _ =>
      throw new RuntimeException("Unexpected input: " + obj + ", " + schema)
  }

  private def buildObjectModel(schema: ClassSchema, obj: AnyRef, mainSchema: ClassSchema, root: Boolean = false) = {
    val properties: List[EditorProperty] = schema.properties.map { property =>
      val hidden = property.metadata.contains(Hidden())
      val value = schema.getPropertyValue(property, obj)
      EditorProperty(property. key, property.key, buildModel(value, property.schema, mainSchema), hidden)
    }
    ObjectModel(schema.simpleName, properties, if (root) { Some(obj) } else { None })
  }

  private def findOneOfSchema(t: AnyOfSchema, obj: AnyRef): Schema = {
    t.alternatives.find { classType =>
      classType.fullClassName == obj.getClass.getName
    }.get
  }
}

