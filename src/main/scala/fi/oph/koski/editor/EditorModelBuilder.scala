package fi.oph.koski.editor

import java.time.LocalDate

import fi.oph.koski.editor.EditorModelBuilder._
import fi.oph.koski.editor.MetadataToModel.propsFromMetadata
import fi.oph.koski.koodisto.{KoodistoViitePalvelu, MockKoodistoViitePalvelu}
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.localization.{Localizable, LocalizedString}
import fi.oph.koski.schema._
import fi.oph.koski.todistus.LocalizedHtml
import fi.oph.scalaschema._
import fi.oph.scalaschema.annotation._

object EditorModelBuilder {
  def buildModel(deserializationContext: ExtractionContext, value: AnyRef, editable: Boolean)(implicit user: KoskiSession, koodisto: KoodistoViitePalvelu): EditorModel = {
    implicit val context = ModelBuilderContext(deserializationContext.rootSchema, deserializationContext, editable)
    deserializationContext.rootSchema.getSchema(value.getClass.getName) match {
      case Some(objectSchema) => builder(objectSchema).buildModelForObject(value, Nil)
      case None => throw new RuntimeException("Schema not found for " + value.getClass.getName)
    }
  }

  def buildPrototype(className: String)(implicit context: ModelBuilderContext) = {
    context.mainSchema.getSchema(className).map(builder(_).buildPrototype(Nil))
  }

  def builder(schema: Schema)(implicit context: ModelBuilderContext): EditorModelBuilder[Any] = (schema match {
    case t: SchemaWithClassName => modelBuilderForClass(t)
    case t: ListSchema => ListModelBuilder(t)
    case t: OptionalSchema => OptionalModelBuilder(t)
    case t: NumberSchema => NumberModelBuilder(t)
    case t: BooleanSchema => BooleanModelBuilder(t)
    case t: DateSchema => DateModelBuilder(t)
    case t: StringSchema => StringModelBuilder(t)
  }).asInstanceOf[EditorModelBuilder[Any]]

  def modelBuilderForClass(t: SchemaWithClassName)(implicit context: ModelBuilderContext): ModelBuilderForClass = t match {
    case t: ClassSchema =>
      Class.forName(t.fullClassName) match {
        case c if classOf[Koodistokoodiviite].isAssignableFrom(c) =>
          KoodistoEnumModelBuilder(t)
        case c =>
          ObjectModelBuilder(t)
      }
    case t: ClassRefSchema => modelBuilderForClass(resolveSchema(t))
    case t: AnyOfSchema => OneOfModelBuilder(t)
  }

  def buildModel(obj: Any, schema: Schema, metadata: List[Metadata])(implicit context: ModelBuilderContext): EditorModel = builder(schema).buildModelForObject(obj, metadata)
  def sanitizeName(s: String) = s.toLowerCase.replaceAll("ä", "a").replaceAll("ö", "o").replaceAll("/", "-")
  def organisaatioEnumValue(localization: LocalizedHtml)(o: OrganisaatioWithOid)() = EnumValue(o.oid, localization.i(o.description), o)
  def koodistoEnumValue(localization: LocalizedHtml)(k: Koodistokoodiviite) = EnumValue(k.koodiarvo, localization.i(k.description), k)
  def resolveSchema(schema: SchemaWithClassName)(implicit context: ModelBuilderContext): SchemaWithClassName = schema match {
    case s: ClassRefSchema => context.mainSchema.getSchema(s.fullClassName).get
    case _ => schema
  }
}


trait EditorModelBuilder[T] {
  def buildModelForObject(obj: T, metadata: List[Metadata]): EditorModel
  def buildPrototype(metadata: List[Metadata]): EditorModel
}

trait ModelBuilderWithData[T] extends EditorModelBuilder[T]{
  def getPrototypeData: T
  def buildPrototype(metadata: List[Metadata]) = buildModelForObject(getPrototypeData, metadata)
}

case class ModelBuilderContext(
  mainSchema: SchemaWithClassName,
  deserializationContext: ExtractionContext,
  editable: Boolean, root: Boolean = true,
  var prototypesRequested: Set[SchemaWithClassName] = Set.empty,
  prototypesBeingCreated: Set[SchemaWithClassName] = Set.empty)(implicit val user: KoskiSession, val koodisto: KoodistoViitePalvelu) extends LocalizedHtml

case class NumberModelBuilder(t: NumberSchema) extends ModelBuilderWithData[Number] {
  override def buildModelForObject(x: Number, metadata: List[Metadata]) = NumberModel(ValueWithData(x), propsFromMetadata(metadata))
  override def getPrototypeData = 0
}

case class BooleanModelBuilder(t: BooleanSchema) extends ModelBuilderWithData[Boolean] {
  override def buildModelForObject(x: Boolean, metadata: List[Metadata]) = BooleanModel(ValueWithData(x), propsFromMetadata(metadata))
  override def getPrototypeData = false
}

case class StringModelBuilder(t: StringSchema) extends ModelBuilderWithData[String] {
  override def buildModelForObject(x: String, metadata: List[Metadata]) = StringModel(ValueWithData(x), propsFromMetadata(metadata))
  override def getPrototypeData = ""
}

case class DateModelBuilder(t: DateSchema) extends ModelBuilderWithData[LocalDate] {
  override def buildModelForObject(x: LocalDate, metadata: List[Metadata]) = DateModel(ValueWithData(x), propsFromMetadata(metadata))
  override def getPrototypeData = LocalDate.now
}

case class OptionalModelBuilder(t: OptionalSchema)(implicit context: ModelBuilderContext) extends EditorModelBuilder[Any] {
  override def buildModelForObject(x: Any, metadata: List[Metadata]) = {
    val innerModel = x match {
      case x: Option[_] => x.map(value => buildModel(value, t.itemSchema, metadata))
      case x: AnyRef => Some(buildModel(x, t.itemSchema, metadata))
    }
    OptionalModel(
      innerModel,
      Prototypes.getPrototypePlaceholder(t.itemSchema, metadata),
      propsFromMetadata(metadata)
    )
  }
  def buildPrototype(metadata: List[Metadata]) = OptionalModel(
    None,
    Prototypes.getPrototypePlaceholder(t.itemSchema, metadata),
    Map.empty
  )
}

case class ListModelBuilder(t: ListSchema)(implicit context: ModelBuilderContext) extends EditorModelBuilder[Iterable[_]] {
  def buildModelForObject(xs: Iterable[_], metadata: List[Metadata]) = {
    val models: List[EditorModel] = xs.toList.map(item => buildModel(item, t.itemSchema, metadata))
    ListModel(models, Prototypes.getPrototypePlaceholder(t.itemSchema, Nil), propsFromMetadata(t.metadata ++ metadata))
  }
  def buildPrototype(metadata: List[Metadata]): EditorModel = ListModel(Nil, Prototypes.getPrototypePlaceholder(t.itemSchema, Nil), propsFromMetadata(metadata))
}

object MetadataToModel {
  def propsFromMetadata(metadata: List[Metadata]) = {
    var props: Map[String, Any] = Map.empty
    metadata.collect { case MinItems(x) => x }.foreach { x => props += ("minItems" -> x)}
    metadata.collect { case MaxItems(x) => x }.foreach { x => props += ("maxItems" -> x)}
    metadata.collect { case MinValue(x) => x }.foreach { x => props += ("minValue" -> x)}
    metadata.collect { case MaxValue(x) => x }.foreach { x => props += ("maxValue" -> x)}
    metadata.collect { case MinValueExclusive(x) => x }.foreach { x => props += ("minValueExclusive" -> x)}
    metadata.collect { case MaxValueExclusive(x) => x }.foreach { x => props += ("maxValueExclusive" -> x)}
    props
  }
}

trait ModelBuilderForClass extends EditorModelBuilder[AnyRef] {
  def buildModelForObject(obj: AnyRef, metadata: List[Metadata]): EditorModel
  def prototypeKey: String
  def buildPrototypePlaceholder(metadata: List[Metadata]) = PrototypeModel(prototypeKey, propsFromMetadata(metadata))
}

object KoodistoEnumModelBuilder {
  val defaults = Map(
    "kieli" -> "FI",
    "arviointiasteikkoyleissivistava" -> "S",
    "suorituksentila" -> "KESKEN"
  )
}

case class KoodistoEnumModelBuilder(t: ClassSchema)(implicit context: ModelBuilderContext) extends EnumModelBuilder[Koodistokoodiviite] {
  val enumValues = t.properties.find(_.key == "koodistoUri").get.schema.asInstanceOf[StringSchema].enumValues.getOrElse(throw new RuntimeException("@KoodistoUri -annotaatio puuttuu"))
  val koodistoUri = enumValues.head.asInstanceOf[String]
  val koodiarvot: List[String] = t.properties.find(_.key == "koodiarvo").get.schema.asInstanceOf[StringSchema].enumValues.getOrElse(Nil).asInstanceOf[List[String]]
  val koodiarvotString = if (koodiarvot.isEmpty) { "" } else { "/" + koodiarvot.mkString(",") }
  val alternativesPath = s"/koski/api/editor/koodit/$koodistoUri$koodiarvotString"
  def toEnumValue(k: Koodistokoodiviite) = koodistoEnumValue(context)(k)
  def defaultValue = KoodistoEnumModelBuilder.defaults.get(koodistoUri).filter(arvo => koodiarvot.isEmpty || koodiarvot.contains(arvo)).orElse(koodiarvot.headOption)
  def getPrototypeData = defaultValue.flatMap(value => MockKoodistoViitePalvelu.validate(Koodistokoodiviite(value, koodistoUri)))
  def buildPrototype(metadata: List[Metadata]): EditorModel = buildModelForObject(getPrototypeData, metadata)
}

trait EnumModelBuilder[A] extends ModelBuilderForClass {
  def alternativesPath: String
  def toEnumValue(a: A): EnumValue
  def prototypeKey = sanitizeName(alternativesPath)

  def buildModelForObject(o: AnyRef, metadata: List[Metadata]) = {
    o match {
      case k: Option[A] => EnumeratedModel(k.map(toEnumValue), None, Some(alternativesPath), propsFromMetadata(metadata))
      case k: A => buildModelForObject(Some(k), metadata)
    }
  }
}

case class OneOfModelBuilder(t: AnyOfSchema)(implicit context: ModelBuilderContext) extends ModelBuilderForClass {
  def buildModelForObject(obj: AnyRef, metadata: List[Metadata]) = obj match {
    case None =>
      throw new RuntimeException("None value not allowed for AnyOf")
    case x: AnyRef =>
      val selectedModel = EditorModelBuilder.buildModel(x, findOneOfSchema(t, x), metadata)
      buildModel(selectedModel, metadata)
  }

  private def buildModel(selectedModel: EditorModel, metadata: List[Metadata]) = {
    OneOfModel(sanitizeName(t.simpleName), selectedModel, t.alternatives.flatMap(Prototypes.getPrototypePlaceholder(_, metadata)), propsFromMetadata(metadata ++ t.metadata))
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

  def buildPrototype(metadata: List[Metadata]): EditorModel = {
    val clazz: Class[_] = Class.forName(t.fullClassName)
    if (clazz == classOf[LocalizedString]) {
      buildModelForObject(LocalizedString.finnish(""), metadata)
    } else {
      buildModel(modelBuilderForClass(t.alternatives.head).buildPrototype(metadata), metadata)
    }
  }
}

case class ObjectModelBuilder(schema: ClassSchema)(implicit context: ModelBuilderContext) extends ModelBuilderForClass {
  import scala.reflect.runtime.{universe => ru}

  def buildModelForObject(obj: AnyRef, metadata: List[Metadata]) = {
    if (obj == None) {
      throw new RuntimeException("None value not allowed for ClassSchema")
    }
    val objectContext = newContext(obj)
    val properties: List[EditorProperty] = schema.properties.map(property => createModelProperty(obj, objectContext, property))
    val objectTitle = obj match {
      case o: Localizable => Some(context.i(o.description))
      case _ => None
    }
    context.prototypesRequested = context.prototypesRequested ++ objectContext.prototypesRequested
    ObjectModel(classes(schema.fullClassName), properties, objectTitle, objectContext.editable, createRequestedPrototypes, propsFromMetadata(metadata ++ schema.metadata))
  }

  def buildPrototype(metadata: List[Metadata]) = {
    val properties: List[EditorProperty] = schema.properties.map{property =>
      val propertyPrototype = Prototypes.getPrototypePlaceholder(property.schema, property.metadata).get
      createModelProperty(property, propertyPrototype)
    }
    ObjectModel(classes(schema.fullClassName), properties, title = None, true, createRequestedPrototypes, propsFromMetadata(metadata ++ schema.metadata))
  }

  private def createModelProperty(obj: AnyRef, objectContext: ModelBuilderContext, property: Property): EditorProperty = {
    val value = schema.getPropertyValue(property, obj)
    val propertyModel = EditorModelBuilder.buildModel(value, property.schema, property.metadata)(objectContext)

    createModelProperty(property, propertyModel)
  }

  def createModelProperty(property: Property, propertyModel: EditorModel): EditorProperty = {
    val hidden = property.metadata.contains(Hidden())
    val representative: Boolean = property.metadata.contains(Representative())
    val flatten: Boolean = property.metadata.contains(Flatten())
    val complexObject: Boolean = property.metadata.contains(ComplexObject())
    val tabular: Boolean = property.metadata.contains(Tabular())
    val readOnly: Boolean = property.metadata.find(_.isInstanceOf[ReadOnly]).isDefined
    var props: Map[String, Any] = Map.empty
    if (hidden) props += ("hidden" -> true)
    if (representative) props += ("representative" -> true)
    if (flatten) props += ("flatten" -> true)
    if (complexObject) props += ("complexObject" -> true)
    if (tabular) props += ("tabular" -> true)
    if (!readOnly) props += ("editable" -> true)

    val propertyTitle = property.metadata.flatMap {
      case Title(t) => Some(t)
      case _ => None
    }.headOption.getOrElse(property.key.split("(?=\\p{Lu})").map(_.toLowerCase).mkString(" ").replaceAll("_ ", "-").capitalize)
    EditorProperty(property.key, propertyTitle, propertyModel, props)
  }

  private def createRequestedPrototypes: Map[String, EditorModel] = {
    if (!context.root) return Map.empty
    var newRequests: Set[SchemaWithClassName] = context.prototypesRequested
    var prototypesCreated: Map[String, EditorModel] = Map.empty
    do {
      val requestsFromPreviousRound = newRequests
      newRequests = Set.empty
      requestsFromPreviousRound.foreach { schema =>
        val helperContext = context.copy(root = false, prototypesBeingCreated = Set(schema))(context.user, context.koodisto)
        val modelBuilderForProto = modelBuilderForClass(schema)(helperContext)
        val (protoKey, model) = (modelBuilderForProto.prototypeKey, modelBuilderForProto.buildPrototype(Nil))

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
    context.copy(editable = context.editable && lähdejärjestelmäAccess && orgAccess, root = false, prototypesBeingCreated = Set.empty)(context.user, context.koodisto)
  }
}


object Prototypes {
  def getPrototypePlaceholder(schema: Schema, metadata: List[Metadata])(implicit context: ModelBuilderContext): Option[EditorModel] = if (context.editable) {
    schema match {
      case s: SchemaWithClassName =>
        val clazz = Class.forName(s.fullClassName)
        if (classOf[Opiskeluoikeus].isAssignableFrom(clazz)) {
          return None // Cuts model build time and size by half
        }
        val classRefSchema = resolveSchema(s)
        context.prototypesRequested += classRefSchema
        Some(modelBuilderForClass(s).buildPrototypePlaceholder(metadata))
      case _ => Some(builder(schema).buildPrototype(metadata))
    }
  } else {
    None
  }
}
