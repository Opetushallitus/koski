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

case class PerusopetuksenOppijamäärätRaportti(db: DB, organisaatioService: OrganisaatioService) extends QueryMethods {
  implicit private val getResult: GetResult[PerusopetuksenOppijamäärätRaporttiRow] = GetResult(r =>
    PerusopetuksenOppijamäärätRaporttiRow(
      oppilaitosNimi = r.rs.getString("oppilaitos_nimi"),
      organisaatioOid = r.rs.getString("oppilaitos_oid"),
      opetuskieli = r.rs.getString("opetuskieli"),
      vuosiluokka = r.rs.getString("vuosiluokka"),
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
      koulukoti = r.rs.getInt("koulukoti"),
      joustavaPerusopetus = r.rs.getInt("joustava_perusopetus"),
      kotiopetus = r.rs.getInt("kotiopetus")
    )
  )

  def build(oppilaitosOids: Seq[String], date: LocalDate, t: LocalizationReader)(implicit u: KoskiSpecificSession): DataSheet = {
    val raporttiQuery = query(oppilaitosOids, date, t).as[PerusopetuksenOppijamäärätRaporttiRow]
    val rows = runDbSync(raporttiQuery, timeout = 10.minutes)
    DataSheet(
      title = t.get("raportti-excel-suoritukset-sheet-name"),
      rows = rows,
      columnSettings = columnSettings(t)
    )
  }

  private def query(oppilaitosOids: Seq[String], date: LocalDate, t: LocalizationReader)(implicit u: KoskiSpecificSession) = {
    val kaikkiVuosiluokatLokalisoituTeksti = t.get("raportti-excel-default-value-kaikki-vuosiluokat")
    val nimiSarake = if(t.language == "sv") "nimi_sv" else "nimi"
    sql"""
    with q as (
      select
        oppilaitos.#$nimiSarake as oppilaitos_nimi,
        oh.oppilaitos_oid,
        opetuskieli_koodisto.#$nimiSarake as opetuskieli,
        pts.koulutusmoduuli_koodiarvo as vuosiluokka,
        count(distinct (case when not kotiopetus then oo.opiskeluoikeus_oid end)) as oppilaita,
        count(distinct (case when not kotiopetus and r_henkilo.aidinkieli not in ('fi', 'sv', 'se', 'ri', 'vk') then oo.opiskeluoikeus_oid end)) as vieraskielisiä,
        count(distinct (case when not kotiopetus and                   vaikeasti_vammainen and pidennetty_oppivelvollisuus then oo.opiskeluoikeus_oid end)) as pidennettyOppivelvollisuusJaVaikeastiVammainen,
        count(distinct (case when not kotiopetus and vammainen and not vaikeasti_vammainen and pidennetty_oppivelvollisuus then oo.opiskeluoikeus_oid end)) as pidennettyOppivelvollisuusJaMuuKuinVaikeimminVammainen,
        count(distinct (case when not kotiopetus and                   vaikeasti_vammainen and not (pidennetty_oppivelvollisuus and erityinen_tuki) then oo.opiskeluoikeus_oid end)) as virheellisestiSiirretytVaikeastiVammaiset,
        count(distinct (case when not kotiopetus and vammainen and not vaikeasti_vammainen and not (pidennetty_oppivelvollisuus and erityinen_tuki) then oo.opiskeluoikeus_oid end)) as virheellisestiSiirretytMuutKuinVaikeimminVammaiset,
        count(distinct (case when not kotiopetus and erityinen_tuki then oo.opiskeluoikeus_oid end)) as erityiselläTuella,
        count(distinct (case when not kotiopetus and majoitusetu then oo.opiskeluoikeus_oid end)) as majoitusetu,
        count(distinct (case when not kotiopetus and kuljetusetu then oo.opiskeluoikeus_oid end)) as kuljetusetu,
        count(distinct (case when not kotiopetus and sisaoppilaitosmainen_majoitus then oo.opiskeluoikeus_oid end)) as sisäoppilaitosmainenMajoitus,
        count(distinct (case when not kotiopetus and koulukoti then oo.opiskeluoikeus_oid end)) as koulukoti,
        count(distinct (case when not kotiopetus and joustava_perusopetus then oo.opiskeluoikeus_oid end)) as joustava_perusopetus,
        count(distinct (case when kotiopetus then oo.opiskeluoikeus_oid end)) as kotiopetus
      from r_opiskeluoikeus oo
      join r_organisaatiohistoria oh on oh.opiskeluoikeus_oid = oo.opiskeluoikeus_oid
      join r_organisaatio oppilaitos on oppilaitos.organisaatio_oid = oh.oppilaitos_oid
      join r_henkilo on r_henkilo.oppija_oid = oo.oppija_oid
      join r_paatason_suoritus pts on pts.opiskeluoikeus_oid = oo.opiskeluoikeus_oid
      join r_opiskeluoikeus_aikajakso aikajakso on aikajakso.opiskeluoikeus_oid = oo.opiskeluoikeus_oid
      left join r_organisaatio_kieli on r_organisaatio_kieli.organisaatio_oid = oh.oppilaitos_oid
      left join r_koodisto_koodi opetuskieli_koodisto
        on opetuskieli_koodisto.koodisto_uri = split_part(r_organisaatio_kieli.kielikoodi, '_', 1)
        and opetuskieli_koodisto.koodiarvo = split_part(split_part(r_organisaatio_kieli.kielikoodi, '_', 2), '#', 1)
      where oh.oppilaitos_oid = any(${oppilaitosOids})
        and oh.alku <= $date
        and oh.loppu >= $date
        and oo.koulutusmuoto = 'perusopetus'
        and (pts.vahvistus_paiva is null or pts.vahvistus_paiva > $date)
        and pts.alkamispaiva <= $date
        and pts.koulutusmoduuli_koodiarvo in ('1', '2', '3', '4', '5', '6', '7', '8', '9')
        and aikajakso.alku <= $date
        and aikajakso.loppu >= $date
        and aikajakso.tila = 'lasna'
        and oo.sisaltyy_opiskeluoikeuteen_oid is null
      group by oppilaitos.#$nimiSarake, oh.oppilaitos_oid, opetuskieli_koodisto.#$nimiSarake, pts.koulutusmoduuli_koodiarvo
    ), totals as (
      select * from q
      union all
      select
        oppilaitos_nimi,
        oppilaitos_oid,
        opetuskieli,
        $kaikkiVuosiluokatLokalisoituTeksti as vuosiluokka,
        sum(oppilaita),
        sum(vieraskielisiä),
        sum(pidennettyOppivelvollisuusJaVaikeastiVammainen),
        sum(pidennettyOppivelvollisuusJaMuuKuinVaikeimminVammainen),
        sum(virheellisestiSiirretytVaikeastiVammaiset),
        sum(virheellisestiSiirretytMuutKuinVaikeimminVammaiset),
        sum(erityiselläTuella),
        sum(majoitusetu),
        sum(kuljetusetu),
        sum(sisäoppilaitosmainenMajoitus),
        sum(koulukoti),
        sum(joustava_perusopetus),
        sum(kotiopetus)
      from q
      group by oppilaitos_nimi, oppilaitos_oid, opetuskieli
    ) select *
    from totals
    order by oppilaitos_nimi, oppilaitos_oid, vuosiluokka
  """
  }

  def columnSettings(t: LocalizationReader): Seq[(String, Column)] = Seq(
    "oppilaitosNimi" -> Column(t.get("raportti-excel-kolumni-oppilaitoksenNimi")),
    "organisaatioOid" -> Column(t.get("raportti-excel-kolumni-organisaatioOid")),
    "opetuskieli" -> Column(t.get("raportti-excel-kolumni-opetuskieli"), comment = Some(t.get("raportti-excel-kolumni-opetuskieli-comment"))),
    "vuosiluokka" -> Column(t.get("raportti-excel-kolumni-vuosiluokka")),
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
    "koulukoti" -> Column(t.get("raportti-excel-kolumni-koulukoti"), comment = Some(t.get("raportti-excel-kolumni-koulukoti-comment"))),
    "joustavaPerusopetus" -> Column(t.get("raportti-excel-kolumni-joustavaPerusopetus"), comment = Some(t.get("raportti-excel-kolumni-joustavaPerusopetus-comment"))),
    "kotiopetus" -> Column(t.get("raportti-excel-kolumni-kotiopetus"), comment = Some(t.get("raportti-excel-kolumni-kotiopetus-comment")))
  )
}

case class PerusopetuksenOppijamäärätRaporttiRow(
  oppilaitosNimi: String,
  organisaatioOid: String,
  opetuskieli: String,
  vuosiluokka: String,
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
  koulukoti: Int,
  joustavaPerusopetus: Int,
  kotiopetus: Int
)
