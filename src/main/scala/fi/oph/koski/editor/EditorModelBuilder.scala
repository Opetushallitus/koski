package fi.oph.koski.editor

import java.time.{LocalDate, LocalDateTime}

import fi.oph.koski.editor.ClassFinder.{forName, forSchema}
import fi.oph.koski.editor.EditorModelBuilder._
import fi.oph.koski.editor.MetadataToModel.classesFromMetadata
import fi.oph.koski.json.{JsonSerializer, SensitiveAndRedundantDataFilter}
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.koskiuser.{AccessType, KoskiSpecificSession}
import fi.oph.koski.localization._
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusAccessChecker
import fi.oph.koski.schema._
import fi.oph.koski.schema.annotation._
import fi.oph.koski.util.OptionalLists
import fi.oph.scalaschema._
import fi.oph.scalaschema.annotation._
import org.json4s.JsonAST.{JBool, JString}
import org.json4s.{JArray, JValue}

object EditorModelBuilder {
  def buildModel(deserializationContext: ExtractionContext, value: AnyRef, editable: Boolean)(implicit user: KoskiSpecificSession, koodisto: KoodistoViitePalvelu, localizations: LocalizationRepository): EditorModel = {
    implicit val context = ModelBuilderContext(deserializationContext, editable = editable, invalidatable = editable)
    builder(deserializationContext.schemaFactory.createSchema(value.getClass.getName)).buildModelForObject(value, Nil)
  }

  def buildPrototype(className: String)(implicit context: ModelBuilderContext) = {
    builder(context.deserializationContext.schemaFactory.createSchema(className)).buildPrototype(Nil)
  }

  def builder(schema: Schema)(implicit context: ModelBuilderContext): EditorModelBuilder[Any] = (schema match {
    case t: SchemaWithClassName => modelBuilderForClass(t)
    case t: ListSchema => ListModelBuilder(t)
    case t: OptionalSchema => OptionalModelBuilder(t)
    case t: NumberSchema => NumberModelBuilder(t)
    case t: BooleanSchema => BooleanModelBuilder(t)
    case t: DateSchema => DateModelBuilder(t)
    case t: StringSchema => StringModelBuilder(t)
    case _ => throw new RuntimeException("Unreachable match arm: builder must be called with a proper schema")
  }).asInstanceOf[EditorModelBuilder[Any]]

  def modelBuilderForClass(t: SchemaWithClassName)(implicit context: ModelBuilderContext): ModelBuilderForClass = t match {
    case t: ClassSchema =>
      forSchema(t) match {
        case c if classOf[Koodistokoodiviite].isAssignableFrom(c) =>
          KoodistoEnumModelBuilder(t)
        case c =>
          ObjectModelBuilder(t)
      }
    case t: ClassRefSchema => modelBuilderForClass(resolveSchema(t))
    case t: AnyOfSchema => OneOfModelBuilder(t)
    case _ => ???
  }

  def buildModel(obj: Any, schema: Schema, metadata: List[Metadata])(implicit context: ModelBuilderContext): EditorModel = builder(schema).buildModelForObject(obj, metadata)
  def sanitizeName(s: String) = s.toLowerCase.replaceAll("ä", "a").replaceAll("ö", "o").replaceAll("/", "-")
  def organisaatioEnumValue(localization: LocalizedHtml)(o: OrganisaatioWithOid)() = EnumValue(o.oid, localization.i(o), JsonSerializer.serializeWithRoot(o), None)
  def resolveSchema(schema: SchemaWithClassName)(implicit context: ModelBuilderContext): SchemaWithClassName = schema match {
    case s: ClassRefSchema => context.deserializationContext.schemaFactory.createSchema(s.fullClassName)
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
  deserializationContext: ExtractionContext,
  editable: Boolean,
  invalidatable: Boolean,
  root: Boolean = true,
  var prototypesRequested: SchemaSet = SchemaSet.empty,
  prototypesBeingCreated: SchemaSet = SchemaSet.empty
)(
  implicit val user: KoskiSpecificSession,
  val koodisto: KoodistoViitePalvelu,
  val localizationRepository: LocalizationRepository
) extends LocalizedHtml

case class NumberModelBuilder(t: NumberSchema) extends ModelBuilderWithData[Number] {
  override def buildModelForObject(x: Number, metadata: List[Metadata]) = NumberModel(ValueWithData(x, classesFromMetadata(metadata)), metadata)
  override def getPrototypeData = Float.NaN
}

case class BooleanModelBuilder(t: BooleanSchema) extends ModelBuilderWithData[Boolean] {
  override def buildModelForObject(x: Boolean, metadata: List[Metadata]) = BooleanModel(ValueWithData(x, classesFromMetadata(metadata)), metadata)
  override def getPrototypeData = false
  override def buildPrototype(metadata: List[Metadata]): EditorModel = buildModelForObject(getDefaultValue(metadata), metadata)
  def getDefaultValue(metadata: List[Metadata]): Boolean = DefaultValue.getDefaultValue(metadata).getOrElse(false)
}

case class StringModelBuilder(t: StringSchema) extends ModelBuilderWithData[String] {
  override def buildModelForObject(x: String, metadata: List[Metadata]) = StringModel(ValueWithData(x, classesFromMetadata(metadata)), metadata)
  override def getPrototypeData = ""
}

case class DateModelBuilder(t: DateSchema) extends ModelBuilderWithData[AnyRef] {
  override def buildModelForObject(x: AnyRef, metadata: List[Metadata]) = x match {
    case x: LocalDate => DateModel(ValueWithData(x, classesFromMetadata(metadata)), metadata)
    case x: LocalDateTime => DateTimeModel(ValueWithData(x, classesFromMetadata(metadata)), metadata)
  }
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
      metadata
    )
  }
  def buildPrototype(metadata: List[Metadata]) = OptionalModel(
    None,
    Prototypes.getPrototypePlaceholder(t.itemSchema, metadata),
    Nil
  )
}

case class ListModelBuilder(t: ListSchema)(implicit context: ModelBuilderContext) extends EditorModelBuilder[Iterable[_]] {
  def buildModelForObject(xs: Iterable[_], metadata: List[Metadata]) = {
    val models: List[EditorModel] = xs.toList.map(item => buildModel(item, t.itemSchema, metadata))
    ListModel(models, Prototypes.getPrototypePlaceholder(t.itemSchema, Nil), t.metadata ++ metadata)
  }
  def buildPrototype(metadata: List[Metadata]): EditorModel = ListModel(Nil, Prototypes.getPrototypePlaceholder(t.itemSchema, Nil), metadata)
}

object MetadataToModel {
  def classesFromMetadata(metadata: List[Metadata]): Option[List[String]] = OptionalLists.optionalList(metadata.collect {
    case ClassName(c) => c
  })
}

trait ModelBuilderForClass extends EditorModelBuilder[AnyRef] {
  def buildModelForObject(obj: AnyRef, metadata: List[Metadata]): EditorModel
  def prototypeKey: String
  def buildPrototypePlaceholder(metadata: List[Metadata]) = PrototypeModel(prototypeKey, metadata)
}

object KoodistoEnumModelBuilder {
  val defaults = Map(
    "kieli" -> "FI",
    "maatjavaltiot2" -> "246",
    "arviointiasteikkoyleissivistava" -> "S",
    "perusopetuksensuoritustapa" -> "koulutus"
  )

  def koodistoEnumValue(k: Koodistokoodiviite)(localization: LocalizedHtml, koodisto: KoodistoViitePalvelu) = {
    def koodistoName(k: Koodistokoodiviite) = {
      val kp = koodisto.koodistoPalvelu
      kp.getLatestVersionOptional(k.koodistoUri).flatMap(kp.getKoodisto(_)).flatMap(_.nimi).map(_.get(localization.lang))
    }

    val title = if (List("arviointiasteikkoammatillinent1k3").contains(k.koodistoUri)) {
      k.koodiarvo
    } else {
      localization.i(k.description)
    }
    EnumValue(k.koodistoUri + "_" + k.koodiarvo, title, JsonSerializer.serializeWithRoot(k), koodistoName(k))
  }

}

case class KoodistoEnumModelBuilder(t: ClassSchema)(implicit context: ModelBuilderContext) extends EnumModelBuilder[Koodistokoodiviite] {
  val enumValues = t.properties.find(_.key == "koodistoUri").get.schema.asInstanceOf[StringSchema].enumValues.getOrElse(throw new RuntimeException(s"@KoodistoUri -annotaatio puuttuu ${t.properties.mkString(", ")}"))
  val koodistoUri = enumValues.map(_.asInstanceOf[String]).mkString(",")
  val koodiarvot: List[String] = t.properties.find(_.key == "koodiarvo").get.schema.asInstanceOf[StringSchema].enumValues.getOrElse(Nil).asInstanceOf[List[String]]
  val koodiarvotString = if (koodiarvot.isEmpty) { "" } else { "/" + koodiarvot.mkString(",") }
  val alternativesPath = s"/koski/api/editor/koodit/$koodistoUri$koodiarvotString"
  def toEnumValue(k: Koodistokoodiviite) = KoodistoEnumModelBuilder.koodistoEnumValue(k)(context, context.koodisto)
  def defaultValue = KoodistoEnumModelBuilder.defaults.get(koodistoUri).filter(arvo => koodiarvot.isEmpty || koodiarvot.contains(arvo)).orElse(koodiarvot.headOption)
  def getPrototypeData = defaultValue.flatMap(value => context.koodisto.validate(Koodistokoodiviite(value, koodistoUri)))
  def buildPrototype(metadata: List[Metadata]): EditorModel = buildModelForObject(getPrototypeData, metadata)
}

trait EnumModelBuilder[A] extends ModelBuilderForClass {
  def alternativesPath: String
  def toEnumValue(a: A): EnumValue
  def prototypeKey = sanitizeName(alternativesPath)

  def buildModelForObject(o: AnyRef, metadata: List[Metadata]) = {
    o match {
      case k: Option[A] => EnumeratedModel(k.map(toEnumValue), None, Some(alternativesPath), metadata)
      case k: A @unchecked => buildModelForObject(Some(k), metadata)
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
    OneOfModel(sanitizeName(t.simpleName), selectedModel, t.alternatives.flatMap(Prototypes.getPrototypePlaceholder(_, metadata)), metadata ++ t.metadata)
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
    val clazz: Class[_] = forSchema(t)
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
    val objectContext = newContext(obj, metadata)
    val sensitiveDataFilter = SensitiveAndRedundantDataFilter(context.user)

    val properties: List[EditorProperty] = schema.properties
      .filter(p => !sensitiveDataFilter.shouldHideField(p.metadata))
      .map(property => createModelProperty(obj, objectContext, property))

    val objectTitle = obj match {
      case o: Localized => Some(context.i(o))
      case _ => None
    }
    context.prototypesRequested = context.prototypesRequested ++ objectContext.prototypesRequested
    ObjectModel(classes(schema.fullClassName), properties, objectTitle, editable = objectContext.editable, invalidatable = objectContext.invalidatable, createRequestedPrototypes, metadata ++ schema.metadata)
  }

  def buildPrototype(metadata: List[Metadata]) = {
    val properties: List[EditorProperty] = schema.properties.map{property =>
      val propertyPrototype = Prototypes.getPrototypePlaceholder(property.schema, property.metadata).get
      createModelProperty(property, propertyPrototype)
    }
    ObjectModel(classes(schema.fullClassName), properties, title = None, editable = true, invalidatable = false, createRequestedPrototypes, metadata ++ schema.metadata)
  }

  private def createModelProperty(obj: AnyRef, objectContext: ModelBuilderContext, property: Property): EditorProperty = {
    val value = schema.getPropertyValue(property, obj)
    val propertyModel = EditorModelBuilder.buildModel(value, property.schema, property.metadata)(objectContext)

    createModelProperty(property, propertyModel)
  }

  def createModelProperty(property: Property, propertyModel: EditorModel): EditorProperty = {
    val readOnly: Boolean = property.metadata.exists(_.isInstanceOf[ReadOnly])
    val onlyWhen = property.metadata.collect{case o:OnlyWhen => EditorModelSerializer.serializeOnlyWhen(o)}
    var props  = Map.empty[String, JValue]
    if (property.metadata.contains(Hidden())) props += ("hidden" -> JBool(true))
    if (property.metadata.contains(Representative())) props += ("representative" -> JBool(true))
    if (property.metadata.contains(FlattenInUI())) props += ("flatten" -> JBool(true))
    if (property.metadata.contains(ComplexObject())) props += ("complexObject" -> JBool(true))
    if (property.metadata.contains(Tabular())) props += ("tabular" -> JBool(true))
    if (readOnly) props += ("readOnly" -> JBool(true))
    if (SensitiveAndRedundantDataFilter(context.user).shouldHideField(property.metadata)) props += ("sensitiveHidden" -> JBool(true))
    if (!onlyWhen.isEmpty) props +=("onlyWhen" -> JArray(onlyWhen))
    KoskiSpecificSchemaLocalization.deprecated(property)
      .map { case (key, _) => context.localizationRepository.get(key).get(context.user.lang) }
      .foreach { d => props += ("deprecated" -> JString(d)) }

    val description = KoskiSpecificSchemaLocalization.tooltip(property).map{ case (key, text) => context.localizationRepository.get(key).get(context.user.lang) }

    EditorProperty(property.key, property.title, description, propertyModel, props)
  }

  private def createRequestedPrototypes: Map[String, EditorModel] = {
    if (!context.root) return Map.empty
    var newRequests: SchemaSet = context.prototypesRequested
    var prototypesCreated: Map[String, EditorModel] = Map.empty
    do {
      val requestsFromPreviousRound = newRequests
      newRequests = SchemaSet.empty
      requestsFromPreviousRound.foreach { schema =>
        val helperContext = context.copy(root = false, prototypesBeingCreated = SchemaSet(schema))(context.user, context.koodisto, context.localizationRepository)
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
    (tpe :: findTraits(tpe)).map(t => sanitizeName(forName(t.typeSymbol.fullName).getSimpleName))
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
    reflect.runtime.currentMirror.classSymbol(forName(className)).toType
  }

  def prototypeKey = sanitizeName(schema.simpleName)

  private def newContext(obj: AnyRef, metadata: List[Metadata]): ModelBuilderContext = {
    def orgWriteAccess = obj match {
      case e: EsiopetuksenOpiskeluoikeus if e.järjestämismuoto.isDefined && e.oppilaitos.isDefined && e.koulutustoimija.isDefined =>
        context.user.hasVarhaiskasvatusAccess(e.koulutustoimija.get.oid, e.getOppilaitos.oid, AccessType.write)
      case oo: Opiskeluoikeus =>
        oo.omistajaOrganisaatio match {
          case Some(o) => context.user.hasWriteAccess(o.oid, oo.koulutustoimija.map(_.oid))
          case None => true
        }
      case _ => true
    }
    def lähdejärjestelmällinen = obj match {
      case o: Lähdejärjestelmällinen => o.lähdejärjestelmänId.nonEmpty
      case _ => false
    }
    val readOnly: Boolean = metadata.exists(_.isInstanceOf[ReadOnly])
    val editable = context.editable && !lähdejärjestelmällinen && orgWriteAccess && !readOnly

    val invalidatable = obj match {
      case o: Opiskeluoikeus => context.invalidatable && OpiskeluoikeusAccessChecker.isInvalidatable(o, context.user)
      /*
      In case of properties (such as Päätason suoritukset), context is relative to containing object (Opiskeluoikeus, in this case).
      Here, we have already resolved invalidatability for Opiskeluoikeus, so we can 'inherit' the invalidatability for these Päätason suoritukset.
      For other Päätason suoritukset, we explicitly mark invalidatability as false (which may be in contrast to their Opiskeluoikeus).
       */
      case _: PerusopetuksenPäätasonSuoritus |
           _: AikuistenPerusopetuksenPäätasonSuoritus |
           _: EsiopetuksenSuoritus |
           _: AmmatillinenPäätasonSuoritus |
           _: InternationalSchoolVuosiluokanSuoritus |
           _: IBPäätasonSuoritus |
           _: LukionOppiaineenOppimääränSuoritus2015 => context.invalidatable
      case _: PäätasonSuoritus => false
      case _ => context.invalidatable
    }

    context.copy(editable = editable, invalidatable = invalidatable, root = false, prototypesBeingCreated = SchemaSet.empty)(context.user, context.koodisto, context.localizationRepository)
  }
}

object Prototypes {
  def getPrototypePlaceholder(schema: Schema, metadata: List[Metadata])(implicit context: ModelBuilderContext): Option[EditorModel] = if (context.editable) {
    schema match {
      case s: SchemaWithClassName =>
        val clazz = forSchema(s)
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

object SchemaSet {
  val empty = new SchemaSet(Map.empty)
  def apply(schema: SchemaWithClassName): SchemaSet = SchemaSet.empty + schema
}
class SchemaSet(private val schemaMap: Map[Int, SchemaWithClassName]) {
  def nonEmpty: Boolean = schemaMap.nonEmpty
  def foreach(f: (SchemaWithClassName) => Unit): Unit = schemaMap.values.foreach(f)
  def toList = schemaMap.values.toList
  def --(toBeRemoved: SchemaSet): SchemaSet = new SchemaSet(schemaMap -- toBeRemoved.schemaMap.keys)
  def ++(prototypesRequested: SchemaSet): SchemaSet = new SchemaSet(schemaMap ++ prototypesRequested.schemaMap)
  def +(schema: SchemaWithClassName): SchemaSet = new SchemaSet(schemaMap + schemaTuple(schema))

  private def schemaHash(schema: SchemaWithClassName) = schema.hashCode()
  private def schemaTuple(schema: SchemaWithClassName) = (schemaHash(schema) -> schema)
}

object ClassFinder {
  private val classes = collection.mutable.Map.empty[String, Class[_]]
  def forName(fullName: String) = synchronized { classes.getOrElseUpdate(fullName, Class.forName(fullName)) }
  def forSchema(schema: SchemaWithClassName) = forName(schema.fullClassName)
}
