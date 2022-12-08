package fi.oph.koski.typemodel

import fi.oph.koski.util.JsStringInterpolation.JsStringInterpolation

import scala.annotation.tailrec

object TypescriptTypes {
  def build(types: Seq[TypeModelWithClassName], generics: Seq[GenericsObject] = Seq.empty): String =
    types
      .groupBy(_.packageName)
      .map { case (packageName, types) => toPackageDefinitions(packageName, types, generics) }
      .mkString("\n\n")

  private def toPackageDefinitions(
    packageName: String,
    types: Seq[TypeModelWithClassName],
    generics: Seq[GenericsObject],
  ): String =
    "/*\n" +
    s" * ${packageName}\n" +
    " */\n\n" +
      types.map(t => toTypeDefinition(t, generics)).mkString("\n\n")

  private def toTypeDefinition(model: TypeModelWithClassName, generics: Seq[GenericsObject]): String = {
    val generic = getGeneric(model, generics)
    val genericsHeader = generic
      .map(_.genericsList)
      .getOrElse("")

    s"export type ${model.className}$genericsHeader = ${toFirstLevelDefinition(model, generics)}"
  }

  private def toFirstLevelDefinition(model: TypeModel, generics: Seq[GenericsObject]): String = {
    val generic = getGeneric(model, generics)
    model match {
      case t: ObjectType =>
        val props = t.properties
          .map(prop => toPropertyField(prop._1, prop._2, generic, generics))
          .mkString(",\n")
        s"{\n$props\n}"
      case t: TypeModel => toDefinition(t, generics)
    }
  }

  private def toDefinition(model: TypeModel, generics: Seq[GenericsObject]): String =
    model match {
      case _: StringType => "string"
      case _: DateType => "string"
      case _: BooleanType => "boolean"
      case _: NumberType => "number"

      case OptionalType(t) => s"${toDefinition(t, generics)} | undefined"
      case ArrayType(t) => s"Array<${toDefinition(t, generics)}>"
      case RecordType(t) => s"Record<string, ${toDefinition(t, generics)}>"
      case t: ObjectType  => objectReference(t, generics)
      case t: ClassRef => t.className
      case t: EnumType[_] => t.enumValues.map(formatLiteral).mkString(" | ")
      case t: UnionType => typeUnion(t, generics)

      case t: Any => s"any // ${t}"
    }

  private def toPropertyField(
    key: String,
    model: TypeModel,
    parentGeneric: Option[GenericsObject],
    generics: Seq[GenericsObject],
  ): String = {
    val isOptional = model.isInstanceOf[OptionalType]

    @tailrec
    def getPropType(t: TypeModel): String =
      t match {
        case o: OptionalType => getPropType(o.item)
        case t: TypeModel => toDefinition(t, generics)
      }

    indent +
      key +
      (if (isOptional) "?: " else ": ") +
      parentGeneric
        .flatMap(_.internalTypeName(key))
        .getOrElse(getPropType(model))
  }

  private def formatLiteral(value: Any): String = js"$value"

  private def objectReference(obj: ObjectType, generics: Seq[GenericsObject]): String = {
    val refTypes = getGeneric(obj, generics)
      .map(_.getRefTypes(obj))
      .map(_.map(_.fold(identity, fb => toDefinition(fb, generics))))
    val header = refTypes.map(t => s"<${t.mkString(", ")}>")
    obj.className + header.getOrElse("")
  }

  private def typeUnion(union: UnionType, generics: Seq[GenericsObject]): String = {
    // Self references are a result of using @ReadFlattened annotation in schema
    val (selfReferences, alternatives) = union.anyOf.partition {
      case a: TypeModelWithClassName => union.fullClassName == a.fullClassName
    }
    val list = selfReferences.map(t => toFirstLevelDefinition(t, generics)) ++ alternatives.map(t => toDefinition(t, generics))
    "\n" + list.map(alt => s"$indent| $alt").mkString("\n")
  }

  private def getGeneric(model: TypeModel, generics: Seq[GenericsObject]): Option[GenericsObject] =
    model match {
      case t: TypeModelWithClassName => generics.find(_.isClass(t.fullClassName))
      case _ => None
    }

  val indent: String = " " * 4

  case class GenericsObject(
    className: String,
    mapping: Map[String, GenericsProperty],
  ) {
    def isClass(name: String): Boolean = name == className
    def genericsList: String = s"<${mapping.values.map(_.headerStr).mkString(", ")}>"
    def internalTypeName(key: String): Option[String] = mapping.get(key).map(_.name)

    def getType(key: String, model: TypeModel): Option[TypeModel] =
      mapping.get(key).flatMap(_.getType(model))

    def getRefTypes(model: ObjectType): Seq[Either[String, TypeModel]] =
      mapping.map { case (key, generic) =>
        val propType = model.properties(key)
        generic.getType(propType).toRight(generic.extend)
      }.toSeq
  }

  case class GenericsProperty(
    name: String,
    extend: String,
    getType: TypeModel => Option[TypeModel],
  ) {
    def headerStr: String = s"$name extends $extend"
  }
}

