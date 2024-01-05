package fi.oph.koski.raportit.tuva

import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.db.{DB, QueryMethods, SQLHelpers}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.organisaatio.OrganisaatioService
import fi.oph.koski.raportit.{Column, DataSheet}
import slick.jdbc.{GetResult}

import java.time.LocalDate
import scala.concurrent.duration.DurationInt

case class TuvaPerusopetuksenOppijamäärätRaportti(db: DB, organisaatioService: OrganisaatioService) extends TuvaPerusopetuksenOppijamääristäRaportoiva {
  implicit private val getResult: GetResult[TuvaPerusopetuksenOppijamäärätRaporttiRow] = GetResult(r =>
    TuvaPerusopetuksenOppijamäärätRaporttiRow(
      oppilaitosNimi = r.rs.getString("oppilaitos_nimi"),
      organisaatioOid = r.rs.getString("oppilaitos_oid"),
      opetuskieli = r.rs.getString("opetuskieli"),
      oppilaita = r.rs.getInt("oppilaita"),
      eritTukiJaVaikeastiVammainen = r.rs.getInt("eritTukiJaVaikeastiVammainen"),
      erityinenTukiJaMuuKuinVaikeimminVammainen = r.rs.getInt("erityinenTukiJaMuuKuinVaikeimminVammainen"),
      tuvaPerusVirheellisestiSiirrettyjaTukitietoja = r.rs.getInt("tuvaPerusVirheellisestiSiirrettyjaTukitietoja"),
      erityiselläTuella = r.rs.getInt("erityiselläTuella"),
      majoitusetu = r.rs.getInt("majoitusetu"),
      kuljetusetu = r.rs.getInt("kuljetusetu"),
      sisäoppilaitosmainenMajoitus = r.rs.getInt("sisäoppilaitosmainenMajoitus"),
      koulukoti = r.rs.getInt("koulukoti"),
    )
  )

  def build(oppilaitosOids: Seq[String], date: LocalDate, t: LocalizationReader)(implicit u: KoskiSpecificSession): DataSheet = {
    val raporttiQuery = query(oppilaitosOids, date, t).as[TuvaPerusopetuksenOppijamäärätRaporttiRow]
    val rows = runDbSync(raporttiQuery, timeout = 10.minutes)
    DataSheet(
      title = t.get("raportti-excel-suoritukset-sheet-name"),
      rows = rows,
      columnSettings = columnSettings(t)
    )
  }

  private def query(oppilaitosOids: Seq[String], date: LocalDate, t: LocalizationReader)(implicit u: KoskiSpecificSession) = {
    val nimiSarake = if(t.language == "sv") "nimi_sv" else "nimi"

    SQLHelpers.concatMany(
      Some(sql"""
      select
        oppilaitos.#$nimiSarake as oppilaitos_nimi,
        oh.oppilaitos_oid,
        string_agg(distinct opetuskieli_koodisto.#$nimiSarake, ',') as opetuskieli,
        count(distinct oo.opiskeluoikeus_oid) as oppilaita,
        count(distinct (case when erityinen_tuki and not vammainen and vaikeasti_vammainen then oo.opiskeluoikeus_oid end)) as eritTukiJaVaikeastiVammainen,
        count(distinct (case when erityinen_tuki and vammainen and not vaikeasti_vammainen then oo.opiskeluoikeus_oid end)) as erityinenTukiJaMuuKuinVaikeimminVammainen,
        count(distinct (case when
"""),
      virheellisestiSiirrettyjäTukitietojaEhtoSqlPart,
      Some(sql"""
          then oo.opiskeluoikeus_oid end)) as tuvaPerusVirheellisestiSiirrettyjaTukitietoja,
        count(distinct (case when erityinen_tuki then oo.opiskeluoikeus_oid end)) as erityiselläTuella,
        count(distinct (case when majoitusetu then oo.opiskeluoikeus_oid end)) as majoitusetu,
        count(distinct (case when kuljetusetu then oo.opiskeluoikeus_oid end)) as kuljetusetu,
        count(distinct (case when sisaoppilaitosmainen_majoitus then oo.opiskeluoikeus_oid end)) as sisäoppilaitosmainenMajoitus,
        count(distinct (case when koulukoti then oo.opiskeluoikeus_oid end)) as koulukoti
"""),
      fromJoinWhereSqlPart(oppilaitosOids, date),
      Some(sql"""
      group by oppilaitos.#$nimiSarake, oh.oppilaitos_oid
      order by oppilaitos.#$nimiSarake, oh.oppilaitos_oid
  """))
  }

  def columnSettings(t: LocalizationReader): Seq[(String, Column)] = Seq(
    "oppilaitosNimi" -> Column(t.get("raportti-excel-kolumni-oppilaitoksenNimi")),
    "organisaatioOid" -> Column(t.get("raportti-excel-kolumni-organisaatioOid")),
    "opetuskieli" -> Column(t.get("raportti-excel-kolumni-opetuskieli"), comment = Some(t.get("raportti-excel-kolumni-opetuskieli-comment"))),
    "oppilaita" -> Column(t.get("raportti-excel-kolumni-oppilaita"), comment = Some(t.get("raportti-excel-kolumni-oppilaita-comment"))),
    "eritTukiJaVaikeastiVammainen" -> Column(t.get("raportti-excel-kolumni-eritTukiJaVaikeastiVammainen"), comment = Some(t.get("raportti-excel-kolumni-eritTukiJaVaikeastiVammainen-comment"))),
    "erityinenTukiJaMuuKuinVaikeimminVammainen" -> Column(t.get("raportti-excel-kolumni-erityinenTukiJaMuuKuinVaikeimminVammainen"), comment = Some(t.get("raportti-excel-kolumni-erityinenTukiJaMuuKuinVaikeimminVammainen-comment"))),
    "tuvaPerusVirheellisestiSiirrettyjaTukitietoja" -> Column(t.get("raportti-excel-kolumni-tuvaPerusVirheellisestiSiirrettyjaTukitietoja"), comment = Some(t.get("raportti-excel-kolumni-tuvaPerusVirheellisestiSiirrettyjaTukitietoja-comment"))),
    "erityiselläTuella" -> Column(t.get("raportti-excel-kolumni-erityiselläTuella"), comment = Some(t.get("raportti-excel-kolumni-erityiselläTuella-comment"))),
    "majoitusetu" -> Column(t.get("raportti-excel-kolumni-majoitusetu"), comment = Some(t.get("raportti-excel-kolumni-majoitusetu-comment"))),
    "kuljetusetu" -> Column(t.get("raportti-excel-kolumni-kuljetusetu"), comment = Some(t.get("raportti-excel-kolumni-kuljetusetu-comment"))),
    "sisäoppilaitosmainenMajoitus" -> Column(t.get("raportti-excel-kolumni-sisäoppilaitosmainenMajoitus"), comment = Some(t.get("raportti-excel-kolumni-sisäoppilaitosmainenMajoitus-comment"))),
    "koulukoti" -> Column(t.get("raportti-excel-kolumni-koulukoti"), comment = Some(t.get("raportti-excel-kolumni-koulukoti-comment"))),
  )
}

case class TuvaPerusopetuksenOppijamäärätRaporttiRow(
  oppilaitosNimi: String,
  organisaatioOid: String,
  opetuskieli: String,
  oppilaita: Int,
  eritTukiJaVaikeastiVammainen: Int,
  erityinenTukiJaMuuKuinVaikeimminVammainen: Int,
  tuvaPerusVirheellisestiSiirrettyjaTukitietoja: Int,
  erityiselläTuella: Int,
  majoitusetu: Int,
  kuljetusetu: Int,
  sisäoppilaitosmainenMajoitus: Int,
  koulukoti: Int,
)
