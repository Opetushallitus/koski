package fi.oph.koski.typemodel

import fi.oph.koski.koskiuser.UserWithAccessRights
import fi.oph.koski.oppija.HenkilönOpiskeluoikeusVersiot
import fi.oph.koski.organisaatio.OrganisaatioHierarkia
import fi.oph.koski.preferences.KeyValue
import fi.oph.koski.schema.{Arviointi, KoodiViite, KoskiSchema, OpiskeluoikeudenTila, Opiskeluoikeusjakso, StorablePreference}
import fi.oph.koski.typemodel.TypescriptTypes.Options

import java.io.{BufferedWriter, File, FileWriter}
import java.nio.file.{Files, Paths}

object TsFileUpdater {
  def updateTypeFiles(): Unit = {
    val types =
      SchemaExport.toTypeDef(KoskiSchema.schema) ++
      TypeExport.toTypeDef(classOf[AdditionalExports]).filter(t => !AdditionalExports.getClass.getName.startsWith(t.fullClassName))

    TypescriptTypes.build(types, options).foreach(writeFile)
  }

  private def writeFile(tsFile: TypescriptTypes.TsFile): Unit = {
    val directory = Paths.get(targetPath, tsFile.directory)
    val filePath = Paths.get(directory.toString, tsFile.fileName)

    Files.createDirectories(directory)
    val file = new File(filePath.toString)
    val bw = new BufferedWriter(new FileWriter(file))
    bw.write(tsFile.content)
    bw.close()
  }

  private def options: Options = Options(
    generics = KoskiSpecificTsGenerics.generics,
    exportClassNamesAs = Some("$class"),
    exportTypeGuards = true,
    exportConstructors = true,
    exportJsDoc = true,
  )

  def targetPath: String = "web/app/types"
}

case class AdditionalExports(
  // Rajapintojen palauttamia rakenteita
  constraint: Constraint,
  putOppijaApiResponse: HenkilönOpiskeluoikeusVersiot,
  getKoodistoApiResponse: GroupedKoodistot,
  organisaatiohierarkia: OrganisaatioHierarkia,
  storablePreference: StorablePreference,
  storablePreferenceKeyValue: KeyValue,
  userWithAccessRights: UserWithAccessRights,

  // Traitit jotka eivæt automaattisesti exporttaudu skeemasta, koska ne eivät sellaisenaan
  // ole minkään tietomallin jäseniä (ainoastaan niistä periytyvät luokat on mainittu).
  opiskeluoikeudenTila: OpiskeluoikeudenTila,
  opiskeluoikeusjakso: Opiskeluoikeusjakso,
  arviointi: Arviointi,
  koodiviite: KoodiViite,
)
