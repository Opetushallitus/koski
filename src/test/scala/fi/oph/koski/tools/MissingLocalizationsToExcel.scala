package fi.oph.koski.tools

import fi.oph.koski.cache.GlobalCacheManager
import fi.oph.koski.localization.{MockLocalizationRepository, ReadOnlyRemoteLocalizationRepository}
import fi.oph.koski.raportit.{Column, DataSheet, ExcelWriter, WorkbookSettings}
import fi.oph.koski.schema.LocalizedString
import java.io.{FileOutputStream}


object MissingLocalizationsToExcel extends App {

  implicit lazy val cacheManager = GlobalCacheManager

  val root = "https://virkailija.opintopolku.fi"
  val remoteLocalizations = new ReadOnlyRemoteLocalizationRepository(root).localizations
  val localLocalizations: Map[String, LocalizedString] = new MockLocalizationRepository().localizations

  val columnSettings = List(
    "key" -> Column("Avain"),
    "suomeksi" -> Column("Suomeksi")
  )

  def missingKeysForLang(lang: String) = {
    val missingKeys = localLocalizations.keySet -- remoteLocalizations.filter(_._2.hasLanguage(lang)).keySet
    val rows = missingKeys.map(key => MissingLocalizationsRow(key, localLocalizations.get(key).map(_.get("fi")))).toList
    DataSheet(lang, rows, columnSettings)
  }

  val output = new FileOutputStream("puuttuvat-koski-kaannokset.xlsx")
  val workbookSettings = WorkbookSettings("Puuttuvat käännökset", None)
  val sheets = List(missingKeysForLang("sv"), missingKeysForLang("en"))

  ExcelWriter.writeExcel(workbookSettings, sheets, output)

  output.close()
}

protected case class MissingLocalizationsRow(key: String, suomeksi: Option[String])
