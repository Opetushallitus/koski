package fi.oph.koski.documentation

import java.time.LocalDate

import fi.oph.koski.editor._
import fi.oph.koski.koski.ValidationAndResolvingContext
import fi.oph.koski.koskiuser.{AccessType, KoskiUser}
import fi.oph.koski.localization.{Localizable, LocalizedString}
import fi.oph.koski.schema._
import fi.oph.koski.todistus.LocalizedHtml
import fi.oph.scalaschema._

class SchemaToEditorModel(context: ValidationAndResolvingContext, mainSchema: ClassSchema)(implicit val user: KoskiUser) extends LocalizedHtml {
  def buildModel(schema: ClassSchema, value: AnyRef): EditorModel = {
    Context().buildObjectModel(value, schema, true)
  }

  private case class Context(editable: Boolean = true) {
    private def buildModel(obj: Any, schema: Schema): EditorModel = (obj, schema) match {
      case (o: AnyRef, t:ClassSchema) => Class.forName(t.fullClassName) match {
        case c if (classOf[Koodistokoodiviite].isAssignableFrom(c)) =>
          lazy val koodistoUri = t.properties.find(_.key == "koodistoUri").get.schema.asInstanceOf[StringSchema].enumValues.get.apply(0).asInstanceOf[String]
          def koodit: List[Koodistokoodiviite] = context.koodistoPalvelu.getLatestVersion(koodistoUri).toList.flatMap(context.koodistoPalvelu.getKoodistoKoodiViitteet(_)).flatten
          def toEnumValue(k: Koodistokoodiviite): EnumValue = EnumValue(k.koodiarvo, i(k.lyhytNimi.orElse(k.nimi).getOrElse(LocalizedString.unlocalized(k.koodiarvo))), k)
          getEnumeratedModel[Koodistokoodiviite](o, koodit, toEnumValue)

        case c if (classOf[OrganisaatioWithOid].isAssignableFrom(c)) =>
          def toEnumValue(o: OrganisaatioWithOid) = EnumValue(o.oid, i(o.description), o)
          def organisaatiot = user.organisationOids(AccessType.read).flatMap(context.organisaatioRepository.getOrganisaatio).toList
          getEnumeratedModel(o, organisaatiot, toEnumValue)

        case c if (classOf[Koulutusmoduuli].isAssignableFrom(c)) =>
          buildObjectModel(o, t, true) // object data should probably be sent only for root and split on the client side

        case c =>
          buildObjectModel(o, t)
      }
      case (xs: Iterable[_], t:ListSchema) => ListModel(xs.toList.map(item => buildModel(item, t.itemSchema)), getPrototypeModel(t.itemSchema))
      case (x: Number, t:NumberSchema) => NumberModel(x)
      case (x: Boolean, t:BooleanSchema) => BooleanModel(x)
      case (x: LocalDate, t:DateSchema) => DateModel(x)
      case (x: String, t:StringSchema) => StringModel(x)
      case (x: Option[_], t:OptionalSchema) => OptionalModel(x.map(value => buildModel(value, t.itemSchema)), getPrototypeModel(t.itemSchema))
      case (x: AnyRef, t:OptionalSchema) => OptionalModel(Some(buildModel(x, t.itemSchema)), getPrototypeModel(t.itemSchema))
      case (None, t: AnyOfSchema) => OneOfModel(t.simpleName, None)
      case (x: AnyRef, t:AnyOfSchema) => OneOfModel(t.simpleName, Some(buildModel(x, findOneOfSchema(t, x))))
      case (x: AnyRef, t:ClassRefSchema) => buildModel(x, mainSchema.getSchema(t.fullClassName).get)
      case _ =>
        throw new RuntimeException("Unexpected input: " + obj + ", " + schema)
    }

    def buildObjectModel(obj: AnyRef, schema: ClassSchema, includeData: Boolean = false) = {
      val objectContext = newContext(obj)
      val properties: List[EditorProperty] = schema.properties.flatMap { property =>
        val hidden = property.metadata.contains(Hidden())
        val hasValue = obj != None
        if (objectContext.editable || hasValue) {
          if (obj == None) {
            println("property " + property.key + "=" + obj)
          }
          val representative: Boolean = property.metadata.contains(Representative())
          val value = obj match {
            case None => getPrototype(property.schema)
            case _ => schema.getPropertyValue(property, obj)
          }
          val title = property.metadata.flatMap {
            case Title(t) => Some(t)
            case _ => None
          }.headOption.getOrElse(property.key.split("(?=\\p{Lu})").map(_.toLowerCase).mkString(" ").replaceAll("_ ", "-").capitalize)
          Some(EditorProperty(property.key, title, objectContext.buildModel(value, property.schema), hidden, representative))
        } else {
          None // missing values are skipped when not editable
        }
      }
      val title = obj match {
        case o: Localizable => Some(i(o.description))
        case _ => None
      }
      val data: Option[AnyRef] = if (includeData) {
        Some(obj)
      } else {
        None
      }
      ObjectModel(schema.simpleName, properties, data, title, objectContext.editable)
    }

    private def newContext(obj: AnyRef): Context = {
      def orgAccess = obj match {
        case o: OrganisaatioonLiittyvä => user.hasWriteAccess(o.omistajaOrganisaatio.oid)
        case _ => true
      }
      def lähdejärjestelmäAccess = obj match {
        case o: Lähdejärjestelmällinen => o.lähdejärjestelmänId == None
        case _ => true
      }
      this.copy(editable = this.editable && lähdejärjestelmäAccess && orgAccess)
    }

    private def getEnumeratedModel[A](o: Any, fetchAlternatives: => List[A], toEnumValue: A => EnumValue) = {
      val alternatives = if (editable) {
        Some(fetchAlternatives.map(toEnumValue(_)).take(1)) // TODO: hard-coded limit won't do
      } else {
        None
      }
      o match {
        case None => EnumeratedModel(None, alternatives)
        case k: A => EnumeratedModel(Some(toEnumValue(k)), alternatives)
      }
    }

    private def getPrototypeModel(schema: Schema): Option[EditorModel] = if (editable) {
      Some(buildModel(getPrototype(schema), schema))
    } else {
      None
    }
  }

  private def getPrototype(schema: Schema): Any = schema match {
    case s: ClassSchema => None
    case s: ClassRefSchema => None
    case s: ListSchema => List(getPrototype(s.itemSchema))
    case s: OptionalSchema => getPrototype(s.itemSchema)
    case s: NumberSchema => 0
    case s: StringSchema => ""
    case s: BooleanSchema => false
    case s: DateSchema => LocalDate.now
    case s: AnyOfSchema => getPrototype(s.alternatives.head)
    case _ =>
      throw new RuntimeException("Cannot create prototype for: " + schema)
  }

  private def findOneOfSchema(t: AnyOfSchema, obj: AnyRef): Schema = {
    t.alternatives.find { classType =>
      classType.fullClassName == obj.getClass.getName
    }.get
  }
}

