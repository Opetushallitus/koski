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

// TODO(TOR-2650): build() ja buildOppijat() ajavat molemmat oman, lähes identtisen
// viiden taulun (r_henkilo/r_opiskeluoikeus/r_paatason_suoritus/r_opiskeluoikeus_aikajakso/
// r_kotikuntahistoria) liitoskyselynsä samalle oppilaitosOids-joukolle — sama rivijoukko
// haetaan ja liitetään tietokannasta kahteen kertaan yhden Excelin tuottamiseksi, vaikka
// aggregaattivälilehti voitaisiin periaatteessa johtaa jo haetuista oppija-riveistä Scalassa.
// Ei kiireellinen: raportti on rajattu yhteen koulutustoimijaan kerrallaan, joten kyselyjen
// koko pysynee pienenä eikä lähellä 5 minuutin timeout-budjettia — mutta jos tähän joskus
// palataan muusta syystä, kannattaa harkita yhdistämistä.
case class Kotikuntalaskelma(db: DB, organisaatioService: OrganisaatioService) extends QueryMethods {
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

  private def query(oppilaitosOids: Seq[String], päivä: LocalDate) = {
    sql"""
    with v as (
      select extract(year from $päivä::date)::int as vuosi
    )
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

      -- Jako perustuu nimenomaan vamman/sairauden/toimintakyvyn rajoitteeseen (vahvistettu
      -- tiketillä), ei toiminta-alueittaiseen opiskeluun eikä pidennettyyn oppivelvollisuuteen
      -- yleensä — nämä kolme eivät ole sama asia.
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

    from v, r_henkilo he
    join r_opiskeluoikeus oo on oo.oppija_oid = he.oppija_oid
    join r_paatason_suoritus pts on pts.opiskeluoikeus_oid = oo.opiskeluoikeus_oid
    left join r_opiskeluoikeus_aikajakso aj on aj.opiskeluoikeus_oid = oo.opiskeluoikeus_oid
    left join esiopetus_opiskeluoik_aikajakso eaj on eaj.opiskeluoikeus_oid = oo.opiskeluoikeus_oid
    -- Julkinen r_kotikuntahistoria: EI koski_confidential-varianttia, ks. tiedoston alun kommentti.
    left join r_kotikuntahistoria kkh
      on kkh.master_oid = he.master_oid
      and coalesce(kkh.muutto_pvm, '1900-01-01'::date) <= $päivä
      and (kkh.poismuutto_pvm >= $päivä or kkh.poismuutto_pvm is null)

    where oo.oppilaitos_oid = any($oppilaitosOids)
      and (
        (oo.koulutusmuoto in ('perusopetus', 'esiopetus')
          and pts.suorituksen_tyyppi in ('perusopetuksenvuosiluokka', 'perusopetuksenoppimaara', 'esiopetuksensuoritus'))
        or
        (oo.koulutusmuoto = 'internationalschool'
          and pts.koulutusmoduuli_koodiarvo in ('explorer', '1', '2', '3', '4', '5', '6', '7', '8', '9')
          and pts.alkamispaiva <= $päivä)
        or
        (oo.koulutusmuoto = 'europeanschoolofhelsinki'
          and pts.koulutusmoduuli_koodiarvo in ('N1', 'N2', 'P1', 'P2', 'P3', 'P4', 'P5', 'S1', 'S2', 'S3', 'S4')
          and pts.alkamispaiva <= $päivä)
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

    group by
      oo.koulutustoimija_oid,
      oo.koulutustoimija_nimi,
      kkh.kotikunta,
      kkh.kotikunta_nimi_fi
    order by oo.koulutustoimija_nimi, kkh.kotikunta
  """
  }

  // "Oppijat"-välilehti (TOR-2650, päätetty jatkokokouksessa, ks. suunnitelman 10.1 §): rivi per
  // oppija. Tavalliselle oppijalle näytetään oid, hetu, yksilöity-lippu (molemmat ennen nimiä),
  // nimet, kotikunta (ennen oppilaitosta), oppilaitos, luokka-aste ja luokka (luokka-aste ennen
  // luokkaa) sekä tosi/epätosi-liput samoille ikäryhmille kuin aggregaattivälilehdellä. Hetu on
  // hetuttomalle oppijalle luonnostaan NULL (r_henkilo.hetu on jo Option[String] skeemassa) — ei
  // erillistä käsittelyä tarvita. Kotikunta resolvoidaan samalla tavalla kuin
  // aggregaattivälilehdellä (suoraan r_kotikuntahistoriasta, ei r_henkilo-varakotikuntaa —
  // ks. 12 §:n päivitetty päätös: aukko jätetään mieluummin "Ei tiedossa" -tilaan kuin
  // arvataan nykyisen kotikunnan perusteella, koska arvattu arvo näyttäisi raportilla
  // täysin samalta kuin oikeasti kyseiselle päivälle vahvistettu tieto). Turvakielto ei
  // vaadi enää erillistä suojausta tässä, koska r_kotikuntahistoria on jo rakenteellisesti
  // turvakielto-suodatettu. Turvakiellon alaiselle oppijalle
  // hetu/yksilöity/nimet/kotikunta/oppilaitos/luokka-aste/luokka piilotetaan (null) ja
  // oid-sarakkeeseen kirjoitetaan "Turvakielto" tyhjän arvon sijaan, jotta rivi ei näytä
  // virheeltä — vain ikäryhmäliput näytetään muuten, jotta koulutustoimija näkee mistä
  // aggregaattivälilehden luku tulee ilman että turvakiellon alaisen oppijan henkilöllisyys
  // paljastuu. Päätetty näin nimenomaisesti (ei kokonaan piilotettu eikä kokonaan näytetty).
  // HUOM (kirjattu, ei ratkaistu suunnitelman 10.1 §:n mukaisesti): muille kuin turvakiellon
  // alaisille oppijoille kuusitoistaErityisenTuenPerusteella paljastaa erityisen tuen statuksen
  // nimetylle, tunnistettavalle oppijalle — ristiriidassa 4 §:n "Ei sisällytetä" -päätöksen hengen
  // kanssa. Toteutettu silti käyttäjän ohjeen mukaisesti.
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
    val raporttiQuery = oppijaQuery(oppilaitosOids, päivä).as[KotikuntalaskelmaOppijaRow]
    val rows = runDbSync(raporttiQuery, timeout = 5.minutes)
    DataSheet(
      title = t.get("raportti-excel-kotikuntalaskelma-oppijat-sheet-name"),
      rows = rows,
      columnSettings = oppijaColumnSettings(t)
    )
  }

  private def oppijaQuery(oppilaitosOids: Seq[String], päivä: LocalDate) = {
    sql"""
    with v as (
      select extract(year from $päivä::date)::int as vuosi
    )
    select
      case when bool_or(he.turvakielto) then 'Turvakielto' else he.master_oid end as oppija_numero,
      case when bool_or(he.turvakielto) then null else max(he.hetu) end as hetu,
      case when bool_or(he.turvakielto) then null else bool_or(he.yksiloity) end as yksiloity,
      case when bool_or(he.turvakielto) then null else max(he.etunimet) end as etunimet,
      case when bool_or(he.turvakielto) then null else max(he.sukunimi) end as sukunimi,
      case when bool_or(he.turvakielto) then null else max(kkh.kotikunta_nimi_fi) end as kotikunta,
      case when bool_or(he.turvakielto) then null else max(oo.oppilaitos_nimi) end as oppilaitos,
      -- TODO(TOR-2650): luokka_aste/luokka valitaan max()-aggregaatilla kaikista oppijan
      -- perusopetuksenvuosiluokka-suorituksista, ei vain päivälle $$päivä voimassa olevasta —
      -- toisin kuin internationalschool/europeanschoolofhelsinki-haaroissa, tässä ei ole
      -- pts.alkamispaiva <= $$päivä -rajausta. max() valitsee aakkosellisesti suurimman arvon,
      -- ei kronologisesti viimeisintä, joten luokan uusinut oppija (esim. vanha "3C", nykyinen
      -- "3A") voi näyttää raportilla väärän, jo korvatun luokan.
      case when bool_or(he.turvakielto) then null else max(pts.koulutusmoduuli_koodiarvo) end as luokka_aste,
      case when bool_or(he.turvakielto) then null else max(pts.luokka_tai_ryhma) end as luokka,

      bool_or(extract(year from he.syntymaaika) = v.vuosi - 6) as kuusi,

      bool_or(extract(year from he.syntymaaika) between v.vuosi - 12 and v.vuosi - 7) as seitseman_kaksitoista,

      bool_or(extract(year from he.syntymaaika) between v.vuosi - 15 and v.vuosi - 13) as kolmetoista_viisitoista,

      -- Jako perustuu nimenomaan vamman/sairauden/toimintakyvyn rajoitteeseen (vahvistettu
      -- tiketillä), ei toiminta-alueittaiseen opiskeluun eikä pidennettyyn oppivelvollisuuteen
      -- yleensä — nämä kolme eivät ole sama asia.
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

    from v, r_henkilo he
    join r_opiskeluoikeus oo on oo.oppija_oid = he.oppija_oid
    join r_paatason_suoritus pts on pts.opiskeluoikeus_oid = oo.opiskeluoikeus_oid
    left join r_opiskeluoikeus_aikajakso aj on aj.opiskeluoikeus_oid = oo.opiskeluoikeus_oid
    left join esiopetus_opiskeluoik_aikajakso eaj on eaj.opiskeluoikeus_oid = oo.opiskeluoikeus_oid
    -- Julkinen r_kotikuntahistoria: EI koski_confidential
    left join r_kotikuntahistoria kkh
      on kkh.master_oid = he.master_oid
      and coalesce(kkh.muutto_pvm, '1900-01-01'::date) <= $päivä
      and (kkh.poismuutto_pvm >= $päivä or kkh.poismuutto_pvm is null)

    where oo.oppilaitos_oid = any($oppilaitosOids)
      and (
        (oo.koulutusmuoto in ('perusopetus', 'esiopetus')
          and pts.suorituksen_tyyppi in ('perusopetuksenvuosiluokka', 'perusopetuksenoppimaara', 'esiopetuksensuoritus'))
        or
        (oo.koulutusmuoto = 'internationalschool'
          and pts.koulutusmoduuli_koodiarvo in ('explorer', '1', '2', '3', '4', '5', '6', '7', '8', '9')
          and pts.alkamispaiva <= $päivä)
        or
        (oo.koulutusmuoto = 'europeanschoolofhelsinki'
          and pts.koulutusmoduuli_koodiarvo in ('N1', 'N2', 'P1', 'P2', 'P3', 'P4', 'P5', 'S1', 'S2', 'S3', 'S4')
          and pts.alkamispaiva <= $päivä)
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

    group by he.master_oid
    order by he.master_oid
  """
  }

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
