package fi.oph.koski.editor

import java.time.LocalDate

import fi.oph.koski.localization.{Localizable, LocalizedString}
import fi.oph.koski.schema._
import fi.oph.scalaschema._
import fi.oph.scalaschema.annotation.Title
import EditorModelBuilder._
import ModelBuilderForClass._
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.todistus.LocalizedHtml

object EditorModelBuilder {
  def buildModel(schema: ClassSchema, value: AnyRef, editable: Boolean)(implicit user: KoskiSession): EditorModel = {
    val context = ModelBuilderContext(schema, editable)
    ObjectModelBuilder(schema, true)(context).buildModelForObject(value)
  }

  def buildModel(obj: Any, schema: Schema, includeData: Boolean)(implicit context: ModelBuilderContext): EditorModel = (obj, schema) match {
    case (o: AnyRef, t: SchemaWithClassName) => modelBuilderForClass(t, includeData).buildModelForObject(o)
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

  def sanitizeName(s: String) = s.toLowerCase.replaceAll("ä", "a").replaceAll("ö", "o").replaceAll("/", "-")

  def organisaatioEnumValue(localization: LocalizedHtml)(o: OrganisaatioWithOid)() = EnumValue(o.oid, localization.i(o.description), o)
  def koodistoEnumValue(localization: LocalizedHtml)(k: Koodistokoodiviite) = EnumValue(k.koodiarvo, localization.i(k.description), k)
}

case class ModelBuilderContext(
  mainSchema: ClassSchema,
  editable: Boolean, root: Boolean = true,
  var prototypesRequested: Set[SchemaWithClassName] = Set.empty,
  prototypesBeingCreated: Set[SchemaWithClassName] = Set.empty)(implicit val user: KoskiSession) extends LocalizedHtml


trait ModelBuilderForClass {
  def buildModelForObject(obj: AnyRef): EditorModel
  def prototypeKey: String
  def buildPrototypePlaceholder = PrototypeModel(prototypeKey)
}

object ModelBuilderForClass {
  def modelBuilderForClass(t: SchemaWithClassName, includeData: Boolean)(implicit context: ModelBuilderContext): ModelBuilderForClass = t match {
    case t: ClassSchema =>
      Class.forName(t.fullClassName) match {
        case c if classOf[Koodistokoodiviite].isAssignableFrom(c) =>
          t.properties.find(_.key == "koodistoUri").get.schema.asInstanceOf[StringSchema].enumValues match {
            case None => throw new RuntimeException("@KoodistoUri -annotaatio puuttuu")
            case Some(enumValues) =>
              val koodistoUri = enumValues.head.asInstanceOf[String]
              val koodiarvot = t.properties.find(_.key == "koodiarvo").get.schema.asInstanceOf[StringSchema].enumValues.map(v => "/" + v.mkString(",")).getOrElse("")
              EnumModelBuilder[Koodistokoodiviite](s"/koski/api/editor/koodit/$koodistoUri$koodiarvot", koodistoEnumValue(context)(_))
          }

        case c if classOf[Oppilaitos].isAssignableFrom(c) =>
          EnumModelBuilder[Oppilaitos]("/koski/api/editor/oppilaitokset", organisaatioEnumValue(context)(_))

        case c if classOf[OrganisaatioWithOid].isAssignableFrom(c) =>
          EnumModelBuilder[OrganisaatioWithOid]("/koski/api/editor/organisaatiot", organisaatioEnumValue(context)(_))

        case c =>
          ObjectModelBuilder(t, includeData)
      }
    case t: ClassRefSchema => modelBuilderForClass(context.mainSchema.getSchema(t.fullClassName).get, includeData)
    case t: AnyOfSchema => AnyOfModelBuilder(t, includeData)
  }
}

case class EnumModelBuilder[A](alternativesPath: String, toEnumValue: A => EnumValue) extends ModelBuilderForClass {
  def prototypeKey = sanitizeName(alternativesPath)

  def buildModelForObject(o: AnyRef) = {
    o match {
      case None => EnumeratedModel(None, None, Some(alternativesPath))
      case k: A => EnumeratedModel(Some(toEnumValue(k)), None, Some(alternativesPath))
    }
  }
}

case class AnyOfModelBuilder(t: AnyOfSchema, includeData: Boolean)(implicit context: ModelBuilderContext) extends ModelBuilderForClass {
  def buildModelForObject(obj: AnyRef) = obj match {
    case None =>
      OneOfModel(sanitizeName(t.simpleName), None, t.alternatives.flatMap(Prototypes.getPrototypePlaceholder(_)))
    case x: AnyRef =>
      OneOfModel(sanitizeName(t.simpleName), Some(EditorModelBuilder.buildModel(x, findOneOfSchema(t, x), includeData)), t.alternatives.flatMap(Prototypes.getPrototypePlaceholder(_)))
  }

  def prototypeKey = sanitizeName(t.simpleName)

  private def findOneOfSchema(t: AnyOfSchema, obj: AnyRef): Schema = {
    t.alternatives.find { classType =>
      classType.fullClassName == obj.getClass.getName
    }.get
  }
}

case class ObjectModelBuilder(schema: ClassSchema, includeData: Boolean)(implicit context: ModelBuilderContext) extends ModelBuilderForClass {
  def buildModelForObject(obj: AnyRef) = {
    if (obj == None && !context.prototypesBeingCreated.contains(schema) && (context.prototypesRequested.contains(schema))) {
      buildPrototypePlaceholder // creating a prototype which already exists or has been requested
    } else {
      if (obj == None) {
        // break possible infinite recursion
        context.prototypesRequested += schema
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
          Some(EditorProperty(property.key, propertyTitle, EditorModelBuilder.buildModel(value, property.schema, false)(objectContext), hidden, representative, flatten, complexObject, tabular, !readOnly))
        } else {
          None // missing values are skipped when not editable
        }
      }
      val objectTitle = obj match {
        case o: Localizable => Some(context.i(o.description))
        case _ => None
      }
      val data: Option[AnyRef] = if (includeData) {
        Some(obj)
      } else {
        None
      }
      context.prototypesRequested = context.prototypesRequested ++ objectContext.prototypesRequested
      var newRequests: Set[SchemaWithClassName] = context.prototypesRequested
      val includedPrototypes: Map[String, EditorModel] = if (context.root) {
        var prototypesCreated: Map[String, EditorModel] = Map.empty
        do {
          val requestsFromPreviousRound = newRequests
          newRequests = Set.empty
          requestsFromPreviousRound.foreach { schema =>
            val helperContext = context.copy(root = false, prototypesBeingCreated = Set(schema))(context.user)
            val prototypeKey: String = modelBuilderForClass(schema, true)(helperContext).prototypeKey
            var prototypeData: Any = None
            if (prototypeKey == "perusopetuksenopiskeluoikeudenlisatiedot") {
              prototypeData = PerusopetuksenOpiskeluoikeudenLisätiedot() // TODO: try to come up with a generic way to instantiate "default" objects
            } else if (prototypeKey == "perusopetuksenkayttaytymisenarviointi") {
              prototypeData = PerusopetuksenKäyttäytymisenArviointi()
            } else if (prototypeKey == "localizedstring" || prototypeKey == "finnish") {
              prototypeData = LocalizedString.finnish("")
            } else if (prototypeKey == "-koski-api-editor-koodit-ammatillisentutkinnonsuoritustapa") {
              // TODO: default values for KoodistoKoodiViite in generic fashion (start with first found value, then add @DefaultValue annotation)
              prototypeData = Koodistokoodiviite("ops", "ammatillisentutkinnonsuoritustapa")
            }
            val model: EditorModel = EditorModelBuilder.buildModel(prototypeData, schema, includeData = true)(helperContext)
            if (model.isInstanceOf[PrototypeModel]) {
              throw new IllegalStateException()
            }
            val newRequestsForThisCreation = helperContext.prototypesRequested -- context.prototypesRequested
            newRequests ++= newRequestsForThisCreation
            context.prototypesRequested ++= newRequestsForThisCreation
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

  private def newContext(obj: AnyRef): ModelBuilderContext = {
    def orgAccess = obj match {
      case o: OrganisaatioonLiittyvä => context.user.hasWriteAccess(o.omistajaOrganisaatio.oid)
      case _ => true
    }
    def lähdejärjestelmäAccess = obj match {
      case o: Lähdejärjestelmällinen => o.lähdejärjestelmänId == None
      case _ => true
    }
    context.copy(editable = context.editable && lähdejärjestelmäAccess && orgAccess, root = false, prototypesBeingCreated = Set.empty)(context.user)
  }
}


object Prototypes {
  def getPrototypePlaceholder(schema: Schema)(implicit context: ModelBuilderContext): Option[EditorModel] = if (context.editable) {
    def resolveSchema(schema: SchemaWithClassName): SchemaWithClassName = schema match {
      case s: ClassRefSchema => context.mainSchema.getSchema(s.fullClassName).get
      case _ => schema
    }

    schema match {
      case s: SchemaWithClassName =>
        val classRefSchema = resolveSchema(s)
        context.prototypesRequested += classRefSchema
        Some(modelBuilderForClass(s, true).buildPrototypePlaceholder)
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
