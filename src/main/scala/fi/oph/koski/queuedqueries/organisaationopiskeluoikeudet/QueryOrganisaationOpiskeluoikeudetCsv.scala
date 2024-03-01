package fi.oph.koski.queuedqueries.organisaationopiskeluoikeudet

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.queuedqueries.{QueryFormat, QueryResultWriter}
import fi.oph.koski.raportointikanta.RaportointiDatabaseSchema.ROpiskeluoikeusTable
import fi.oph.koski.raportointikanta.{EsiopetusOpiskeluoikeusAikajaksoRow, OpiskeluoikeusLoaderRowBuilder, ROpiskeluoikeusAikajaksoRow, ROpiskeluoikeusRow, ROsasuoritusRow, RPäätasonSuoritusRow}
import fi.oph.koski.schema.Organisaatio
import fi.oph.scalaschema.annotation.EnumValue

import java.sql.Timestamp
import java.time.LocalDate
import scala.util.{Try, Using}

case class QueryOrganisaationOpiskeluoikeudetCsv(
  @EnumValue("organisaationOpiskeluoikeudet")
  `type`: String = "organisaationOpiskeluoikeudet",
  @EnumValue(QueryFormat.csv)
  format: String = QueryFormat.csv,
  organisaatioOid: Option[Organisaatio.Oid],
  alkamispaiva: LocalDate,
  tila: Option[String],
  koulutusmuoto: Option[String],
  suoritustyyppi: Option[String],
) extends QueryOrganisaationOpiskeluoikeudet {

  def fetchData(
    application: KoskiApplication,
    writer: QueryResultWriter,
    oppilaitosOids: List[Organisaatio.Oid],
  ): Try[Unit] = Using.Manager { use =>
    val db = getDb(application)
    val filters = defaultBaseFilter(oppilaitosOids)
    val oppijaOids = getOppijaOids(db, filters)

    val opiskeluoikeusCsv = use(writer.createCsv[ROpiskeluoikeusRow]("opiskeluoikeus"))
    val päätasonSuoritusCsv = use(writer.createCsv[RPäätasonSuoritusRow]("paatason_suoritus"))
    val osasuoritusCsv = use(writer.createCsv[ROsasuoritusRow]("osasuoritus"))
    val opiskeluoikeudenAikajaksoCsv = use(writer.createCsv[ROpiskeluoikeusAikajaksoRow]("opiskeluoikeus_aikajakso"))
    val esiopetuksenAikajaksoCsv = use(writer.createCsv[EsiopetusOpiskeluoikeusAikajaksoRow]("esiopetus_opiskeluoik_aikajakso"))

    forEachOpiskeluoikeus(application, filters, oppijaOids) { (henkilö, opiskeluoikeudet) =>
      opiskeluoikeudet.foreach { row =>
        OpiskeluoikeusLoaderRowBuilder
          .buildKoskiRow(row)
          .foreach { rows =>
            opiskeluoikeusCsv.put(rows.rOpiskeluoikeusRow)
            päätasonSuoritusCsv.put(rows.rPäätasonSuoritusRows)
            osasuoritusCsv.put(rows.rOsasuoritusRows)
            opiskeluoikeudenAikajaksoCsv.put(rows.rOpiskeluoikeusAikajaksoRows)
            esiopetuksenAikajaksoCsv.put(rows.esiopetusOpiskeluoikeusAikajaksoRows)
          }
      }
    }

    // TODO: Päällekkäiset opiskeluoikeudet

    opiskeluoikeusCsv.save()
    päätasonSuoritusCsv.save()
    osasuoritusCsv.save()
    opiskeluoikeudenAikajaksoCsv.save()
    esiopetuksenAikajaksoCsv.save()
  }
}
