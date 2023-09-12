package fi.oph.koski.typemodel

import fi.oph.koski.typemodel.ClassNameResolver.{nameToAbsolutePath, nameToRelativePath, safeFilename, safePath}
import fi.oph.koski.util.JsStringInterpolation.{JsStringInterpolation, parseExpression}

import java.nio.file.{Path, Paths}
import scala.annotation.tailrec

object TypescriptTypes {
  def build(types: Seq[TypeModelWithClassName], options: Options = Options()): Seq[TsFile] =
    types.map(model => buildFile(model, types, options))

  private def buildFile(
    typeModel: TypeModelWithClassName,
    types: Seq[TypeModelWithClassName],
    options: Options,
  ): TsFile = {
    var content = toImports(typeModel, options) + "\n\n"

    content += toTypeDefinition(typeModel, options) + "\n\n"

    if (options.exportConstructors) {
      content += (typeModel match {
        case o: ObjectType => objectConstructor(o, types, options) + "\n\n"
        case _ => ""
      })
    }

    if (options.exportTypeGuards) {
      content += typeGuard(typeModel, options.exportClassNamesAs.get) + "\n\n"
    }

    TsFile(
      directory = safePath(nameToRelativePath(typeModel.packageName)),
      fileName = safeFilename(s"${typeModel.className}.ts"),
      content = content,
    )
  }

  private def toImports(
    model: TypeModelWithClassName,
    options: Options,
  ): String = {
    def importRow(fullClassName: String): String = {
      val className = ClassNameResolver.className(fullClassName)
      val imports = List(
        Some(className),
        if (options.exportTypeGuards && model.isInstanceOf[UnionType]) Some(s"is$className") else None
      ).flatten
      s"""import { ${imports.mkString(", ")} } from "${getImportPath(model.fullClassName, fullClassName)}""""
    }

    model
      .dependencies
      .filterNot(_ == model.fullClassName)
      .map(importRow)
      .mkString("\n")
  }

  private def getImportPath(importerFullClassName: String, importedFullClassName: String): String = {
    val importer = Paths.get(nameToAbsolutePath(importerFullClassName))
    val imported = Paths.get(nameToAbsolutePath(importedFullClassName))
    val path = safePath(importer.getParent.relativize(imported).toString)
    if (path.startsWith(".")) path else s"./$path"
  }

  private def toTypeDefinition(
    model: TypeModelWithClassName,
    options: Options,
  ): String = {
    val generic = options.getGeneric(model)
    val genericsHeader = generic
      .map(_.genericsList)
      .getOrElse("")

    val jsDoc = if (options.exportJsDoc) {
      val jsDocLines =
        (if (model.description.nonEmpty) model.description else List(model.className)) ++ List(
          "",
          s"@see `${model.fullClassName}`",
        )

      (List("/**") ++ jsDocLines.map(l => s" * $l") ++ List(" */")).mkString("\n")
    } else {
      ""
    }

    s"$jsDoc\nexport type ${model.className}$genericsHeader = ${toFirstLevelDefinition(model, options)}"
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

      case OptionalType(t, _, _, _) => s"${toDefinition(t, options)} | undefined"
      case ArrayType(t) => s"Array<${toDefinition(t, options)}>"
      case RecordType(t) => s"Record<string, ${toDefinition(t, options)}>"
      case t: ObjectType  => objectReference(t, options)
      case t: ClassRef => t.className
      case t: EnumType[_] => t.enumValues.map(formatLiteral).mkString(" | ")
      case t: UnionType => typeUnion(t, options)
      case t: LiteralType => "\"" + t.literal + "\""

      case _: AnyObjectType => "object"
      case _: AnyArrayType => "any[]"
      case _: AnyType => "any"
      case t: Any => s"any /* ${t} */"
    }

  private def toObject(
    obj: ObjectType,
    options: Options
  ): String = {
    val generic = options.getGeneric(obj)
    val metaProps = options.exportClassNamesAs.toList
      .map(key => toPropertyField(key, LiteralType(obj.fullClassName), generic, options))
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
      case t: TypeModelWithClassName => buildFn(t.className, s"""a?.$classNameProp === "${t.fullClassName}"""")
    }
  }

  private def objectConstructor(obj: ObjectType, allTypes: Seq[TypeModelWithClassName], options: Options): String = {
    def getClassProp(t: TypeModelWithClassName): Seq[(String, ObjectType.ObjectDefaultNode)] =
      options.exportClassNamesAs.toList.map(key => (key, ObjectType.ObjectDefaultsProperty(t.fullClassName)))

    def resolveKnownProps(o: ObjectType): Map[String, Any] =
      o.properties
        .mapValues {
          case ref: ClassRef => ref.resolve(allTypes).getOrElse(ref)
          case p: TypeModel => p
        }
        .mapValues(_.unambigiousDefaultValue)
        .flatMap { case (key, defaults) => defaults.map(d => (key, d)) }

    def valueToTs(value: Any, assignToObject: Option[String] = None): String = value match {
      case obj: Map[_, _] =>
        "{" + obj.flatMap {
          case (_, None) => None
          case (_, ObjectType.ObjectDefaultsProperty(None)) => None
          case (key, value) => Some(s"$key: ${valueToTs(value)}")
        }.mkString(",") + assignToObject.map(t => s", ...$t").getOrElse("") + "}"
      case obj: ObjectType.ObjectDefaultsMap =>
        s"${obj.className}(${valueToTs(obj.properties)})"
      case ObjectType.ObjectDefaultsProperty(default) =>
        parseExpression(default)
      case any: Any =>
        parseExpression(any)
    }

    val knownProperties = resolveKnownProps(obj)
    val argType = obj.makeOptional(knownProperties.keys.toList)
    val argTypeStr = toObject(argType, options.copy(exportClassNamesAs = None))
    val defaultArg = if (argType.properties.values.forall(_.isInstanceOf[OptionalType])) " = {}" else ""

    val fnName = obj.className
    val generics = options.getGeneric(obj).map(_.genericsList).getOrElse("")
    val arguments = s"$generics(o: $argTypeStr$defaultArg)"
    val returnType = objectConstructorReturnValue(obj, options)

    val properties = getClassProp(obj) ++ knownProperties
    val fnBody = s"(${valueToTs(properties.toMap, Some("o"))})"

    List(
      s"export const $fnName = $arguments: $returnType => $fnBody",
      s"""$fnName.className = "${obj.fullClassName}" as const""",
    ).mkString("\n\n")
  }

  private def objectConstructorReturnValue(obj: ObjectType, options: Options): String =
    obj.className + options.getGeneric(obj).map(_.returnValueGenerics).getOrElse("")

  val indent: String = " " * 4

  case class Options(
    generics: Seq[GenericsObject] = Seq.empty,
    exportClassNamesAs: Option[String] = None,
    exportTypeGuards: Boolean = false,
    exportConstructors: Boolean = false,
    exportJsDoc: Boolean = false,
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
    def returnValueGenerics: String = s"<${mapping.values.map(_.name).mkString(", ")}>"
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

  case class TsFile(
    directory: String,
    fileName: String,
    content: String
  ) {
    def fullPath(targetPath: String): Path =
      Paths.get(targetPath, directory, fileName)
  }
}
