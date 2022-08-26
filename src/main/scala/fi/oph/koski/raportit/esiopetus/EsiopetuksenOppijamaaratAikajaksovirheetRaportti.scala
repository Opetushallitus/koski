package fi.oph.koski.raportit.esiopetus

import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.db.{DB, SQLHelpers}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.organisaatio.OrganisaatioService
import fi.oph.koski.raportit.{Column, DataSheet}
import slick.jdbc.GetResult

import java.time.LocalDate
import scala.concurrent.duration.DurationInt

case class EsiopetuksenOppijamäärätAikajaksovirheetRaportti(db: DB, organisaatioService: OrganisaatioService) extends EsiopetuksenOppijamääristäRaportoiva {
  implicit private val getResult: GetResult[EsiopetuksenOppijamäärätAikajaksovirheetRaporttiRow] = GetResult(r =>
    EsiopetuksenOppijamäärätAikajaksovirheetRaporttiRow(
      oppilaitosNimi = r.<<,
      oppijaOid = r.<<,
      opiskeluoikeusOid = r.<<
    )
  )

  def build(oppilaitosOids: List[String], päivä: LocalDate, t: LocalizationReader)(implicit u: KoskiSpecificSession): DataSheet = {
    val raporttiQuery = query(validateOids(oppilaitosOids), päivä, t.language).as[EsiopetuksenOppijamäärätAikajaksovirheetRaporttiRow]
    val rows = runDbSync(raporttiQuery, timeout = 5.minutes)
    DataSheet(
      title = t.get("raportti-excel-aikajaksovirheet-sheet-name"),
      rows = rows,
      columnSettings = columnSettings(t)
    )
  }

  protected def query(oppilaitosOidit: List[String], päivä: LocalDate, lang: String)(implicit u: KoskiSpecificSession) = {
    val oppilaitosNimiSarake = if(lang == "sv") "oppilaitos_nimi_sv" else "oppilaitos_nimi"

    SQLHelpers.concatMany(
Some(sql"""
    select distinct on (oppilaitos_nimi, oppija_oid, opiskeluoikeus_oid)
           r_opiskeluoikeus.#$oppilaitosNimiSarake as oppilaitos_nimi,
           r_opiskeluoikeus.oppija_oid,
           r_opiskeluoikeus.opiskeluoikeus_oid
"""),
fromJoinWhereSqlPart(oppilaitosOidit, päivä),
Some(sql"""
      and (
"""),
virheellisestiSiirrettyjäTukitietojaEhtoSqlPart,
Some(sql"""
      )
    order by oppilaitos_nimi, oppija_oid, opiskeluoikeus_oid
  """))
  }

  def columnSettings(t: LocalizationReader): Seq[(String, Column)] = Seq(
    "oppilaitosNimi" -> Column(t.get("raportti-excel-kolumni-oppilaitoksenNimi")),
    "oppijaOid" -> Column(t.get("raportti-excel-kolumni-oppijaOid")),
    "opiskeluoikeusOid" -> Column(t.get("raportti-excel-kolumni-opiskeluoikeusOid")),
  )
}

case class EsiopetuksenOppijamäärätAikajaksovirheetRaporttiRow(
  oppilaitosNimi: String,
  oppijaOid: String,
  opiskeluoikeusOid: String
)
