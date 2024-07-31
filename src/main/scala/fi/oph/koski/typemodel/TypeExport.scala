package fi.oph.koski.typemodel

import fi.oph.scalaschema.SchemaFactory

object TypeExport {
  def toTypeDef(clss: Class[_], followClassRefs: Boolean = false): Seq[TypeModelWithClassName] = {
    val schemaFactory: SchemaFactory = SchemaFactory()
    val schema = schemaFactory.createSchema(clss)
    SchemaExport.toTypeDef(schema, followClassRefs)
  }

  def getObjectModels(clss: Class[_]): List[ObjectType] =
    getTypeModel(clss).toList.flatMap(getObjectModels)


  def getObjectModels(model: TypeModel): List[ObjectType] =
    model match {
      case a: ArrayType => getObjectModels(a.items)
      case o: ObjectType => List(o)
      case u: UnionType => u.anyOf.flatMap(getObjectModels)
      case m: ClassRef => getTypeModel(m).toList.flatMap(getObjectModels)
      case _ => List.empty
    }

  def getTypeModel(clss: Class[_]): Option[TypeModelWithClassName] =
    toTypeDef(clss).find(_.fullClassName == clss.getName)

  def getTypeModel(model: TypeModelWithClassName): Option[TypeModelWithClassName] =
    getTypeModel(Class.forName(model.fullClassName))
}
