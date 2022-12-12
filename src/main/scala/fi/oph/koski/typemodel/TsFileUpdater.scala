package fi.oph.koski.typemodel

import fi.oph.koski.schema.KoskiSchema

import java.io.{BufferedWriter, File, FileWriter}

object TsFileUpdater {
  def updateAll(): Unit = {
    updateKoskiSchema()
  }

  def updateKoskiSchema(): Unit = {
    val types = SchemaExport.toTypeDef(KoskiSchema.schema)
    val source = TypescriptTypes.build(types, generics)
    writeFile(s"$path/schema.ts", source)
  }

  private def writeFile(name: String, source: String): Unit = {
    val file = new File(name)
    val bw = new BufferedWriter(new FileWriter(file))
    bw.write(source)
    bw.close()
  }

  def generics: Seq[GenericsObject] = KoskiSpecificTsGenerics.generics
  def path: String = "web/app/types/imported"
}
