package fi.oph.koski.raportit

import java.sql.{Date, ResultSet}
import java.time.LocalDate
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.log.Logging
import fi.oph.koski.raportointikanta.{RaportointiDatabase, Schema}
import fi.oph.koski.schema.InternationalSchoolOpiskeluoikeus
import slick.jdbc.GetResult

import scala.concurrent.duration.DurationInt

object PaallekkaisetOpiskeluoikeudet extends Logging {

  def datasheet(
     koulutustoimijaOid: String,
    oids: Seq[String],
    aikaisintaan: LocalDate,
    viimeistaan: LocalDate,
    db: RaportointiDatabase
  )(implicit t: LocalizationReader) =
    DataSheet(
      title = t.get("raportti-excel-paallekkaiset-opiskeluoikeudet-sheet-name"),
      rows = db
        .runDbSync(
          query(koulutustoimijaOid, oids, Date.valueOf(aikaisintaan), Date.valueOf(viimeistaan))
            .as[PaallekkaisetOpiskeluoikeudetRow], timeout = 5.minutes
        ),
      columnSettings(t)
    )

  def createPrecomputedTable(s: Schema) =
    sqlu"""
      create table #${s.name}.paallekkaiset_opiskeluoikeudet as
        select distinct
          opiskeluoikeus.oppija_oid,
          henkilo.sukunimi,
          henkilo.etunimet,
          opiskeluoikeus.opiskeluoikeus_oid,
          opiskeluoikeus.oppilaitos_nimi,
          opiskeluoikeus.oppilaitos_nimi_sv,
          opiskeluoikeus.koulutusmuoto,
          opiskeluoikeus.alkamispaiva,
          opiskeluoikeus.viimeisin_tila,
          opiskeluoikeus.paattymispaiva,
          opiskeluoikeus_diaarit.diaarit_agg    opiskeluoikeus_diaarit,
          paallekkainen.opiskeluoikeus_oid      paallekkainen_opiskeluoikeus_oid,
          paallekkainen.koulutustoimija_nimi    paallekkainen_koulutustoimija_nimi,
          paallekkainen.koulutustoimija_nimi_sv paallekkainen_koulutustoimija_nimi_sv,
          paallekkainen.oppilaitos_oid          paallekkainen_oppilaitos_oid,
          paallekkainen.oppilaitos_nimi         paallekkainen_oppilaitos_nimi,
          paallekkainen.oppilaitos_nimi_sv      paallekkainen_oppilaitos_nimi_sv,
          paallekkainen.koulutusmuoto           paallekkainen_koulutusmuoto,
          paallekkainen.viimeisin_tila          paallekkainen_viimeisin_tila,
          paallekkainen.alkamispaiva            paallekkainen_alkamispaiva,
          paallekkainen.paattymispaiva          paallekkainen_paattymispaiva
      from #${s.name}.r_opiskeluoikeus opiskeluoikeus
        join lateral (
          select *
          from #${s.name}.r_opiskeluoikeus paallekkainen
          where paallekkainen.oppija_oid = opiskeluoikeus.oppija_oid
            and not paallekkainen.opiskeluoikeus_oid = opiskeluoikeus.opiskeluoikeus_oid
            and paallekkainen.sisaltyy_opiskeluoikeuteen_oid is null
            and coalesce(paallekkainen.paattymispaiva, '9999-12-31'::date) >= opiskeluoikeus.alkamispaiva
            and paallekkainen.alkamispaiva <= coalesce(opiskeluoikeus.paattymispaiva, '9999-12-31'::date)
        ) paallekkainen on paallekkainen.oppija_oid = opiskeluoikeus.oppija_oid
        join lateral (
          select suorituksen_tyyppi, opiskeluoikeus_oid
          from #${s.name}.r_paatason_suoritus suoritus
          where suoritus.opiskeluoikeus_oid = opiskeluoikeus.opiskeluoikeus_oid
        ) suoritus on suoritus.opiskeluoikeus_oid = opiskeluoikeus.opiskeluoikeus_oid
        join lateral (
          select suorituksen_tyyppi, opiskeluoikeus_oid
          from #${s.name}.r_paatason_suoritus paallekkainensuoritus
          where paallekkainensuoritus.opiskeluoikeus_oid = paallekkainen.opiskeluoikeus_oid
        ) paallekkainensuoritus on paallekkainensuoritus.opiskeluoikeus_oid = paallekkainen.opiskeluoikeus_oid
        join lateral (
          select opiskeluoikeus_oid, string_agg(perusteen_diaarinumero, ',') diaarit_agg
          from (
            select distinct
              opiskeluoikeus_oid,
              coalesce(
                data -> 'koulutusmoduuli' ->> 'perusteenDiaarinumero',
                data -> 'tutkinto' ->> 'perusteenDiaarinumero',
                data -> 'täydentääTutkintoa' ->> 'perusteenDiaarinumero',
                '-'
              ) as perusteen_diaarinumero
            from #${s.name}.r_paatason_suoritus
            where opiskeluoikeus_oid = opiskeluoikeus.opiskeluoikeus_oid
            order by perusteen_diaarinumero asc
          ) as diaarilista
          group by opiskeluoikeus_oid
        ) opiskeluoikeus_diaarit on opiskeluoikeus_diaarit.opiskeluoikeus_oid = opiskeluoikeus.opiskeluoikeus_oid
        join lateral (
          select henkilo.oppija_oid, henkilo.sukunimi, henkilo.etunimet
          from #${s.name}.r_henkilo henkilo
          where henkilo.oppija_oid = opiskeluoikeus.oppija_oid
        ) henkilo on henkilo.oppija_oid = opiskeluoikeus.oppija_oid
        where opiskeluoikeus.sisaltyy_opiskeluoikeuteen_oid is null
          and not suoritus.suorituksen_tyyppi = 'vstvapaatavoitteinenkoulutus'
          and not paallekkainensuoritus.suorituksen_tyyppi = 'vstvapaatavoitteinenkoulutus'
          and not opiskeluoikeus.koulutusmuoto = 'taiteenperusopetus'
          and not paallekkainen.koulutusmuoto = 'taiteenperusopetus'
    """

  def createIndex(s: Schema) =
    sqlu"create index on #${s.name}.paallekkaiset_opiskeluoikeudet(opiskeluoikeus_oid)"

  private def query(koulutustoimijaOid: String, oppilaitosOids: Seq[String], aikaisintaan: Date, viimeistaan: Date) =
    sql"""
      select
        paallekkaiset_opiskeluoikeudet.*,
        coalesce(rahoitusmuodot.koodiarvot, '-') rahoitusmuodot,
        coalesce(rahoitusmuodot_osuu_parametreille.koodiarvot, '-') rahoitusmuodot_osuu_parametreille,
        paallekkaiset_opiskeluoikeudet.paallekkainen_alkamispaiva = paallekkaiset_opiskeluoikeudet.alkamispaiva sama_alkupaiva,
        paallekkaiset_opiskeluoikeudet.paallekkainen_alkamispaiva < paallekkaiset_opiskeluoikeudet.alkamispaiva paallekkainen_alkanut_eka,
        coalesce(paallekkainen_rahoitusmuodot.koodiarvot, '-') paallekkainen_rahoitusmuodot,
        coalesce(paallekkainen_rahoitusmuodot_osuu_parametreille.koodiarvot, '-') paallekkainen_rahoitusmuodot_parametrien_sisalla,
        coalesce(paallekkaisen_opiskeluoikeuden_tilat_parametrien_sisalla.tilat, '-') paallekkainen_tilat_parametrien_sisalla,
        haetun_opiskeluoikeuden_tilat_parametrien_sisalla.tilat tilat_parametrien_sisalla,
        paatason_suoritukset.tyyppi_ja_koodiarvo paatason_suoritukset,
        paallekkainen_paatason_suoritukset.tyyppi_ja_koodiarvo paallekkainen_paatason_suoritukset,
        paallekkainen_alkamispaiva <= $viimeistaan and coalesce(paallekkainen_paattymispaiva, '9999-12-31'::date) >= $aikaisintaan paallekkainen_voimassa_aikajaksolla
      from (
        select
          distinct r_opiskeluoikeus.opiskeluoikeus_oid
        from r_opiskeluoikeus
          left join r_opiskeluoikeus_aikajakso on r_opiskeluoikeus_aikajakso.opiskeluoikeus_oid = r_opiskeluoikeus.opiskeluoikeus_oid
          left join esiopetus_opiskeluoik_aikajakso on esiopetus_opiskeluoik_aikajakso.opiskeluoikeus_oid = r_opiskeluoikeus.opiskeluoikeus_oid
        where (oppilaitos_oid = any($oppilaitosOids) or (r_opiskeluoikeus.koulutustoimija_oid = $koulutustoimijaOid and r_opiskeluoikeus.koulutusmuoto = 'esiopetus' and r_opiskeluoikeus.data -> 'järjestämismuoto' ->> 'koodiarvo' IN('JM02', 'JM03')))
          and (
            ( r_opiskeluoikeus_aikajakso.opiskeluoikeus_oid is not null
              and r_opiskeluoikeus_aikajakso.alku <= $viimeistaan
              and r_opiskeluoikeus_aikajakso.loppu >= $aikaisintaan
              and r_opiskeluoikeus_aikajakso.tila in ('lasna', 'valiaikaisestikeskeytynyt', 'valmistunut')
            )
            or
            ( esiopetus_opiskeluoik_aikajakso.opiskeluoikeus_oid is not null
              and esiopetus_opiskeluoik_aikajakso.alku <= $viimeistaan
              and esiopetus_opiskeluoik_aikajakso.loppu >= $aikaisintaan
              and esiopetus_opiskeluoik_aikajakso.tila in ('lasna', 'valiaikaisestikeskeytynyt', 'valmistunut')
            )
          )
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
          union
          select
            opiskeluoikeus_oid,
            string_agg(tila, ',' order by alku) tilat
          from esiopetus_opiskeluoik_aikajakso oman_organisaation_aikajakso
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
          union
          select
            opiskeluoikeus_oid,
            string_agg(tila, ',' order by alku) tilat
          from esiopetus_opiskeluoik_aikajakso oman_organisaation_aikajakso
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
            where opiskeluoikeus_oid = paallekkaiset_opiskeluoikeudet.opiskeluoikeus_oid
            group by opiskeluoikeus_oid
        ) paatason_suoritukset on paallekkaiset_opiskeluoikeudet.opiskeluoikeus_oid = paatason_suoritukset.opiskeluoikeus_oid
        join lateral (
          select
            opiskeluoikeus_oid,
            array_to_json(array_agg(array[suorituksen_tyyppi, koulutusmoduuli_koodiarvo])) tyyppi_ja_koodiarvo
          from r_paatason_suoritus
            where opiskeluoikeus_oid = paallekkainen_opiskeluoikeus_oid
            group by opiskeluoikeus_oid
        ) paallekkainen_paatason_suoritukset on paallekkainen_opiskeluoikeus_oid = paallekkainen_paatason_suoritukset.opiskeluoikeus_oid
      order by paallekkaiset_opiskeluoikeudet.oppilaitos_nimi
    """

  implicit private def getResult(implicit t: LocalizationReader): GetResult[PaallekkaisetOpiskeluoikeudetRow] = GetResult(
    r => {
      val rs: ResultSet = r.rs
      PaallekkaisetOpiskeluoikeudetRow(
        oppijaOid = rs.getString("oppija_oid"),
        oppijaSukunimi = Option(rs.getString("sukunimi")),
        oppijaEtunimet = Option(rs.getString("etunimet")),
        opiskeluoikeusOid = rs.getString("opiskeluoikeus_oid"),
        oppilaitosNimi = rs.getString(if(t.language == "sv") "oppilaitos_nimi_sv" else "oppilaitos_nimi"),
        koulutusmuoto = rs.getString("koulutusmuoto"),
        suoritusTyyppi = suorituksistaKaytettavaNimi(rs.getString("paatason_suoritukset"), t),
        perusteenDiaarinumero = Option(rs.getString("opiskeluoikeus_diaarit")),
        alkamispaiva = r.getLocalDate("alkamispaiva"),
        tilatParametrienSisalla = removeConsecutiveDuplicates(rs.getString("tilat_parametrien_sisalla")),
        paattymispaiva = Option(r.getLocalDate("paattymispaiva")),
        viimeisinTila = rs.getString("viimeisin_tila"),
        rahoitusmuodot = Option(rs.getString("rahoitusmuodot")).map(removeConsecutiveDuplicates),
        rahoitusmuodotParametrienSisalla = Option(rs.getString("rahoitusmuodot_osuu_parametreille"))
          .map(removeConsecutiveDuplicates),
        paallekkainenOpiskeluoikeusOid = rs.getString("paallekkainen_opiskeluoikeus_oid"),
        paallekkainenKoulutustoimijaNimi = rs.getString(if(t.language == "sv") "paallekkainen_koulutustoimija_nimi_sv" else "paallekkainen_koulutustoimija_nimi"),
        paallekkainenOppilaitosOid = rs.getString("paallekkainen_oppilaitos_oid"),
        paallekkainenOppilaitosNimi = rs.getString(if(t.language == "sv") "paallekkainen_oppilaitos_nimi_sv" else "paallekkainen_oppilaitos_nimi"),
        paallekkainenKoulutusmuoto = rs.getString("paallekkainen_koulutusmuoto"),
        paallekkainenSuoritusTyyppi = suorituksistaKaytettavaNimi(rs.getString("paallekkainen_paatason_suoritukset"), t),
        paallekkainenTilatParametrienSisalla = Option(rs.getString("paallekkainen_tilat_parametrien_sisalla"))
          .map(removeConsecutiveDuplicates),
        paallekkainenViimeisinTila = rs.getString("paallekkainen_viimeisin_tila"),
        paallekkainenAlkamispaiva = r.getLocalDate("paallekkainen_alkamispaiva"),
        paallekkainenPaattymispaiva = Option(r.getLocalDate("paallekkainen_paattymispaiva")),
        paallekkainenAlkanutEka = if (rs.getBoolean("sama_alkupaiva")) {
          t.get("raportti-excel-default-value-sama-alkamispäivä")
        } else {
          if (rs.getBoolean("paallekkainen_alkanut_eka")){ t.get("excel-export-default-value-kyllä") }
          else { t.get("excel-export-default-value-ei") }
        },
        paallekkainenRahoitusmuodot = Option(rs.getString("paallekkainen_rahoitusmuodot"))
          .map(removeConsecutiveDuplicates),
        paallekkainenRahoitusmuodotParametrienSisalla = Option(rs
          .getString("paallekkainen_rahoitusmuodot_parametrien_sisalla")
        ).map(removeConsecutiveDuplicates),
        paallekkainenVoimassaParametrienSisalla = rs.getBoolean("paallekkainen_voimassa_aikajaksolla")
      )
    }
  )

  type SuorituksenTyyppi = String
  type KoulutusmoduuliKoodiarvo = String
  def suorituksistaKaytettavaNimi(jsonb: String, t: LocalizationReader): String = {
    val suoritukset: List[(SuorituksenTyyppi, KoulutusmoduuliKoodiarvo)] = JsonSerializer.parse[List[List[String]]](jsonb).map(x => (x(0), x(1)))
    val nimi = suoritukset.foldLeft[String](suoritukset.head._1) {
      case (_, ("aikuistenperusopetuksenoppimaara", _)) => t.get("raportti-excel-default-value-aikuistenperusopetuksenoppimaara")
      case (_, ("aikuistenperusopetuksenoppimaaranalkuvaihe", _)) => t.get("raportti-excel-default-value-aikuistenperusopetuksenoppimaara")
      case (_, ("perusopetuksenoppiaineenoppimaara", _)) => t.get("raportti-excel-default-value-perusopetuksenaineopiskelija")
      case (_, ("ammatillinentutkintoosittainen", _)) => t.get("raportti-excel-default-value-ammatillisenosia")
      case (_, ("ammatillinentutkinto", _)) => t.get("raportti-excel-default-value-ammatillisensuoritus")
      case (acc, ("nayttotutkintoonvalmistavakoulutus", _)) if acc == t.get("raportti-excel-default-value-ammatillisenosia") => acc
      case (acc, ("nayttotutkintoonvalmistavakoulutus", _)) if acc == t.get("raportti-excel-default-value-ammatillisensuoritus") => acc
      case (_, ("nayttotutkintoonvalmistavakoulutus", _)) => t.get("raportti-excel-default-value-näyttötutkintoonvalmistavansuoritus")
      case (_, ("telma", _)) => t.get("raportti-excel-default-value-telmasuoritus")
      case (_, ("valma", _)) => t.get("raportti-excel-default-value-valmasuoritus")
      case (_, ("muuammatillinenkoulutus", _)) => t.get("raportti-excel-default-value-muuammatillinen")
      case (_, ("tutkinnonosaapienemmistäkokonaisuuksistakoostuvasuoritus", _)) => t.get("raportti-excel-default-value-tutkinnonosaapienemmistä")
      case (_, ("diatutkintovaihe", _ )) => t.get("raportti-excel-default-value-diasuoritus")
      case (_, ("diavalmistavavaihe", _)) => t.get("raportti-excel-default-value-diasuoritus")
      case (_, ("esiopetuksensuoritus", _)) => t.get("raportti-excel-default-value-esiopetus")
      case (_, ("ibtutkinto", _)) => t.get("raportti-excel-default-value-ibsuoritus")
      case (_, ("preiboppimaara", _)) => t.get("raportti-excel-default-value-ibsuoritus")
      case (_, (suorituksenTyyppi, koodiarvo))
        if InternationalSchoolOpiskeluoikeus.onLukiotaVastaavaInternationalSchoolinSuoritus(suorituksenTyyppi, koodiarvo) => t.get("raportti-excel-default-value-intschoollukio")
      case (acc, (_, _)) if acc == t.get("raportti-excel-default-value-intschoollukio") => acc
      case (_, (suorituksenTyyppi, koodiarvo))
        if InternationalSchoolOpiskeluoikeus.onPeruskouluaVastaavaInternationalSchoolinSuoritus(suorituksenTyyppi, koodiarvo) => t.get("raportti-excel-default-value-intschoolperusopetus")
      case (_, ("europeanschoolofhelsinkivuosiluokkanursery", _)) => t.get("raportti-excel-default-value-eshesiopetus")
      case (_, ("europeanschoolofhelsinkivuosiluokkaprimary", _)) => t.get("raportti-excel-default-value-eshperusopetus")
      case (_, ("europeanschoolofhelsinkivuosiluokkasecondarylower", _)) => t.get("raportti-excel-default-value-eshperusopetus")
      case (_, ("europeanschoolofhelsinkivuosiluokkasecondaryupper", _)) => t.get("raportti-excel-default-value-eshlukio")
      case (_, ("ebtutkinto", _)) => t.get("raportti-excel-default-value-ebtutkinto")
      case (_, ("lukionoppiaineenoppimaara", _)) => t.get("raportti-excel-default-value-lukionaineopiskelija")
      case (_, ("lukionaineopinnot", _)) => t.get("raportti-excel-default-value-lukionaineopiskelija")
      case (_, ("lukionoppimaara", _)) => t.get("raportti-excel-default-value-lukionoppimäärä")
      case (_, ("luva", _)) => t.get("raportti-excel-default-value-luva")
      case (_, ("perusopetukseenvalmistavaopetus", _)) => t.get("raportti-excel-default-value-perusopetukseenvalmistava")
      case (_, ("perusopetuksenlisaopetus", _)) => t.get("raportti-excel-default-value-perusopetuksenlisäopetus")
      case (_, ("nuortenperusopetuksenoppiaineenoppimaara", _)) => t.get("raportti-excel-default-value-perusopetuksenaineopiskelija")
      case (_, ("perusopetuksenoppimaara", _)) => t.get("raportti-excel-default-value-perusopetuksenoppimäärä")
      case (_, ("perusopetuksenvuosiluokka", _)) => t.get("raportti-excel-default-value-perusopetuksenoppimäärä")
      case (_, ("vstoppivelvollisillesuunnattukoulutus", _)) => t.get("raportti-excel-default-value-vst")
      case (_, ("vstvapaatavoitteinenkoulutus", _)) => t.get("raportti-excel-default-value-vst-vapaatavoitteinen")
      case (_, ("vstjotpakoulutus", _)) => t.get("raportti-excel-default-value-vst-jotpa")
      case (_, ("vstmaahanmuuttajienkotoutumiskoulutus", _)) => t.get("raportti-excel-default-value-vst-maahanmuuttajienkotoutumiskoulutus")
      case (_, ("tuvakoulutuksensuoritus", _)) => t.get("raportti-excel-default-value-tuva")
      case (_, ("muukuinsaanneltykoulutus", _)) => t.get("raportti-excel-default-value-muks")
      case (acc, (_, _)) => acc
    }
    if (nimi.forall(_.isLower)) logger.error(s"Unhandled suorituksen tyyppi $nimi. Raportin voi ladata, mutta päällekkäisen opiskeluoikeuden suorituksen nimenä käytettiin suorituksen tyyppiä")
    nimi
  }

  private def removeConsecutiveDuplicates(str: String) =
    str.split(",").foldRight(List.empty[String])((current, result) => if (result.headOption.contains(current)) result else current :: result).mkString(",")

  def columnSettings(t: LocalizationReader) = Columns.flattenGroupingColumns(Seq(
    t.get("raportti-excel-kolumni-koulutuksenJärjestäjänOpiskeluoikeus") -> GroupColumnsWithTitle(List(
      "oppijaOid" -> Column(t.get("raportti-excel-kolumni-oppijaOid")),
      "oppijaSukunimi" -> Column(t.get("raportti-excel-kolumni-sukunimi")),
      "oppijaEtunimet" -> Column(t.get("raportti-excel-kolumni-etunimet")),
      "opiskeluoikeusOid" -> Column(t.get("raportti-excel-kolumni-opiskeluoikeusOid")),
      "oppilaitosNimi" -> Column(t.get("raportti-excel-kolumni-oppilaitoksenNimi")),
      "koulutusmuoto" -> Column(t.get("raportti-excel-kolumni-koulutusmuoto")),
      "suoritusTyyppi" -> Column(t.get("raportti-excel-kolumni-suorituksenTyyppi")),
      "perusteenDiaarinumero" -> Column(t.get("raportti-excel-kolumni-perusteenDiaarinumero")),
      "alkamispaiva" -> Column(t.get("raportti-excel-kolumni-opiskeluoikeudenAlkamispäivä")),
      "paattymispaiva" -> Column(t.get("raportti-excel-kolumni-opiskeluoikeudenPäättymispäivä")),
      "tilatParametrienSisalla" -> Column(t.get("raportti-excel-kolumni-tilatParametrienSisalla"), comment = Some(t.get("raportti-excel-kolumni-tilatParametrienSisalla-comment"))),
      "viimeisinTila" -> Column(t.get("raportti-excel-kolumni-viimeisinTila"), comment = Some(t.get("raportti-excel-kolumni-viimeisinTila-comment"))),
      "rahoitusmuodot" -> Column(t.get("raportti-excel-kolumni-oo-rahoitusmuodot"), comment = Some(t.get("raportti-excel-kolumni-oo-rahoitusmuodot-comment"))),
      "rahoitusmuodotParametrienSisalla" -> Column(t.get("raportti-excel-kolumni-rahoitusmuodotParametrienSisalla"), comment = Some(t.get("raportti-excel-kolumni-rahoitusmuodotParametrienSisalla-comment"))),
    )),
    t.get("raportti-excel-kolumni-päällekkäinenOpiskeluoikeus") -> GroupColumnsWithTitle(List(
      "paallekkainenOpiskeluoikeusOid" -> Column(t.get("raportti-excel-kolumni-paallekkainenOpiskeluoikeusOid")),
      "paallekkainenKoulutustoimijaNimi" -> Column(t.get("raportti-excel-kolumni-paallekkainenKoulutustoimijaNimi")),
      "paallekkainenOppilaitosOid" -> Column(t.get("raportti-excel-kolumni-paallekkainenOppilaitosOid")),
      "paallekkainenOppilaitosNimi" -> Column(t.get("raportti-excel-kolumni-paallekkainenOppilaitosNimi")),
      "paallekkainenKoulutusmuoto" -> Column(t.get("raportti-excel-kolumni-paallekkainenKoulutusmuoto")),
      "paallekkainenSuoritusTyyppi" -> Column(t.get("raportti-excel-kolumni-paallekkainenSuoritusTyyppi"), comment = Some(t.get("raportti-excel-kolumni-paallekkainenSuoritusTyyppi-comment"))),
      "paallekkainenViimeisinTila" -> Column(t.get("raportti-excel-kolumni-paallekkainenViimeisinTila"), comment = Some(t.get("raportti-excel-kolumni-paallekkainenViimeisinTila-comment"))),
      "paallekkainenAlkamispaiva" -> Column(t.get("raportti-excel-kolumni-paallekkainenAlkamispaiva")),
      "paallekkainenPaattymispaiva" -> Column(t.get("raportti-excel-kolumni-paallekkainenPaattymispaiva")),
      "paallekkainenTilatParametrienSisalla" -> Column(t.get("raportti-excel-kolumni-paallekkainenTilatParametrienSisalla"), comment = Some(t.get("raportti-excel-kolumni-paallekkainenTilatParametrienSisalla-comment"))),
      "paallekkainenAlkanutEka" -> Column(t.get("raportti-excel-kolumni-paallekkainenAlkanutEka"), comment = Some(t.get("raportti-excel-kolumni-paallekkainenAlkanutEka-comment"))),
      "paallekkainenRahoitusmuodot" -> Column(t.get("raportti-excel-kolumni-paallekkainenRahoitusmuodot"), comment = Some(t.get("raportti-excel-kolumni-paallekkainenRahoitusmuodot-comment"))),
      "paallekkainenRahoitusmuodotParametrienSisalla" -> Column(t.get("raportti-excel-kolumni-paallekkainenRahoitusmuodotParametrienSisalla"), comment = Some(t.get("raportti-excel-kolumni-paallekkainenRahoitusmuodotParametrienSisalla-comment"))),
      "paallekkainenVoimassaParametrienSisalla" -> Column(t.get("raportti-excel-kolumni-paallekkainenVoimassaParametrienSisalla"), comment = Some(t.get("raportti-excel-kolumni-paallekkainenVoimassaParametrienSisalla-comment")))
    ))
  ))
}

case class PaallekkaisetOpiskeluoikeudetRow(
  oppijaOid: String,
  oppijaSukunimi: Option[String],
  oppijaEtunimet: Option[String],
  opiskeluoikeusOid: String,
  oppilaitosNimi: String,
  koulutusmuoto: String,
  suoritusTyyppi: String,
  perusteenDiaarinumero: Option[String],
  alkamispaiva: LocalDate,
  paattymispaiva: Option[LocalDate],
  tilatParametrienSisalla: String,
  viimeisinTila: String,
  rahoitusmuodot: Option[String],
  rahoitusmuodotParametrienSisalla: Option[String],
  paallekkainenOpiskeluoikeusOid: String,
  paallekkainenKoulutustoimijaNimi: String,
  paallekkainenOppilaitosOid: String,
  paallekkainenOppilaitosNimi: String,
  paallekkainenKoulutusmuoto: String,
  paallekkainenSuoritusTyyppi: String,
  paallekkainenViimeisinTila: String,
  paallekkainenAlkamispaiva: LocalDate,
  paallekkainenPaattymispaiva: Option[LocalDate],
  paallekkainenTilatParametrienSisalla: Option[String],
  paallekkainenAlkanutEka: String,
  paallekkainenRahoitusmuodot: Option[String],
  paallekkainenRahoitusmuodotParametrienSisalla: Option[String],
  paallekkainenVoimassaParametrienSisalla: Boolean
)
