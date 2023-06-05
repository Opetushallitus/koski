package fi.oph.koski.oppivelvollisuustieto

import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.raportit.AhvenanmaanKunnat.ahvenanmaanKunnat
import fi.oph.koski.raportointikanta.{RaportointiDatabase, Schema}
import fi.oph.koski.valpas.opiskeluoikeusrepository.ValpasRajapäivätService
import org.postgresql.util.PSQLException
import slick.jdbc.GetResult

import java.time.LocalDate


object Oppivelvollisuustiedot {
    def oppivelvollisuudenUlkopuolisetKunnat = ahvenanmaanKunnat ++ List(
      "198",  // Ei kotikuntaa Suomessa
      "200",  // Ulkomaat
      "",     // Virheellinen null
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

  /*
    Voimassaolojen päättelyssä ei tällä hetkellä oteta ylioppilastutkinnon suoritusta ollenkaan huomioon, koska tekovaiheessa tähän dataan ei ollut pääsyä.
    Tämä kierretään sillä, että voimassaolot päättyvät aina syntymäajan mukaan.

    Kun ylioppilastutkinnon datat ovat saatavilla, tulisi voimassaolot päätellä seuraavasti:

      - Jos oppija suorittaa lukion oppimäärää, voimassaolot päättyvät kun molemmat lukion oppimäärä ja ylioppilastutkinto ovat valmiita, jos henkilön ikä sitä ei aikaisemmin päätä.
  */
  def createPrecomputedTable(s: Schema, valpasRajapäivätService: ValpasRajapäivätService)= {
    val tarkastelupäivä = valpasRajapäivätService.tarkastelupäivä
    val valpasLakiVoimassaVanhinSyntymäaika = valpasRajapäivätService.lakiVoimassaVanhinSyntymäaika
    val valpasLakiVoimassaPeruskoulustaValmistuneilla = valpasRajapäivätService.lakiVoimassaPeruskoulustaValmistuneillaAlku
    val oppivelvollisuusAlkaaIka = valpasRajapäivätService.oppivelvollisuusAlkaaIka.toInt
    val oppivelvollisuusAlkaaKuukausi = valpasRajapäivätService.oppivelvollisuusAlkaaPäivämäärä.getMonthValue
    val oppivelvollisuusAlkaaPäivä = valpasRajapäivätService.oppivelvollisuusAlkaaPäivämäärä.getDayOfMonth
    val oppivelvollisuusLoppuuIka = valpasRajapäivätService.oppivelvollisuusLoppuuIka
    val maksuttomuusLoppuuIka = valpasRajapäivätService.maksuttomuusLoppuuIka

    val oppivelvollisuudenUlkopuolisetKunnatList = validatedUnboundCodeList(oppivelvollisuudenUlkopuolisetKunnat)

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
                                  and vahvistus_paiva < '#$valpasLakiVoimassaPeruskoulustaValmistuneilla'::date) or
                                  (suorituksen_tyyppi = 'europeanschoolofhelsinkivuosiluokkasecondarylower'
                                  and koulutusmoduuli_koodiarvo = 'S5'
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

        ),

        internationalschool as (

            select
              distinct master_oid,
              first_value(vahvistus_paiva) over (partition by master_oid order by vahvistus_paiva asc nulls last) international_schoolin_toisen_asteen_vahvistus_paiva
            from
              oppivelvolliset_henkilot
              join #${s.name}.r_opiskeluoikeus opiskeluoikeus on oppivelvolliset_henkilot.oppija_oid = opiskeluoikeus.oppija_oid
              join #${s.name}.r_paatason_suoritus paatason_suoritus on opiskeluoikeus.opiskeluoikeus_oid = paatason_suoritus.opiskeluoikeus_oid
            where paatason_suoritus.suorituksen_tyyppi = 'internationalschooldiplomavuosiluokka'
              and paatason_suoritus.koulutusmoduuli_koodiarvo = '12'
              and paatason_suoritus.vahvistus_paiva is not null

        ),

        europeanschoolofhelsinki as (

            select
              distinct master_oid,
              first_value(vahvistus_paiva) over (partition by master_oid order by vahvistus_paiva asc nulls last) european_school_of_helsinki_toisen_asteen_vahvistus_paiva
            from
              oppivelvolliset_henkilot
              join #${s.name}.r_opiskeluoikeus opiskeluoikeus on oppivelvolliset_henkilot.oppija_oid = opiskeluoikeus.oppija_oid
              join #${s.name}.r_paatason_suoritus paatason_suoritus on opiskeluoikeus.opiskeluoikeus_oid = paatason_suoritus.opiskeluoikeus_oid
            where paatason_suoritus.suorituksen_tyyppi = 'ebtutkinto'
              and paatason_suoritus.koulutusmoduuli_koodiarvo = '301104'
              and paatason_suoritus.vahvistus_paiva is not null

        ),

        ibtutkinto as (

            select
              distinct master_oid,
              first_value(vahvistus_paiva) over (partition by master_oid order by vahvistus_paiva asc nulls last) ib_tutkinnon_vahvistuspaiva
            from
              oppivelvolliset_henkilot
              join #${s.name}.r_opiskeluoikeus opiskeluoikeus on oppivelvolliset_henkilot.oppija_oid = opiskeluoikeus.oppija_oid
              join #${s.name}.r_paatason_suoritus paatason_suoritus on opiskeluoikeus.opiskeluoikeus_oid = paatason_suoritus.opiskeluoikeus_oid
            where paatason_suoritus.suorituksen_tyyppi = 'ibtutkinto'
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
          case
            when suorittaa_ammattitutkintoa and suorittaa_lukionoppimaaraa then least(
              oppivelvollisuudesta_vapautus,
              dia_tutkinnon_vahvistuspaiva,
              ib_tutkinnon_vahvistuspaiva,
              international_schoolin_toisen_asteen_vahvistus_paiva,
              european_school_of_helsinki_toisen_asteen_vahvistus_paiva,
              ammattitutkinnon_vahvistus_paiva,
              (syntymaaika + interval '#$oppivelvollisuusLoppuuIka year' - interval '1 day')::date)

            when suorittaa_ammattitutkintoa then least(
              oppivelvollisuudesta_vapautus,
              dia_tutkinnon_vahvistuspaiva,
              ib_tutkinnon_vahvistuspaiva,
              international_schoolin_toisen_asteen_vahvistus_paiva,
              european_school_of_helsinki_toisen_asteen_vahvistus_paiva,
              ammattitutkinnon_vahvistus_paiva,
              (syntymaaika + interval '#$oppivelvollisuusLoppuuIka year' - interval '1 day')::date)

            when suorittaa_lukionoppimaaraa then least(
              oppivelvollisuudesta_vapautus,
              dia_tutkinnon_vahvistuspaiva,
              ib_tutkinnon_vahvistuspaiva,
              international_schoolin_toisen_asteen_vahvistus_paiva,
              european_school_of_helsinki_toisen_asteen_vahvistus_paiva,
              (syntymaaika + interval '#$oppivelvollisuusLoppuuIka year' - interval '1 day')::date)

            else least(
              oppivelvollisuudesta_vapautus,
              dia_tutkinnon_vahvistuspaiva,
              ib_tutkinnon_vahvistuspaiva,
              international_schoolin_toisen_asteen_vahvistus_paiva,
              european_school_of_helsinki_toisen_asteen_vahvistus_paiva,
              (syntymaaika + interval '#$oppivelvollisuusLoppuuIka year' - interval '1 day')::date)
          end
            oppivelvollisuusVoimassaAsti,

          -- Huom! Osa samasta logiikasta on myös Scala-koodina ValpasRajapäivätService-luokassa. Varmista muutosten jälkeen,
          -- että logiikka säilyy samana.
          case
            when suorittaa_ammattitutkintoa and suorittaa_lukionoppimaaraa then least(
              oppivelvollisuudesta_vapautus,
              dia_tutkinnon_vahvistuspaiva,
              ib_tutkinnon_vahvistuspaiva,
              international_schoolin_toisen_asteen_vahvistus_paiva,
              european_school_of_helsinki_toisen_asteen_vahvistus_paiva,
              #${s.name}.vuodenViimeinenPaivamaara(syntymaaika + interval '#$maksuttomuusLoppuuIka year'))

            when suorittaa_ammattitutkintoa then least(
              oppivelvollisuudesta_vapautus,
              dia_tutkinnon_vahvistuspaiva,
              ib_tutkinnon_vahvistuspaiva,
              international_schoolin_toisen_asteen_vahvistus_paiva,
              european_school_of_helsinki_toisen_asteen_vahvistus_paiva,
              ammattitutkinnon_vahvistus_paiva,
              #${s.name}.vuodenViimeinenPaivamaara(syntymaaika + interval '#$maksuttomuusLoppuuIka year'))

            else least(
              oppivelvollisuudesta_vapautus,
              dia_tutkinnon_vahvistuspaiva,
              ib_tutkinnon_vahvistuspaiva,
              international_schoolin_toisen_asteen_vahvistus_paiva,
              european_school_of_helsinki_toisen_asteen_vahvistus_paiva,
              #${s.name}.vuodenViimeinenPaivamaara(syntymaaika + interval '#$maksuttomuusLoppuuIka year'))
          end
            oikeusKoulutuksenMaksuttomuuteenVoimassaAsti

        from
          oppivelvolliset_henkilot
          left join ammattitutkinto on oppivelvolliset_henkilot.master_oid = ammattitutkinto.master_oid
          left join lukionoppimaara on oppivelvolliset_henkilot.master_oid = lukionoppimaara.master_oid
          left join internationalschool on oppivelvolliset_henkilot.master_oid = internationalschool.master_oid
          left join europeanschoolofhelsinki on oppivelvolliset_henkilot.master_oid = europeanschoolofhelsinki.master_oid
          left join ibtutkinto on oppivelvolliset_henkilot.master_oid = ibtutkinto.master_oid
          left join diatutkinto on oppivelvolliset_henkilot.master_oid = diatutkinto.master_oid
          left join oppivelvollisuudesta_vapautus on oppivelvolliset_henkilot.master_oid = oppivelvollisuudesta_vapautus.master_oid
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
