package fi.oph.koski.oppivelvollisuustieto

import java.time.LocalDate
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.raportit.AhvenanmaanKunnat.ahvenanmaanKunnat
import fi.oph.koski.raportointikanta.{RaportointiDatabase, Schema}
import fi.oph.koski.valpas.opiskeluoikeusrepository.ValpasRajapäivätService
import slick.jdbc.GetResult


object Oppivelvollisuustiedot {
    def oppivelvollisuudenUlkopuolisetKunnat = ahvenanmaanKunnat ++ List(
      "198",  // Ei kotikuntaa Suomessa
      "200",  // Ulkomaat
      "",     // Virheellinen null
    )

  def queryByOids(oids: Seq[String], db: RaportointiDatabase): Seq[Oppivelvollisuustieto] = {
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
  def createMaterializedView(s: Schema, valpasRajapäivätService: ValpasRajapäivätService)= {
    val valpasLakiVoimassaVanhinSyntymäaika = valpasRajapäivätService.lakiVoimassaVanhinSyntymäaika
    val valpasLakiVoimassaPeruskoulustaValmistuneilla = valpasRajapäivätService.lakiVoimassaPeruskoulustaValmistuneillaAlku
    val oppivelvollisuusLoppuuIka = valpasRajapäivätService.oppivelvollisuusLoppuuIka
    val maksuttomuusLoppuuIka = valpasRajapäivätService.maksuttomuusLoppuuIka

    val oppivelvollisuudenUlkopuolisetKunnatList = validatedUnboundCodeList(oppivelvollisuudenUlkopuolisetKunnat)

    sqlu"""
      create materialized view #${s.name}.oppivelvollisuustiedot as
        with
          oppivelvolliset_henkilot as (

              select
                oppija_oid,
                master_oid,
                syntymaaika,
                (select count(distinct paivat) from (
                    select generate_series(alku, loppu, interval '1 day') paivat
                    from #${s.name}.r_opiskeluoikeus_aikajakso ooaj
                    inner join #${s.name}.r_opiskeluoikeus oo on (oo.oppija_oid = henkilo.oppija_oid
                      or oo.oppija_oid = any(henkilo.linkitetyt_oidit)
                      or oo.oppija_oid = henkilo.master_oid)
                    where oikeutta_maksuttomuuteen_pidennetty = true
                    and (ooaj.opiskeluoikeus_oid = oo.opiskeluoikeus_oid)
                    )
                pidennyspaivat) as maksuttomuutta_pidennetty_yhteensa
              from
                #${s.name}.r_henkilo henkilo
              where syntymaaika >= '#$valpasLakiVoimassaVanhinSyntymäaika'::date
                and (
                  turvakielto = true
                  or not (kotikunta is null or kotikunta = any(#$oppivelvollisuudenUlkopuolisetKunnatList))
                )
                and master_oid not in (
                                select
                                  henkilo.master_oid
                                from
                                  #${s.name}.r_henkilo henkilo
                                  join #${s.name}.r_opiskeluoikeus opiskeluoikeus on henkilo.oppija_oid = opiskeluoikeus.oppija_oid
                                  join #${s.name}.r_paatason_suoritus paatason_suoritus on opiskeluoikeus.opiskeluoikeus_oid = paatason_suoritus.opiskeluoikeus_oid
                                -- HUOMIOI, JOS TÄTÄ MUUTAT: Pitää olla synkassa getPerusopetuksenAikavälitIlmanKäyttöoikeustarkistusta-metodissa
                                -- tuotantokantaan tehtävän tarkistuksen kanssa. Muuten Valppaan maksuttomuushaku menee rikki.
                                where (suorituksen_tyyppi = 'perusopetuksenoppimaara'
                                  and vahvistus_paiva < '#$valpasLakiVoimassaPeruskoulustaValmistuneilla'::date) or
                                  (suorituksen_tyyppi = 'aikuistenperusopetuksenoppimaara'
                                  and vahvistus_paiva < '#$valpasLakiVoimassaPeruskoulustaValmistuneilla'::date) or
                                  (suorituksen_tyyppi = 'internationalschoolmypvuosiluokka'
                                  and koulutusmoduuli_koodiarvo = '9'
                                  and vahvistus_paiva < '#$valpasLakiVoimassaPeruskoulustaValmistuneilla'::date)
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
            when suorittaa_ammattitutkintoa and suorittaa_lukionoppimaaraa then (syntymaaika + interval '#$oppivelvollisuusLoppuuIka year')::date
            when suorittaa_ammattitutkintoa then least(ammattitutkinnon_vahvistus_paiva, (syntymaaika + interval '#$oppivelvollisuusLoppuuIka year')::date)
            when suorittaa_lukionoppimaaraa then (syntymaaika + interval '#$oppivelvollisuusLoppuuIka year')::date
            else (syntymaaika + interval '#$oppivelvollisuusLoppuuIka year')::date
          end
            oppivelvollisuusVoimassaAsti,
          case
            when suorittaa_ammattitutkintoa and suorittaa_lukionoppimaaraa then (#${s.name}.vuodenViimeinenPaivamaara(syntymaaika + interval '#$maksuttomuusLoppuuIka year') + interval '1 day' * maksuttomuutta_pidennetty_yhteensa)::date
            when suorittaa_ammattitutkintoa then (least(ammattitutkinnon_vahvistus_paiva, #${s.name}.vuodenViimeinenPaivamaara(syntymaaika + interval '#$maksuttomuusLoppuuIka year')) + interval '1 day' * maksuttomuutta_pidennetty_yhteensa)::date
            when suorittaa_lukionoppimaaraa then (#${s.name}.vuodenViimeinenPaivamaara(syntymaaika + interval '#$maksuttomuusLoppuuIka year') + interval '1 day' * maksuttomuutta_pidennetty_yhteensa)::date
            else (#${s.name}.vuodenViimeinenPaivamaara(syntymaaika + interval '#$maksuttomuusLoppuuIka year') + interval '1 day' * maksuttomuutta_pidennetty_yhteensa)::date
          end
            oikeusKoulutuksenMaksuttomuuteenVoimassaAsti
        from
          oppivelvolliset_henkilot
          left join ammattitutkinto on oppivelvolliset_henkilot.master_oid = ammattitutkinto.master_oid
          left join lukionoppimaara on oppivelvolliset_henkilot.master_oid = lukionoppimaara.master_oid
      """
  }

  def createIndexes(s: Schema) = {
    sqlu"""
          create index on #${s.name}.oppivelvollisuustiedot (
               oppija_oid,
               oppivelvollisuusvoimassaasti,
               oikeuskoulutuksenmaksuttomuuteenvoimassaasti
          )"""
  }

  implicit private val oppivelvollisuustietoGetResult: GetResult[Oppivelvollisuustieto] = GetResult(row =>
    Oppivelvollisuustieto(
      oid = row.rs.getString("oppija_oid"),
      oppivelvollisuusVoimassaAsti = row.getLocalDate("oppivelvollisuusVoimassaAsti"),
      oikeusMaksuttomaanKoulutukseenVoimassaAsti = row.getLocalDate("oikeusKoulutuksenMaksuttomuuteenVoimassaAsti")
    )
  )

  private def validatedUnboundCodeList(list: Seq[String]): String = {
    assert(list.forall(_.forall(Character.isDigit)), "Annettu kuntakoodilista sisälsi ei-numeerisia merkkejä")
    val listString = list
      .map(c => s""""$c"""")
      .mkString(",")
    s"'{$listString}'"
  }
}

case class Oppivelvollisuustieto(
  oid: String,
  oppivelvollisuusVoimassaAsti: LocalDate,
  oikeusMaksuttomaanKoulutukseenVoimassaAsti: LocalDate
)
