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

  private def query(oppilaitosOids: Seq[String], päivä: LocalDate, hetutonTeksti: String) = concatMany(
    Some(sql"with "),
    Some(vuosiJaLukuvuosi(päivä)),
    Some(sql""",
    oppijat as (
      select
        oo.koulutustoimija_oid,
        oo.koulutustoimija_nimi,
        kkh.kotikunta,
        kkh.kotikunta_nimi_fi,
        bool_or(kkh.kotikunta is null and he.hetu is null) as hetuton,
    """),
    Some(ikäryhmät(päivä)),
    // Turvakiellon alaiset lasketaan todellisiin kotikuntiinsa
    Some(lähteet(päivä, turvakiellollinenKotikuntahistoria)),
    Some(ehdot(oppilaitoksissa(oppilaitosOids), päivä)),
    Some(sql"""
      group by oo.koulutustoimija_oid, oo.koulutustoimija_nimi, kkh.kotikunta, kkh.kotikunta_nimi_fi, he.master_oid
    )
    select
      koulutustoimija_oid as opetuksen_jarjestaja_oid,
      koulutustoimija_nimi as opetuksen_jarjestaja,
      kotikunta as kotikunnan_koodi,
      case when hetuton then $hetutonTeksti else kotikunta_nimi_fi end as oppilaan_kotikunta,
      count(*) filter (where kuusi) as kuusi,
      count(*) filter (where seitseman_kaksitoista) as seitseman_kaksitoista,
      count(*) filter (where kolmetoista_viisitoista) as kolmetoista_viisitoista,
      count(*) filter (where kuusitoista_erityisen_tuen_perusteella) as kuusitoista_erityisen_tuen_perusteella,
      count(*) filter (where kuusitoista_ei_erityisen_tuen_perusteella) as kuusitoista_ei_erityisen_tuen_perusteella,
      count(*) as yhteensa
    from oppijat
    group by koulutustoimija_oid, koulutustoimija_nimi, kotikunta, kotikunta_nimi_fi, hetuton
    order by koulutustoimija_nimi, kotikunta, hetuton
    """)
  )

  def buildOppijat(oppilaitosOids: Seq[String], päivä: LocalDate, t: LocalizationReader)(implicit u: KoskiSpecificSession): DataSheet = {
    val raporttiQuery = oppijaQuery(oppilaitoksissa(oppilaitosOids), oppilaitoksissa(oppilaitosOids), päivä, t.get("raportti-excel-default-value-esiopetus-lyhyt"), julkinenKotikuntahistoria).as[KotikuntalaskelmaOppijaRow]
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
