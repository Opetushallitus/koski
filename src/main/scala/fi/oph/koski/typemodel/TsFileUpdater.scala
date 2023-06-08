package fi.oph.koski.typemodel

import fi.oph.koski.history.OpiskeluoikeusHistoryPatch
import fi.oph.koski.koskiuser.UserWithAccessRights
import fi.oph.koski.oppija.HenkilönOpiskeluoikeusVersiot
import fi.oph.koski.organisaatio.OrganisaatioHierarkia
import fi.oph.koski.preferences.KeyValue
import fi.oph.koski.schema._
import fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotOppija
import fi.oph.koski.suoritusjako.suoritetuttutkinnot.{SuoritetutTutkinnotKoskeenTallennettavaOpiskeluoikeus, SuoritetutTutkinnotOppija}
import fi.oph.koski.typemodel.TypescriptTypes.Options
import fi.oph.koski.ytr.YtrCertificateResponse

import java.io.{BufferedWriter, File, FileWriter}
import java.nio.file.{Files, Paths}

object TsFileUpdater {
  def updateTypeFiles(dryRun: Boolean = false): Seq[TypescriptTypes.TsFile] = {
    val types =
      SchemaExport.toTypeDef(KoskiSchema.schema) ++
      TypeExport.toTypeDef(classOf[AdditionalExports]).filter(t => !AdditionalExports.getClass.getName.startsWith(t.fullClassName))

    val tsFiles = TypescriptTypes.build(types, options)
    if (!dryRun) tsFiles.foreach(writeFile)
    tsFiles
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
  opiskeluoikeusHistoryPatch: OpiskeluoikeusHistoryPatch,
  päätasonSuoritus: PäätasonSuoritus,
  ytrCertificateResponse: YtrCertificateResponse,
  suoritetutTutkinnotOppija: SuoritetutTutkinnotOppija,
  aktiivisetOpinnotOppija: AktiivisetJaPäättyneetOpinnotOppija,

  // Traitit jotka eivæt automaattisesti exporttaudu skeemasta, koska ne eivät sellaisenaan
  // ole minkään tietomallin jäseniä (ainoastaan niistä periytyvät luokat on mainittu).
  opiskeluoikeudenTila: OpiskeluoikeudenTila,
  opiskeluoikeusjakso: Opiskeluoikeusjakso,
  koskiOpiskeluoikeusjakso: KoskiOpiskeluoikeusjakso,
  arviointi: Arviointi,
  koodiviite: KoodiViite,
  selitettyOsaamisenTunnustaminen: SelitettyOsaamisenTunnustaminen,
  suoritetutTutkinnotKoskeenTallennettavaOpiskeluoikeus: SuoritetutTutkinnotKoskeenTallennettavaOpiskeluoikeus
)
