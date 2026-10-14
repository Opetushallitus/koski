package fi.oph.koski.raportit

import java.time.LocalDate
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.db.DB
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.db.SQLHelpers.concatMany
import slick.jdbc.{GetResult, SQLActionBuilder}

import scala.concurrent.duration.DurationInt

case class Kotikuntalaskelma(db: DB) extends KotikunnistaRaportoiva {
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
    val raporttiQuery = query(oppilaitosOids, päivä, t.get("raportti-excel-default-value-hetuton")).as[KotikuntalaskelmaRow]
    val rows = runDbSync(raporttiQuery, timeout = 5.minutes)
    DataSheet(
      title = t.get("raportti-excel-kotikuntalaskelma-sheet-name"),
      rows = rows,
      columnSettings = columnSettings(t)
    )
  }

  private def oppilaitoksissa(oppilaitosOids: Seq[String]): SQLActionBuilder = sql"oo.oppilaitos_oid = any($oppilaitosOids)"

  private def query(oppilaitosOids: Seq[String], päivä: LocalDate, hetutonTeksti: String) = {
    val rajaus = oppilaitoksissa(oppilaitosOids)
    concatMany(
    Some(sql"with "),
    Some(vuosiJaLukuvuosi(päivä)),
    Some(sql"""
    select
      oo.koulutustoimija_oid as opetuksen_jarjestaja_oid,
      oo.koulutustoimija_nimi as opetuksen_jarjestaja,
      kkh.kotikunta as kotikunnan_koodi,
      case
        when kkh.kotikunta is null and he.hetu is null then $hetutonTeksti
        else kkh.kotikunta_nimi_fi
      end as oppilaan_kotikunta,

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
    // Turvakiellon alaiset lasketaan todellisiin kotikuntiinsa
    Some(lähteet(päivä, turvakiellollinenKotikuntahistoria)),
    Some(ehdot(rajaus, päivä)),
    Some(sql"""
    group by
      oo.koulutustoimija_oid,
      oo.koulutustoimija_nimi,
      kkh.kotikunta,
      kkh.kotikunta_nimi_fi,
      kkh.kotikunta is null and he.hetu is null
    order by oo.koulutustoimija_nimi, kkh.kotikunta, kkh.kotikunta is null and he.hetu is null
    """)
  )
  }

  def buildOppijat(oppilaitosOids: Seq[String], päivä: LocalDate, t: LocalizationReader)(implicit u: KoskiSpecificSession): DataSheet = {
    val raporttiQuery = oppijaQuery(oppilaitoksissa(oppilaitosOids), päivä, t.get("raportti-excel-default-value-esiopetus-lyhyt"), julkinenKotikuntahistoria).as[KotikuntalaskelmaOppijaRow]
    val rows = runDbSync(raporttiQuery, timeout = 5.minutes)
    DataSheet(
      title = t.get("raportti-excel-kotikuntalaskelma-oppijat-sheet-name"),
      rows = rows,
      columnSettings = oppijaColumnSettings(t)
    )
  }

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
