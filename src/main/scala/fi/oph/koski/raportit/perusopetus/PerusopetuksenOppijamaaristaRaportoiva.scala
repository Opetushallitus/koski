package fi.oph.koski.raportit.perusopetus

import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.db.QueryMethods
import fi.oph.koski.koskiuser.KoskiSpecificSession
import slick.jdbc.SQLActionBuilder

import java.time.LocalDate

trait PerusopetuksenOppijamääristäRaportoiva extends QueryMethods {
  protected def virheellisestiSiirrettyjäTukitietojaEhtoSqlPart: Option[SQLActionBuilder] =
    Some(sql"""
      not kotiopetus and ((vaikeasti_vammainen and vammainen) or (pidennetty_oppivelvollisuus and (not erityinen_tuki or (not vaikeasti_vammainen and not vammainen))) or (not pidennetty_oppivelvollisuus and (vaikeasti_vammainen or vammainen)))
    """)

  protected def fromJoinWhereSqlPart(oppilaitosOids: Seq[String], date: LocalDate)(implicit u: KoskiSpecificSession): Option[SQLActionBuilder] =
    Some(sql"""
      from r_opiskeluoikeus oo
      join r_organisaatiohistoria oh on oh.opiskeluoikeus_oid = oo.opiskeluoikeus_oid
      join r_organisaatio oppilaitos on oppilaitos.organisaatio_oid = oh.oppilaitos_oid
      join r_henkilo on r_henkilo.oppija_oid = oo.oppija_oid
      join r_paatason_suoritus pts on pts.opiskeluoikeus_oid = oo.opiskeluoikeus_oid
      join r_opiskeluoikeus_aikajakso aikajakso on aikajakso.opiskeluoikeus_oid = oo.opiskeluoikeus_oid
      left join r_organisaatio_kieli on r_organisaatio_kieli.organisaatio_oid = oh.oppilaitos_oid
      left join r_koodisto_koodi opetuskieli_koodisto
        on opetuskieli_koodisto.koodisto_uri = split_part(r_organisaatio_kieli.kielikoodi, '_', 1)
        and opetuskieli_koodisto.koodiarvo = split_part(split_part(r_organisaatio_kieli.kielikoodi, '_', 2), '#', 1)
      where oh.oppilaitos_oid = any(${oppilaitosOids})
        and oh.alku <= $date
        and oh.loppu >= $date
        and oo.koulutusmuoto = 'perusopetus'
        and (pts.vahvistus_paiva is null or pts.vahvistus_paiva > $date)
        and pts.alkamispaiva <= $date
        and pts.koulutusmoduuli_koodiarvo in ('1', '2', '3', '4', '5', '6', '7', '8', '9')
        and aikajakso.alku <= $date
        and aikajakso.loppu >= $date
        and aikajakso.tila = 'lasna'
        and oo.sisaltyy_opiskeluoikeuteen_oid is null
""")
}
