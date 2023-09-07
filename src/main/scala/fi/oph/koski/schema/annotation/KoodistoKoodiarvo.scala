package fi.oph.koski.schema.annotation

import fi.oph.koski.kela.KelaKoodistokoodiviite
import fi.oph.koski.schema.Koodistokoodiviite
import fi.oph.koski.schema.annotation.SchemaClassMapper.mapClasses
import fi.oph.scalaschema._
import org.json4s.JsonAST._

/*
  Vain tietty koodiarvo hyväksytään -annotaatio
 */
case class KoodistoKoodiarvo(arvo: String) extends Metadata {
  private val tuetutKoodiarvoluokat = List(
    classOf[Koodistokoodiviite].getName,
    classOf[KelaKoodistokoodiviite].getName,
  )

  override def appendMetadataToJsonSchema(obj: JObject) = {
    appendToDescription(obj, "(Hyväksytty koodiarvo: " + arvo + ")")
  }

  override def applyMetadata(x: ObjectWithMetadata[_], schemaFactory: SchemaFactory) = {
    super.applyMetadata(mapClasses(x, schemaFactory, { case s: ClassSchema if tuetutKoodiarvoluokat.contains(s.fullClassName)  =>
      s.copy(properties = s.properties.map {
        case p: Property if p.key == "koodiarvo" => addEnumValue(this.arvo, p)
        case p: Property => p
      })
    }), schemaFactory)
  }
}

object SchemaClassMapper {
  def mapClasses(x: ObjectWithMetadata[_], schemaFactory: SchemaFactory, f: PartialFunction[ClassSchema, ClassSchema]): ObjectWithMetadata[_] = x match {
    case property: Property =>
      property.copy(schema = property.schema.mapItems {
        case schema: Schema with ObjectWithMetadata[_] => mapClasses(schema, schemaFactory, f).asInstanceOf[ElementSchema]
        case schema: Schema => schema
      })
    case s: Schema with ObjectWithMetadata[_] =>
      s.mapItems {
        case classy: SchemaWithClassName =>
          classy.resolve(schemaFactory) match {
            case classSchema: ClassSchema =>
              if (f.isDefinedAt(classSchema)) {
                f(classSchema.copy(specialized = true))
              } else {
                val mappedProperties: List[Property] = classSchema.properties.map { property => mapClasses(property, schemaFactory, f).asInstanceOf[Property] }
                classSchema.copy(specialized = true, properties = mappedProperties)
              }
            case x: ElementSchema => x
          }
        case x: ElementSchema => x
      }.asInstanceOf[ObjectWithMetadata[_]]
    case x =>
      x
  }
}

