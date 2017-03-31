package fi.oph.koski.editor

import java.time.LocalDate

import fi.oph.koski.editor.EditorModelBuilder._
import fi.oph.koski.koodisto.{KoodistoViitePalvelu, MockKoodistoPalvelu, MockKoodistoViitePalvelu}
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.localization.{Localizable, LocalizedString}
import fi.oph.koski.schema._
import fi.oph.koski.todistus.LocalizedHtml
import fi.oph.scalaschema._
import fi.oph.scalaschema.annotation.Title

object EditorModelBuilder {
  def buildModel(deserializationContext: ExtractionContext, value: AnyRef, editable: Boolean)(implicit user: KoskiSession, koodisto: KoodistoViitePalvelu): EditorModel = {
    val context = ModelBuilderContext(deserializationContext.rootSchema, deserializationContext, editable)
    ObjectModelBuilder(deserializationContext.rootSchema.asInstanceOf[ClassSchema])(context).buildModelForObject(value)
  }

  def builder(schema: Schema)(implicit context: ModelBuilderContext): EditorModelBuilder[Any] = (schema match {
    case t: ClassSchema =>
      Class.forName(t.fullClassName) match {
        case c if classOf[Koodistokoodiviite].isAssignableFrom(c) =>
          KoodistoEnumModelBuilder(t)
        case c if classOf[Oppilaitos].isAssignableFrom(c) =>
          OppilaitosEnumBuilder(t)
        case c if classOf[OrganisaatioWithOid].isAssignableFrom(c) =>
          OrganisaatioEnumBuilder(t)
        case c =>
          ObjectModelBuilder(t)
      }
    case t: ClassRefSchema => modelBuilderForClass(context.mainSchema.getSchema(t.fullClassName).get)
    case t: AnyOfSchema => OneOfModelBuilder(t)
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
        case c if classOf[Oppilaitos].isAssignableFrom(c) =>
          OppilaitosEnumBuilder(t)
        case c if classOf[OrganisaatioWithOid].isAssignableFrom(c) =>
          OrganisaatioEnumBuilder(t)
        case c =>
          ObjectModelBuilder(t)
      }
    case t: ClassRefSchema => modelBuilderForClass(resolveSchema(t))
    case t: AnyOfSchema => OneOfModelBuilder(t)
  }

  def buildModel(obj: Any, schema: Schema)(implicit context: ModelBuilderContext): EditorModel = builder(schema).buildModelForObject(obj)
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
  def buildPrototype: EditorModel
}

trait ModelBuilderWithData[T] extends EditorModelBuilder[T]{
  def getPrototypeData: T
  def buildPrototype = buildModelForObject(getPrototypeData)
}

case class ModelBuilderContext(
  mainSchema: SchemaWithClassName,
  deserializationContext: ExtractionContext,
  editable: Boolean, root: Boolean = true,
  var prototypesRequested: Set[SchemaWithClassName] = Set.empty,
  prototypesBeingCreated: Set[SchemaWithClassName] = Set.empty)(implicit val user: KoskiSession, val koodisto: KoodistoViitePalvelu) extends LocalizedHtml

case class NumberModelBuilder(t: NumberSchema) extends ModelBuilderWithData[Number] {
  override def buildModelForObject(x: Number) = NumberModel(ValueWithData(x))
  override def getPrototypeData = 0
}

case class BooleanModelBuilder(t: BooleanSchema) extends ModelBuilderWithData[Boolean] {
  override def buildModelForObject(x: Boolean) = BooleanModel(ValueWithData(x))
  override def getPrototypeData = false
}

case class StringModelBuilder(t: StringSchema) extends ModelBuilderWithData[String] {
  override def buildModelForObject(x: String) = StringModel(ValueWithData(x))
  override def getPrototypeData = ""
}

case class DateModelBuilder(t: DateSchema) extends ModelBuilderWithData[LocalDate] {
  override def buildModelForObject(x: LocalDate) = DateModel(ValueWithData(x))
  override def getPrototypeData = LocalDate.now
}

case class OptionalModelBuilder(t: OptionalSchema)(implicit context: ModelBuilderContext) extends EditorModelBuilder[Any] {
  override def buildModelForObject(x: Any) = {
    val innerModel = x match {
      case x: Option[_] => x.map(value => buildModel(value, t.itemSchema))
      case x: AnyRef => Some(buildModel(x, t.itemSchema))
    }
    OptionalModel(
      innerModel,
      Prototypes.getPrototypePlaceholder(t.itemSchema)
    )
  }
  def buildPrototype = OptionalModel(
    None,
    Prototypes.getPrototypePlaceholder(t.itemSchema)
  )
}

case class ListModelBuilder(t: ListSchema)(implicit context: ModelBuilderContext) extends EditorModelBuilder[Iterable[_]] {
  def buildModelForObject(xs: Iterable[_]) = {
    val models: List[EditorModel] = xs.toList.map(item => buildModel(item, t.itemSchema))
    ListModel(models, Prototypes.getPrototypePlaceholder(t.itemSchema))
  }
  def buildPrototype: EditorModel = ListModel(Nil, Prototypes.getPrototypePlaceholder(t.itemSchema))
}

trait ModelBuilderForClass extends EditorModelBuilder[AnyRef] {
  def buildModelForObject(obj: AnyRef): EditorModel
  def prototypeKey: String
  def buildPrototypePlaceholder = PrototypeModel(prototypeKey)
  def buildPrototype: EditorModel
}

case class OppilaitosEnumBuilder(t: ClassSchema)(implicit context: ModelBuilderContext) extends EnumModelBuilder[OrganisaatioWithOid] {
  override def alternativesPath = "/koski/api/editor/oppilaitokset"
  override def toEnumValue(o: OrganisaatioWithOid) = organisaatioEnumValue(context)(o)
  def buildPrototype: EditorModel = buildModelForObject(Oppilaitos(""))
}

case class OrganisaatioEnumBuilder(t: ClassSchema)(implicit context: ModelBuilderContext) extends EnumModelBuilder[OrganisaatioWithOid] {
  override def alternativesPath = "/koski/api/editor/organisaatiot"
  override def toEnumValue(o: OrganisaatioWithOid) = organisaatioEnumValue(context)(o)
  def buildPrototype: EditorModel = buildModelForObject(OidOrganisaatio(""))
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
  def defaultValue = koodiarvot.headOption.orElse(KoodistoEnumModelBuilder.defaults.get(koodistoUri)).getOrElse(context.koodisto.getKoodistoKoodiViitteet(context.koodisto.koodistoPalvelu.getLatestVersion(koodistoUri).get).get.head.koodiarvo)
  def getPrototypeData = MockKoodistoViitePalvelu.validate(Koodistokoodiviite(defaultValue, koodistoUri)).get
  def buildPrototype: EditorModel = buildModelForObject(getPrototypeData)
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

case class OneOfModelBuilder(t: AnyOfSchema)(implicit context: ModelBuilderContext) extends ModelBuilderForClass {
  def buildModelForObject(obj: AnyRef) = obj match {
    case None =>
      throw new RuntimeException("None value not allowed for AnyOf")
    case x: AnyRef =>
      val selectedModel = EditorModelBuilder.buildModel(x, findOneOfSchema(t, x))
      buildModel(selectedModel)
  }

  private def buildModel(selectedModel: EditorModel) = {
    OneOfModel(sanitizeName(t.simpleName), selectedModel, t.alternatives.flatMap(Prototypes.getPrototypePlaceholder(_)))
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

  def buildPrototype: EditorModel = {
    val clazz: Class[_] = Class.forName(t.fullClassName)
    if (clazz == classOf[LocalizedString]) {
      buildModelForObject(LocalizedString.finnish(""))
    } else {
      buildModel(modelBuilderForClass(t.alternatives.head).buildPrototype)
    }
  }
}

case class ObjectModelBuilder(schema: ClassSchema)(implicit context: ModelBuilderContext) extends ModelBuilderForClass {
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
    context.prototypesRequested = context.prototypesRequested ++ objectContext.prototypesRequested
    val includedPrototypes: Map[String, EditorModel] = if (context.root) {
      createRequestedPrototypes
    } else {
      Map.empty
    }
    ObjectModel(classes(schema.fullClassName), properties, objectTitle, objectContext.editable, includedPrototypes)
  }

  def buildPrototype = {
    val properties: List[EditorProperty] = schema.properties.map{property =>
      val propertyPrototype = Prototypes.getPrototypePlaceholder(property.schema).get
      createModelProperty(property, propertyPrototype)
    }
    val objectTitle = None
    ObjectModel(classes(schema.fullClassName), properties, title = None, true, Map.empty)
  }

  private def createModelProperty(obj: AnyRef, objectContext: ModelBuilderContext, property: Property): EditorProperty = {
    val value = schema.getPropertyValue(property, obj)
    val propertyModel = EditorModelBuilder.buildModel(value, property.schema)(objectContext)

    createModelProperty(property, propertyModel)
  }

  def createModelProperty(property: Property, propertyModel: EditorModel): EditorProperty = {
    val hidden = property.metadata.contains(Hidden())
    val representative: Boolean = property.metadata.contains(Representative())
    val flatten: Boolean = property.metadata.contains(Flatten())
    val complexObject: Boolean = property.metadata.contains(ComplexObject())
    val tabular: Boolean = property.metadata.contains(Tabular())
    val readOnly: Boolean = property.metadata.find(_.isInstanceOf[ReadOnly]).isDefined
    val propertyTitle = property.metadata.flatMap {
      case Title(t) => Some(t)
      case _ => None
    }.headOption.getOrElse(property.key.split("(?=\\p{Lu})").map(_.toLowerCase).mkString(" ").replaceAll("_ ", "-").capitalize)
    EditorProperty(property.key, propertyTitle, propertyModel, hidden, representative, flatten, complexObject, tabular, !readOnly)
  }

  private def createRequestedPrototypes: Map[String, EditorModel] = {
    var newRequests: Set[SchemaWithClassName] = context.prototypesRequested
    var prototypesCreated: Map[String, EditorModel] = Map.empty
    do {
      val requestsFromPreviousRound = newRequests
      newRequests = Set.empty
      requestsFromPreviousRound.foreach { schema =>
        val helperContext = context.copy(root = false, prototypesBeingCreated = Set(schema))(context.user, context.koodisto)
        val modelBuilderForProto = modelBuilderForClass(schema)(helperContext)
        val (protoKey, model) = (modelBuilderForProto.prototypeKey, modelBuilderForProto.buildPrototype)

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
  def getPrototypePlaceholder(schema: Schema)(implicit context: ModelBuilderContext): Option[EditorModel] = if (context.editable) {
    schema match {
      case s: SchemaWithClassName =>
        val classRefSchema = resolveSchema(s)
        context.prototypesRequested += classRefSchema
        Some(modelBuilderForClass(s).buildPrototypePlaceholder)
      case _ => Some(builder(schema).buildPrototype)
    }
  } else {
    None
  }
}
