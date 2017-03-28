package fi.oph.koski.editor

import java.time.LocalDate

import fi.oph.koski.editor.EditorModelBuilder._
import fi.oph.koski.json.Json
import fi.oph.koski.koodisto.{MockKoodistoPalvelu, MockKoodistoViitePalvelu}
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.localization.{Localizable, LocalizedString}
import fi.oph.koski.organisaatio.Opetushallitus
import fi.oph.koski.schema._
import fi.oph.koski.todistus.LocalizedHtml
import fi.oph.scalaschema._
import fi.oph.scalaschema.annotation.Title

object EditorModelBuilder {
  def buildModel(deserializationContext: ExtractionContext, value: AnyRef, editable: Boolean)(implicit user: KoskiSession): EditorModel = {
    val context = ModelBuilderContext(deserializationContext.rootSchema, deserializationContext, editable)
    ObjectModelBuilder(deserializationContext.rootSchema.asInstanceOf[ClassSchema], false)(context).buildModelForObject(value)
  }

  def builder(schema: Schema, includeData: Boolean)(implicit context: ModelBuilderContext): EditorModelBuilder[Any] = (schema match {
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
    case t: ListSchema => ListModelBuilder(t)
    case t: OptionalSchema => OptionalModelBuilder(t, includeData)
    case t: NumberSchema => NumberModelBuilder(t)
    case t: BooleanSchema => BooleanModelBuilder(t)
    case t: DateSchema => DateModelBuilder(t)
    case t: StringSchema => StringModelBuilder(t)
  }).asInstanceOf[EditorModelBuilder[Any]]

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
    case t: ClassRefSchema => modelBuilderForClass(resolveSchema(t), includeData)
    case t: AnyOfSchema => AnyOfModelBuilder(t, includeData)
  }

  def buildModel(obj: Any, schema: Schema, includeData: Boolean)(implicit context: ModelBuilderContext): EditorModel = builder(schema, includeData).buildModelForObject(obj)
  def sanitizeName(s: String) = s.toLowerCase.replaceAll("ä", "a").replaceAll("ö", "o").replaceAll("/", "-")
  def organisaatioEnumValue(localization: LocalizedHtml)(o: OrganisaatioWithOid)() = EnumValue(o.oid, localization.i(o.description), o)
  def koodistoEnumValue(localization: LocalizedHtml)(k: Koodistokoodiviite) = EnumValue(k.koodiarvo, localization.i(k.description), k)
  def resolveSchema(schema: SchemaWithClassName)(implicit context: ModelBuilderContext): SchemaWithClassName = schema match {
    case s: ClassRefSchema => context.mainSchema.getSchema(s.fullClassName).get
    case _ => schema
  }
}


trait EditorModelBuilder[T] {
  def buildModelForObject(obj: T): EditorModel
  def getPrototypeData: T
}

case class ModelBuilderContext(
  mainSchema: SchemaWithClassName,
  deserializationContext: ExtractionContext,
  editable: Boolean, root: Boolean = true,
  var prototypesRequested: Set[SchemaWithClassName] = Set.empty,
  prototypesBeingCreated: Set[SchemaWithClassName] = Set.empty)(implicit val user: KoskiSession) extends LocalizedHtml

case class NumberModelBuilder(t: NumberSchema) extends EditorModelBuilder[Number] {
  override def buildModelForObject(x: Number) = NumberModel(x)
  override def getPrototypeData = 0
}

case class BooleanModelBuilder(t: BooleanSchema) extends EditorModelBuilder[Boolean] {
  override def buildModelForObject(x: Boolean) = BooleanModel(x)
  override def getPrototypeData = false
}

case class StringModelBuilder(t: StringSchema) extends EditorModelBuilder[String] {
  override def buildModelForObject(x: String) = StringModel(x)
  override def getPrototypeData = ""
}

case class DateModelBuilder(t: DateSchema) extends EditorModelBuilder[LocalDate] {
  override def buildModelForObject(x: LocalDate) = DateModel(x)
  override def getPrototypeData = LocalDate.now
}

case class OptionalModelBuilder(t: OptionalSchema, includeData: Boolean)(implicit context: ModelBuilderContext) extends EditorModelBuilder[Any] {
  override def buildModelForObject(x: Any) = {
    val innerModel = x match {
      case x: Option[_] => x.map(value => buildModel(value, t.itemSchema, includeData))
      case x: AnyRef => Some(buildModel(x, t.itemSchema, includeData))
    }
    OptionalModel(
      innerModel,
      Prototypes.getPrototypePlaceholder(t.itemSchema)
    )
  }

  override def getPrototypeData = Prototypes.getPrototypeData(t.itemSchema)
}

case class ListModelBuilder(t: ListSchema)(implicit context: ModelBuilderContext) extends EditorModelBuilder[Iterable[_]] {
  def buildModelForObject(xs: Iterable[_]) = ListModel(xs.toList.map(item => buildModel(item, t.itemSchema, false)), Prototypes.getPrototypePlaceholder(t.itemSchema))
  override def getPrototypeData = List(Prototypes.getPrototypeData(t.itemSchema))
}

trait ModelBuilderForClass extends EditorModelBuilder[AnyRef] {
  def buildModelForObject(obj: AnyRef): EditorModel
  protected def prototypeKey: String
  def buildPrototypePlaceholder = PrototypeModel(prototypeKey)
  def getPrototypeData: AnyRef
  def buildPrototype = (prototypeKey, buildModelForObject(getPrototypeData))
}

case class OppilaitosEnumBuilder(t: ClassSchema)(implicit context: ModelBuilderContext) extends EnumModelBuilder[OrganisaatioWithOid] {
  override def alternativesPath = "/koski/api/editor/oppilaitokset"
  override def toEnumValue(o: OrganisaatioWithOid) = organisaatioEnumValue(context)(o)
  override def getPrototypeData = context.user.organisaatiot.headOption.getOrElse(OidOrganisaatio(Opetushallitus.organisaatioOid)) // TODO: not the best default. We should allow for things that don't have a prototype
}

case class OrganisaatioEnumBuilder(t: ClassSchema)(implicit context: ModelBuilderContext) extends EnumModelBuilder[OrganisaatioWithOid] {
  override def alternativesPath = "/koski/api/editor/organisaatiot"
  override def toEnumValue(o: OrganisaatioWithOid) = organisaatioEnumValue(context)(o)
  override def getPrototypeData = context.user.organisaatiot.headOption.getOrElse(OidOrganisaatio(Opetushallitus.organisaatioOid)) // TODO: not the best default
}

object KoodistoEnumModelBuilder {
  val defaults = Map(
    "kieli" -> "FI",
    "arviointiasteikkoyleissivistava" -> "S"
  )
}

case class KoodistoEnumModelBuilder(t: ClassSchema)(implicit context: ModelBuilderContext) extends EnumModelBuilder[Koodistokoodiviite] {
  val enumValues = t.properties.find(_.key == "koodistoUri").get.schema.asInstanceOf[StringSchema].enumValues.getOrElse(throw new RuntimeException("@KoodistoUri -annotaatio puuttuu"))
  val koodistoUri = enumValues.head.asInstanceOf[String]
  val koodiarvot: List[String] = t.properties.find(_.key == "koodiarvo").get.schema.asInstanceOf[StringSchema].enumValues.getOrElse(Nil).asInstanceOf[List[String]]
  val koodiarvotString = if (koodiarvot.isEmpty) { "" } else { "/" + koodiarvot.mkString(",") }
  val alternativesPath = s"/koski/api/editor/koodit/$koodistoUri$koodiarvotString"
  def toEnumValue(k: Koodistokoodiviite) = koodistoEnumValue(context)(k) // TODO: don't use MockKoodisto, use the real one!
  def defaultValue = koodiarvot.headOption.orElse(KoodistoEnumModelBuilder.defaults.get(koodistoUri)).getOrElse(MockKoodistoViitePalvelu.getKoodistoKoodiViitteet(MockKoodistoPalvelu().getLatestVersion(koodistoUri).get).get.head.koodiarvo)
  def getPrototypeData = MockKoodistoViitePalvelu.validate(Koodistokoodiviite(defaultValue, koodistoUri)).get
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
    } match {
      case Some(schema) =>
        schema
      case None =>
        throw new RuntimeException("Alternative not found for schema " + t.simpleName + " / object " + obj)
    }
  }

  override def getPrototypeData = {
    val clazz: Class[_] = Class.forName(t.fullClassName)
    if (clazz == classOf[LocalizedString]) {
      LocalizedString.finnish("")
    } else {
      modelBuilderForClass(t.alternatives.head, true).getPrototypeData
    }
  }
}

case class ObjectModelBuilder(schema: ClassSchema, includeData: Boolean)(implicit context: ModelBuilderContext) extends ModelBuilderForClass {
  import scala.reflect.runtime.{universe => ru}

  def buildModelForObject(obj: AnyRef) = {
    if (obj == None) {
      throw new RuntimeException("None value not allowed for ClassSchema")
    }
    val objectContext = newContext(obj)
    val properties: List[EditorProperty] = schema.properties.map(property => createModelProperty(obj, objectContext, property))
    val objectTitle = obj match {
      case o: Localizable => Some(context.i(o.description))
      case _ => None
    }
    val data: Option[AnyRef] = if (includeData) { Some(obj) } else { None }
    context.prototypesRequested = context.prototypesRequested ++ objectContext.prototypesRequested
    val includedPrototypes: Map[String, EditorModel] = if (context.root) { createRequestedPrototypes} else { Map.empty }
    ObjectModel(classes(schema.fullClassName), properties, data, objectTitle, objectContext.editable, includedPrototypes)
  }

  private def createModelProperty(obj: AnyRef, objectContext: ModelBuilderContext, property: Property): EditorProperty = {
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
    EditorProperty(property.key, propertyTitle, EditorModelBuilder.buildModel(value, property.schema, false)(objectContext), hidden, representative, flatten, complexObject, tabular, !readOnly)
  }

  private def createRequestedPrototypes: Map[String, EditorModel] = {
    var newRequests: Set[SchemaWithClassName] = context.prototypesRequested
    var prototypesCreated: Map[String, EditorModel] = Map.empty
    do {
      val requestsFromPreviousRound = newRequests
      newRequests = Set.empty
      requestsFromPreviousRound.foreach { schema =>
        val helperContext = context.copy(root = false, prototypesBeingCreated = Set(schema))(context.user)
        val modelBuilderForProto = modelBuilderForClass(schema, true)(helperContext)
        val (protoKey, model) = modelBuilderForProto.buildPrototype

        if (model.isInstanceOf[PrototypeModel]) {
          throw new IllegalStateException()
        }
        val newRequestsForThisCreation = helperContext.prototypesRequested -- context.prototypesRequested
        newRequests ++= newRequestsForThisCreation
        context.prototypesRequested ++= newRequestsForThisCreation
        prototypesCreated += (protoKey -> model)
      }
    } while (newRequests.nonEmpty)
    prototypesCreated
  }


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

  override def getPrototypeData: AnyRef = {
    val clazz: Class[_] = Class.forName(schema.fullClassName)
    val keysAndValues = schema.properties.map(property => (property.key, Prototypes.getPrototypeData(property))).toMap
    val asJValue = Json.toJValue(keysAndValues)
    SchemaValidatingExtractor.extract(asJValue, clazz)(context.deserializationContext) match {
      case Right(obj) => obj
      case Left(errors) => throw new RuntimeException("Unable to build prototype for " + schema.fullClassName + ": " + errors)
    }
  }
}


object Prototypes {
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
      case t: ClassSchema => modelBuilderForClass(t, true).getPrototypeData
      case s: OptionalSchema => None // For object properties, always default to empty values if possible.
      case s: ListSchema => Nil // For object properties, always default to empty values if possible.
      case s => getPrototypeData(property.schema)
    }
  }

  def getPrototypeData(schema: Schema)(implicit context: ModelBuilderContext): Any = builder(schema, true).getPrototypeData
}
