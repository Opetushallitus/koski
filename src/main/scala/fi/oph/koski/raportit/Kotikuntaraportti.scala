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

// TOR-2650: alustava luonnos (ks. documentation/kotikuntaraportti-suunnitelma.md).
// Aggregaattivälilehti: oppilasmäärä opetuksen järjestäjän x oppilaan kotikunnan x ikäryhmän
// mukaan valitulta päivältä. Perustuu analyytikolta saatuun esimerkkikyselyyn (suunnitelman
// 8.1 §), mutta korjattu ja täydennetty seuraavasti:
//   - Käyttää julkista r_kotikuntahistoria-taulua confidential-variantin sijaan, jotta
//     turvakiellon alaisten oppijoiden kotikuntaa ei paljasteta (ks. suunnitelman 5 §, kohta 6).
//     Turvakiellon alaiset oppijat eivät tämän vuoksi resolvoi kotikuntaa ja päätyvät samaan
//     "Ei tiedossa" -ryhmään kuin hetuttomat oppijat.
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
//   3. Hetuttomien / turvakiellon alaisten oppijoiden esitystapa "Ei tiedossa" -ryhmänä on tämän
//      toteutuksen valinta, ei suunnitelmassa erikseen päätetty asia.
case class Kotikuntaraportti(db: DB, organisaatioService: OrganisaatioService) extends QueryMethods {
  implicit private val getResult: GetResult[KotikuntaraporttiRow] = GetResult(r =>
    KotikuntaraporttiRow(
      opetuksenJärjestäjäOid = r.rs.getString("opetuksen_jarjestaja_oid"),
      opetuksenJärjestäjä = r.rs.getString("opetuksen_jarjestaja"),
      kotikunnanKoodi = Option(r.rs.getString("kotikunnan_koodi")),
      oppilaanKotikunta = r.rs.getString("oppilaan_kotikunta"),
      kuusi = r.rs.getInt("kuusi"),
      seitsemänKaksitoista = r.rs.getInt("seitseman_kaksitoista"),
      kolmetoistaViisitoista = r.rs.getInt("kolmetoista_viisitoista"),
      kuusitoistaErityisenTuenPerusteella = r.rs.getInt("kuusitoista_erityisen_tuen_perusteella"),
      kuusitoistaEiErityisenTuenPerusteella = r.rs.getInt("kuusitoista_ei_erityisen_tuen_perusteella"),
      yhteensä = r.rs.getInt("yhteensa")
    )
  )

  def build(oppilaitosOids: Seq[String], päivä: LocalDate, t: LocalizationReader)(implicit u: KoskiSpecificSession): DataSheet = {
    val raporttiQuery = query(oppilaitosOids, päivä).as[KotikuntaraporttiRow]
    val rows = runDbSync(raporttiQuery, timeout = 5.minutes)
    DataSheet(
      title = t.get("raportti-excel-kotikuntaraportti-sheet-name"),
      rows = rows,
      columnSettings = columnSettings(t)
    )
  }

  private def query(oppilaitosOids: Seq[String], päivä: LocalDate) = {
    sql"""
    with v as (
      select extract(year from $päivä)::int as vuosi
    )
    select
      oo.koulutustoimija_oid as opetuksen_jarjestaja_oid,
      oo.koulutustoimija_nimi as opetuksen_jarjestaja,
      kkh.kotikunta as kotikunnan_koodi,
      coalesce(kkh.kotikunta_nimi_fi, 'Ei tiedossa') as oppilaan_kotikunta,

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
      and kkh.muutto_pvm <= $päivä
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

    group by oo.koulutustoimija_oid, oo.koulutustoimija_nimi, kkh.kotikunta, coalesce(kkh.kotikunta_nimi_fi, 'Ei tiedossa')
    order by oo.koulutustoimija_nimi, kkh.kotikunta
  """
  }

  def columnSettings(t: LocalizationReader): Seq[(String, Column)] = Seq(
    "opetuksenJärjestäjäOid" -> Column(t.get("raportti-excel-kolumni-opetuksenJarjestajaOid")),
    "opetuksenJärjestäjä" -> Column(t.get("raportti-excel-kolumni-opetuksenJarjestaja")),
    "kotikunnanKoodi" -> Column(t.get("raportti-excel-kolumni-kotikunnanKoodi")),
    "oppilaanKotikunta" -> Column(t.get("raportti-excel-kolumni-kotikunta")),
    "kuusi" -> Column(t.get("raportti-excel-kolumni-kotikuntaraportti-kuusi")),
    "seitsemänKaksitoista" -> Column(t.get("raportti-excel-kolumni-kotikuntaraportti-seitsemanKaksitoista")),
    "kolmetoistaViisitoista" -> Column(t.get("raportti-excel-kolumni-kotikuntaraportti-kolmetoistaViisitoista")),
    "kuusitoistaErityisenTuenPerusteella" -> Column(t.get("raportti-excel-kolumni-kotikuntaraportti-kuusitoistaErityisenTuenPerusteella")),
    "kuusitoistaEiErityisenTuenPerusteella" -> Column(t.get("raportti-excel-kolumni-kotikuntaraportti-kuusitoistaEiErityisenTuenPerusteella")),
    "yhteensä" -> Column(t.get("raportti-excel-kolumni-kotikuntaraportti-yhteensa"))
  )
}

case class KotikuntaraporttiRow(
  opetuksenJärjestäjäOid: String,
  opetuksenJärjestäjä: String,
  kotikunnanKoodi: Option[String],
  oppilaanKotikunta: String,
  kuusi: Int,
  seitsemänKaksitoista: Int,
  kolmetoistaViisitoista: Int,
  kuusitoistaErityisenTuenPerusteella: Int,
  kuusitoistaEiErityisenTuenPerusteella: Int,
  yhteensä: Int
)
