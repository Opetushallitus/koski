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
        haetun_opiskeluoikeuden_tilat_parametrien_sisalla.tilat haetun_tilat_parametrien_sisalla,
        paatason_suoritukset.tyyppi_ja_koodiarvo paallekkainen_paatason_suoritukset,
        paallekkainen_alkamispaiva <= $viimeistaan and paallekkainen_paattymispaiva >= $aikaisintaan paallekkainen_voimassa_aikajaksolla
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
            string_agg(opintojen_rahoitus, ',' order by alku) koodiarvot
          from r_opiskeluoikeus_aikajakso aikajakso
            where opiskeluoikeus_oid = haetun_organisaation_opiskeluoikeudet.opiskeluoikeus_oid
            group by opiskeluoikeus_oid
        ) rahoitusmuodot on haetun_organisaation_opiskeluoikeudet.opiskeluoikeus_oid = rahoitusmuodot.opiskeluoikeus_oid
        left join lateral (
          select
            opiskeluoikeus_oid,
            string_agg(opintojen_rahoitus, ',' order by alku) koodiarvot
          from r_opiskeluoikeus_aikajakso aikajakso
            where opiskeluoikeus_oid = haetun_organisaation_opiskeluoikeudet.opiskeluoikeus_oid
              and aikajakso.alku <= $viimeistaan
              and aikajakso.loppu >= $aikaisintaan
            group by opiskeluoikeus_oid
        ) rahoitusmuodot_osuu_parametreille on haetun_organisaation_opiskeluoikeudet.opiskeluoikeus_oid = rahoitusmuodot_osuu_parametreille.opiskeluoikeus_oid
        left join lateral (
          select
            opiskeluoikeus_oid,
            string_agg(opintojen_rahoitus, ',' order by alku) koodiarvot
          from r_opiskeluoikeus_aikajakso aikajakso
            where opiskeluoikeus_oid = paallekkainen_opiskeluoikeus_oid
            group by opiskeluoikeus_oid
        ) paallekkainen_rahoitusmuodot on paallekkainen_opiskeluoikeus_oid = paallekkainen_rahoitusmuodot.opiskeluoikeus_oid
        left join lateral (
          select
            opiskeluoikeus_oid,
            string_agg(opintojen_rahoitus, ',' order by alku) koodiarvot
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
      viimeisinTila = rs.getString("viimeisin_tila"),
      rahoitusmuodot = optional(rs.getString("rahoitusmuodot")).map(removeConsecutiveDuplicates),
      rahoitusmuodotParametrienSisalla = optional(rs.getString("rahoitusmuodot_osuu_parametreille")).map(removeConsecutiveDuplicates),
      paallekkainenOpiskeluoikeusOid = rs.getString("paallekkainen_opiskeluoikeus_oid"),
      paallekkainenOppilaitosNimi = rs.getString("paallekkainen_oppilaitos_nimi"),
      paallekkainenKoulutusmuoto = rs.getString("paallekkainen_koulutusmuoto"),
      paallekkainenSuoritusTyyppi = suorituksistaKaytettavaNimi(rs.getString("paallekkainen_paatason_suoritukset")),
      paallekkainenViimeisinTila = rs.getString("paallekkainen_viimeisin_tila"),
      paallekkainenAlkamispaiva = rs.getDate("paallekkainen_alkamispaiva").toLocalDate,
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
      case (_, ("aikuistenperusopetuksenoppimaara", _)) => "Perusopetuksen oppimäärä"
      case (_, ("aikuistenperusopetuksenoppimaaranalkuvaihe", _)) => "Perusopetuksen oppimäärä"
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

  private def optional(str: String) = if (str == null) None else Some(str)

  val columnSettings = Seq(
    "oppijaOid" -> Column("oppijaOid", comment = Some("")),
    "opiskeluoikeusOid" -> Column("opiskeluoikeusOid", comment = Some("")),
    "oppilaitosNimi" -> Column("Oppilaitoksen nimi"),
    "koulutusmuoto" -> Column("Koulutusmuoto"),
    "alkamispaiva" -> Column("alkamispaiva", comment = Some("")),
    "viimeisinTila" -> Column("viimeisinTila", comment = Some("")),
    "rahoitusmuodot" -> Column("rahoitusmuodot"),
    "rahoitusmuodotParametrienSisalla" -> Column("rahoitusmuodotParametrienSisalla"),
    "paallekkainenOpiskeluoikeusOid" -> Column("paallekkainenOpiskeluoikeusOid", comment = Some("")),
    "paallekkainenOppilaitosNimi" -> Column("paallekkainenOppilaitosNimi", comment = Some("")),
    "paallekkainenKoulutusmuoto" -> Column("paallekkainenKoulutusmuoto", comment = Some("")),
    "paallekkainenSuoritusTyyppi" -> Column("paallekkainenSuoritusTyyppi", comment = Some("")),
    "paallekkainenViimeisinTila" -> Column("paallekkainenViimeisinTila", comment = Some("")),
    "paallekkainenAlkamispaiva" -> Column("paallekkainenAlkamispaiva", comment = Some("")),
    "paallekkainenAlkanutEka" -> Column("paallekkainenAlkanutEka", comment = Some("")),
    "paallekkainenRahoitusmuodot" -> Column("paallekkainenRahoitusmuodot", comment = Some("")),
    "paallekkainenRahoitusmuodotParametrienSisalla" -> Column("paallekkainenRahoitusmuodotParametrienSisalla", comment = Some("")),
    "paallekkainenVoimassaParametrienSisalla" -> Column("paallekkainenVoimassaParametrienSisalla")
  )
}

case class PaallekkaisetOpiskeluoikeudetRow(
  oppijaOid: String,
  opiskeluoikeusOid: String,
  oppilaitosNimi: String,
  koulutusmuoto: String,
  alkamispaiva: LocalDate,
  viimeisinTila: String,
  rahoitusmuodot: Option[String],
  rahoitusmuodotParametrienSisalla: Option[String],
  paallekkainenOpiskeluoikeusOid: String,
  paallekkainenOppilaitosNimi: String,
  paallekkainenKoulutusmuoto: String,
  paallekkainenSuoritusTyyppi: String,
  paallekkainenViimeisinTila: String,
  paallekkainenAlkamispaiva: LocalDate,
  paallekkainenAlkanutEka: Boolean,
  paallekkainenRahoitusmuodot: Option[String],
  paallekkainenRahoitusmuodotParametrienSisalla: Option[String],
  paallekkainenVoimassaParametrienSisalla: Boolean
)
