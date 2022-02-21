package fi.oph.koski.raportit

import java.time.LocalDate
import fi.oph.koski.db.QueryMethods
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.organisaatio.OrganisaatioService
import fi.oph.koski.db.DB
import fi.oph.koski.localization.LocalizationReader
import slick.jdbc.GetResult

import scala.concurrent.duration.DurationInt

case class PerusopetuksenLisäopetusOppijamäärätRaportti(db: DB, organisaatioService: OrganisaatioService) extends QueryMethods {
  implicit private val getResult: GetResult[PerusopetuksenLisäopetusOppijamäärätRaporttiRow] = GetResult(r =>
    PerusopetuksenLisäopetusOppijamäärätRaporttiRow(
      oppilaitosNimi = r.rs.getString("oppilaitos_nimi"),
      organisaatioOid = r.rs.getString("oppilaitos_oid"),
      opetuskieli = r.rs.getString("opetuskieli"),
      oppilaita = r.rs.getInt("oppilaita"),
      vieraskielisiä = r.rs.getInt("vieraskielisiä"),
      pidennettyOppivelvollisuusJaVaikeastiVammainen = r.rs.getInt("pidennettyOppivelvollisuusJaVaikeastiVammainen"),
      pidennettyOppivelvollisuusJaMuuKuinVaikeimminVammainen = r.rs.getInt("pidennettyOppivelvollisuusJaMuuKuinVaikeimminVammainen"),
      virheellisestiSiirretytVaikeastiVammaiset = r.rs.getInt("virheellisestiSiirretytVaikeastiVammaiset"),
      virheellisestiSiirretytMuutKuinVaikeimminVammaiset = r.rs.getInt("virheellisestiSiirretytMuutKuinVaikeimminVammaiset"),
      erityiselläTuella = r.rs.getInt("erityiselläTuella"),
      majoitusetu = r.rs.getInt("majoitusetu"),
      kuljetusetu = r.rs.getInt("kuljetusetu"),
      sisäoppilaitosmainenMajoitus = r.rs.getInt("sisäoppilaitosmainenMajoitus"),
      koulukoti = r.rs.getInt("koulukoti")
    )
  )

  def build(oppilaitosOids: Seq[String], date: LocalDate, t: LocalizationReader)(implicit u: KoskiSpecificSession): DataSheet = {
    val raporttiQuery = query(oppilaitosOids, date).as[PerusopetuksenLisäopetusOppijamäärätRaporttiRow]
    val rows = runDbSync(raporttiQuery, timeout = 5.minutes)
    DataSheet(
      title = t.get("raportti-excel-suoritukset-sheet-name"),
      rows = rows,
      columnSettings = columnSettings(t)
    )
  }

  private def query(oppilaitosOids: Seq[String], date: LocalDate)(implicit u: KoskiSpecificSession) = {
    sql"""
    select
      oppilaitos.nimi as oppilaitos_nimi,
      oh.oppilaitos_oid,
      opetuskieli_koodisto.nimi as opetuskieli,
      count(distinct oo.opiskeluoikeus_oid) as oppilaita,
      count(distinct (case when r_henkilo.aidinkieli not in ('fi', 'sv', 'se', 'ri', 'vk') then oo.opiskeluoikeus_oid end)) as vieraskielisiä,
      count(distinct (case when                   vaikeasti_vammainen and pidennetty_oppivelvollisuus then oo.opiskeluoikeus_oid end)) as pidennettyOppivelvollisuusJaVaikeastiVammainen,
      count(distinct (case when vammainen and not vaikeasti_vammainen and pidennetty_oppivelvollisuus then oo.opiskeluoikeus_oid end)) as pidennettyOppivelvollisuusJaMuuKuinVaikeimminVammainen,
      count(distinct (case when                   vaikeasti_vammainen and not (pidennetty_oppivelvollisuus and erityinen_tuki) then oo.opiskeluoikeus_oid end)) as virheellisestiSiirretytVaikeastiVammaiset,
      count(distinct (case when vammainen and not vaikeasti_vammainen and not (pidennetty_oppivelvollisuus and erityinen_tuki) then oo.opiskeluoikeus_oid end)) as virheellisestiSiirretytMuutKuinVaikeimminVammaiset,
      count(distinct (case when erityinen_tuki then oo.opiskeluoikeus_oid end)) as erityiselläTuella,
      count(distinct (case when majoitusetu then oo.opiskeluoikeus_oid end)) as majoitusetu,
      count(distinct (case when kuljetusetu then oo.opiskeluoikeus_oid end)) as kuljetusetu,
      count(distinct (case when sisaoppilaitosmainen_majoitus then oo.opiskeluoikeus_oid end)) as sisäoppilaitosmainenMajoitus,
      count(distinct (case when koulukoti then oo.opiskeluoikeus_oid end)) as koulukoti
    from r_opiskeluoikeus oo
    join r_organisaatiohistoria oh on oh.opiskeluoikeus_oid = oo.opiskeluoikeus_oid
    join r_organisaatio oppilaitos on oppilaitos.organisaatio_oid = oh.oppilaitos_oid
    join r_henkilo on r_henkilo.oppija_oid = oo.oppija_oid
    join r_paatason_suoritus on r_paatason_suoritus.opiskeluoikeus_oid = oo.opiskeluoikeus_oid
    join r_opiskeluoikeus_aikajakso aikajakso on aikajakso.opiskeluoikeus_oid = oo.opiskeluoikeus_oid
    left join r_organisaatio_kieli on r_organisaatio_kieli.organisaatio_oid = oh.oppilaitos_oid
    left join r_koodisto_koodi opetuskieli_koodisto
      on opetuskieli_koodisto.koodisto_uri = split_part(r_organisaatio_kieli.kielikoodi, '_', 1)
      and opetuskieli_koodisto.koodiarvo = split_part(split_part(r_organisaatio_kieli.kielikoodi, '_', 2), '#', 1)
    where oh.oppilaitos_oid = any(${oppilaitosOids})
      and oh.alku <= $date
      and oh.loppu >= $date
      and oo.koulutusmuoto = 'perusopetuksenlisaopetus'
      and aikajakso.alku <= $date
      and aikajakso.loppu >= $date
      and aikajakso.tila = 'lasna'
      and oo.sisaltyy_opiskeluoikeuteen_oid is null
    group by oppilaitos.nimi, oh.oppilaitos_oid, opetuskieli_koodisto.nimi, r_paatason_suoritus.koulutusmoduuli_koodiarvo
  """
  }

  def columnSettings(t: LocalizationReader): Seq[(String, Column)] = Seq(
    "oppilaitosNimi" -> Column(t.get("raportti-excel-kolumni-oppilaitoksenNimi")),
    "organisaatioOid" -> Column(t.get("raportti-excel-kolumni-organisaatioOid")),
    "opetuskieli" -> Column(t.get("raportti-excel-kolumni-opetuskieli"), comment = Some(t.get("raportti-excel-kolumni-opetuskieli-comment"))),
    "oppilaita" -> Column(t.get("raportti-excel-kolumni-oppilaita"), comment = Some(t.get("raportti-excel-kolumni-oppilaita-comment"))),
    "vieraskielisiä" -> Column(t.get("raportti-excel-kolumni-vieraskielisiä"), comment = Some(t.get("raportti-excel-kolumni-vieraskielisiä-comment"))),
    "pidennettyOppivelvollisuusJaVaikeastiVammainen" -> Column(t.get("raportti-excel-kolumni-pidennettyOppivelvollisuusJaVaikeastiVammainen"), comment = Some(t.get("raportti-excel-kolumni-pidennettyOppivelvollisuusJaVaikeastiVammainen-comment"))),
    "pidennettyOppivelvollisuusJaMuuKuinVaikeimminVammainen" -> Column(t.get("raportti-excel-kolumni-pidennettyOppivelvollisuusJaMuuKuinVaikeimminVammainen"), comment = Some(t.get("raportti-excel-kolumni-pidennettyOppivelvollisuusJaMuuKuinVaikeimminVammainen-comment"))),
    "virheellisestiSiirretytVaikeastiVammaiset" -> Column(t.get("raportti-excel-kolumni-virheellisestiSiirretytVaikeastiVammaiset"), comment = Some(t.get("raportti-excel-kolumni-virheellisestiSiirretytVaikeastiVammaiset-comment"))),
    "virheellisestiSiirretytMuutKuinVaikeimminVammaiset" -> Column(t.get("raportti-excel-kolumni-virheellisestiSiirretytMuutKuinVaikeimminVammaiset"), comment = Some(t.get("raportti-excel-kolumni-virheellisestiSiirretytMuutKuinVaikeimminVammaiset-comment"))),
    "erityiselläTuella" -> Column(t.get("raportti-excel-kolumni-erityiselläTuella"), comment = Some(t.get("raportti-excel-kolumni-erityiselläTuella-comment"))),
    "majoitusetu" -> Column(t.get("raportti-excel-kolumni-majoitusetu"), comment = Some(t.get("raportti-excel-kolumni-majoitusetu-comment"))),
    "kuljetusetu" -> Column(t.get("raportti-excel-kolumni-kuljetusetu"), comment = Some(t.get("raportti-excel-kolumni-kuljetusetu-comment"))),
    "sisäoppilaitosmainenMajoitus" -> Column(t.get("raportti-excel-kolumni-sisäoppilaitosmainenMajoitus"), comment = Some(t.get("raportti-excel-kolumni-sisäoppilaitosmainenMajoitus-comment"))),
    "koulukoti" -> Column(t.get("raportti-excel-kolumni-koulukoti"), comment = Some(t.get("raportti-excel-kolumni-koulukoti-comment")))
  )
}

case class PerusopetuksenLisäopetusOppijamäärätRaporttiRow(
  oppilaitosNimi: String,
  organisaatioOid: String,
  opetuskieli: String,
  oppilaita: Int,
  vieraskielisiä: Int,
  pidennettyOppivelvollisuusJaVaikeastiVammainen: Int,
  pidennettyOppivelvollisuusJaMuuKuinVaikeimminVammainen: Int,
  virheellisestiSiirretytVaikeastiVammaiset: Int,
  virheellisestiSiirretytMuutKuinVaikeimminVammaiset: Int,
  erityiselläTuella: Int,
  majoitusetu: Int,
  kuljetusetu: Int,
  sisäoppilaitosmainenMajoitus: Int,
  koulukoti: Int
)
