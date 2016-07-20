package fi.oph.koski.documentation

import java.time.LocalDate

import fi.oph.koski.editor._
import fi.oph.koski.koski.ValidationAndResolvingContext
import fi.oph.koski.koskiuser.KoskiUser
import fi.oph.koski.localization.{Localizable, LocalizedString}
import fi.oph.koski.schema._
import fi.oph.koski.todistus.LocalizedHtml
import fi.oph.scalaschema._

class SchemaToEditorModel(context: ValidationAndResolvingContext, mainSchema: ClassSchema)(implicit val user: KoskiUser) extends LocalizedHtml {
  def buildModel(schema: ClassSchema, value: AnyRef): EditorModel = {
    Context().buildObjectModel(value, schema, true)
  }

  def koodistoEnumValue(k: Koodistokoodiviite): EnumValue = EnumValue(k.koodiarvo, i(k.lyhytNimi.orElse(k.nimi).getOrElse(LocalizedString.unlocalized(k.koodiarvo))), k)
  def organisaatioEnumValue(o: OrganisaatioWithOid) = EnumValue(o.oid, i(o.description), o)

  private case class Context(editable: Boolean = true, root: Boolean = true, var prototypesCreated: Map[String, EditorModel] = Map.empty, var prototypesRequested: Set[SchemaWithClassName] = Set.empty, prototypesBeingCreated: Set[SchemaWithClassName] = Set.empty) {
    private def buildModel(obj: Any, schema: Schema): EditorModel = (obj, schema) match {
      case (o: AnyRef, t:ClassSchema) => Class.forName(t.fullClassName) match {
        case c if (classOf[Koodistokoodiviite].isAssignableFrom(c)) =>
          val koodistoUri = t.properties.find(_.key == "koodistoUri").get.schema.asInstanceOf[StringSchema].enumValues.get.apply(0).asInstanceOf[String]
          // TODO: rajaus @KoodistiKoodiarvo
          getEnumeratedModel[Koodistokoodiviite](o, s"/koski/api/editor/koodit/$koodistoUri", koodistoEnumValue(_))

        case c if (classOf[OrganisaatioWithOid].isAssignableFrom(c)) =>
          getEnumeratedModel(o, "/koski/api/editor/organisaatiot", organisaatioEnumValue(_))

        case c if (classOf[Koulutusmoduuli].isAssignableFrom(c)) =>
          buildObjectModel(o, t, true) // object data should probably be sent only for root and split on the client side

        case c =>
          buildObjectModel(o, t)
      }
      case (xs: Iterable[_], t:ListSchema) => ListModel(xs.toList.map(item => buildModel(item, t.itemSchema)), getPrototypePlaceholder(t.itemSchema))
      case (x: Option[_], t:OptionalSchema) => OptionalModel(x.map(value => buildModel(value, t.itemSchema)), getPrototypePlaceholder(t.itemSchema))
      case (x: AnyRef, t:OptionalSchema) => OptionalModel(Some(buildModel(x, t.itemSchema)), getPrototypePlaceholder(t.itemSchema))
      case (None, t: AnyOfSchema) => OneOfModel(t.simpleName, None, t.alternatives.flatMap(getPrototypePlaceholder(_)))
      case (x: AnyRef, t:AnyOfSchema) => OneOfModel(t.simpleName, Some(buildModel(x, findOneOfSchema(t, x))), t.alternatives.flatMap(getPrototypePlaceholder(_)))
      case (x: Number, t:NumberSchema) => NumberModel(x)
      case (x: Boolean, t:BooleanSchema) => BooleanModel(x)
      case (x: LocalDate, t:DateSchema) => DateModel(x)
      case (x: String, t:StringSchema) => StringModel(x)
      case (x: AnyRef, t:ClassRefSchema) => buildModel(x, mainSchema.getSchema(t.fullClassName).get)
      case _ =>
        throw new RuntimeException("Unexpected input: " + obj + ", " + schema)
    }

    def buildObjectModel(obj: AnyRef, schema: ClassSchema, includeData: Boolean = false) = {
      if (obj == None && !prototypesBeingCreated.contains(schema) && (prototypesRequested.contains(schema) || prototypesCreated.contains(schema.simpleName) )) {
        PrototypeModel(schema.simpleName) // creating a prototype which already exists or has been requested
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
              case None => getPrototypeData(property.schema)
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
          do {
            val requestsFromPreviousRound = newRequests
            newRequests = Set.empty
            val newPrototypesCreated = requestsFromPreviousRound.map { schema =>
              val helperContext = copy(root = false, prototypesBeingCreated = Set(schema) )
              val model: EditorModel = helperContext.buildModel(None, schema)
              if (model.isInstanceOf[PrototypeModel]) {
                throw new IllegalStateException()
              }
              val newRequestsForThisCreation = helperContext.prototypesRequested -- prototypesRequested
              newRequests ++= newRequestsForThisCreation
              prototypesRequested ++= newRequestsForThisCreation
              (schema.simpleName, model)
            }.toMap
            prototypesCreated ++= newPrototypesCreated
          } while(newRequests.nonEmpty)
          prototypesCreated
        } else {
          Map.empty
        }
        ObjectModel(schema.simpleName, properties, data, title, objectContext.editable, includedPrototypes)
      }
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
      this.copy(editable = this.editable && lähdejärjestelmäAccess && orgAccess, root = false, prototypesBeingCreated = Set.empty )
    }

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

    private def getEnumeratedModel[A](o: Any, alternativesPath: String, toEnumValue: A => EnumValue): EnumeratedModel = {
      o match {
        case None => EnumeratedModel(None, None, Some(alternativesPath))
        case k: A => EnumeratedModel(Some(toEnumValue(k)), None, Some(alternativesPath))
      }
    }

    private def getPrototypePlaceholder(schema: Schema): Option[EditorModel] = if (editable) {
      schema match {
        case s: SchemaWithClassName =>
          val classRefSchema = resolveSchema(s)
          if (!prototypesCreated.contains(s.simpleName)) {
            prototypesRequested += classRefSchema
          }
          Some(PrototypeModel(s.simpleName))
        case s: ListSchema => getPrototypePlaceholder(s.itemSchema)
        case s: OptionalSchema => getPrototypePlaceholder(s.itemSchema)
        case _ => Some(buildModel(getPrototypeData(schema), schema))
      }
    } else {
      None
    }
  }

  private def resolveSchema(schema: SchemaWithClassName): SchemaWithClassName = schema match {
    case s: ClassRefSchema => mainSchema.getSchema(s.fullClassName).get
    case _ => schema
  }

  private def getPrototypeData(schema: Schema): Any = schema match {
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

  private def findOneOfSchema(t: AnyOfSchema, obj: AnyRef): Schema = {
    t.alternatives.find { classType =>
      classType.fullClassName == obj.getClass.getName
    }.get
  }
}

