package fi.oph.koski.typemodel

import fi.oph.scalaschema._

import scala.language.implicitConversions

object SchemaExport {
  case class Context(
    followClassRefs: Boolean,
    depth: Int,
    stopParsingAt: List[String]
  ) {
    def next(): Context = copy(depth = depth - 1)
    def isEnd: Boolean = depth <= 0
    def isEnd(className: String): Boolean = depth <= 0 || stopParsingAt.contains(className)
  }

  def toTypeDef(schema: Schema, followClassRefs: Boolean = false, maxDepth: Int = Int.MaxValue, stopParsingAt: List[String] = Nil): Seq[TypeModelWithClassName] =
    parse(schema, Context(followClassRefs, maxDepth, stopParsingAt)).collect { case s: TypeModelWithClassName => s }

  private def parse(schema: Schema, ctx: Context): Seq[TypeModel] =
    Seq(parseSchema(schema, ctx)) ++ parseAssociatedSchemas(schema, ctx)

  private def parseSchema(schema: Schema, ctx: Context): TypeModel = {
    (schema match {
      case s: SchemaWithClassName if ctx.isEnd(s.fullClassName) => BoundaryType(s.fullClassName)
      case StringSchema(enumValues) =>
        enumValues match {
          case Some(enumValues) => EnumType(DataTypes.String, enumValues)
          case None => StringType()
        }
      case BooleanSchema(enumValues) =>
        enumValues match {
          case Some(enumValues) => EnumType(DataTypes.Boolean, enumValues)
          case None => BooleanType()
        }
      case NumberSchema(numberType, enumValues) =>
        enumValues match {
          case Some(enumValues) => EnumType(DataTypes.Number, enumValues)
          case None => NumberType() // TODO: Erottele desimaalien määrä nollaksi inteille
        }
      case DateSchema(_) =>
        DateType() // TODO: Pitää varmaan tukea myös kellonaikoja
      case OptionalSchema(itemSchema) =>
        OptionalType(parseSchema(itemSchema, ctx.next()))
      case ListSchema(itemSchema) =>
        ArrayType(parseSchema(itemSchema, ctx.next()))
      case MapSchema(itemSchema) =>
        RecordType(parseSchema(itemSchema, ctx.next()))
      case schema: ClassSchema if schema.readFlattened.isDefined =>
        parseSchema(schema.asAnyOfSchema, ctx.next())
      case classSchema: ClassSchema =>
        parseClassSchema(classSchema, ctx.next())
      case classRef: ClassRefSchema =>
        ClassRef(classRef.fullClassName)
      case anyOf: AnyOfSchema =>
        UnionType(
          fullClassName = anyOf.fullClassName,
          anyOf = anyOf.alternatives.map(parseSchema(_, ctx.next()))
        )
      case flattenedSchema: FlattenedSchema =>
        parseSchema(flattenedSchema.property.schema, ctx.next())
      case _: AnyObjectSchema =>
        AnyObjectType()
      case _: AnyListSchema =>
        AnyArrayType()
      case anySchema: AnySchema =>
        AnyType(anySchema.toString)
    }).withMetadata(schema.metadata)
  }

  private def parseAssociatedSchemas(schema: Schema, ctx: Context): Seq[TypeModel] = {
    schema match {
      case _ if ctx.isEnd => Seq.empty
      case classSchema: ClassSchema => classSchema.definitions.flatMap(clss => parse(clss, ctx.next()))
      case classRefSchema: ClassRefSchema if ctx.followClassRefs =>
        // println(s"Parse associated schemas: ref ${classRefSchema.fullClassName}")
        TypeExport.getObjectModels(Class.forName(classRefSchema.fullClassName))
      case anyOfSchema: AnyOfSchema =>
        // println(s"Parse associated schemas: anyOf ${anyOfSchema.fullClassName}")
        anyOfSchema.alternatives.flatMap(alt => parseAssociatedSchemas(alt, ctx.next()))
      case _ => Seq.empty
    }
  }

  private def parseClassSchema(classSchema: ClassSchema, ctx: Context): TypeModel = {
    val nextCtx = ctx.next()
    val propTypes = classSchema.properties.map(p => parseSchema(p.schema, nextCtx).withMetadata(p.metadata))
    val propKeys = classSchema.properties.map(_.key)
    ObjectType(
      fullClassName = classSchema.fullClassName,
      properties = propKeys.zip(propTypes).toMap,
    ).withMetadata(classSchema.metadata)
  }
}
