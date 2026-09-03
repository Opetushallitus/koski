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

// TOR-2650: alustava luonnos (ks. documentation/kotikuntalaskelma-suunnitelma.md).
// Aggregaattivälilehti: oppilasmäärä opetuksen järjestäjän x oppilaan kotikunnan x ikäryhmän
// mukaan valitulta päivältä. Perustuu analyytikolta saatuun esimerkkikyselyyn (suunnitelman
// 8.1 §), mutta korjattu ja täydennetty seuraavasti:
//   - Käyttää julkista r_kotikuntahistoria-taulua confidential-variantin sijaan, jotta
//     turvakiellon alaisten oppijoiden kotikuntaa ei paljasteta (ks. suunnitelman 5 §, kohta 6).
//     Turvakiellon alaiset oppijat eivät tämän vuoksi resolvoi kotikuntaa ja päätyvät samaan
//     tyhjään (NULL) ryhmään kuin hetuttomat oppijat — kotikunnanKoodi- ja oppilaanKotikunta-
//     sarakkeet ovat molemmat Option[String] eikä kumpaakaan täytetä millään korvaavalla
//     tekstillä, jotta rivi ei näytä siltä kuin kotikunta olisi jotain tiettyä ("Ei tiedossa").
//   - Jos r_kotikuntahistoria ei sisällä paivä-parametrin kattavaa jaksoa (esim. historiatieto
//     alkaa myöhemmin kuin kysytty päivä, tai jaksoissa on aukko), pudotaan oppijan tämänhetkiseen
//     (r_henkilo) kotikuntaan sen sijaan että aina jätettäisiin tyhjäksi — sama malli kuin
//     EsiopetusRaportti.scala käyttää. TÄRKEÄÄ: tämä varakotikunta haetaan vain, jos
//     he.turvakielto = false — r_henkilo.kotikunta* EI ole suodatettu turvakiellon alaisille
//     (toisin kuin r_kotikuntahistoria), joten suora käyttö ilman tätä tarkistusta vuotaisi
//     turvakiellon alaisten oppijoiden osoitetiedon.
//   - Ikäryhmät lasketaan syntymävuoden ja parametrina saadun päivän perusteella (ei
//     kovakoodattuja vuosilukuja kuten alkuperäisessä esimerkkikyselyssä).
//   - Ei sisällä analyytikon kyselyn "yritysmuoto"/"y_tunnus"/"opetuksen_järjestäjän_kuntakoodi"
//     -sarakkeita: niitä ei ole suunnitelman 3-4 §:ssä sovittu mukaan otettaviksi, ja ne olisivat
//     tulleet organisaatio.organisaatio-taulusta, jota mikään muu Koskin raportti ei käytä eikä
//     jota raportointikanta-skeema mallinna (ROrganisaatioTable ei sisällä yritysmuoto-saraketta).
//     Opetuksen järjestäjän nimi/oid haetaan sen sijaan suoraan r_opiskeluoikeus-taulusta, kuten
//     muissakin raporteissa.
//
// AVOIMET KYSYMYKSET (ks. suunnitelman 5 § ja 8.4 §) — ei vielä ratkaistu, päätökset tarvitaan
// ennen tuotantoon vientiä:
//   1. Pidennetyn oppivelvollisuuden kanoninen kenttä: tämä toteutus käyttää suunnitelman 8.1 §:n
//      tapaan johdettua logiikkaa (toiminta_alueittain_opiskelu TAI
//      opetus_vamman_sairauden_tai_rajoitteen_perusteella). Vaihtoehtoinen, suunnitelman 8.2/8.3
//      §:ssä nähty tapa käyttää suoraan pidennetty_oppivelvollisuus-kenttää — nämä eivät
//      taatusti tarkoita samaa asiaa.
//   2. Kansainvälisten koulujen (internationalschool, europeanschoolofhelsinki) suoritusrajaus:
//      analyytikon esimerkki rajasi suorituksen alkamispäivän kuluvaan lukuvuoteen
//      (1.8.-tilastointipäivä). Tässä on yksinkertaisuuden vuoksi vain "alkamispaiva <= paiva" —
//      lukuvuoden alkupäivän laskenta pitää lisätä ennen tuotantoon vientiä jos rajaus on tarpeen.
//   3. Hetuttomien / turvakiellon alaisten oppijoiden esitystapa tyhjänä (NULL) kotikunta-
//      ryhmänä (yhdistettynä) on tämän toteutuksen valinta, ei suunnitelmassa erikseen
//      päätetty asia.
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
      coalesce(kkh.kotikunta, case when he.turvakielto then null else he.kotikunta end) as kotikunnan_koodi,
      coalesce(kkh.kotikunta_nimi_fi, case when he.turvakielto then null else he.kotikunta_nimi_fi end) as oppilaan_kotikunta,

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
          and (aj.toiminta_alueittain_opiskelu or aj.opetus_vamman_sairauden_tai_rajoitteen_perusteella)
        then he.master_oid
      end) as kuusitoista_erityisen_tuen_perusteella,

      count(distinct case
        when extract(year from he.syntymaaika) = v.vuosi - 16
          and aj.alku <= $päivä and aj.loppu >= $päivä
          and not (aj.toiminta_alueittain_opiskelu or aj.opetus_vamman_sairauden_tai_rajoitteen_perusteella)
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
      coalesce(kkh.kotikunta, case when he.turvakielto then null else he.kotikunta end),
      coalesce(kkh.kotikunta_nimi_fi, case when he.turvakielto then null else he.kotikunta_nimi_fi end)
    order by oo.koulutustoimija_nimi, coalesce(kkh.kotikunta, case when he.turvakielto then null else he.kotikunta end)
  """
  }

  // "Oppijat"-välilehti (TOR-2650, päätetty jatkokokouksessa, ks. suunnitelman 10.1 §): rivi per
  // oppija. Tavalliselle oppijalle näytetään oid, hetu, yksilöity-lippu (molemmat ennen nimiä),
  // nimet, kotikunta (ennen oppilaitosta), oppilaitos ja luokka sekä tosi/epätosi-liput samoille
  // ikäryhmille kuin aggregaattivälilehdellä. Hetu on hetuttomalle oppijalle luonnostaan NULL
  // (r_henkilo.hetu on jo Option[String] skeemassa) — ei erillistä käsittelyä tarvita. Kotikunta
  // resolvoidaan samalla tavalla kuin aggregaattivälilehdellä (r_kotikuntahistoria ensisijaisena,
  // turvakielto-suojattu r_henkilo-varakotikunta toissijaisena). Turvakiellon alaiselle oppijalle
  // hetu/yksilöity/nimet/kotikunta/oppilaitos/luokka piilotetaan (null) ja oid-sarakkeeseen
  // kirjoitetaan "Turvakielto" tyhjän arvon sijaan, jotta rivi ei näytä virheeltä — vain
  // ikäryhmäliput näytetään muuten, jotta koulutustoimija näkee mistä aggregaattivälilehden luku
  // tulee ilman että turvakiellon alaisen oppijan henkilöllisyys paljastuu. Päätetty näin
  // nimenomaisesti (ei kokonaan piilotettu eikä kokonaan näytetty).
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
    // HUOM: etunimet/sukunimi/oppilaitos/luokka kerätään max()-aggregaatilla per oppija, koska
    // rivit tulevat opiskeluoikeuskohtaisesti mutta tulos on yksi rivi per oppija. Oletus (käyttäjän
    // vahvistama): oppijalla ei ole kahta samanaikaista kelpaavaa opiskeluoikeutta, joten max()
    // palauttaa aina yksikäsitteisen arvon käytännössä — ei erillistä käsittelyä tälle tapaukselle.
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
      case when bool_or(he.turvakielto) then null else max(coalesce(kkh.kotikunta_nimi_fi, he.kotikunta_nimi_fi)) end as kotikunta,
      case when bool_or(he.turvakielto) then null else max(oo.oppilaitos_nimi) end as oppilaitos,
      case when bool_or(he.turvakielto) then null else max(pts.luokka_aste) end as luokka_aste,
      case when bool_or(he.turvakielto) then null else max(pts.luokka_tai_ryhma) end as luokka,

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
        and not (aj.toiminta_alueittain_opiskelu or aj.opetus_vamman_sairauden_tai_rajoitteen_perusteella)
      ) as kuusitoista_ei_erityisen_tuen_perusteella

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
