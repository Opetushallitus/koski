package fi.oph.koski.raportit

import java.time.LocalDate
import fi.oph.koski.db.QueryMethods
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.db.DB
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.db.SQLHelpers.concatMany
import slick.jdbc.{GetResult, SQLActionBuilder}

import scala.concurrent.duration.DurationInt


case class Kotikuntalaskelma(db: DB) extends QueryMethods {
  implicit private val getResult: GetResult[KotikuntalaskelmaRow] = GetResult(r =>
    KotikuntalaskelmaRow(
      opetuksenJärjestäjäOid = r.rs.getString("opetuksen_jarjestaja_oid"),
      opetuksenJärjestäjä = r.rs.getString("opetuksen_jarjestaja"),
      kotikunnanKoodi = Option(r.rs.getString("kotikunnan_koodi")),
      oppilaanKotikunta = Option(r.rs.getString("oppilaan_kotikunta")),
      kuusi = r.rs.getInt("kuusi"),
      seitsemänKaksitoista = r.rs.getInt("seitseman_kaksitoista"),
      kolmetoistaViisitoista = r.rs.getInt("kolmetoista_viisitoista"),
      kuusitoistaErityisenTuenPerusteella = r.rs.getInt("kuusitoista_erityisen_tuen_perusteella"),
      kuusitoistaEiErityisenTuenPerusteella = r.rs.getInt("kuusitoista_ei_erityisen_tuen_perusteella"),
      yhteensä = r.rs.getInt("yhteensa")
    )
  )

  def build(oppilaitosOids: Seq[String], päivä: LocalDate, t: LocalizationReader)(implicit u: KoskiSpecificSession): DataSheet = {
    val raporttiQuery = query(oppilaitosOids, päivä).as[KotikuntalaskelmaRow]
    val rows = runDbSync(raporttiQuery, timeout = 5.minutes)
    DataSheet(
      title = t.get("raportti-excel-kotikuntalaskelma-sheet-name"),
      rows = rows,
      columnSettings = columnSettings(t)
    )
  }

  private def vuosiJaLukuvuosi(päivä: LocalDate): SQLActionBuilder = sql"""
    v as (
      select
        extract(year from $päivä::date)::int as vuosi,
        -- Kansainvälisten koulujen (internationalschool, europeanschoolofhelsinki) luokka-asteet
        -- lasketaan vain kuluvalta lukuvuodelta
        case
          when extract(month from $päivä::date) >= 8
            then make_date(extract(year from $päivä::date)::int, 8, 1)
          else make_date(extract(year from $päivä::date)::int - 1, 8, 1)
        end as edellinen_elokuu
    )
  """

  // Raportin oppijajoukon määrittävät liitokset ja ehdot. Yhteiset kaikille kyselyille, jotta
  // aggregaatti- ja oppijat-välilehti laskevat täsmälleen samat oppijat.
  private def lähteet(päivä: LocalDate): SQLActionBuilder = sql"""
    from v, r_henkilo he
    join r_opiskeluoikeus oo on oo.oppija_oid = he.oppija_oid
    join r_paatason_suoritus pts on pts.opiskeluoikeus_oid = oo.opiskeluoikeus_oid
    left join r_opiskeluoikeus_aikajakso aj on aj.opiskeluoikeus_oid = oo.opiskeluoikeus_oid
    left join esiopetus_opiskeluoik_aikajakso eaj on eaj.opiskeluoikeus_oid = oo.opiskeluoikeus_oid
    left join r_kotikuntahistoria kkh
      on kkh.master_oid = he.master_oid
      and coalesce(kkh.muutto_pvm, '1900-01-01'::date) <= $päivä
      and (kkh.poismuutto_pvm >= $päivä or kkh.poismuutto_pvm is null)
  """

  private def ehdot(oppilaitosOids: Seq[String], päivä: LocalDate): SQLActionBuilder = sql"""
    where oo.oppilaitos_oid = any($oppilaitosOids)
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
  """

  private def query(oppilaitosOids: Seq[String], päivä: LocalDate) = concatMany(
    Some(sql"with "),
    Some(vuosiJaLukuvuosi(päivä)),
    Some(sql"""
    select
      oo.koulutustoimija_oid as opetuksen_jarjestaja_oid,
      oo.koulutustoimija_nimi as opetuksen_jarjestaja,
      kkh.kotikunta as kotikunnan_koodi,
      kkh.kotikunta_nimi_fi as oppilaan_kotikunta,

      count(distinct case
        when extract(year from he.syntymaaika) = v.vuosi - 6
        then he.master_oid
      end) as kuusi,

      count(distinct case
        when extract(year from he.syntymaaika) between v.vuosi - 12 and v.vuosi - 7
        then he.master_oid
      end) as seitseman_kaksitoista,

      count(distinct case
        when extract(year from he.syntymaaika) between v.vuosi - 15 and v.vuosi - 13
        then he.master_oid
      end) as kolmetoista_viisitoista,

      count(distinct case
        when extract(year from he.syntymaaika) = v.vuosi - 16
          and aj.alku <= $päivä and aj.loppu >= $päivä
          and aj.opetus_vamman_sairauden_tai_rajoitteen_perusteella
        then he.master_oid
      end) as kuusitoista_erityisen_tuen_perusteella,

      count(distinct case
        when extract(year from he.syntymaaika) = v.vuosi - 16
          and aj.alku <= $päivä and aj.loppu >= $päivä
          and not aj.opetus_vamman_sairauden_tai_rajoitteen_perusteella
        then he.master_oid
      end) as kuusitoista_ei_erityisen_tuen_perusteella,

      count(distinct case
        when extract(year from he.syntymaaika) between v.vuosi - 16 and v.vuosi - 6
        then he.master_oid
      end) as yhteensa
    """),
    Some(lähteet(päivä)),
    Some(ehdot(oppilaitosOids, päivä)),
    Some(sql"""
    group by
      oo.koulutustoimija_oid,
      oo.koulutustoimija_nimi,
      kkh.kotikunta,
      kkh.kotikunta_nimi_fi
    order by oo.koulutustoimija_nimi, kkh.kotikunta
    """)
  )

  implicit private val getOppijaResult: GetResult[KotikuntalaskelmaOppijaRow] = GetResult(r =>
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

  def buildOppijat(oppilaitosOids: Seq[String], päivä: LocalDate, t: LocalizationReader)(implicit u: KoskiSpecificSession): DataSheet = {
    val raporttiQuery = oppijaQuery(oppilaitosOids, päivä, t.get("raportti-excel-default-value-esiopetus")).as[KotikuntalaskelmaOppijaRow]
    val rows = runDbSync(raporttiQuery, timeout = 5.minutes)
    DataSheet(
      title = t.get("raportti-excel-kotikuntalaskelma-oppijat-sheet-name"),
      rows = rows,
      columnSettings = oppijaColumnSettings(t)
    )
  }

  private def oppijaQuery(oppilaitosOids: Seq[String], päivä: LocalDate, esiopetusLuokkaAste: String) = concatMany(
    Some(sql"with "),
    Some(vuosiJaLukuvuosi(päivä)),
    // Oppija voi osua raportille usealla päätason suorituksella (esim. vuosiluokka ja oppimäärä, tai
    // raportointipäivänä vaihdettu koulu). Oppilaitos, luokka-aste ja luokka otetaan kaikki samalta,
    // raportointipäivänä ajankohtaisimmalta suoritukselta:
    //   1. läsnä-tilainen opiskeluoikeus ennen eronnutta/valmistunutta
    //   2. vuosiluokan suoritus ennen perusopetuksen oppimäärää ja esiopetusta
    //   3. raportointipäivään mennessä alkanut ennen myöhemmin alkavaa
    //   4. viimeisimpänä alkanut ensin; alkamispäivättömät (ennen v. 2019 tallennetut) viimeisinä
    Some(sql""",
    valittu as (
      select distinct on (he.master_oid)
        he.master_oid,
        oo.oppilaitos_nimi,
        case pts.suorituksen_tyyppi
          when 'perusopetuksenoppimaara' then null
          when 'esiopetuksensuoritus' then $esiopetusLuokkaAste
          else pts.koulutusmoduuli_koodiarvo
        end as luokka_aste,
        pts.luokka_tai_ryhma as luokka
    """),
    Some(lähteet(päivä)),
    Some(ehdot(oppilaitosOids, päivä)),
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
      -- valittu-rivejä on yksi oppijaa kohden, joten max() vain poimii sen arvon
      case when bool_or(he.turvakielto) then null else max(valittu.oppilaitos_nimi) end as oppilaitos,
      case when bool_or(he.turvakielto) then null else max(valittu.luokka_aste) end as luokka_aste,
      case when bool_or(he.turvakielto) then null else max(valittu.luokka) end as luokka,

      bool_or(extract(year from he.syntymaaika) = v.vuosi - 6) as kuusi,

      bool_or(extract(year from he.syntymaaika) between v.vuosi - 12 and v.vuosi - 7) as seitseman_kaksitoista,

      bool_or(extract(year from he.syntymaaika) between v.vuosi - 15 and v.vuosi - 13) as kolmetoista_viisitoista,

      bool_or(
        extract(year from he.syntymaaika) = v.vuosi - 16
        and aj.alku <= $päivä and aj.loppu >= $päivä
        and aj.opetus_vamman_sairauden_tai_rajoitteen_perusteella
      ) as kuusitoista_erityisen_tuen_perusteella,

      bool_or(
        extract(year from he.syntymaaika) = v.vuosi - 16
        and aj.alku <= $päivä and aj.loppu >= $päivä
        and not aj.opetus_vamman_sairauden_tai_rajoitteen_perusteella
      ) as kuusitoista_ei_erityisen_tuen_perusteella
    """),
    Some(lähteet(päivä)),
    Some(sql"join valittu on valittu.master_oid = he.master_oid"),
    Some(ehdot(oppilaitosOids, päivä)),
    Some(sql"""
    group by he.master_oid
    -- Turvakiellon alaiset oppijat aina listan loppuun (bool_or(turvakielto) järjestetään ensin:
    -- false=0 ennen true=1), jotta niiden todellista sijaintia listassa ei voi päätellä
    -- vertaamalla naapuririvien näkyviä oppijanumeroita — muuten piilotettu rivi istuisi tarkalleen
    -- kahden näkyvän oidin välissä ja sen identiteetin voisi rajata näiden perusteella.
    order by bool_or(he.turvakielto), he.master_oid
    """)
  )

  private def oppijaColumnSettings(t: LocalizationReader): Seq[(String, Column)] = Seq(
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

  def columnSettings(t: LocalizationReader): Seq[(String, Column)] = Seq(
    "opetuksenJärjestäjäOid" -> Column(t.get("raportti-excel-kolumni-opetuksenJarjestajaOid")),
    "opetuksenJärjestäjä" -> Column(t.get("raportti-excel-kolumni-opetuksenJarjestaja")),
    "kotikunnanKoodi" -> Column(t.get("raportti-excel-kolumni-kotikunnanKoodi")),
    "oppilaanKotikunta" -> Column(t.get("raportti-excel-kolumni-kotikunta")),
    "kuusi" -> Column(t.get("raportti-excel-kolumni-kotikuntalaskelma-kuusi")),
    "seitsemänKaksitoista" -> Column(t.get("raportti-excel-kolumni-kotikuntalaskelma-seitsemanKaksitoista")),
    "kolmetoistaViisitoista" -> Column(t.get("raportti-excel-kolumni-kotikuntalaskelma-kolmetoistaViisitoista")),
    "kuusitoistaErityisenTuenPerusteella" -> Column(t.get("raportti-excel-kolumni-kotikuntalaskelma-kuusitoistaErityisenTuenPerusteella")),
    "kuusitoistaEiErityisenTuenPerusteella" -> Column(t.get("raportti-excel-kolumni-kotikuntalaskelma-kuusitoistaEiErityisenTuenPerusteella")),
    "yhteensä" -> Column(t.get("raportti-excel-kolumni-kotikuntalaskelma-yhteensa"))
  )
}

case class KotikuntalaskelmaRow(
  opetuksenJärjestäjäOid: String,
  opetuksenJärjestäjä: String,
  kotikunnanKoodi: Option[String],
  oppilaanKotikunta: Option[String],
  kuusi: Int,
  seitsemänKaksitoista: Int,
  kolmetoistaViisitoista: Int,
  kuusitoistaErityisenTuenPerusteella: Int,
  kuusitoistaEiErityisenTuenPerusteella: Int,
  yhteensä: Int
)

case class KotikuntalaskelmaOppijaRow(
  oppijaNumero: Option[String],
  hetu: Option[String],
  yksiloity: Option[Boolean],
  etunimet: Option[String],
  sukunimi: Option[String],
  kotikunta: Option[String],
  oppilaitos: Option[String],
  luokkaAste: Option[String],
  luokka: Option[String],
  kuusi: Boolean,
  seitsemänKaksitoista: Boolean,
  kolmetoistaViisitoista: Boolean,
  kuusitoistaErityisenTuenPerusteella: Boolean,
  kuusitoistaEiErityisenTuenPerusteella: Boolean
)
