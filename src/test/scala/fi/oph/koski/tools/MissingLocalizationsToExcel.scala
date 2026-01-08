package fi.oph.koski.tools

import fi.oph.koski.cache.{CacheManager, GlobalCacheManager}
import fi.oph.koski.localization.{KoskiLocalizationConfig, LocalizationReader, MockLocalizationRepository, ReadOnlyRemoteLocalizationRepository}
import fi.oph.koski.raportit.{Column, DataSheet, ExcelWriter, WorkbookSettings}
import fi.oph.koski.schema.LocalizedString
import java.io.FileOutputStream
import fi.oph.koski.valpas.localization.ValpasLocalizationConfig


object MissingLocalizationsToExcel extends App {

  implicit lazy val cacheManager: CacheManager = GlobalCacheManager

  lazy val localizationConfig = sys.env.getOrElse("LOCALIZATION_CATEGORY", "koski") match {
    case "koski" => new KoskiLocalizationConfig
    case "valpas" => new ValpasLocalizationConfig
  }

  val root = sys.env.getOrElse("VIRKAILIJA_ROOT", throw new RuntimeException("Environment variable VIRKAILIJA_ROOT missing"))
  val remoteLocalizations = new ReadOnlyRemoteLocalizationRepository(root, localizationConfig).localizations
  val localizationRepository = new MockLocalizationRepository(localizationConfig)
  val localLocalizations: Map[String, LocalizedString] = localizationRepository.localizations
  val t = new LocalizationReader(localizationRepository, "fi")
  val booleanTextValues = ExcelWriter.BooleanCellStyleLocalizedValues(t)

  val columnSettings = List(
    "key" -> Column("Avain"),
    "suomeksi" -> Column("Suomeksi")
  )

  def missingKeysForLang(lang: String) = {
    val missingKeys = localLocalizations.keySet -- remoteLocalizations.filter(_._2.hasLanguage(lang)).keySet
    val rows = missingKeys.map(key => MissingLocalizationsRow(key, localLocalizations.get(key).map(_.get("fi")))).toList
    DataSheet(lang, rows, columnSettings)
  }

  val output = new FileOutputStream(s"puuttuvat-${localizationConfig.localizationCategory}-kaannokset.xlsx")
  val workbookSettings = WorkbookSettings("Puuttuvat käännökset", None)
  val sheets = List(missingKeysForLang("sv"), missingKeysForLang("en"))

  ExcelWriter.writeExcel(workbookSettings, sheets, booleanTextValues, output)

  output.close()
}

protected case class MissingLocalizationsRow(key: String, suomeksi: Option[String])
