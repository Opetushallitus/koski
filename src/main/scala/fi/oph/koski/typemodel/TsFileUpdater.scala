package fi.oph.koski.typemodel

import fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKoskeenTallennettavaOpiskeluoikeus
import fi.oph.koski.config.ConfigForFrontend
import fi.oph.koski.history.OpiskeluoikeusHistoryPatch
import fi.oph.koski.koskiuser.UserWithAccessRights
import fi.oph.koski.oppija.HenkilönOpiskeluoikeusVersiot
import fi.oph.koski.organisaatio.OrganisaatioHierarkia
import fi.oph.koski.preferences.KeyValue
import fi.oph.koski.schema._
import fi.oph.koski.servlet.Osaamismerkkikuva
import fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotKoskeenTallennettavaOpiskeluoikeus
import fi.oph.koski.suoritusjako.{AktiivisetJaPäättyneetOpinnotOppijaJakolinkillä, SuoritetutTutkinnotOppijaJakolinkillä}
import fi.oph.koski.todistus.TodistusJob
import fi.oph.koski.tutkinto.TutkintoPeruste
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
  suoritetutTutkinnotOppija: SuoritetutTutkinnotOppijaJakolinkillä,
  aktiivisetOpinnotOppija: AktiivisetJaPäättyneetOpinnotOppijaJakolinkillä,
  osaamismerkkikuva: Osaamismerkkikuva,
  opiskeluoikeusClass: OpiskeluoikeusClass,
  tutkintoperuste: TutkintoPeruste,
  configForFrontend: ConfigForFrontend,
  todistusJob: TodistusJob,

  // Traitit jotka eivät automaattisesti exporttaudu skeemasta, koska ne eivät sellaisenaan
  // ole minkään tietomallin jäseniä (ainoastaan traitista periintyvät konkreettiset luokat exportataan automaattisesti).
  opiskeluoikeudenTila: OpiskeluoikeudenTila,
  opiskeluoikeusjakso: Opiskeluoikeusjakso,
  koskiOpiskeluoikeusjakso: KoskiOpiskeluoikeusjakso,
  arviointi: Arviointi,
  koodiviite: KoodiViite,
  selitettyOsaamisenTunnustaminen: SelitettyOsaamisenTunnustaminen,
  suoritetutTutkinnotKoskeenTallennettavaOpiskeluoikeus: SuoritetutTutkinnotKoskeenTallennettavaOpiskeluoikeus,
  aktiivisetJaPäättyneetOpinnotKoskeenTallennettavaOpiskeluoikeus: AktiivisetJaPäättyneetOpinnotKoskeenTallennettavaOpiskeluoikeus,
  paikallinenKoulutusmoduuli: PaikallinenKoulutusmoduuli,
  vapaanSivistystyönKoulutuksenPäätasonSuoritus: VapaanSivistystyönKoulutuksenPäätasonSuoritus,
  suorituskielellinen: Suorituskielellinen,
  maksuttomuustieto: MaksuttomuusTieto,
  valinnanMahdollisuus: ValinnanMahdollisuus,
  arvioinniton: Arvioinniton,
  mahdollisestiArvioinniton: MahdollisestiArvioinniton,
  valinnaisuus: Valinnaisuus,
  preIBLukionModuulinSuoritus2019: PreIBLukionModuulinSuoritus2019,
  preIBLukionModuulinTaiPaikallisenOpintojaksonSuoritus2019: PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritus2019,
  ibTaso: IBTaso,
  arviointiPäivämäärällä: ArviointiPäivämäärällä,
)
