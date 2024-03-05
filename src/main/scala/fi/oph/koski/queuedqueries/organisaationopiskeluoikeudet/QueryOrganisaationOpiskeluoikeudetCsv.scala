package fi.oph.koski.queuedqueries.organisaationopiskeluoikeudet

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.queuedqueries.QueryUtils.QueryResourceManager
import fi.oph.koski.queuedqueries.{QueryFormat, QueryResultWriter}
import fi.oph.koski.raportointikanta._
import fi.oph.koski.schema.Organisaatio
import fi.oph.koski.schema.Organisaatio.Oid
import fi.oph.scalaschema.annotation.EnumValue

import java.time.LocalDate
import scala.util.Using

case class QueryOrganisaationOpiskeluoikeudetCsv(
  @EnumValue("organisaationOpiskeluoikeudet")
  `type`: String = "organisaationOpiskeluoikeudet",
  @EnumValue(QueryFormat.csv)
  format: String = QueryFormat.csv,
  organisaatioOid: Option[Organisaatio.Oid] = None,
  alkamispaiva: LocalDate,
  tila: Option[String] = None,
  koulutusmuoto: Option[String] = None,
  suoritustyyppi: Option[String] = None,
) extends QueryOrganisaationOpiskeluoikeudet {

  def withOrganisaatioOid(organisaatioOid: Oid): QueryOrganisaationOpiskeluoikeudetCsv = copy(organisaatioOid = Some(organisaatioOid))

  def fetchData(
    application: KoskiApplication,
    writer: QueryResultWriter,
    oppilaitosOids: List[Organisaatio.Oid],
  ): Either[String, Unit] = QueryResourceManager(logger) { mgr =>
    implicit val manager: Using.Manager = mgr

    val db = getDb(application)
    val filters = defaultBaseFilter(oppilaitosOids)
    val oppijaOids = getOppijaOids(db, filters)

    val opiskeluoikeusCsv = writer.createCsv[ROpiskeluoikeusRow]("opiskeluoikeus")
    val päätasonSuoritusCsv = writer.createCsv[RPäätasonSuoritusRow]("paatason_suoritus")
    val osasuoritusCsv = writer.createCsv[ROsasuoritusRow]("osasuoritus")
    val opiskeluoikeudenAikajaksoCsv = writer.createCsv[ROpiskeluoikeusAikajaksoRow]("opiskeluoikeus_aikajakso")
    val esiopetuksenAikajaksoCsv = writer.createCsv[EsiopetusOpiskeluoikeusAikajaksoRow]("esiopetus_opiskeluoik_aikajakso")

    forEachOpiskeluoikeus(application, filters, oppijaOids) { row =>
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

    opiskeluoikeusCsv.save()
    päätasonSuoritusCsv.save()
    osasuoritusCsv.save()
    opiskeluoikeudenAikajaksoCsv.save()
    esiopetuksenAikajaksoCsv.save()
  }
}
