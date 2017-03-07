package fi.oph.koski.editor

import java.time.LocalDate

import fi.oph.koski.editor.ModelBuilder._
import fi.oph.koski.json.Json
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.localization.{Localizable, LocalizedString}
import fi.oph.koski.schema._
import fi.oph.koski.todistus.LocalizedHtml
import fi.oph.koski.validation.ValidationAndResolvingContext
import fi.oph.scalaschema._
import fi.oph.scalaschema.annotation.Title

case class EditorModelBuilder(context: ValidationAndResolvingContext, mainSchema: ClassSchema,
                              editable: Boolean = true, root: Boolean = true, private var prototypesRequested: Set[SchemaWithClassName] = Set.empty, private val prototypesBeingCreated: Set[SchemaWithClassName] = Set.empty)
                             (implicit val user: KoskiSession) extends LocalizedHtml {

  def buildModel(schema: ClassSchema, value: AnyRef): EditorModel = {
    ObjectModelBuilder(schema, true).buildObjectModel(value)
  }

  def koodistoEnumValue(k: Koodistokoodiviite): EnumValue = EnumValue(k.koodiarvo, i(k.description), k)

  def organisaatioEnumValue(o: OrganisaatioWithOid) = EnumValue(o.oid, i(o.description), o)

  private def buildModel(obj: Any, schema: Schema, includeData: Boolean): EditorModel = (obj, schema) match {
    case (o: AnyRef, t: SchemaWithClassName) => ModelBuilder.getModelBuilder(t, includeData).buildObjectModel(o)
    case (xs: Iterable[_], t: ListSchema) => ListModel(xs.toList.map(item => buildModel(item, t.itemSchema, false)), Prototypes.getPrototypePlaceholder(t.itemSchema))
    case (x: Option[_], t: OptionalSchema) => OptionalModel(x.map(value => buildModel(value, t.itemSchema, includeData)), Prototypes.getPrototypePlaceholder(t.itemSchema))
    case (x: AnyRef, t: OptionalSchema) => OptionalModel(Some(buildModel(x, t.itemSchema, includeData)), Prototypes.getPrototypePlaceholder(t.itemSchema))
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
          Some(ModelBuilder.getModelBuilder(s, true).buildPrototypePlaceholder)
        case s: ListSchema => getPrototypePlaceholder(s.itemSchema).map(prototypeModel => ListModel(List(prototypeModel), Some(prototypeModel)))
        case s: OptionalSchema => getPrototypePlaceholder(s.itemSchema)
        case _ => Some(buildModel(Prototypes.getPrototypeData(schema), schema, true))
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
    def getModelBuilder(t: SchemaWithClassName, includeData: Boolean): ModelBuilder = t match {
      case t: ClassSchema =>
        Class.forName(t.fullClassName) match {
          case c if classOf[Koodistokoodiviite].isAssignableFrom(c) =>
            t.properties.find(_.key == "koodistoUri").get.schema.asInstanceOf[StringSchema].enumValues match {
              case None => throw new RuntimeException("@KoodistoUri -annotaatio puuttuu")
              case Some(enumValues) =>
                val koodistoUri = enumValues.head.asInstanceOf[String]
                val koodiarvot = t.properties.find(_.key == "koodiarvo").get.schema.asInstanceOf[StringSchema].enumValues.map(v => "/" + v.mkString(",")).getOrElse("")
                EnumModelBuilder[Koodistokoodiviite](s"/koski/api/editor/koodit/$koodistoUri$koodiarvot", koodistoEnumValue)
            }

          case c if classOf[Oppilaitos].isAssignableFrom(c) =>
            EnumModelBuilder[Oppilaitos]("/koski/api/editor/oppilaitokset", organisaatioEnumValue(_))

          case c if classOf[OrganisaatioWithOid].isAssignableFrom(c) =>
            EnumModelBuilder[OrganisaatioWithOid]("/koski/api/editor/organisaatiot", organisaatioEnumValue)

          case c =>
            ObjectModelBuilder(t, includeData)
        }
      case t: ClassRefSchema => ModelBuilder.getModelBuilder(mainSchema.getSchema(t.fullClassName).get, includeData)
      case t: AnyOfSchema => AnyOfModelBuilder(t, includeData)
    }
  }

  private case class AnyOfModelBuilder(t: AnyOfSchema, includeData: Boolean) extends ModelBuilder {
    def buildObjectModel(obj: AnyRef) = obj match {
      case None =>
        OneOfModel(sanitizeName(t.simpleName), None, t.alternatives.flatMap(Prototypes.getPrototypePlaceholder(_)))
      case x: AnyRef =>
        OneOfModel(sanitizeName(t.simpleName), Some(buildModel(x, findOneOfSchema(t, x), includeData)), t.alternatives.flatMap(Prototypes.getPrototypePlaceholder(_)))
    }

    def prototypeKey = sanitizeName(t.simpleName)

    private def findOneOfSchema(t: AnyOfSchema, obj: AnyRef): Schema = {
      t.alternatives.find { classType =>
        classType.fullClassName == obj.getClass.getName
      }.get
    }
  }

  private case class EnumModelBuilder[A](alternativesPath: String, toEnumValue: A => EnumValue) extends ModelBuilder {
    def prototypeKey = sanitizeName(alternativesPath)

    def buildObjectModel(o: AnyRef) = {
      o match {
        case None => EnumeratedModel(None, None, Some(alternativesPath))
        case k: A => EnumeratedModel(Some(toEnumValue(k)), None, Some(alternativesPath))
      }
    }
  }

  private case class ObjectModelBuilder(schema: ClassSchema, includeData: Boolean) extends ModelBuilder {
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
            val flatten: Boolean = property.metadata.contains(Flatten())
            val complexObject: Boolean = property.metadata.contains(ComplexObject())
            val tabular: Boolean = property.metadata.contains(Tabular())
            val readOnly: Boolean = property.metadata.find(_.isInstanceOf[ReadOnly]).isDefined
            val value = obj match {
              case None => Prototypes.getPrototypeData(property.schema) // Object has no value (means optional property with missing value) -> get prototypal value for the property
              case _ => schema.getPropertyValue(property, obj)
            }
            val propertyTitle = property.metadata.flatMap {
              case Title(t) => Some(t)
              case _ => None
            }.headOption.getOrElse(property.key.split("(?=\\p{Lu})").map(_.toLowerCase).mkString(" ").replaceAll("_ ", "-").capitalize)
            Some(EditorProperty(property.key, propertyTitle, objectContext.buildModel(value, property.schema, false), hidden, representative, flatten, complexObject, tabular, !readOnly))
          } else {
            None // missing values are skipped when not editable
          }
        }
        val objectTitle = obj match {
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
              val prototypeKey: String = ModelBuilder.getModelBuilder(schema, true).prototypeKey
              var prototypeData: Any = None
              if (prototypeKey == "perusopetuksenopiskeluoikeudenlisatiedot") {
                prototypeData = PerusopetuksenOpiskeluoikeudenLisätiedot() // TODO: how to do this properly?
              } else if (prototypeKey == "perusopetuksenkayttaytymisenarviointi") {
                prototypeData = PerusopetuksenKäyttäytymisenArviointi()
              } else if (prototypeKey == "localizedstring" || prototypeKey == "finnish") {
                prototypeData = LocalizedString.finnish("-")
              } else if (prototypeKey == "-koski-api-editor-koodit-ammatillisentutkinnonsuoritustapa") {
                prototypeData = Koodistokoodiviite("ops", "ammatillisentutkinnonsuoritustapa")
              }
              val model: EditorModel = helperContext.buildModel(prototypeData, schema, includeData = true)
              if (model.isInstanceOf[PrototypeModel]) {
                throw new IllegalStateException()
              }
              val newRequestsForThisCreation = helperContext.prototypesRequested -- prototypesRequested
              newRequests ++= newRequestsForThisCreation
              prototypesRequested ++= newRequestsForThisCreation
              prototypesCreated += (prototypeKey -> model)
            }
          } while (newRequests.nonEmpty)
          prototypesCreated
        } else {
          Map.empty
        }
        ObjectModel(classes(schema.fullClassName), properties, data, objectTitle, objectContext.editable, includedPrototypes)
      }
    }

    import scala.reflect.runtime.{universe => ru}

    private def classes(className: String) = {
      val tpe = typeByName(className)
      (tpe :: findTraits(tpe)).map(t => sanitizeName(Class.forName(t.typeSymbol.fullName).getSimpleName))
    }

    private def findTraits(tpe: ru.Type) = {
      tpe.baseClasses
        .map(_.fullName)
        .filter(_.startsWith("fi.oph.koski"))
        .map {typeByName(_)}
        .filter {_.typeSymbol.asClass.isTrait}
        .filterNot {_ == tpe}
        .distinct
    }

    private def typeByName(className: String): ru.Type = {
      reflect.runtime.currentMirror.classSymbol(Class.forName(className)).toType
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
  def sanitizeName(s: String) = s.toLowerCase.replaceAll("ä", "a").replaceAll("ö", "o").replaceAll("/", "-")
}