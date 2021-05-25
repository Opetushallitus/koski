package fi.oph.koski.oppivelvollisuustieto

import java.time.LocalDate

import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.raportointikanta.{RaportointiDatabase, Schema}

import slick.jdbc.GetResult


object Oppivelvollisuustiedot {
  def queryByOids(oids: List[String], db: RaportointiDatabase): Seq[Oppivelvollisuustieto] = {
    db.runDbSync(
      sql"select * from oppivelvollisuustiedot where oppija_oid = any($oids)".as[Oppivelvollisuustieto]
    )
  }

  /*
    Voimassaolojen päättelyssä ei tällä hetkellä oteta ylioppilastutkinnon suoritusta ollenkaan huomioon, koska tekovaiheessa tähän dataan ei ollut pääsyä.
    Tämä kierretään sillä, että voimassaolot päättyvät aina syntymäajan mukaan.

    Kun ylioppilastutkinnon datat ovat saatavilla, tulisi voimassaolot päätellä seuraavasti:

      - Jos oppija suorittaa lukion oppimäärää, voimassaolot päättyvät kun molemmat lukion oppimäärä ja ylioppilastutkinto ovat valmiita, jos henkilön ikä sitä ei aikaisemmin päätä.
  */
  def createMaterializedView(s: Schema)= {
    sqlu"""
      create materialized view #${s.name}.oppivelvollisuustiedot as
        with
          oppivelvolliset_henkilot as (

              select
                oppija_oid,
                master_oid,
                syntymaaika
              from
                #${s.name}.r_henkilo henkilo
              where date_part('year', syntymaaika) >= 2004
                and master_oid not in (
                                select
                                  henkilo.master_oid
                                from
                                  #${s.name}.r_henkilo henkilo
                                  join #${s.name}.r_opiskeluoikeus opiskeluoikeus on henkilo.oppija_oid = opiskeluoikeus.oppija_oid
                                  join #${s.name}.r_paatason_suoritus paatason_suoritus on opiskeluoikeus.opiskeluoikeus_oid = paatason_suoritus.opiskeluoikeus_oid
                                where (suorituksen_tyyppi = 'perusopetuksenoppimaara'
                                  and vahvistus_paiva < '2021-01-01'::date) or
                                  (suorituksen_tyyppi = 'aikuistenperusopetuksenoppimaara'
                                  and vahvistus_paiva < '2021-01-01'::date) or
                                  (suorituksen_tyyppi = 'internationalschoolmypvuosiluokka'
                                  and koulutusmoduuli_koodiarvo = '9'
                                  and vahvistus_paiva < '2021-01-01'::date)
                )

        ),

        ammattitutkinto as (

            select
              distinct master_oid,
              first_value(vahvistus_paiva) over (partition by master_oid order by vahvistus_paiva asc nulls last) ammattitutkinnon_vahvistus_paiva,
              true suorittaa_ammattitutkintoa
            from
              oppivelvolliset_henkilot
              join #${s.name}.r_opiskeluoikeus opiskeluoikeus on oppivelvolliset_henkilot.oppija_oid = opiskeluoikeus.oppija_oid
              join #${s.name}.r_paatason_suoritus paatason_suoritus on opiskeluoikeus.opiskeluoikeus_oid = paatason_suoritus.opiskeluoikeus_oid
            where paatason_suoritus.suorituksen_tyyppi = 'ammatillinentutkinto'

        ),

        lukionoppimaara as (

            select
              distinct master_oid,
              first_value(vahvistus_paiva) over (partition by master_oid order by vahvistus_paiva asc nulls last) lukion_oppimaaraan_vahvistus_paiva,
              true suorittaa_lukionoppimaaraa
            from
              oppivelvolliset_henkilot
              join #${s.name}.r_opiskeluoikeus opiskeluoikeus on oppivelvolliset_henkilot.oppija_oid = opiskeluoikeus.oppija_oid
              join #${s.name}.r_paatason_suoritus paatason_suoritus on opiskeluoikeus.opiskeluoikeus_oid = paatason_suoritus.opiskeluoikeus_oid
            where paatason_suoritus.suorituksen_tyyppi = 'lukionoppimaara'

        )

        select
          oppivelvolliset_henkilot.oppija_oid,
          case
            when suorittaa_ammattitutkintoa and suorittaa_lukionoppimaaraa then (syntymaaika + interval '18 year')::date
            when suorittaa_ammattitutkintoa then least(ammattitutkinnon_vahvistus_paiva, (syntymaaika + interval '18 year')::date)
            when suorittaa_lukionoppimaaraa then (syntymaaika + interval '18 year')::date
            else (syntymaaika + interval '18 year')::date
          end
            oppivelvollisuusVoimassaAsti,
          case
            when suorittaa_ammattitutkintoa and suorittaa_lukionoppimaaraa then #${s.name}.vuodenViimeinenPaivamaara(syntymaaika + interval '20 year')
            when suorittaa_ammattitutkintoa then least(ammattitutkinnon_vahvistus_paiva, #${s.name}.vuodenViimeinenPaivamaara(syntymaaika + interval '20 year'))
            when suorittaa_lukionoppimaaraa then #${s.name}.vuodenViimeinenPaivamaara(syntymaaika + interval '20 year')
            else #${s.name}.vuodenViimeinenPaivamaara(syntymaaika + interval '20 year')
          end
            oikeusKoulutuksenMaksuttomuuteenVoimassaAsti
        from
          oppivelvolliset_henkilot
          left join ammattitutkinto on oppivelvolliset_henkilot.master_oid = ammattitutkinto.master_oid
          left join lukionoppimaara on oppivelvolliset_henkilot.master_oid = lukionoppimaara.master_oid
      """
  }

  implicit private val oppivelvollisuustietoGetResult: GetResult[Oppivelvollisuustieto] = GetResult(row =>
    Oppivelvollisuustieto(
      oid = row.rs.getString("oppija_oid"),
      oppivelvollisuusVoimassaAsti = row.getLocalDate("oppivelvollisuusVoimassaAsti"),
      oikeusMaksuttomaanKoulutukseenVoimassaAsti = row.getLocalDate("oikeusKoulutuksenMaksuttomuuteenVoimassaAsti")
    )
  )
}

case class Oppivelvollisuustieto(
  oid: String,
  oppivelvollisuusVoimassaAsti: LocalDate,
  oikeusMaksuttomaanKoulutukseenVoimassaAsti: LocalDate
)
