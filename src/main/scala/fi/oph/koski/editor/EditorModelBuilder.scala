package fi.oph.koski.editor

import java.time.LocalDate

import fi.oph.koski.koski.ValidationAndResolvingContext
import fi.oph.koski.koskiuser.KoskiUser
import fi.oph.koski.localization.{Localizable, LocalizedString}
import fi.oph.koski.schema._
import fi.oph.koski.todistus.LocalizedHtml
import fi.oph.scalaschema._
import ModelBuilder._

case class EditorModelBuilder(context: ValidationAndResolvingContext, mainSchema: ClassSchema,
                              editable: Boolean = true, root: Boolean = true, private var prototypesRequested: Set[SchemaWithClassName] = Set.empty, private val prototypesBeingCreated: Set[SchemaWithClassName] = Set.empty)
                             (implicit val user: KoskiUser) extends LocalizedHtml {

  def buildModel(schema: ClassSchema, value: AnyRef): EditorModel = {
    ObjectModelBuilder(schema, true).buildObjectModel(value)
  }

  def koodistoEnumValue(k: Koodistokoodiviite): EnumValue = EnumValue(k.koodiarvo, i(k.lyhytNimi.orElse(k.nimi).getOrElse(LocalizedString.unlocalized(k.koodiarvo))), k)

  def organisaatioEnumValue(o: OrganisaatioWithOid) = EnumValue(o.oid, i(o.description), o)

  private def buildModel(obj: Any, schema: Schema): EditorModel = (obj, schema) match {
    case (o: AnyRef, t: SchemaWithClassName) => ModelBuilder(t).buildObjectModel(o)
    case (xs: Iterable[_], t: ListSchema) => ListModel(xs.toList.map(item => buildModel(item, t.itemSchema)), Prototypes.getPrototypePlaceholder(t.itemSchema))
    case (x: Option[_], t: OptionalSchema) => OptionalModel(x.map(value => buildModel(value, t.itemSchema)), Prototypes.getPrototypePlaceholder(t.itemSchema))
    case (x: AnyRef, t: OptionalSchema) => OptionalModel(Some(buildModel(x, t.itemSchema)), Prototypes.getPrototypePlaceholder(t.itemSchema))
    case (x: Number, t: NumberSchema) => NumberModel(x)
    case (x: Boolean, t: BooleanSchema) => BooleanModel(x)
    case (x: LocalDate, t: DateSchema) => DateModel(x)
    case (x: String, t: StringSchema) => StringModel(x)
    case _ =>
      throw new RuntimeException("Unexpected input: " + obj + ", " + schema)
  }

  private object Prototypes {
    def getPrototypePlaceholder(schema: Schema): Option[EditorModel] = if (editable) {
      def resolveSchema(schema: SchemaWithClassName): SchemaWithClassName = schema match {
        case s: ClassRefSchema => mainSchema.getSchema(s.fullClassName).get
        case _ => schema
      }

      schema match {
        case s: SchemaWithClassName =>
          val classRefSchema = resolveSchema(s)
          prototypesRequested += classRefSchema
          Some(ModelBuilder(s).buildPrototypePlaceholder)
        case s: ListSchema => getPrototypePlaceholder(s.itemSchema)
        case s: OptionalSchema => getPrototypePlaceholder(s.itemSchema)
        case _ => Some(buildModel(Prototypes.getPrototypeData(schema), schema))
      }
    } else {
      None
    }

    def getPrototypeData(schema: Schema): Any = schema match {
      case s: ClassSchema => None
      case s: ClassRefSchema => None
      case s: AnyOfSchema => None
      case s: ListSchema => List(getPrototypeData(s.itemSchema))
      case s: OptionalSchema => getPrototypeData(s.itemSchema)
      case s: NumberSchema => 0
      case s: StringSchema => ""
      case s: BooleanSchema => false
      case s: DateSchema => LocalDate.now
      case _ =>
        throw new RuntimeException("Cannot create prototype for: " + schema)
    }
  }

  private trait ModelBuilder {
    def buildObjectModel(obj: AnyRef): EditorModel

    def prototypeKey: String

    def buildPrototypePlaceholder = PrototypeModel(prototypeKey)
  }

  private object ModelBuilder {
    def apply(t: SchemaWithClassName): ModelBuilder = t match {
      case t: ClassSchema =>
        Class.forName(t.fullClassName) match {
          case c if (classOf[Koodistokoodiviite].isAssignableFrom(c)) =>
            val koodistoUri = t.properties.find(_.key == "koodistoUri").get.schema.asInstanceOf[StringSchema].enumValues.get.apply(0).asInstanceOf[String]
            // TODO: rajaus @KoodistoKoodiarvo
            EnumModelBuilder[Koodistokoodiviite](s"/koski/api/editor/koodit/$koodistoUri", koodistoEnumValue(_))

          case c if (classOf[Oppilaitos].isAssignableFrom(c)) =>
            EnumModelBuilder[Oppilaitos]("/koski/api/editor/oppilaitokset", organisaatioEnumValue(_))

          case c if (classOf[OrganisaatioWithOid].isAssignableFrom(c)) =>
            EnumModelBuilder[OrganisaatioWithOid]("/koski/api/editor/organisaatiot", organisaatioEnumValue(_))

          case c =>
            ObjectModelBuilder(t)
        }
      case t: ClassRefSchema => ModelBuilder(mainSchema.getSchema(t.fullClassName).get)
      case t: AnyOfSchema => AnyOfModelBuilder(t)
    }
  }

  private case class AnyOfModelBuilder(t: AnyOfSchema) extends ModelBuilder {
    def buildObjectModel(obj: AnyRef) = obj match {
      case None => OneOfModel(sanitizeName(t.simpleName), None, t.alternatives.flatMap(Prototypes.getPrototypePlaceholder(_)))
      case x: AnyRef => OneOfModel(sanitizeName(t.simpleName), Some(buildModel(x, findOneOfSchema(t, x))), t.alternatives.flatMap(Prototypes.getPrototypePlaceholder(_)))
    }

    def prototypeKey = sanitizeName(t.simpleName)

    private def findOneOfSchema(t: AnyOfSchema, obj: AnyRef): Schema = {
      t.alternatives.find { classType =>
        classType.fullClassName == obj.getClass.getName
      }.get
    }
  }

  private case class EnumModelBuilder[A](alternativesPath: String, toEnumValue: A => EnumValue) extends ModelBuilder {
    /*
    private def getEnumeratedModel[A](o: Any, fetchAlternatives: => List[A], toEnumValue: A => EnumValue): EnumeratedModel = {
      val alternatives = if (editable) {
        Some(fetchAlternatives.map(toEnumValue(_)))
      } else {
        None
      }
      o match {
        case None => EnumeratedModel(None, alternatives, None)
        case k: A => EnumeratedModel(Some(toEnumValue(k)), alternatives, None)
      }
    }
    */

    def prototypeKey = sanitizeName(alternativesPath)

    def buildObjectModel(o: AnyRef) = {
      o match {
        case None => EnumeratedModel(None, None, Some(alternativesPath))
        case k: A => EnumeratedModel(Some(toEnumValue(k)), None, Some(alternativesPath))
      }
    }
  }

  private case class ObjectModelBuilder(schema: ClassSchema, includeData: Boolean = false) extends ModelBuilder {
    def buildObjectModel(obj: AnyRef) = {
      if (obj == None && !prototypesBeingCreated.contains(schema) && (prototypesRequested.contains(schema))) {
        buildPrototypePlaceholder // creating a prototype which already exists or has been requested
      } else {
        if (obj == None) {
          // break possible infinite recursion
          prototypesRequested += schema
        }
        val objectContext = newContext(obj)
        val properties: List[EditorProperty] = schema.properties.flatMap { property =>
          val hidden = property.metadata.contains(Hidden())
          val hasValue = obj != None
          if (objectContext.editable || hasValue) {
            val representative: Boolean = property.metadata.contains(Representative())
            val value = obj match {
              case None => Prototypes.getPrototypeData(property.schema)
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
        prototypesRequested = prototypesRequested ++ objectContext.prototypesRequested
        var newRequests: Set[SchemaWithClassName] = prototypesRequested
        val includedPrototypes: Map[String, EditorModel] = if (root) {
          var prototypesCreated: Map[String, EditorModel] = Map.empty
          do {
            val requestsFromPreviousRound = newRequests
            newRequests = Set.empty
            requestsFromPreviousRound.foreach { schema =>
              val helperContext = EditorModelBuilder.this.copy(root = false, prototypesBeingCreated = Set(schema))
              val model: EditorModel = helperContext.buildModel(None, schema)
              if (model.isInstanceOf[PrototypeModel]) {
                throw new IllegalStateException()
              }
              val newRequestsForThisCreation = helperContext.prototypesRequested -- prototypesRequested
              newRequests ++= newRequestsForThisCreation
              prototypesRequested ++= newRequestsForThisCreation
              prototypesCreated += (ModelBuilder(schema).prototypeKey -> model)
            }
          } while (newRequests.nonEmpty)
          prototypesCreated
        } else {
          Map.empty
        }
        ObjectModel(sanitizeName(schema.simpleName), properties, data, title, objectContext.editable, includedPrototypes)
      }
    }

    def prototypeKey = sanitizeName(schema.simpleName)

    private def newContext(obj: AnyRef): EditorModelBuilder = {
      def orgAccess = obj match {
        case o: OrganisaatioonLiittyvä => user.hasWriteAccess(o.omistajaOrganisaatio.oid)
        case _ => true
      }
      def lähdejärjestelmäAccess = obj match {
        case o: Lähdejärjestelmällinen => o.lähdejärjestelmänId == None
        case _ => true
      }
      EditorModelBuilder.this.copy(editable = editable && lähdejärjestelmäAccess && orgAccess, root = false, prototypesBeingCreated = Set.empty)
    }
  }
}

object ModelBuilder {
  def sanitizeName(s: String) = s.replaceAll("ä", "a").replaceAll("ö", "o").replaceAll("/", "-")
}