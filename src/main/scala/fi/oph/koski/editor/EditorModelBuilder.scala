package fi.oph.koski.editor

import java.time.LocalDate

import fi.oph.koski.editor.EditorModelBuilder._
import fi.oph.koski.editor.ModelBuilderForClass._
import fi.oph.koski.json.Json
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.localization.{Localizable, LocalizedString}
import fi.oph.koski.schema._
import fi.oph.koski.todistus.LocalizedHtml
import fi.oph.scalaschema._
import fi.oph.scalaschema.annotation.Title

object EditorModelBuilder {
  def buildModel(schema: ClassSchema, value: AnyRef, editable: Boolean)(implicit user: KoskiSession): EditorModel = {
    val context = ModelBuilderContext(schema, editable)
    ObjectModelBuilder(schema, true)(context).buildModelForObject(value)
  }

  def buildModel(obj: Any, schema: Schema, includeData: Boolean)(implicit context: ModelBuilderContext): EditorModel = (obj, schema) match {
    case (o: AnyRef, t: SchemaWithClassName) => modelBuilderForClass(t, includeData).buildModelForObject(o)
    case (xs: Iterable[_], t: ListSchema) => ListModel(xs.toList.map(item => buildModel(item, t.itemSchema, false)), Prototypes.getPrototypePlaceholder(t.itemSchema))
    case (x: Option[_], t: OptionalSchema) => OptionalModel(
      x.map(value => buildModel(value, t.itemSchema, includeData)),
      Prototypes.getPrototypePlaceholder(t.itemSchema)
    )
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
  def getPrototypeData: Any
}

object ModelBuilderForClass {
  def modelBuilderForClass(t: SchemaWithClassName, includeData: Boolean)(implicit context: ModelBuilderContext): ModelBuilderForClass = t match {
    case t: ClassSchema =>
      Class.forName(t.fullClassName) match {
        case c if classOf[Koodistokoodiviite].isAssignableFrom(c) =>
          KoodistoEnumModelBuilder(t)
        case c if classOf[Oppilaitos].isAssignableFrom(c) =>
          OppilaitosEnumBuilder(t)
        case c if classOf[OrganisaatioWithOid].isAssignableFrom(c) =>
          OrganisaatioEnumBuilder(t)
        case c =>
          ObjectModelBuilder(t, includeData)
      }
    case t: ClassRefSchema => modelBuilderForClass(context.mainSchema.getSchema(t.fullClassName).get, includeData)
    case t: AnyOfSchema => AnyOfModelBuilder(t, includeData)
  }
}

case class OppilaitosEnumBuilder(t: ClassSchema)(implicit context: ModelBuilderContext) extends EnumModelBuilder[OrganisaatioWithOid] {
  override def alternativesPath = "/koski/api/editor/oppilaitokset"
  override def toEnumValue(o: OrganisaatioWithOid) = organisaatioEnumValue(context)(o)
  override def getPrototypeData = context.user.organisaatiot.headOption.getOrElse(throw new RuntimeException("No org for user "  + context.user.username)) // TODO: not the best default
}

case class OrganisaatioEnumBuilder(t: ClassSchema)(implicit context: ModelBuilderContext) extends EnumModelBuilder[OrganisaatioWithOid] {
  override def alternativesPath = "/koski/api/editor/organisaatiot"
  override def toEnumValue(o: OrganisaatioWithOid) = organisaatioEnumValue(context)(o)
  override def getPrototypeData = context.user.organisaatiot.headOption.getOrElse(throw new RuntimeException("No org for user "  + context.user.username)) // TODO: not the best default
}

case class KoodistoEnumModelBuilder(t: ClassSchema)(implicit context: ModelBuilderContext) extends EnumModelBuilder[Koodistokoodiviite] {
  val enumValues = t.properties.find(_.key == "koodistoUri").get.schema.asInstanceOf[StringSchema].enumValues.getOrElse(throw new RuntimeException("@KoodistoUri -annotaatio puuttuu"))
  val koodistoUri = enumValues.head.asInstanceOf[String]
  val koodiarvot: List[String] = t.properties.find(_.key == "koodiarvo").get.schema.asInstanceOf[StringSchema].enumValues.getOrElse(Nil).asInstanceOf[List[String]]
  val koodiarvotString = if (koodiarvot.isEmpty) { "" } else { "/" + koodiarvot.mkString(",") }
  val alternativesPath = s"/koski/api/editor/koodit/$koodistoUri$koodiarvotString"
  def toEnumValue(k: Koodistokoodiviite) = koodistoEnumValue(context)(k)
  def defaultValue = koodiarvot.headOption.getOrElse("S") // TODO: better resolving of default value, this is random
  def getPrototypeData = Koodistokoodiviite(defaultValue, koodistoUri)
}

trait EnumModelBuilder[A] extends ModelBuilderForClass {
  def alternativesPath: String
  def toEnumValue(a: A): EnumValue
  def prototypeKey = sanitizeName(alternativesPath)

  def buildModelForObject(o: AnyRef) = {
    o match {
      case None => throw new RuntimeException("None value not allowed for Enum")
      case k: A => EnumeratedModel(Some(toEnumValue(k)), None, Some(alternativesPath))
    }
  }
}

case class AnyOfModelBuilder(t: AnyOfSchema, includeData: Boolean)(implicit context: ModelBuilderContext) extends ModelBuilderForClass {
  def buildModelForObject(obj: AnyRef) = obj match {
    case None =>
      throw new RuntimeException("None value not allowed for AnyOf")
    case x: AnyRef =>
      OneOfModel(sanitizeName(t.simpleName), Some(EditorModelBuilder.buildModel(x, findOneOfSchema(t, x), includeData)), t.alternatives.flatMap(Prototypes.getPrototypePlaceholder(_)))
  }

  def prototypeKey = sanitizeName(t.simpleName)

  private def findOneOfSchema(t: AnyOfSchema, obj: AnyRef): Schema = {
    t.alternatives.find { classType =>
      classType.fullClassName == obj.getClass.getName
    }.get
  }

  override def getPrototypeData = {
    val clazz: Class[_] = Class.forName(t.fullClassName)
    if (clazz == classOf[LocalizedString]) {
      LocalizedString.finnish("")
    } else {
      Prototypes.getPrototypeData(t.alternatives.head)
    }
  }
}

case class ObjectModelBuilder(schema: ClassSchema, includeData: Boolean)(implicit context: ModelBuilderContext) extends ModelBuilderForClass {
  def buildModelForObject(obj: AnyRef) = {
    if (obj == None) {
      throw new RuntimeException("None value not allowed for ClassSchema")
    }
    val objectContext = newContext(obj)
    val properties: List[EditorProperty] = schema.properties.flatMap { property =>
      val hidden = property.metadata.contains(Hidden())
      val representative: Boolean = property.metadata.contains(Representative())
      val flatten: Boolean = property.metadata.contains(Flatten())
      val complexObject: Boolean = property.metadata.contains(ComplexObject())
      val tabular: Boolean = property.metadata.contains(Tabular())
      val readOnly: Boolean = property.metadata.find(_.isInstanceOf[ReadOnly]).isDefined
      val value = schema.getPropertyValue(property, obj)
      val propertyTitle = property.metadata.flatMap {
        case Title(t) => Some(t)
        case _ => None
      }.headOption.getOrElse(property.key.split("(?=\\p{Lu})").map(_.toLowerCase).mkString(" ").replaceAll("_ ", "-").capitalize)
      Some(EditorProperty(property.key, propertyTitle, EditorModelBuilder.buildModel(value, property.schema, false)(objectContext), hidden, representative, flatten, complexObject, tabular, !readOnly))
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
          val prototypeData = Prototypes.getPrototypeData(schema)
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
      case o: OrganisaatioonLiittyvä =>
        o.omistajaOrganisaatio match {
          case Some(o) => context.user.hasWriteAccess(o.oid)
          case None => true
        }
      case _ => true
    }
    def lähdejärjestelmäAccess = obj match {
      case o: Lähdejärjestelmällinen => o.lähdejärjestelmänId == None
      case _ => true
    }
    context.copy(editable = context.editable && lähdejärjestelmäAccess && orgAccess, root = false, prototypesBeingCreated = Set.empty)(context.user)
  }

  override def getPrototypeData = {
    val clazz: Class[_] = Class.forName(schema.fullClassName)
    val keysAndValues = schema.properties.map(property => (property.key, Prototypes.getPrototypeData(property))).toMap
    val asJValue = Json.toJValue(keysAndValues)
    val deserialized: Any = Json.fromJValue(asJValue)(Manifest.classType(clazz))
    //println(deserialized)
    deserialized
  }
}


object Prototypes {
  private def resolveSchema(schema: SchemaWithClassName)(implicit context: ModelBuilderContext): SchemaWithClassName = schema match {
    case s: ClassRefSchema => context.mainSchema.getSchema(s.fullClassName).get
    case _ => schema
  }


  def getPrototypePlaceholder(schema: Schema)(implicit context: ModelBuilderContext): Option[EditorModel] = if (context.editable) {

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

  def getPrototypeData(property: Property)(implicit context: ModelBuilderContext): Any = {
    property.schema match {
      case t: ClassSchema => ModelBuilderForClass.modelBuilderForClass(t, true).getPrototypeData
      case s: OptionalSchema => None // For object properties, always default to empty values if possible.
      case s: ListSchema => Nil // For object properties, always default to empty values if possible.
      case s => getPrototypeData(property.schema)
    }
  }

  def getPrototypeData(schema: Schema)(implicit context: ModelBuilderContext): Any = schema match {
    case s: ClassRefSchema => getPrototypeData(resolveSchema(s))
    case s: SchemaWithClassName => ModelBuilderForClass.modelBuilderForClass(s, true).getPrototypeData
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
