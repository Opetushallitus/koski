package fi.oph.koski.typemodel

import fi.oph.scalaschema.SchemaFactory

object TypeExport {
  def toTypeDef(clss: Class[_]): Seq[TypeModelWithClassName] = {
    val schemaFactory: SchemaFactory = SchemaFactory()
    val schema = schemaFactory.createSchema(clss)
    SchemaExport.toTypeDef(schema)
  }
}
