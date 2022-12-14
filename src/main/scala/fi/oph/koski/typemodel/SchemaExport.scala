package fi.oph.koski.typemodel

import fi.oph.scalaschema._
import fi.oph.scalaschema.annotation.DefaultValue

import scala.language.implicitConversions

object SchemaExport {
  def toTypeDef(schema: Schema): Seq[TypeModelWithClassName] =
    parse(schema).collect { case s: TypeModelWithClassName => s }

  private def parse(schema: Schema): Seq[TypeModel] =
    Seq(parseSchema(schema)) ++ parseAssociatedSchemas(schema)

  private def parseSchema(schema: Schema): TypeModel = {
    val x = schema.metadata
    (schema match {
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
        OptionalType(parseSchema(itemSchema))
      case ListSchema(itemSchema) =>
        ArrayType(parseSchema(itemSchema))
      case MapSchema(itemSchema) =>
        RecordType(parseSchema(itemSchema))
      case schema: ClassSchema if schema.readFlattened.isDefined =>
        parseSchema(schema.asAnyOfSchema)
      case classSchema: ClassSchema =>
        parseClassSchema(classSchema)
      case classRef: ClassRefSchema =>
        ClassRef(classRef.fullClassName)
      case anyOf: AnyOfSchema =>
        UnionType(
          fullClassName = anyOf.fullClassName,
          anyOf = anyOf.alternatives.map(parseSchema)
        )
      case flattenedSchema: FlattenedSchema =>
        parseSchema(flattenedSchema.property.schema)
      case anySchema: AnySchema =>
        AnyType(anySchema.toString)
    }).withMetadata(schema.metadata)
  }

  private def parseAssociatedSchemas(schema: Schema): Seq[TypeModel] = schema match {
    case classSchema: ClassSchema => classSchema.definitions.flatMap(parse)
    case _ => Seq.empty
  }

  private def parseClassSchema(classSchema: ClassSchema): TypeModel = {
    val propTypes = classSchema.properties.map(p => parseSchema(p.schema).withMetadata(p.metadata))
    val propKeys = classSchema.properties.map(_.key)
    ObjectType(
      fullClassName = classSchema.fullClassName,
      properties = propKeys.zip(propTypes).toMap,
    ).withMetadata(classSchema.metadata)
  }
}
