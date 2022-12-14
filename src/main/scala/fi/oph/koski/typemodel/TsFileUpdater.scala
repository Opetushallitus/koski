package fi.oph.koski.typemodel

import fi.oph.koski.schema.KoskiSchema
import fi.oph.koski.typemodel.TypescriptTypes.Options

import java.io.{BufferedWriter, File, FileWriter}

object TsFileUpdater {
  def updateAll(): Unit = {
    updateKoskiSchema()
  }

  def updateKoskiSchema(): Unit = {
    val types = SchemaExport.toTypeDef(KoskiSchema.schema)
    val source = TypescriptTypes.build(types, options)
    writeFile(s"$path/schema.ts", source)
  }

  private def writeFile(name: String, source: String): Unit = {
    val file = new File(name)
    val bw = new BufferedWriter(new FileWriter(file))
    bw.write(source)
    bw.close()
  }

  def options: Options = Options(
    generics = KoskiSpecificTsGenerics.generics,
    exportClassNamesAs = Some("$class"),
    exportTypeGuards = true,
    exportConstructors = true,
  )
  def path: String = "web/app/types/imported"
}
