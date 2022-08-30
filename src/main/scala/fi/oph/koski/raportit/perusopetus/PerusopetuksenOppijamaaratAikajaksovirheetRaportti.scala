package fi.oph.koski.raportit.perusopetus

import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.db.{DB, SQLHelpers}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.organisaatio.OrganisaatioService
import fi.oph.koski.raportit.{Column, DataSheet}
import slick.jdbc.{GetResult}

import java.time.LocalDate
import scala.concurrent.duration.DurationInt

case class PerusopetuksenOppijamäärätAikajaksovirheetRaportti(db: DB, organisaatioService: OrganisaatioService) extends PerusopetuksenOppijamääristäRaportoiva {
  implicit private val getResult: GetResult[PerusopetuksenOppijamäärätAikajaksovirheetRaporttiRow] = GetResult(r =>
    PerusopetuksenOppijamäärätAikajaksovirheetRaporttiRow(
      oppilaitosNimi = r.rs.getString("oppilaitos_nimi"),
      organisaatioOid = r.rs.getString("oppilaitos_oid"),
      oppijaOid = r.rs.getString("oppija_oid"),
      opiskeluoikeusOid = r.rs.getString("opiskeluoikeus_oid")
    )
  )

  def build(oppilaitosOids: Seq[String], date: LocalDate, t: LocalizationReader)(implicit u: KoskiSpecificSession): DataSheet = {
    val raporttiQuery = query(oppilaitosOids, date, t).as[PerusopetuksenOppijamäärätAikajaksovirheetRaporttiRow]
    val rows = runDbSync(raporttiQuery, timeout = 10.minutes)
    DataSheet(
      title = t.get("raportti-excel-aikajaksovirheet-sheet-name"),
      rows = rows,
      columnSettings = columnSettings(t)
    )
  }

  private def query(oppilaitosOids: Seq[String], date: LocalDate, t: LocalizationReader)(implicit u: KoskiSpecificSession) = {
    val nimiSarake = if(t.language == "sv") "nimi_sv" else "nimi"

    SQLHelpers.concatMany(
Some(sql"""
    select distinct on (oppilaitos_nimi, oppilaitos_oid, oppija_oid, opiskeluoikeus_oid)
      oppilaitos.#$nimiSarake as oppilaitos_nimi,
      oh.oppilaitos_oid,
      oo.oppija_oid,
      oo.opiskeluoikeus_oid
"""),
      fromJoinWhereSqlPart(oppilaitosOids, date),
Some(sql"""
      and (
"""),
      virheellisestiSiirrettyjäTukitietojaEhtoSqlPart,
Some(sql"""
      )
      order by oppilaitos_nimi, oppilaitos_oid, oppija_oid, opiskeluoikeus_oid
"""))
  }

  def columnSettings(t: LocalizationReader): Seq[(String, Column)] = Seq(
    "oppilaitosNimi" -> Column(t.get("raportti-excel-kolumni-oppilaitoksenNimi")),
    "organisaatioOid" -> Column(t.get("raportti-excel-kolumni-organisaatioOid")),
    "oppijaOid" -> Column(t.get("raportti-excel-kolumni-oppijaOid")),
    "opiskeluoikeusOid" -> Column(t.get("raportti-excel-kolumni-opiskeluoikeusOid")),
  )
}

case class PerusopetuksenOppijamäärätAikajaksovirheetRaporttiRow(
  oppilaitosNimi: String,
  organisaatioOid: String,
  oppijaOid: String,
  opiskeluoikeusOid: String
)
