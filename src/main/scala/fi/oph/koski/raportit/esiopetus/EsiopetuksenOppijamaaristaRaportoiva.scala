package fi.oph.koski.raportit.esiopetus

import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.db.QueryMethods
import fi.oph.koski.koskiuser.{AccessType, KoskiSpecificSession}
import fi.oph.koski.schema.Organisaatio.isValidOrganisaatioOid
import slick.jdbc.SQLActionBuilder

import java.time.LocalDate

trait EsiopetuksenOppijamääristäRaportoiva extends QueryMethods{
  protected def virheellisestiSiirrettyjäTukitietojaEhtoSqlPart: Option[SQLActionBuilder] =
    Some(sql"""
      ((vaikeasti_vammainen and vammainen) or (pidennetty_oppivelvollisuus and (not erityisen_tuen_paatos or (not vaikeasti_vammainen and not vammainen))) or (not pidennetty_oppivelvollisuus and (vaikeasti_vammainen or vammainen)))
""")

  protected def fromJoinWhereSqlPart(oppilaitosOidit: List[String], päivä: LocalDate)(implicit u: KoskiSpecificSession): Option[SQLActionBuilder] =
    Some(sql"""
    from r_opiskeluoikeus
    join r_henkilo on r_henkilo.oppija_oid = r_opiskeluoikeus.oppija_oid
    join esiopetus_opiskeluoik_aikajakso aikajakso on aikajakso.opiskeluoikeus_oid = r_opiskeluoikeus.opiskeluoikeus_oid
    join r_organisaatio_kieli on r_organisaatio_kieli.organisaatio_oid = oppilaitos_oid
    join r_koodisto_koodi
      on r_koodisto_koodi.koodisto_uri = split_part(r_organisaatio_kieli.kielikoodi, '_', 1)
      and r_koodisto_koodi.koodiarvo = split_part(split_part(r_organisaatio_kieli.kielikoodi, '_', 2), '#', 1)
    join r_organisaatio on r_organisaatio.organisaatio_oid = oppilaitos_oid
    left join r_paatason_suoritus on r_paatason_suoritus.opiskeluoikeus_oid = r_opiskeluoikeus.opiskeluoikeus_oid
    where (r_opiskeluoikeus.oppilaitos_oid = any($oppilaitosOidit) or r_opiskeluoikeus.koulutustoimija_oid = any($oppilaitosOidit))
      and r_opiskeluoikeus.koulutusmuoto = 'esiopetus'
      and aikajakso.alku <= $päivä
      and aikajakso.loppu >= $päivä
      and aikajakso.tila = 'lasna'
    -- access check
      and (
        #${(if (u.hasGlobalReadAccess) "true" else "false")}
        or
        r_opiskeluoikeus.oppilaitos_oid = any($käyttäjänOrganisaatioOidit)
        or
        (r_opiskeluoikeus.koulutustoimija_oid = any($käyttäjänKoulutustoimijaOidit))
      )
""")

  private def käyttäjänOrganisaatioOidit(implicit u: KoskiSpecificSession) = u.organisationOids(AccessType.read).toSeq

  private def käyttäjänKoulutustoimijaOidit(implicit u: KoskiSpecificSession) = u.varhaiskasvatusKäyttöoikeudet.toSeq
    .filter(_.organisaatioAccessType.contains(AccessType.read))
    .map(_.koulutustoimija.oid)

  protected def validateOids(oppilaitosOids: List[String]) = {
    val invalidOid = oppilaitosOids.find(oid => !isValidOrganisaatioOid(oid))
    if (invalidOid.isDefined) {
      throw new IllegalArgumentException(s"Invalid oppilaitos oid ${invalidOid.get}")
    }
    oppilaitosOids
  }
}
