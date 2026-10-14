package fi.oph.koski.raportit

import java.time.LocalDate
import fi.oph.koski.db.QueryMethods
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.raportointikanta.Confidential
import fi.oph.koski.db.SQLHelpers.concatMany
import slick.jdbc.{GetResult, SQLActionBuilder}

trait KotikunnistaRaportoiva extends QueryMethods {
  protected val julkinenKotikuntahistoria = "r_kotikuntahistoria"
  protected val turvakiellollinenKotikuntahistoria = s"${Confidential.name}.r_kotikuntahistoria"

  protected def vuosiJaLukuvuosi(päivä: LocalDate): SQLActionBuilder = sql"""
    v as (
      select
        extract(year from $päivä::date)::int as vuosi,
        case
          when extract(month from $päivä::date) >= 8
            then make_date(extract(year from $päivä::date)::int, 8, 1)
          else make_date(extract(year from $päivä::date)::int - 1, 8, 1)
        end as edellinen_elokuu
    )
  """

  protected def lähteet(päivä: LocalDate, kotikuntahistoria: String): SQLActionBuilder = sql"""
    from v, r_henkilo he
    join r_opiskeluoikeus oo on oo.oppija_oid = he.oppija_oid
    join r_paatason_suoritus pts on pts.opiskeluoikeus_oid = oo.opiskeluoikeus_oid
    left join r_opiskeluoikeus_aikajakso aj on aj.opiskeluoikeus_oid = oo.opiskeluoikeus_oid
    left join esiopetus_opiskeluoik_aikajakso eaj on eaj.opiskeluoikeus_oid = oo.opiskeluoikeus_oid
    -- Muuttopäivänä voimassa kaksi kuntaa, viimeisin voittaa
    left join lateral (
      select k.kotikunta, k.kotikunta_nimi_fi
      from #$kotikuntahistoria k
      -- r_henkilo.master_oid on C-kollaatiolla, r_kotikuntahistorian indeksi ei: ilman tätä jokainen haku lukee koko taulun
      where k.master_oid = he.master_oid collate "default"
        and coalesce(k.muutto_pvm, '1900-01-01'::date) <= $päivä
        and (k.poismuutto_pvm >= $päivä or k.poismuutto_pvm is null)
      order by k.muutto_pvm desc nulls last
      limit 1
    ) kkh on true
  """

  protected def ehdot(rajaus: SQLActionBuilder, päivä: LocalDate): SQLActionBuilder = concatMany(Some(sql"""
    where """), Some(rajaus), Some(sql"""
      and (
        (oo.koulutusmuoto in ('perusopetus', 'esiopetus')
          and pts.suorituksen_tyyppi in ('perusopetuksenvuosiluokka', 'perusopetuksenoppimaara', 'esiopetuksensuoritus'))
        or
        (oo.koulutusmuoto = 'internationalschool'
          and pts.koulutusmoduuli_koodiarvo in ('explorer', '1', '2', '3', '4', '5', '6', '7', '8', '9')
          and pts.alkamispaiva between v.edellinen_elokuu and $päivä)
        or
        (oo.koulutusmuoto = 'europeanschoolofhelsinki'
          and pts.koulutusmoduuli_koodiarvo in ('N1', 'N2', 'P1', 'P2', 'P3', 'P4', 'P5', 'S1', 'S2', 'S3', 'S4', 'S5')
          and pts.alkamispaiva between v.edellinen_elokuu and $päivä)
      )
      and (
        (aj.alku <= $päivä and aj.loppu >= $päivä
          and aj.tila in ('lasna', 'eronnut', 'valmistunut')
          and not aj.kotiopetus)
        or
        (eaj.alku <= $päivä and eaj.loppu >= $päivä
          and eaj.tila in ('lasna', 'eronnut', 'valmistunut'))
      )
      and extract(year from he.syntymaaika) between v.vuosi - 16 and v.vuosi - 6
  """))

  // Oppija kuuluu erityisen tuen sarakkeeseen, jos yksikin hänen raportointipäivän jaksoistaan on sen perusteella
  protected def ikäryhmät(päivä: LocalDate): SQLActionBuilder = sql"""
      bool_or(extract(year from he.syntymaaika) = v.vuosi - 6) as kuusi,
      bool_or(extract(year from he.syntymaaika) between v.vuosi - 12 and v.vuosi - 7) as seitseman_kaksitoista,
      bool_or(extract(year from he.syntymaaika) between v.vuosi - 15 and v.vuosi - 13) as kolmetoista_viisitoista,
      bool_or(
        extract(year from he.syntymaaika) = v.vuosi - 16
        and aj.alku <= $päivä and aj.loppu >= $päivä
        and (aj.toiminta_alueittain_opiskelu or aj.opetus_vamman_sairauden_tai_rajoitteen_perusteella)
      ) as kuusitoista_erityisen_tuen_perusteella,
      bool_or(
        extract(year from he.syntymaaika) = v.vuosi - 16
        and aj.alku <= $päivä and aj.loppu >= $päivä
      ) and not bool_or(
        extract(year from he.syntymaaika) = v.vuosi - 16
        and aj.alku <= $päivä and aj.loppu >= $päivä
        and (aj.toiminta_alueittain_opiskelu or aj.opetus_vamman_sairauden_tai_rajoitteen_perusteella)
      ) as kuusitoista_ei_erityisen_tuen_perusteella
  """

  implicit protected lazy val getOppijaResult: GetResult[KotikuntalaskelmaOppijaRow] = GetResult(r =>
    KotikuntalaskelmaOppijaRow(
      oppijaNumero = Option(r.rs.getString("oppija_numero")),
      hetu = Option(r.rs.getString("hetu")),
      yksiloity = {
        val value = r.rs.getBoolean("yksiloity")
        if (r.rs.wasNull()) None else Some(value)
      },
      etunimet = Option(r.rs.getString("etunimet")),
      sukunimi = Option(r.rs.getString("sukunimi")),
      kotikunta = Option(r.rs.getString("kotikunta")),
      oppilaitos = Option(r.rs.getString("oppilaitos")),
      luokkaAste = Option(r.rs.getString("luokka_aste")),
      luokka = Option(r.rs.getString("luokka")),
      kuusi = r.rs.getBoolean("kuusi"),
      seitsemänKaksitoista = r.rs.getBoolean("seitseman_kaksitoista"),
      kolmetoistaViisitoista = r.rs.getBoolean("kolmetoista_viisitoista"),
      kuusitoistaErityisenTuenPerusteella = r.rs.getBoolean("kuusitoista_erityisen_tuen_perusteella"),
      kuusitoistaEiErityisenTuenPerusteella = r.rs.getBoolean("kuusitoista_ei_erityisen_tuen_perusteella")
    )
  )

  protected def oppijaQuery(rajaus: SQLActionBuilder, päivä: LocalDate, esiopetusLuokkaAste: String, kotikuntahistoria: String) = concatMany(
    Some(sql"with "),
    Some(vuosiJaLukuvuosi(päivä)),
    Some(sql""",
    valittu as (
      select distinct on (he.master_oid)
        he.master_oid,
        oo.koulutustoimija_nimi,
        oo.oppilaitos_nimi,
        case pts.suorituksen_tyyppi
          when 'perusopetuksenoppimaara' then null
          when 'esiopetuksensuoritus' then $esiopetusLuokkaAste
          else pts.koulutusmoduuli_koodiarvo
        end as luokka_aste,
        pts.luokka_tai_ryhma as luokka
    """),
    Some(lähteet(päivä, kotikuntahistoria)),
    Some(ehdot(rajaus, päivä)),
    Some(sql"""
      order by
        he.master_oid,
        coalesce(aj.tila, eaj.tila) = 'lasna' desc,
        pts.suorituksen_tyyppi in ('perusopetuksenoppimaara', 'esiopetuksensuoritus'),
        coalesce(pts.alkamispaiva <= $päivä, true) desc,
        pts.alkamispaiva desc nulls last,
        pts.paatason_suoritus_id
    )
    select
      case when bool_or(he.turvakielto) then 'Turvakielto' else he.master_oid end as oppija_numero,
      case when bool_or(he.turvakielto) then null else max(he.hetu) end as hetu,
      case when bool_or(he.turvakielto) then null else bool_or(he.yksiloity) end as yksiloity,
      case when bool_or(he.turvakielto) then null else max(he.etunimet) end as etunimet,
      case when bool_or(he.turvakielto) then null else max(he.sukunimi) end as sukunimi,
      case when bool_or(he.turvakielto) then null else max(kkh.kotikunta_nimi_fi) end as kotikunta,
      case when bool_or(he.turvakielto) then null else max(valittu.koulutustoimija_nimi) end as opetuksen_jarjestaja,
      case when bool_or(he.turvakielto) then null else max(valittu.oppilaitos_nimi) end as oppilaitos,
      case when bool_or(he.turvakielto) then null else max(valittu.luokka_aste) end as luokka_aste,
      case when bool_or(he.turvakielto) then null else max(valittu.luokka) end as luokka,
    """),
    Some(ikäryhmät(päivä)),
    Some(lähteet(päivä, kotikuntahistoria)),
    Some(sql"join valittu on valittu.master_oid = he.master_oid"),
    Some(ehdot(rajaus, päivä)),
    // Turvakieltorivit loppuun, ettei oidia voi rajata naapuririvien perusteella
    Some(sql"""
    group by he.master_oid
    order by bool_or(he.turvakielto), he.master_oid""")
  )

  protected def oppijaColumnSettings(t: LocalizationReader): Seq[(String, Column)] = Seq(
    "oppijaNumero" -> Column(t.get("raportti-excel-kolumni-oppijaNumero")),
    "hetu" -> Column(t.get("raportti-excel-kolumni-hetu")),
    "yksiloity" -> Column(t.get("raportti-excel-kolumni-yksiloity"), comment = Some(t.get("raportti-excel-kolumni-yksiloity-comment"))),
    "etunimet" -> Column(t.get("raportti-excel-kolumni-etunimet")),
    "sukunimi" -> Column(t.get("raportti-excel-kolumni-sukunimi")),
    "kotikunta" -> Column(t.get("raportti-excel-kolumni-kotikunta")),
    "oppilaitos" -> Column(t.get("raportti-excel-kolumni-oppilaitoksenNimi")),
    "luokkaAste" -> Column(t.get("raportti-excel-kolumni-luokkaAste")),
    "luokka" -> Column(t.get("raportti-excel-kolumni-luokka")),
    "kuusi" -> Column(t.get("raportti-excel-kolumni-kotikuntalaskelma-kuusi")),
    "seitsemänKaksitoista" -> Column(t.get("raportti-excel-kolumni-kotikuntalaskelma-seitsemanKaksitoista")),
    "kolmetoistaViisitoista" -> Column(t.get("raportti-excel-kolumni-kotikuntalaskelma-kolmetoistaViisitoista")),
    "kuusitoistaErityisenTuenPerusteella" -> Column(t.get("raportti-excel-kolumni-kotikuntalaskelma-kuusitoistaErityisenTuenPerusteella")),
    "kuusitoistaEiErityisenTuenPerusteella" -> Column(t.get("raportti-excel-kolumni-kotikuntalaskelma-kuusitoistaEiErityisenTuenPerusteella"))
  )
}
