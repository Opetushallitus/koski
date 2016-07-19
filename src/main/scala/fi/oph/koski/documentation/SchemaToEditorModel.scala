package fi.oph.koski.documentation

import java.time.LocalDate

import fi.oph.koski.editor._
import fi.oph.koski.koski.ValidationAndResolvingContext
import fi.oph.koski.koskiuser.{AccessType, KoskiUser}
import fi.oph.koski.localization.{Localizable, LocalizedString}
import fi.oph.koski.schema._
import fi.oph.koski.todistus.LocalizedHtml
import fi.oph.scalaschema._

class SchemaToEditorModel(context: ValidationAndResolvingContext)(implicit val user: KoskiUser) extends LocalizedHtml {
  def buildModel(schema: ClassSchema, value: AnyRef): EditorModel = {
    buildObjectModel(value, schema, schema, root = true)
  }

  private val KoodistokoodiviiteClass = classOf[Koodistokoodiviite]

  private def buildModel(obj: Any, schema: Schema, mainSchema: ClassSchema): EditorModel = (obj, schema) match {
    case (o: AnyRef, t:ClassSchema) => Class.forName(t.fullClassName) match {
      case c if (classOf[Koodistokoodiviite].isAssignableFrom(c)) =>
        val koodistoUri = t.properties.find(_.key == "koodistoUri").get.schema.asInstanceOf[StringSchema].enumValues.get.apply(0).asInstanceOf[String]
        def toEnumValue(k: Koodistokoodiviite) = EnumValue(k.koodiarvo, i(k.lyhytNimi.orElse(k.nimi).getOrElse(LocalizedString.unlocalized(k.koodiarvo))), k)
        val alternatives = context.koodistoPalvelu.getLatestVersion(koodistoUri).toList.flatMap(context.koodistoPalvelu.getKoodistoKoodiViitteet(_)).flatten.map(toEnumValue(_))

        o match {
          case k: Koodistokoodiviite => EnumeratedModel(Some(toEnumValue(k)), alternatives)
          case None => EnumeratedModel(None, alternatives)
        }

      case c if (classOf[OrganisaatioWithOid].isAssignableFrom(c)) =>
        def toEnumValue(o: OrganisaatioWithOid) = EnumValue(o.oid, i(o.description), o)
        val organisaatiot = user.organisationOids(AccessType.read).flatMap(context.organisaatioRepository.getOrganisaatio).map(toEnumValue).toList
        o match {
          case o: OrganisaatioWithOid => EnumeratedModel(Some(toEnumValue(o)), organisaatiot)
          case None => EnumeratedModel(None, organisaatiot)
        }

      case c if (classOf[Koulutusmoduuli].isAssignableFrom(c)) =>
        buildObjectModel(o, t, mainSchema, true) // object data should probably be sent only for root and split on the client side

      case c =>
        buildObjectModel(o, t, mainSchema)
    }
    case (xs: Iterable[_], t:ListSchema) => ListModel(xs.toList.map(item => buildModel(item, t.itemSchema, mainSchema)), getPrototypeModel(t.itemSchema, mainSchema))
    case (x: Number, t:NumberSchema) => NumberModel(x)
    case (x: Boolean, t:BooleanSchema) => BooleanModel(x)
    case (x: LocalDate, t:DateSchema) => DateModel(x)
    case (x: String, t:StringSchema) => StringModel(x)
    case (x: Option[_], t:OptionalSchema) => OptionalModel(x.map(value => buildModel(value, t.itemSchema, mainSchema)), getPrototypeModel(t.itemSchema, mainSchema))
    case (x: AnyRef, t:OptionalSchema) => OptionalModel(Some(buildModel(x, t.itemSchema, mainSchema)), getPrototypeModel(t.itemSchema, mainSchema))
    case (None, t: AnyOfSchema) => OneOfModel(t.simpleName, None)
    case (x: AnyRef, t:AnyOfSchema) => OneOfModel(t.simpleName, Some(buildModel(x, findOneOfSchema(t, x), mainSchema)))
    case (x: AnyRef, t:ClassRefSchema) => buildModel(x, mainSchema.getSchema(t.fullClassName).get, mainSchema)
    case _ =>
      throw new RuntimeException("Unexpected input: " + obj + ", " + schema)
  }

  private def getPrototypeModel(schema: Schema, mainSchema: ClassSchema): EditorModel = buildModel(getPrototype(schema, mainSchema), schema, mainSchema)

  private def getPrototype(schema: Schema, mainSchema: ClassSchema): Any = schema match {
    case s: ClassSchema => None
    case s: ClassRefSchema => None
    case s: ListSchema => List(getPrototype(s.itemSchema, mainSchema))
    case s: OptionalSchema => getPrototype(s.itemSchema, mainSchema)
    case s: NumberSchema => 0
    case s: StringSchema => ""
    case s: BooleanSchema => false
    case s: DateSchema => LocalDate.now
    case s: AnyOfSchema => getPrototype(s.alternatives.head, mainSchema)
    case _ =>
      throw new RuntimeException("Cannot create prototype for: " + schema)
  }

  private def buildObjectModel(obj: AnyRef, schema: ClassSchema, mainSchema: ClassSchema, root: Boolean = false) = {
    val properties: List[EditorProperty] = schema.properties.map { property =>
      val hidden = property.metadata.contains(Hidden())
      val representative: Boolean = property.metadata.contains(Representative())
      val value = obj match {
        case None => getPrototype(property.schema, mainSchema)
        case _ => schema.getPropertyValue(property, obj)
      }
      val title = property.metadata.flatMap {
        case Title(t) => Some(t)
        case _ => None
      }.headOption.getOrElse(property.key.split("(?=\\p{Lu})").map(_.toLowerCase).mkString(" ").replaceAll("_ ", "-").capitalize)
      EditorProperty(property.key, title, buildModel(value, property.schema, mainSchema), hidden, representative)
    }
    val title = obj match {
      case o: Localizable => Some(i(o.description))
      case _ => None
    }
    ObjectModel(schema.simpleName, properties, if (root) { Some(obj) } else { None }, title)
  }

  private def findOneOfSchema(t: AnyOfSchema, obj: AnyRef): Schema = {
    t.alternatives.find { classType =>
      classType.fullClassName == obj.getClass.getName
    }.get
  }
}

