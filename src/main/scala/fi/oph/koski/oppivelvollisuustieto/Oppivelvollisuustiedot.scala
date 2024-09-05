package fi.oph.koski.oppivelvollisuustieto

import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.raportit.AhvenanmaanKunnat.ahvenanmaanKunnat
import fi.oph.koski.raportointikanta.{RaportointiDatabase, Schema}
import fi.oph.koski.valpas.opiskeluoikeusrepository.ValpasRajapäivätService
import org.postgresql.util.PSQLException
import slick.jdbc.GetResult

import java.time.LocalDate


object Oppivelvollisuustiedot {
    def oppivelvollisuudenUlkopuolisetKunnatTaiKuntaVirheellinen: List[String] =
      oppivelvollisuudenUlkopuolisetKunnat ++ List("")

  def oppivelvollisuudenUlkopuolisetKunnat: List[String] =
    ahvenanmaanKunnat ++ List(
      "198", // Ei kotikuntaa Suomessa
      "200", // Ulkomaat
    )

  def queryByOid(oid: String, db: RaportointiDatabase): Option[Oppivelvollisuustieto] = {
    try {
      db.runDbSync(
        sql"select * from oppivelvollisuustiedot where oppija_oid = $oid".as[Oppivelvollisuustieto]
      ).headOption
    } catch {
      // Tämä poikkeus syntyy tilanteessa, jossa oppivelvollisuustiedot-taulu ei ole vielä materialisoitu,
      // mutta lokaalisti ajettaessa palvelimen käynnistyessä yritetään luoda mock-opiskeluoikeuksia.
      case _: PSQLException => None
    }
  }

  def queryByOids(oids: Seq[String], db: RaportointiDatabase): Seq[Oppivelvollisuustieto] = {
    db.runDbSync(
      sql"select * from oppivelvollisuustiedot where oppija_oid = any($oids)".as[Oppivelvollisuustieto]
    )
  }

  def queryByOidsIncludeMissing(oids: Seq[String], db: RaportointiDatabase): Seq[OptionalOppivelvollisuustieto] = {
    db.runDbSync(
      sql"""
        SELECT
          r_henkilo.oppija_oid,
          r_henkilo.hetu,
          r_henkilo.etunimet,
          r_henkilo.sukunimi,
          oppivelvollisuustiedot.oppivelvollisuusvoimassaasti,
          oppivelvollisuustiedot.oikeuskoulutuksenmaksuttomuuteenvoimassaasti
        FROM r_henkilo
        FULL JOIN oppivelvollisuustiedot ON r_henkilo.master_oid = oppivelvollisuustiedot.oppija_oid
        WHERE r_henkilo.oppija_oid = any($oids)
        ORDER BY r_henkilo.oppija_oid
      """.as[OptionalOppivelvollisuustieto]
    )
  }

  def queryByHetusIncludeMissing(hetus: Seq[String], db: RaportointiDatabase): Seq[OptionalOppivelvollisuustieto] =
    db.runDbSync(
      sql"""
        SELECT
        r_henkilo.oppija_oid,
        r_henkilo.hetu,
        r_henkilo.etunimet,
        r_henkilo.sukunimi,
        oppivelvollisuustiedot.oppivelvollisuusvoimassaasti,
        oppivelvollisuustiedot.oikeuskoulutuksenmaksuttomuuteenvoimassaasti
          FROM r_henkilo
          FULL JOIN oppivelvollisuustiedot ON r_henkilo.master_oid = oppivelvollisuustiedot.oppija_oid
          WHERE r_henkilo.hetu = any($hetus)
      """.as[OptionalOppivelvollisuustieto]
    )

  def createPrecomputedTable(s: Schema, confidentalSchema: Schema, valpasRajapäivätService: ValpasRajapäivätService) = {
    val tarkastelupäivä = valpasRajapäivätService.tarkastelupäivä
    val valpasLakiVoimassaVanhinSyntymäaika = valpasRajapäivätService.lakiVoimassaVanhinSyntymäaika
    val valpasLakiVoimassaPeruskoulustaValmistuneilla = valpasRajapäivätService.lakiVoimassaPeruskoulustaValmistuneillaAlku
    val oppivelvollisuusAlkaaIka = valpasRajapäivätService.oppivelvollisuusAlkaaIka.toInt
    val oppivelvollisuusAlkaaKuukausi = valpasRajapäivätService.oppivelvollisuusAlkaaPäivämäärä.getMonthValue
    val oppivelvollisuusAlkaaPäivä = valpasRajapäivätService.oppivelvollisuusAlkaaPäivämäärä.getDayOfMonth
    val oppivelvollisuusLoppuuIka = valpasRajapäivätService.oppivelvollisuusLoppuuIka
    val maksuttomuusLoppuuIka = valpasRajapäivätService.maksuttomuusLoppuuIka

    val ulkopuolisetKunnatTaiKuntaVirheellinen = validatedUnboundCodeList(oppivelvollisuudenUlkopuolisetKunnatTaiKuntaVirheellinen)
    val ulkopuolisetKunnat = validatedUnboundCodeList(oppivelvollisuudenUlkopuolisetKunnat)

    sqlu"""
      create table #${s.name}.oppivelvollisuustiedot as
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
                  or not (kotikunta is null or kotikunta = any(#$ulkopuolisetKunnatTaiKuntaVirheellinen))
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
                                  and vahvistus_paiva < '#$valpasLakiVoimassaPeruskoulustaValmistuneilla'::date) or
                                  (suorituksen_tyyppi = 'europeanschoolofhelsinkivuosiluokkasecondarylower'
                                  and koulutusmoduuli_koodiarvo = 'S4'
                                  and vahvistus_paiva < '#$valpasLakiVoimassaPeruskoulustaValmistuneilla'::date)
                )

        ),

        kotikunta_suomessa_alkaen as (
          select
            master_oid,
		        min(coalesce(muutto_pvm, poismuutto_pvm)) pvm
          from #${confidentialSchema.name}.r_kotikuntahistoria
          where not kotikunta = any(#$ulkopuolisetKunnat)
          group by master_oid
        ),

        ammattitutkinto as (

            select
              distinct master_oid,
              first_value(opiskeluoikeus.alkamispaiva) over (partition by master_oid order by opiskeluoikeus.alkamispaiva asc nulls last) ammattitutkinnon_alkamispaiva,
              first_value(opiskeluoikeus.paattymispaiva) over (partition by master_oid order by opiskeluoikeus.paattymispaiva desc nulls last) ammattitutkinnon_paattymispaiva,
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
              first_value(opiskeluoikeus.alkamispaiva) over (partition by master_oid order by opiskeluoikeus.alkamispaiva asc nulls last) lukion_oppimaaraan_alkamispaiva,
              first_value(opiskeluoikeus.paattymispaiva) over (partition by master_oid order by opiskeluoikeus.paattymispaiva desc nulls last) lukion_oppimaaraan_paattymispaiva,
              first_value(vahvistus_paiva) over (partition by master_oid order by vahvistus_paiva asc nulls last) lukion_oppimaaraan_vahvistus_paiva,
              true suorittaa_lukionoppimaaraa
            from
              oppivelvolliset_henkilot
              join #${s.name}.r_opiskeluoikeus opiskeluoikeus on oppivelvolliset_henkilot.oppija_oid = opiskeluoikeus.oppija_oid
              join #${s.name}.r_paatason_suoritus paatason_suoritus on opiskeluoikeus.opiskeluoikeus_oid = paatason_suoritus.opiskeluoikeus_oid
            where paatason_suoritus.suorituksen_tyyppi = 'lukionoppimaara'

        ),

        lukionaineopinnot as (

            select
              distinct master_oid,
              first_value(opiskeluoikeus.alkamispaiva) over (partition by master_oid order by opiskeluoikeus.alkamispaiva asc nulls last) lukion_aineopintojen_alkamispaiva,
              first_value(opiskeluoikeus.paattymispaiva) over (partition by master_oid order by opiskeluoikeus.paattymispaiva desc nulls last) lukion_aineopintojen_paattymispaiva,
              first_value(vahvistus_paiva) over (partition by master_oid order by vahvistus_paiva asc nulls last) lukion_aineopintojen_vahvistus_paiva,
              true suorittaa_lukionaineopintoja
            from
              oppivelvolliset_henkilot
              join #${s.name}.r_opiskeluoikeus opiskeluoikeus on oppivelvolliset_henkilot.oppija_oid = opiskeluoikeus.oppija_oid
              join #${s.name}.r_paatason_suoritus paatason_suoritus on opiskeluoikeus.opiskeluoikeus_oid = paatason_suoritus.opiskeluoikeus_oid
            where paatason_suoritus.suorituksen_tyyppi = 'lukionaineopinnot'

        ),

        ylioppilastutkinto as (

            select
              distinct master_oid,
              first_value(vahvistus_paiva) over (partition by master_oid order by vahvistus_paiva asc nulls last) ylioppilastutkinnon_vahvistus_paiva,
              true suorittaa_ylioppilastutkintoa
            from
              oppivelvolliset_henkilot
              join #${s.name}.r_opiskeluoikeus opiskeluoikeus on oppivelvolliset_henkilot.oppija_oid = opiskeluoikeus.oppija_oid
              join #${s.name}.r_paatason_suoritus paatason_suoritus on opiskeluoikeus.opiskeluoikeus_oid = paatason_suoritus.opiskeluoikeus_oid
            where paatason_suoritus.suorituksen_tyyppi = 'ylioppilastutkinto'

        ),

        amis_ja_lukio_samanaikaisuus as (
          select
            distinct ammattitutkinto.master_oid as master_oid,
            true as amis_ja_lukio_samaan_aikaan
          from
            ammattitutkinto
            left join lukionoppimaara on ammattitutkinto.master_oid = lukionoppimaara.master_oid
            left join lukionaineopinnot on ammattitutkinto.master_oid = lukionaineopinnot.master_oid
            left join ylioppilastutkinto on ammattitutkinto.master_oid = ylioppilastutkinto.master_oid
          where
            (ammattitutkinnon_alkamispaiva is not null and (lukion_oppimaaraan_alkamispaiva is not null or lukion_aineopintojen_alkamispaiva is not null))
            and ((ammattitutkinnon_alkamispaiva, least(ammattitutkinnon_paattymispaiva, ammattitutkinnon_vahvistus_paiva, 'infinity') + interval '1 day') overlaps (lukion_oppimaaraan_alkamispaiva, least(lukion_oppimaaraan_paattymispaiva, ylioppilastutkinnon_vahvistus_paiva, 'infinity') + interval '1 day')
            or (ammattitutkinnon_alkamispaiva, least(ammattitutkinnon_paattymispaiva, ammattitutkinnon_vahvistus_paiva, 'infinity') + interval '1 day') overlaps (lukion_aineopintojen_alkamispaiva, least(lukion_aineopintojen_paattymispaiva, ylioppilastutkinnon_vahvistus_paiva, 'infinity') + interval '1 day'))

        ),

        ebtutkinto as (

            select
              distinct master_oid,
              first_value(vahvistus_paiva) over (partition by master_oid order by vahvistus_paiva asc nulls last) ebtutkinto_toisen_asteen_vahvistus_paiva
            from
              oppivelvolliset_henkilot
              join #${s.name}.r_opiskeluoikeus opiskeluoikeus on oppivelvolliset_henkilot.oppija_oid = opiskeluoikeus.oppija_oid
              join #${s.name}.r_paatason_suoritus paatason_suoritus on opiskeluoikeus.opiskeluoikeus_oid = paatason_suoritus.opiskeluoikeus_oid
            where paatason_suoritus.suorituksen_tyyppi = 'ebtutkinto'
              and paatason_suoritus.koulutusmoduuli_koodiarvo = '301104'
              and paatason_suoritus.vahvistus_paiva is not null

        ),

        diatutkinto as (

            select
              distinct master_oid,
              first_value(vahvistus_paiva) over (partition by master_oid order by vahvistus_paiva asc nulls last) dia_tutkinnon_vahvistuspaiva
            from
              oppivelvolliset_henkilot
              join #${s.name}.r_opiskeluoikeus opiskeluoikeus on oppivelvolliset_henkilot.oppija_oid = opiskeluoikeus.oppija_oid
              join #${s.name}.r_paatason_suoritus paatason_suoritus on opiskeluoikeus.opiskeluoikeus_oid = paatason_suoritus.opiskeluoikeus_oid
            where paatason_suoritus.suorituksen_tyyppi = 'diatutkintovaihe'
              and paatason_suoritus.vahvistus_paiva is not null

        ),

        oppivelvollisuudesta_vapautus as (

          select
            master_oid,
            vapautettu - interval '1 day' as oppivelvollisuudesta_vapautus
          from #${s.name}.r_oppivelvollisuudesta_vapautus
          left join #${s.name}.r_henkilo on r_oppivelvollisuudesta_vapautus.oppija_oid = r_henkilo.oppija_oid
        ),

        maksuttomuuden_pidennysjaksot as (
          select
            oppija_oid,
            jsonb_array_elements(data->'lisätiedot'->'oikeuttaMaksuttomuuteenPidennetty') as jakso
          from #${s.name}.r_opiskeluoikeus
          where data->'lisätiedot'->'oikeuttaMaksuttomuuteenPidennetty' is not null
        ),

        maksuttomuuden_pidennysjakso as (
          select
            oppija_oid as master_oid,
            max(jakso->>'loppu')::date as loppu
          from maksuttomuuden_pidennysjaksot
          group by oppija_oid
        )

        select
          oppivelvolliset_henkilot.oppija_oid,

          make_date(
            (extract(year from syntymaaika::date) + #$oppivelvollisuusAlkaaIka)::integer,
            #$oppivelvollisuusAlkaaKuukausi,
            #$oppivelvollisuusAlkaaPäivä
          ) AS oppivelvollisuusVoimassaAlkaen,

          -- Huom! Osa samasta logiikasta on myös Scala-koodina ValpasRajapäivätService-luokassa. Varmista muutosten jälkeen,
          -- että logiikka säilyy samana.

          least(
              oppivelvollisuudesta_vapautus,
              dia_tutkinnon_vahvistuspaiva,
              ebtutkinto_toisen_asteen_vahvistus_paiva,
              ylioppilastutkinnon_vahvistus_paiva,
              ammattitutkinnon_vahvistus_paiva,
              (syntymaaika + interval '#$oppivelvollisuusLoppuuIka year' - interval '1 day')::date)
            as oppivelvollisuusVoimassaAsti,

          -- Huom! Osa samasta logiikasta on myös Scala-koodina ValpasRajapäivätService-luokassa. Varmista muutosten jälkeen,
          -- että logiikka säilyy samana.
          greatest(
            maksuttomuuden_pidennysjakso.loppu,
            case
              when amis_ja_lukio_samaan_aikaan and ylioppilastutkinnon_vahvistus_paiva is not null and ammattitutkinnon_vahvistus_paiva is not null then least(
                oppivelvollisuudesta_vapautus,
                dia_tutkinnon_vahvistuspaiva,
                ebtutkinto_toisen_asteen_vahvistus_paiva,
                greatest(ylioppilastutkinnon_vahvistus_paiva, ammattitutkinnon_vahvistus_paiva),
                #${s.name}.vuodenViimeinenPaivamaara(syntymaaika + interval '#$maksuttomuusLoppuuIka year'))

             when amis_ja_lukio_samaan_aikaan then least(
                oppivelvollisuudesta_vapautus,
                dia_tutkinnon_vahvistuspaiva,
                ebtutkinto_toisen_asteen_vahvistus_paiva,
                #${s.name}.vuodenViimeinenPaivamaara(syntymaaika + interval '#$maksuttomuusLoppuuIka year'))

              else least(
                oppivelvollisuudesta_vapautus,
                dia_tutkinnon_vahvistuspaiva,
                ebtutkinto_toisen_asteen_vahvistus_paiva,
                ylioppilastutkinnon_vahvistus_paiva,
                ammattitutkinnon_vahvistus_paiva,
                #${s.name}.vuodenViimeinenPaivamaara(syntymaaika + interval '#$maksuttomuusLoppuuIka year'))
            end::date
          ) as oikeusKoulutuksenMaksuttomuuteenVoimassaAsti,

          kotikunta_suomessa_alkaen.pvm as kotikuntaSuomessaAlkaen

        from
          oppivelvolliset_henkilot
          left join ammattitutkinto on oppivelvolliset_henkilot.master_oid = ammattitutkinto.master_oid
          left join lukionoppimaara on oppivelvolliset_henkilot.master_oid = lukionoppimaara.master_oid
          left join lukionaineopinnot on oppivelvolliset_henkilot.master_oid = lukionaineopinnot.master_oid
          left join ebtutkinto on oppivelvolliset_henkilot.master_oid = ebtutkinto.master_oid
          left join diatutkinto on oppivelvolliset_henkilot.master_oid = diatutkinto.master_oid
          left join oppivelvollisuudesta_vapautus on oppivelvolliset_henkilot.master_oid = oppivelvollisuudesta_vapautus.master_oid
          left join ylioppilastutkinto on oppivelvolliset_henkilot.master_oid = ylioppilastutkinto.master_oid
          left join maksuttomuuden_pidennysjakso on oppivelvolliset_henkilot.master_oid = maksuttomuuden_pidennysjakso.master_oid
          left join amis_ja_lukio_samanaikaisuus on oppivelvolliset_henkilot.master_oid = amis_ja_lukio_samanaikaisuus.master_oid
          left join kotikunta_suomessa_alkaen on oppivelvolliset_henkilot.master_oid = kotikunta_suomessa_alkaen.master_oid
        where
          (kotikunta_suomessa_alkaen.pvm is null or kotikunta_suomessa_alkaen.pvm < oppivelvolliset_henkilot.syntymaaika + interval '18 years')
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

  def onOppivelvollinenPelkänIänPerusteella(syntymäaika: Option[LocalDate], valpasRajapäivätService: ValpasRajapäivätService): Boolean = {
    syntymäaika match {
      case Some(syntymäaika) => {
        val oppivelvollisuusAlkaa = valpasRajapäivätService.oppivelvollisuusAlkaa(syntymäaika)
        val oppivelvollisuusLoppuu = syntymäaika.plusYears(valpasRajapäivätService.oppivelvollisuusLoppuuIka.toLong)
        !oppivelvollisuusAlkaa.isAfter(valpasRajapäivätService.tarkastelupäivä) && oppivelvollisuusLoppuu.isAfter(valpasRajapäivätService.tarkastelupäivä)
      }
      case None => false
    }
  }

  implicit private val oppivelvollisuustietoGetResult: GetResult[Oppivelvollisuustieto] = GetResult(row =>
    Oppivelvollisuustieto(
      oid = row.rs.getString("oppija_oid"),
      oppivelvollisuusVoimassaAsti = row.getLocalDate("oppivelvollisuusVoimassaAsti"),
      oikeusMaksuttomaanKoulutukseenVoimassaAsti = row.getLocalDate("oikeusKoulutuksenMaksuttomuuteenVoimassaAsti")
    )
  )

  implicit private val getOptionalOppivelvollisuustietoResult: GetResult[OptionalOppivelvollisuustieto] = GetResult(row =>
    OptionalOppivelvollisuustieto(
      oid = row.rs.getString("oppija_oid"),
      hetu = Option(row.rs.getString("hetu")),
      oppivelvollisuusVoimassaAsti = Option(row.getLocalDate("oppivelvollisuusVoimassaAsti")),
      oikeusMaksuttomaanKoulutukseenVoimassaAsti = Option(row.getLocalDate("oikeusKoulutuksenMaksuttomuuteenVoimassaAsti")),
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

case class OptionalOppivelvollisuustieto(
  oid: String,
  hetu: Option[String],
  oppivelvollisuusVoimassaAsti: Option[LocalDate],
  oikeusMaksuttomaanKoulutukseenVoimassaAsti: Option[LocalDate]
)
