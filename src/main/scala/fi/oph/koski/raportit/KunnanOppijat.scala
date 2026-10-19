package fi.oph.koski.raportit

import java.time.LocalDate
import fi.oph.koski.db.DB
import fi.oph.koski.db.SQLHelpers.concatMany
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.localization.LocalizationReader
import slick.jdbc.{GetResult, SQLActionBuilder}

import scala.concurrent.duration.DurationInt

// Kuntalaiset haetaan luottamuksellisesta kotikuntahistoriasta, jotta myös turvakiellon alaiset näkyvät ilman tunnistetietoja
case class KunnanOppijat(db: DB) extends KotikunnistaRaportoiva {
  implicit private val getKunnanOppijaResult: GetResult[KunnanOppijaRow] = GetResult(r => {
    val o = getOppijaResult(r)
    KunnanOppijaRow(o.oppijaNumero, o.hetu, o.yksiloity, o.etunimet, o.sukunimi, o.kotikunta,
      Option(r.rs.getString("opetuksen_jarjestaja")), o.oppilaitos, o.luokkaAste, o.kuusi,
      o.seitsemänKaksitoista, o.kolmetoistaViisitoista, o.kuusitoistaErityisenTuenPerusteella, o.kuusitoistaEiErityisenTuenPerusteella)
  })

  def build(kuntaOid: String, kuntakoodi: String, päivä: LocalDate, t: LocalizationReader)(implicit u: KoskiSpecificSession): DataSheet =
    DataSheet(
      title = t.get("raportti-excel-kunnanoppijat-sheet-name"),
      rows = oppijat(kuntaOid, kuntakoodi, päivä, t.get("raportti-excel-default-value-esiopetus-lyhyt")),
      columnSettings = columnSettings(t)
    )

  def oppijat(kuntaOid: String, kuntakoodi: String, päivä: LocalDate, esiopetusLuokkaAste: String): Seq[KunnanOppijaRow] =
    runDbSync(oppijaQuery(kuntalaisetEnnenKotikuntahakua(kuntaOid, kuntakoodi, päivä), kotikunnassaMuunJärjestämänä(kuntaOid, kuntakoodi), päivä, esiopetusLuokkaAste, turvakiellollinenKotikuntahistoria).as[KunnanOppijaRow], timeout = 5.minutes)

  private def kotikunnassaMuunJärjestämänä(kuntaOid: String, kuntakoodi: String): SQLActionBuilder =
    sql"""kkh.kotikunta = $kuntakoodi and oo.koulutustoimija_oid <> $kuntaOid"""

  // in-ehto rajaa kuntalaiset ennen kotikunnan per rivi -hakua. Vain valintaan: pääkyselyssä toistettuna pienen kunnan rivimääräarvio putoaa yhteen ja kysely hidastuu neliöllisesti.
  private def kuntalaisetEnnenKotikuntahakua(kuntaOid: String, kuntakoodi: String, päivä: LocalDate): SQLActionBuilder = concatMany(
    Some(kotikunnassaMuunJärjestämänä(kuntaOid, kuntakoodi)),
    Some(sql"""
      and he.master_oid in (
        select k.master_oid from #$turvakiellollinenKotikuntahistoria k
        where k.kotikunta = $kuntakoodi
          and coalesce(k.muutto_pvm, '1900-01-01'::date) <= $päivä
          and (k.poismuutto_pvm >= $päivä or k.poismuutto_pvm is null))""")
  )

  private def columnSettings(t: LocalizationReader): Seq[(String, Column)] = {
    val (ennen, jälkeen) = oppijaColumnSettings(t).filterNot(_._1 == "luokka").span(_._1 != "oppilaitos")
    ennen ++ Seq("opetuksenJärjestäjä" -> Column(t.get("raportti-excel-kolumni-opetuksenJarjestaja"))) ++ jälkeen
  }
}

case class KunnanOppijaRow(
  oppijaNumero: Option[String],
  hetu: Option[String],
  yksiloity: Option[Boolean],
  etunimet: Option[String],
  sukunimi: Option[String],
  kotikunta: Option[String],
  opetuksenJärjestäjä: Option[String],
  oppilaitos: Option[String],
  luokkaAste: Option[String],
  kuusi: Boolean,
  seitsemänKaksitoista: Boolean,
  kolmetoistaViisitoista: Boolean,
  kuusitoistaErityisenTuenPerusteella: Boolean,
  kuusitoistaEiErityisenTuenPerusteella: Boolean
)
