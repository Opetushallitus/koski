package fi.oph.koski.raportit

import java.sql.{Date, ResultSet}
import java.time.LocalDate

import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.log.Logging
import fi.oph.koski.raportointikanta.RaportointiDatabase
import slick.jdbc.GetResult
import scala.concurrent.duration._

object PaallekkaisetOpiskeluoikeudet extends Logging {

  def datasheet(oids: Seq[String], aikaisintaan: LocalDate, viimeistaan: LocalDate, db: RaportointiDatabase) =
    DataSheet(
      title = "Päällekäiset opiskeluoikeudet",
      rows = db.runDbSync(query(oids, Date.valueOf(aikaisintaan), Date.valueOf(viimeistaan)).as[PaallekkaisetOpiskeluoikeudetRow], timeout = 5.minutes),
      columnSettings
    )

  def createMaterializedView =
    sqlu"""
      create materialized view paallekkaiset_opiskeluoikeudet as
        select
          opiskeluoikeus.oppija_oid,
          opiskeluoikeus.opiskeluoikeus_oid,
          opiskeluoikeus.oppilaitos_nimi,
          opiskeluoikeus.koulutusmuoto,
          opiskeluoikeus.alkamispaiva,
          opiskeluoikeus.viimeisin_tila,
          opiskeluoikeus.paattymispaiva,
          paallekkainen.opiskeluoikeus_oid paallekkainen_opiskeluoikeus_oid,
          paallekkainen.oppilaitos_nimi    paallekkainen_oppilaitos_nimi,
          paallekkainen.koulutusmuoto      paallekkainen_koulutusmuoto,
          paallekkainen.viimeisin_tila     paallekkainen_viimeisin_tila,
          paallekkainen.alkamispaiva       paallekkainen_alkamispaiva,
          paallekkainen.paattymispaiva     paallekkainen_paattymispaiva
      from r_opiskeluoikeus opiskeluoikeus
        join lateral (
          select *
          from r_opiskeluoikeus paallekkainen
          where paallekkainen.oppija_oid = opiskeluoikeus.oppija_oid
               and not paallekkainen.opiskeluoikeus_oid = opiskeluoikeus.opiskeluoikeus_oid
               and paallekkainen.sisaltyy_opiskeluoikeuteen_oid is null
               and coalesce(paallekkainen.paattymispaiva, '9999-12-31'::date) >= opiskeluoikeus.alkamispaiva
               and paallekkainen.alkamispaiva <= coalesce(opiskeluoikeus.paattymispaiva, '9999-12-31'::date)
        ) paallekkainen on paallekkainen.oppija_oid = opiskeluoikeus.oppija_oid
        where opiskeluoikeus.sisaltyy_opiskeluoikeuteen_oid is null
    """

  def createIndex =
    sqlu"create index on paallekkaiset_opiskeluoikeudet(opiskeluoikeus_oid)"

  private def query(oppilaitosOids: Seq[String], aikaisintaan: Date, viimeistaan: Date) =
    sql"""
      select
        paallekkaiset_opiskeluoikeudet.*,
        rahoitusmuodot.koodiarvot rahoitusmuodot,
        rahoitusmuodot_osuu_parametreille.koodiarvot rahoitusmuodot_osuu_parametreille,
        paallekkaiset_opiskeluoikeudet.paallekkainen_alkamispaiva < paallekkaiset_opiskeluoikeudet.alkamispaiva paallekkainen_alkanut_eka,
        paallekkainen_rahoitusmuodot.koodiarvot paallekkainen_rahoitusmuodot,
        paallekkainen_rahoitusmuodot_osuu_parametreille.koodiarvot paallekkainen_rahoitusmuodot_parametrien_sisalla,
        paallekkaisen_opiskeluoikeuden_tilat_parametrien_sisalla.tilat paallekkainen_tilat_parametrien_sisalla,
        haetun_opiskeluoikeuden_tilat_parametrien_sisalla.tilat tilat_parametrien_sisalla,
        paatason_suoritukset.tyyppi_ja_koodiarvo paallekkainen_paatason_suoritukset,
        paallekkainen_alkamispaiva <= $viimeistaan and coalesce(paallekkainen_paattymispaiva, '9999-12-31'::date) >= $aikaisintaan paallekkainen_voimassa_aikajaksolla
      from (
        select
          distinct r_opiskeluoikeus.opiskeluoikeus_oid
        from r_opiskeluoikeus
          join r_opiskeluoikeus_aikajakso on r_opiskeluoikeus_aikajakso.opiskeluoikeus_oid = r_opiskeluoikeus.opiskeluoikeus_oid
            where oppilaitos_oid = any($oppilaitosOids)
              and r_opiskeluoikeus_aikajakso.alku <= $viimeistaan
              and r_opiskeluoikeus_aikajakso.loppu >= $aikaisintaan
              and r_opiskeluoikeus_aikajakso.tila in ('lasna', 'valiaikaisestikeskeytynyt', 'valmistunut')
      ) haetun_organisaation_opiskeluoikeudet
        join paallekkaiset_opiskeluoikeudet on paallekkaiset_opiskeluoikeudet.opiskeluoikeus_oid = haetun_organisaation_opiskeluoikeudet.opiskeluoikeus_oid
        left join lateral (
          select
            opiskeluoikeus_oid,
            string_agg(coalesce(opintojen_rahoitus, '-'), ',' order by alku) koodiarvot
          from r_opiskeluoikeus_aikajakso aikajakso
            where opiskeluoikeus_oid = haetun_organisaation_opiskeluoikeudet.opiskeluoikeus_oid
            group by opiskeluoikeus_oid
        ) rahoitusmuodot on haetun_organisaation_opiskeluoikeudet.opiskeluoikeus_oid = rahoitusmuodot.opiskeluoikeus_oid
        left join lateral (
          select
            opiskeluoikeus_oid,
            string_agg(coalesce(opintojen_rahoitus, '-'), ',' order by alku) koodiarvot
          from r_opiskeluoikeus_aikajakso aikajakso
            where opiskeluoikeus_oid = haetun_organisaation_opiskeluoikeudet.opiskeluoikeus_oid
              and aikajakso.alku <= $viimeistaan
              and aikajakso.loppu >= $aikaisintaan
            group by opiskeluoikeus_oid
        ) rahoitusmuodot_osuu_parametreille on haetun_organisaation_opiskeluoikeudet.opiskeluoikeus_oid = rahoitusmuodot_osuu_parametreille.opiskeluoikeus_oid
        left join lateral (
          select
            opiskeluoikeus_oid,
            string_agg(coalesce(opintojen_rahoitus, '-'), ',' order by alku) koodiarvot
          from r_opiskeluoikeus_aikajakso aikajakso
            where opiskeluoikeus_oid = paallekkainen_opiskeluoikeus_oid
            group by opiskeluoikeus_oid
        ) paallekkainen_rahoitusmuodot on paallekkainen_opiskeluoikeus_oid = paallekkainen_rahoitusmuodot.opiskeluoikeus_oid
        left join lateral (
          select
            opiskeluoikeus_oid,
            string_agg(coalesce(opintojen_rahoitus, '-'), ',' order by alku) koodiarvot
          from r_opiskeluoikeus_aikajakso aikajakso
            where opiskeluoikeus_oid = paallekkainen_opiskeluoikeus_oid
              and aikajakso.alku <= $viimeistaan
              and aikajakso.loppu >= $aikaisintaan
            group by opiskeluoikeus_oid
        ) paallekkainen_rahoitusmuodot_osuu_parametreille on paallekkainen_opiskeluoikeus_oid = paallekkainen_rahoitusmuodot_osuu_parametreille.opiskeluoikeus_oid
        join lateral (
          select
            opiskeluoikeus_oid,
            string_agg(tila, ',' order by alku) tilat
          from r_opiskeluoikeus_aikajakso oman_organisaation_aikajakso
            where opiskeluoikeus_oid = haetun_organisaation_opiskeluoikeudet.opiskeluoikeus_oid
              and oman_organisaation_aikajakso.alku <= $viimeistaan
              and oman_organisaation_aikajakso.loppu >= $aikaisintaan
            group by opiskeluoikeus_oid
        ) haetun_opiskeluoikeuden_tilat_parametrien_sisalla on haetun_organisaation_opiskeluoikeudet.opiskeluoikeus_oid = haetun_opiskeluoikeuden_tilat_parametrien_sisalla.opiskeluoikeus_oid
        left join lateral (
          select
            opiskeluoikeus_oid,
            string_agg(tila, ',' order by alku) tilat
          from r_opiskeluoikeus_aikajakso oman_organisaation_aikajakso
            where opiskeluoikeus_oid = paallekkainen_opiskeluoikeus_oid
              and oman_organisaation_aikajakso.alku <= $viimeistaan
              and oman_organisaation_aikajakso.loppu >= $aikaisintaan
            group by opiskeluoikeus_oid
        ) paallekkaisen_opiskeluoikeuden_tilat_parametrien_sisalla on paallekkainen_opiskeluoikeus_oid = paallekkaisen_opiskeluoikeuden_tilat_parametrien_sisalla.opiskeluoikeus_oid
        join lateral (
          select
            opiskeluoikeus_oid,
            array_to_json(array_agg(array[suorituksen_tyyppi, koulutusmoduuli_koodiarvo])) tyyppi_ja_koodiarvo
          from r_paatason_suoritus
            where opiskeluoikeus_oid = paallekkainen_opiskeluoikeus_oid
            group by opiskeluoikeus_oid
        ) paatason_suoritukset on paallekkainen_opiskeluoikeus_oid = paatason_suoritukset.opiskeluoikeus_oid
      order by paallekkaiset_opiskeluoikeudet.oppilaitos_nimi
    """

  implicit private val getResult: GetResult[PaallekkaisetOpiskeluoikeudetRow] = GetResult(r => {
    val rs: ResultSet = r.rs
    PaallekkaisetOpiskeluoikeudetRow(
      oppijaOid = rs.getString("oppija_oid"),
      opiskeluoikeusOid = rs.getString("opiskeluoikeus_oid"),
      oppilaitosNimi = rs.getString("oppilaitos_nimi"),
      koulutusmuoto = rs.getString("koulutusmuoto"),
      alkamispaiva = rs.getDate("alkamispaiva").toLocalDate,
      tilatParametrienSisalla = removeConsecutiveDuplicates(rs.getString("tilat_parametrien_sisalla")),
      paattymispaiva = optional(rs.getDate("paattymispaiva")).map(_.toLocalDate),
      viimeisinTila = rs.getString("viimeisin_tila"),
      rahoitusmuodot = optional(rs.getString("rahoitusmuodot")).map(removeConsecutiveDuplicates),
      rahoitusmuodotParametrienSisalla = optional(rs.getString("rahoitusmuodot_osuu_parametreille")).map(removeConsecutiveDuplicates),
      paallekkainenOpiskeluoikeusOid = rs.getString("paallekkainen_opiskeluoikeus_oid"),
      paallekkainenOppilaitosNimi = rs.getString("paallekkainen_oppilaitos_nimi"),
      paallekkainenKoulutusmuoto = rs.getString("paallekkainen_koulutusmuoto"),
      paallekkainenSuoritusTyyppi = suorituksistaKaytettavaNimi(rs.getString("paallekkainen_paatason_suoritukset")),
      paallekkainenTilatParametrienSisalla = optional(rs.getString("paallekkainen_tilat_parametrien_sisalla")).map(removeConsecutiveDuplicates),
      paallekkainenViimeisinTila = rs.getString("paallekkainen_viimeisin_tila"),
      paallekkainenAlkamispaiva = rs.getDate("paallekkainen_alkamispaiva").toLocalDate,
      paallekkainenPaattymispaiva = optional(rs.getDate("paallekkainen_paattymispaiva")).map(_.toLocalDate),
      paallekkainenAlkanutEka = rs.getBoolean("paallekkainen_alkanut_eka"),
      paallekkainenRahoitusmuodot = optional(rs.getString("paallekkainen_rahoitusmuodot")).map(removeConsecutiveDuplicates),
      paallekkainenRahoitusmuodotParametrienSisalla = optional(rs.getString("paallekkainen_rahoitusmuodot_parametrien_sisalla")).map(removeConsecutiveDuplicates),
      paallekkainenVoimassaParametrienSisalla = rs.getBoolean("paallekkainen_voimassa_aikajaksolla")
    )
  })

  type SuorituksenTyyppi = String
  type KoulutusmoduuliKoodiarvo = String
  def suorituksistaKaytettavaNimi(jsonb: String): String = {
    val suoritukset: List[(SuorituksenTyyppi, KoulutusmoduuliKoodiarvo)] = JsonSerializer.parse[List[List[String]]](jsonb).map(x => (x(0), x(1)))
    val nimi = (suoritukset.head :: suoritukset).foldLeft[String](suoritukset.head._1) {
      case (_, ("aikuistenperusopetuksenoppimaara", _)) => "Aikuisten perusopetuksen oppimäärä"
      case (_, ("aikuistenperusopetuksenoppimaaranalkuvaihe", _)) => "Aikuisten perusopetuksen oppimäärä"
      case (_, ("perusopetuksenoppiaineenoppimaara", _)) => "Perusopetuksen aineopiskelija"
      case (_, ("ammatillinentutkintoosittainen", _)) => "Ammatillisen tutkinnon osa/osia"
      case (_, ("ammatillinentutkinto", _)) => "Ammatillisen tutkinnon suoritus"
      case ("Ammatillisen tutkinnon osa/osia", ("nayttotutkintoonvalmistavakoulutus", _)) => "Ammatillisen tutkinnon osa/osia"
      case ("Ammatillisen tutkinnon suoritus", ("nayttotutkintoonvalmistavakoulutus", _)) => "Ammatillisen tutkinnon suoritus"
      case (_, ("nayttotutkintoonvalmistavakoulutus", _)) => "Näyttötutkintoon valmistavan koulutuksen suoritus"
      case (_, ("telma", _)) => "TELMA-koulutuksen suoritus"
      case (_, ("valma", _)) => "VALMA-koulutuksen suoritus"
      case (_, ("muuammatillinenkoulutus", _)) => "Muun ammatillisen koulutuksen suoritus"
      case (_, ("tutkinnonosaapienemmistäkokonaisuuksistakoostuvasuoritus", _)) => "Tutkinnon osaa pienimmistä kokonaisuuksista koostuva suoritus"
      case (_, ("diatutkintovaihe", _ )) => "DIA-tutkinnon suoritus"
      case (_, ("diavalmistavavaihe", _)) => "DIA-tutkinnon suoritus"
      case (_, ("esiopetuksensuoritus", _)) => "Esiopetuksen suoritus"
      case (_, ("ibtutkinto", _)) => "IB-tutkinnon suoritus"
      case (_, ("preiboppimaara", _)) => "IB-tutkinnon suoritus"
      case (_, ("internationalschooldiplomavuosiluokka", _)) => "International school lukio"
      case (_, ("internationalschoolmypvuosiluokka", "10")) => "International school lukio"
      case ("International school lukio", ("internationalschoolmypvuosiluokka", _)) => "International school lukio"
      case (_, ("internationalschoolmypvuosiluokka", _)) => "International school perusopetus"
      case (_, ("lukionoppiaineenoppimaara", _)) => "Lukio aineopiskelija"
      case (_, ("lukionaineopinnot", _)) => "Lukio aineopiskelija"
      case (_, ("lukionoppimaara", _)) => "Lukion oppimäärä"
      case (_, ("luva", _)) => "Lukioon valmistavan koulutus (LUVA) suoritus"
      case (_, ("perusopetukseenvalmistavaopetus", _)) => "Perusopetukseen valmistava suoritus"
      case (_, ("perusopetuksenlisaopetus", _)) => "Perusopetuksen lisäopetus"
      case (_, ("nuortenperusopetuksenoppiaineenoppimaara", _)) => "Perusopetuksen aineopiskelija"
      case (_, ("perusopetuksenoppimaara", _)) => "Perusopetuksen oppimäärä"
      case (_, ("perusopetuksenvuosiluokka", _)) => "Perusopetuksen oppimäärä"
      case (acc, (_, _)) => acc
    }
    if (nimi.forall(_.isLower)) logger.error(s"Unhandled suorituksen tyyppi $nimi. Raportin voi ladata, mutta päällekkäisen opiskeluoikeuden suorituksen nimenä käytettiin suorituksen tyyppiä")
    nimi
  }

  private def removeConsecutiveDuplicates(str: String) =
    str.split(",").foldRight(List.empty[String])((current, result) => if (result.headOption.contains(current)) result else current :: result).mkString(",")

  private def optional[A](str: A): Option[A] = if (str == null) None else Some(str)

  val columnSettings = Columns.flattenGroupingColumns(Seq(
    "Koulutuksen järjestäjän oma opiskeluoikeus" -> GroupColumnsWithTitle(List(
      "oppijaOid" -> Column("Oppijanumero"),
      "opiskeluoikeusOid" -> Column("Oman opiskeluoikeuden oid"),
      "oppilaitosNimi" -> Column("Oppilaitoksen nimi"),
      "koulutusmuoto" -> Column("Oman opiskeluoikeuden koulutusmuoto"),
      "alkamispaiva" -> Column("Oman opiskeluoikeuden alkamispäivä"),
      "tilatParametrienSisalla" -> Column("Oman opiskeluoikeuden tilat parametrien sisällä"),
      "paattymispaiva" -> Column("Opiskeluoikeuden päättymispäivä"),
      "viimeisinTila" -> Column("Oman opiskeluoikeuden viimeisin tila"),
      "rahoitusmuodot" -> Column("Oman opiskeluoikeuden rahoitusmuodot", comment = Some("Sarakkeessa näytetään kaikki opiskeluoikeuden aikana käytetyt rahoitusmuodot, jos opiskeluoikeudelle on merkitty rahoitusmuotoja. Rahoitusmuotoja käytetään aikuisten perusopetuksen, lukiokoulutuksen ja ammatillisen koulutuksen opiskeluoikeuksissa. Rahoitusmuotojen koodiarvojen selitteet koodistossa: https://koski.opintopolku.fi/koski/dokumentaatio/koodisto/opintojenrahoitus/latest")),
      "rahoitusmuodotParametrienSisalla" -> Column("Oman opiskeluoikeuden rahoitusmuodot valitulla ajanjaksolla", comment = Some("Sarakkeessa näytetään opiskeluoikeudella raporttiin valitulla aikajaksolla käytetyt rahoitusmuodot, jos opiskeluoikeudelle on merkitty rahoitusmuotoja. Rahoitusmuotoja käytetään aikuisten perusopetuksen, lukiokoulutuksen ja ammatillisen koulutuksen opiskeluoikeuksissa. Rahoitusmuotojen koodiarvojen selitteet koodistossa: https://koski.opintopolku.fi/koski/dokumentaatio/koodisto/opintojenrahoitus/latest")),
    )),
    "Päällekkäinen opiskeluoikeus" -> GroupColumnsWithTitle(List(
      "paallekkainenOpiskeluoikeusOid" -> Column("Päällekkäisen opiskeluoikeuden oid"),
      "paallekkainenOppilaitosNimi" -> Column("Päällekkäisen opiskeluoikeuden oppilaitoksen nimi"),
      "paallekkainenKoulutusmuoto" -> Column("Päällekkäisen opiskeluoikeuden koulutusmuoto"),
      "paallekkainenSuoritusTyyppi" -> Column("Päällekkäisen opiskeluoikeuden suorituksen tyyppi"),
      "paallekkainenTilatParametrienSisalla" -> Column("Päällekkäisen opiskeluoikeuden tilat parametrien sisällä"),
      "paallekkainenViimeisinTila" -> Column("Päällekkäisen opiskeluoikeuden viimeisin tila"),
      "paallekkainenAlkamispaiva" -> Column("Päällekkäisen opiskeluoikeuden alkamispäivä"),
      "paallekkainenPaattymispaiva" -> Column("Päällekkäisen opiskeluoikeuden päättymispäivä"),
      "paallekkainenAlkanutEka" -> Column("Päällekkäinen opiskeluoikeus alkanut ensin", comment = Some("Sarakkeella arvo \"kyllä\" jos päällekkäisen opiskeluoikeuden alkamispäivämäärä on ennen organisaation oman opiskeluoikeuden alkamispävää.")),
      "paallekkainenRahoitusmuodot" -> Column("Päällekkäisen opiskeluoikeuden rahoitusmuodot", comment = Some("Sarakkeessa näytetään kaikki opiskeluoikeuden aikana käytetyt rahoitusmuodot, jos opiskeluoikeudelle on merkitty rahoitusmuotoja. Rahoitusmuotoja käytetään aikuisten perusopetuksen, lukiokoulutuksen ja ammatillisen koulutuksen opiskeluoikeuksissa. Rahoitusmuotojen koodiarvojen selitteet koodistossa: https://koski.opintopolku.fi/koski/dokumentaatio/koodisto/opintojenrahoitus/latest")),
      "paallekkainenRahoitusmuodotParametrienSisalla" -> Column("Päällekkäisen opiskeluoikeuden rahoitusmuodot valitulla ajanjaksolla", comment = Some("Sarakkeessa näytetään opiskeluoikeudella raporttiin valitulla aikajaksolla käytetyt rahoitusmuodot, jos opiskeluoikeudelle on merkitty rahoitusmuotoja. Rahoitusmuotoja käytetään aikuisten perusopetuksen, lukiokoulutuksen ja ammatillisen koulutuksen opiskeluoikeuksissa. Rahoitusmuotojen koodiarvojen selitteet koodistossa: https://koski.opintopolku.fi/koski/dokumentaatio/koodisto/opintojenrahoitus/latest")),
      "paallekkainenVoimassaParametrienSisalla" -> Column("Päällekkäinen opiskeluoikeus aktiviinen valitulla ajanjaksolla", comment = Some("Raportille tulostuvat kaikki oman opiskeluoikeuden kanssa päällekkäin sattuvat opiskeluoikeudet. Päällekkäisyys voi olla raportille valitun ajanjakson ulkopuolella. Sarakkeessa arvo \"kyllä\" jos päällekkäinen opiskeluoikeus on ollut aktiivinen raportille valitulla ajanjaksolla."))
    ))
  ))
}

case class PaallekkaisetOpiskeluoikeudetRow(
  oppijaOid: String,
  opiskeluoikeusOid: String,
  oppilaitosNimi: String,
  koulutusmuoto: String,
  alkamispaiva: LocalDate,
  tilatParametrienSisalla: String,
  paattymispaiva: Option[LocalDate],
  viimeisinTila: String,
  rahoitusmuodot: Option[String],
  rahoitusmuodotParametrienSisalla: Option[String],
  paallekkainenOpiskeluoikeusOid: String,
  paallekkainenOppilaitosNimi: String,
  paallekkainenKoulutusmuoto: String,
  paallekkainenSuoritusTyyppi: String,
  paallekkainenTilatParametrienSisalla: Option[String],
  paallekkainenViimeisinTila: String,
  paallekkainenAlkamispaiva: LocalDate,
  paallekkainenPaattymispaiva: Option[LocalDate],
  paallekkainenAlkanutEka: Boolean,
  paallekkainenRahoitusmuodot: Option[String],
  paallekkainenRahoitusmuodotParametrienSisalla: Option[String],
  paallekkainenVoimassaParametrienSisalla: Boolean
)
