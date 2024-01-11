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

case class EsiopetuksenOppijamäärätRaportti(db: DB, organisaatioService: OrganisaatioService) extends EsiopetuksenOppijamääristäRaportoiva {
  implicit private val getResult: GetResult[EsiopetuksenOppijamäärätRaporttiRow] = GetResult(r =>
    EsiopetuksenOppijamäärätRaporttiRow(
      oppilaitosNimi = r.<<,
      opetuskieli = r.<<,
      esiopetusoppilaidenMäärä = r.<<,
      vieraskielisiä = r.<<,
      koulunesiopetuksessa = r.<<,
      päiväkodinesiopetuksessa = r.<<,
      viisivuotiaita = r.<<,
      viisivuotiaitaEiPidennettyäOppivelvollisuutta = r.<<,
      pidOppivelvollisuusEritTukiJaVaikeastiVammainen = r.<<,
      pidOppivelvollisuusEritTukiJaMuuKuinVaikeimminVammainen = r.<<,
      virheellisestiSiirrettyjaTukitietoja = r.<<,
      erityiselläTuella = r.<<,
      majoitusetu = r.<<,
      kuljetusetu = r.<<,
      sisäoppilaitosmainenMajoitus = r.<<
    )
  )

  def build(oppilaitosOids: List[String], päivä: LocalDate, t: LocalizationReader)(implicit u: KoskiSpecificSession): DataSheet = {
    val raporttiQuery = query(validateOids(oppilaitosOids), päivä, t.language).as[EsiopetuksenOppijamäärätRaporttiRow]
    val rows = runDbSync(raporttiQuery, timeout = 5.minutes)
    DataSheet(
      title = t.get("raportti-excel-suoritukset-sheet-name"),
      rows = rows,
      columnSettings = columnSettings(t)
    )
  }

  private def query(oppilaitosOidit: List[String], päivä: LocalDate, lang: String)(implicit u: KoskiSpecificSession) = {
    val year = päivä.getYear
    val oppilaitosNimiSarake = if(lang == "sv") "oppilaitos_nimi_sv" else "oppilaitos_nimi"
    val koodistoNimiSarake = if(lang == "sv") "nimi_sv" else "nimi"

    SQLHelpers.concatMany(
Some(sql"""
    select
      r_opiskeluoikeus.#$oppilaitosNimiSarake,
      string_agg(distinct r_koodisto_koodi.#$koodistoNimiSarake, ',') opetuskieli,
      count(distinct r_opiskeluoikeus.opiskeluoikeus_oid) as esiopetusoppilaidenMäärä,
      count(distinct case when aidinkieli != 'fi' and aidinkieli != 'sv' and aidinkieli != 'se' and aidinkieli != 'ri' and aidinkieli != 'vk' then r_opiskeluoikeus.opiskeluoikeus_oid end) as vieraskielisiä,
      count(distinct case when koulutusmoduuli_koodiarvo = '001101' then r_opiskeluoikeus.opiskeluoikeus_oid end) as koulunesiopetuksessa,
      count(distinct case when koulutusmoduuli_koodiarvo = '001102' then r_opiskeluoikeus.opiskeluoikeus_oid end) as päiväkodinesiopetuksessa,
      count(distinct case when $year - extract(year from syntymaaika) = 5 then r_opiskeluoikeus.opiskeluoikeus_oid end) as viisivuotiaita,
      count(distinct case when $year - extract(year from syntymaaika) = 5 and pidennetty_oppivelvollisuus = false then r_opiskeluoikeus.opiskeluoikeus_oid end) as viisivuotiaitaEiPidennettyäOppivelvollisuutta,
      count(distinct case when erityisen_tuen_paatos and not vammainen and vaikeasti_vammainen and pidennetty_oppivelvollisuus then r_opiskeluoikeus.opiskeluoikeus_oid end) as pidOppivelvollisuusEritTukiJaVaikeastiVammainen,
      count(distinct case when erityisen_tuen_paatos and vammainen and not vaikeasti_vammainen and pidennetty_oppivelvollisuus then r_opiskeluoikeus.opiskeluoikeus_oid end) as pidOppivelvollisuusEritTukiJaMuuKuinVaikeimminVammainen,
      count(distinct case when
"""),
virheellisestiSiirrettyjäTukitietojaEhtoSqlPart,
Some(sql"""
        then r_opiskeluoikeus.opiskeluoikeus_oid end) as virheellisestiSiirrettyjaTukitietoja,
      count(distinct case when erityisen_tuen_paatos = true then r_opiskeluoikeus.opiskeluoikeus_oid end) as erityiselläTuella,
      count(distinct case when majoitusetu = true then r_opiskeluoikeus.opiskeluoikeus_oid end) as majoitusetu,
      count(distinct case when kuljetusetu = true then r_opiskeluoikeus.opiskeluoikeus_oid end) as kuljetusetu,
      count(distinct case when sisaoppilaitosmainen_majoitus = true then r_opiskeluoikeus.opiskeluoikeus_oid end) as sisäoppilaitosmainenMajoitus
"""),
fromJoinWhereSqlPart(oppilaitosOidit, päivä),
Some(sql"""
    group by r_opiskeluoikeus.#$oppilaitosNimiSarake
"""))
  }

  def columnSettings(t: LocalizationReader): Seq[(String, Column)] = Seq(
    "oppilaitosNimi" -> Column(t.get("raportti-excel-kolumni-oppilaitoksenNimi")),
    "opetuskieli" -> Column(t.get("raportti-excel-kolumni-opetuskieli")),
    "esiopetusoppilaidenMäärä" -> Column(t.get("raportti-excel-kolumni-esiopetusoppilaidenMäärä"), comment = Some(t.get("raportti-excel-kolumni-esiopetusoppilaidenMäärä-comment"))),
    "vieraskielisiä" -> Column(t.get("raportti-excel-kolumni-esiopetusvieraskielisiä")),
    "koulunesiopetuksessa" -> Column(t.get("raportti-excel-kolumni-koulunesiopetuksessa")),
    "päiväkodinesiopetuksessa" -> Column(t.get("raportti-excel-kolumni-päiväkodinesiopetuksessa")),
    "viisivuotiaita" -> Column(t.get("raportti-excel-kolumni-viisivuotiaita"), comment = Some(t.get("raportti-excel-kolumni-viisivuotiaitaEiPidennettyäOppivelvollisuutta-comment"))),
    "viisivuotiaitaEiPidennettyäOppivelvollisuutta" -> Column(t.get("raportti-excel-kolumni-viisivuotiaitaEiPidennettyäOppivelvollisuutta")),
    "pidOppivelvollisuusEritTukiJaVaikeastiVammainen" -> Column(t.get("raportti-excel-kolumni-pidOppivelvollisuusEritTukiJaVaikeastiVammainen"), comment = Some(t.get("raportti-excel-kolumni-pidOppivelvollisuusEritTukiJaVaikeastiVammainen-comment"))),
    "pidOppivelvollisuusEritTukiJaMuuKuinVaikeimminVammainen" -> Column(t.get("raportti-excel-kolumni-pidOppivelvollisuusEritTukiJaMuuKuinVaikeimminVammainen"), comment = Some(t.get("raportti-excel-kolumni-pidOppivelvollisuusEritTukiJaMuuKuinVaikeimminVammainen-comment"))),
    "virheellisestiSiirrettyjaTukitietoja" -> Column(t.get("raportti-excel-kolumni-virheellisestiSiirrettyjaTukitietoja"), comment = Some(t.get("raportti-excel-kolumni-virheellisestiSiirrettyjaTukitietoja-comment"))),
    "erityiselläTuella" -> Column(t.get("raportti-excel-kolumni-erityiselläTuella"), comment = Some(t.get("raportti-excel-kolumni-erityiselläTuella-comment"))),
    "majoitusetu" -> Column(t.get("raportti-excel-kolumni-majoitusetu"), comment = Some(t.get("raportti-excel-kolumni-majoitusetu-comment"))),
    "kuljetusetu" -> Column(t.get("raportti-excel-kolumni-kuljetusetu"), comment = Some(t.get("raportti-excel-kolumni-kuljetusetu-comment"))),
    "sisäoppilaitosmainenMajoitus" -> Column(t.get("raportti-excel-kolumni-sisäoppilaitosmainenMajoitus"), comment = Some(t.get("raportti-excel-kolumni-sisäoppilaitosmainenMajoitus-comment")))
  )
}

case class EsiopetuksenOppijamäärätRaporttiRow(
  oppilaitosNimi: String,
  opetuskieli: String,
  esiopetusoppilaidenMäärä: Int,
  vieraskielisiä: Int,
  koulunesiopetuksessa: Int,
  päiväkodinesiopetuksessa: Int,
  viisivuotiaita: Int,
  viisivuotiaitaEiPidennettyäOppivelvollisuutta: Int,
  pidOppivelvollisuusEritTukiJaVaikeastiVammainen: Int,
  pidOppivelvollisuusEritTukiJaMuuKuinVaikeimminVammainen: Int,
  virheellisestiSiirrettyjaTukitietoja: Int,
  erityiselläTuella: Int,
  majoitusetu: Int,
  kuljetusetu: Int,
  sisäoppilaitosmainenMajoitus: Int
)
