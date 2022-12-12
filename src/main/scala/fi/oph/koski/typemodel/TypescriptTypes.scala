package fi.oph.koski.typemodel

import fi.oph.koski.util.JsStringInterpolation.JsStringInterpolation

import scala.annotation.tailrec

object TypescriptTypes {
  def build(types: Seq[TypeModelWithClassName], options: Options = Options()): String =
    types
      .groupBy(_.packageName)
      .map { case (packageName, types) => toPackageDefinitions(packageName, types, options) }
      .mkString("\n\n")

  private def toPackageDefinitions(
    packageName: String,
    types: Seq[TypeModelWithClassName],
    options: Options,
  ): String = {
    var result = "/*\n" +
      s" * ${packageName}\n" +
      " */\n\n" +
        types.map(t => toTypeDefinition(t, options)).mkString("\n\n")

    if (options.exportTypeGuards) {
      result = result +
        "\n\n// Type guards\n\n" ++
        types.map(t => typeGuard(t, options.exportClassNamesAs.get)).mkString("\n\n")
    }

    result
      .split("\n")
      .map(line => line.stripTrailing())
      .mkString("\n")
  }

  private def toTypeDefinition(
    model: TypeModelWithClassName,
    options: Options,
  ): String = {
    val generic = options.getGeneric(model)
    val genericsHeader = generic
      .map(_.genericsList)
      .getOrElse("")

    s"export type ${model.className}$genericsHeader = ${toFirstLevelDefinition(model, options)}"
  }

  private def toFirstLevelDefinition(
    model: TypeModel,
    options: Options,
  ): String =
    model match {
      case t: ObjectType => toObject(t, options)
      case t: TypeModel => toDefinition(t, options)
    }

  private def toDefinition(
    model: TypeModel,
    options: Options,
  ): String =
    model match {
      case _: StringType => "string"
      case _: DateType => "string"
      case _: BooleanType => "boolean"
      case _: NumberType => "number"

      case OptionalType(t) => s"${toDefinition(t, options)} | undefined"
      case ArrayType(t) => s"Array<${toDefinition(t, options)}>"
      case RecordType(t) => s"Record<string, ${toDefinition(t, options)}>"
      case t: ObjectType  => objectReference(t, options)
      case t: ClassRef => t.className
      case t: EnumType[_] => t.enumValues.map(formatLiteral).mkString(" | ")
      case t: UnionType => typeUnion(t, options)
      case t: LiteralType => "\"" + t.literal + "\""

      case t: Any => s"any // ${t}"
    }

  private def toObject(
    obj: ObjectType,
    options: Options
  ): String = {
    val generic = options.getGeneric(obj)
    val metaProps = options.exportClassNamesAs.toList
      .map(key => toPropertyField(key, LiteralType(obj.className), generic, options))
    val objProps = obj.properties
      .map(prop => toPropertyField(prop._1, prop._2, generic, options))
    val props = (metaProps ++ objProps).mkString(",\n")
    s"{\n$props\n}"
  }

  private def toPropertyField(
    key: String,
    model: TypeModel,
    parentGeneric: Option[GenericsObject],
    options: Options,
  ): String = {
    val isOptional = model.isInstanceOf[OptionalType]

    @tailrec
    def getPropType(t: TypeModel): String =
      t match {
        case o: OptionalType => getPropType(o.item)
        case t: TypeModel => toDefinition(t, options)
      }

    indent +
      key +
      (if (isOptional) "?: " else ": ") +
      parentGeneric
        .flatMap(_.internalTypeName(key))
        .getOrElse(getPropType(model))
  }

  private def formatLiteral(value: Any): String = js"$value"

  private def objectReference(obj: ObjectType, options: Options): String = {
    val refTypes = options.getGeneric(obj)
      .map(_.getRefTypes(obj))
      .map(_.map(_.fold(identity, fb => toDefinition(fb, options))))
    val header = refTypes.map(t => s"<${t.mkString(", ")}>")
    obj.className + header.getOrElse("")
  }

  private def typeUnion(union: UnionType, options: Options): String = {
    // Self references are a result of using @ReadFlattened annotation in schema
    val (selfReferences, alternatives) = union.anyOf.partition {
      case a: TypeModelWithClassName => union.fullClassName == a.fullClassName
    }
    val list = selfReferences.map(t => toFirstLevelDefinition(t, options)) ++ alternatives.map(t => toDefinition(t, options))
    "\n" + list.map(alt => s"$indent| $alt").mkString("\n")
  }

  private def typeGuard(model: TypeModelWithClassName, classNameProp: String): String = {
    def fnName(className: String): String = s"is$className"
    def buildFn(className: String, condition: String): String =
      s"export const is$className = (a: any): a is $className => $condition"
    model match {
      case t: UnionType =>
        buildFn(
          t.className,
          t.anyOf
            .collect { case t: TypeModelWithClassName => t }
            .map(s => s"${fnName(s.className)}(a)")
            .mkString(" || ")
        )
      case t: TypeModelWithClassName => buildFn(t.className, s"""a?.$classNameProp === "${t.className}"""")
    }
  }

  val indent: String = " " * 4

  case class Options(
    generics: Seq[GenericsObject] = Seq.empty,
    exportClassNamesAs: Option[String] = None,
    exportTypeGuards: Boolean = false,
  ) {
    def getGeneric(model: TypeModel): Option[GenericsObject] =
      model match {
        case t: TypeModelWithClassName => generics.find(_.isClass(t.fullClassName))
        case _ => None
      }
  }

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
    def headerStr: String = s"$name extends $extend = $extend"
  }
}

