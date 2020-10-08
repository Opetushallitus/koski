package fi.oph.koski.raportit

import java.sql.Date

import fi.oph.koski.db.KoskiDatabaseMethods
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.koskiuser.{AccessType, KoskiSession}
import fi.oph.koski.organisaatio.OrganisaatioService
import fi.oph.koski.raportointikanta.RaportointiDatabase.DB
import fi.oph.koski.schema.Organisaatio.isValidOrganisaatioOid
import fi.oph.koski.util.SQL.toSqlListUnsafe
import slick.jdbc.GetResult

import scala.concurrent.duration._

case class AikuistenPerusopetuksenOppijamäärätRaportti(db: DB, organisaatioService: OrganisaatioService) extends KoskiDatabaseMethods {
  implicit private val getResult: GetResult[AikuistenPerusopetuksenOppijamäärätRaporttiRow] = GetResult(r =>
    AikuistenPerusopetuksenOppijamäärätRaporttiRow(
      oppilaitosNimi = r.<<,
      opetuskieli = r.<<,
      oppilaidenMääräVOS = r.<<,
      oppilaidenMääräMuuKuinVOS = r.<<,
      oppimääränSuorittajiaVOS = r.<<,
      oppimääränSuorittajiaMuuKuinVOS = r.<<,
      aineopiskelijoitaVOS = r.<<,
      aineopiskelijoitaMuuKuinVOS = r.<<,
      vieraskielisiäVOS = r.<<,
      vieraskielisiäMuuKuinVOS = r.<<
    )
  )

  def build(oppilaitosOids: List[String], päivä: Date)(implicit u: KoskiSession): DataSheet = {
    val raporttiQuery = query(validateOids(oppilaitosOids), päivä).as[AikuistenPerusopetuksenOppijamäärätRaporttiRow]
    val rows = runDbSync(raporttiQuery, timeout = 5.minutes)
    DataSheet(
      title = "Suoritukset",
      rows = rows,
      columnSettings = columnSettings
    )
  }

  private def query(oppilaitosOidit: List[String], päivä: Date)(implicit u: KoskiSession) = {
    sql"""
    select
      r_opiskeluoikeus.oppilaitos_nimi,
      r_koodisto_koodi.nimi,
      count(case when opintojen_rahoitus = '1' then 1 end) as oppilaidenMääräVOS,
      count(case when opintojen_rahoitus != '1' then 1 end) as oppilaidenMääräMuuKuinVOS,
      count(case when opintojen_rahoitus = '1' and oppimaaran_suorittaja then 1 end) as oppimääränSuorittajiaVOS,
      count(case when opintojen_rahoitus != '1' and oppimaaran_suorittaja then 1 end) as oppimääränSuorittajiaMuuKuinVOS,
      count(case when opintojen_rahoitus = '1' and not oppimaaran_suorittaja then 1 end) as aineopiskelijoitaVOS,
      count(case when opintojen_rahoitus != '1' and not oppimaaran_suorittaja then 1 end) as aineopiskelijoitaMuuKuinVOS,
      count(case when aidinkieli not in('fi', 'sv', 'se', 'ri', 'vk') and opintojen_rahoitus = '1' then 1 end) as vieraskielisiäVOS,
      count(case when aidinkieli not in('fi', 'sv', 'se', 'ri', 'vk') and opintojen_rahoitus != '1' then 1 end) as vieraskielisiäMuuKuinVOS
    from r_opiskeluoikeus
    join r_henkilo on r_henkilo.oppija_oid = r_opiskeluoikeus.oppija_oid
    join r_opiskeluoikeus_aikajakso aikajakso on aikajakso.opiskeluoikeus_oid = r_opiskeluoikeus.opiskeluoikeus_oid
    join r_organisaatio_kieli on r_organisaatio_kieli.organisaatio_oid = oppilaitos_oid
    join r_koodisto_koodi on r_koodisto_koodi.koodisto_uri = split_part(split_part(kielikoodi, '#', 1), '_', 1) and r_koodisto_koodi.koodiarvo = split_part(kielikoodi, '#', 2)
    join r_organisaatio on r_organisaatio.organisaatio_oid = oppilaitos_oid
    left join r_paatason_suoritus on r_paatason_suoritus.opiskeluoikeus_oid = r_opiskeluoikeus.opiskeluoikeus_oid
    where r_opiskeluoikeus.oppilaitos_oid in (#${toSqlListUnsafe(oppilaitosOidit)})
      and r_opiskeluoikeus.koulutusmuoto = 'aikuistenperusopetus'
      and aikajakso.alku <= $päivä
      and aikajakso.loppu >= $päivä
      and aikajakso.tila = 'lasna'
      and r_opiskeluoikeus.sisaltyy_opiskeluoikeuteen_oid is null
    -- access check
      and (
        #${(if (u.hasGlobalReadAccess) "true" else "false")}
        or
        r_opiskeluoikeus.oppilaitos_oid in (#${toSqlListUnsafe(käyttäjänOrganisaatioOidit)})
        or
        r_opiskeluoikeus.koulutustoimija_oid in (#${toSqlListUnsafe(käyttäjänKoulutustoimijaOidit)})
      )
    group by r_opiskeluoikeus.oppilaitos_nimi, r_koodisto_koodi.nimi
  """
  }

  private def käyttäjänOrganisaatioOidit(implicit u: KoskiSession) = u.organisationOids(AccessType.read)

  private def käyttäjänKoulutustoimijaOidit(implicit u: KoskiSession) = u.varhaiskasvatusKäyttöoikeudet
    .filter(_.organisaatioAccessType.contains(AccessType.read))
    .map(_.koulutustoimija.oid)

  private def käyttäjänOstopalveluOidit(implicit u: KoskiSession) =
    organisaatioService.omatOstopalveluOrganisaatiot.map(_.oid)

  private def validateOids(oppilaitosOids: List[String]) = {
    val invalidOid = oppilaitosOids.find(oid => !isValidOrganisaatioOid(oid))
    if (invalidOid.isDefined) {
      throw new IllegalArgumentException(s"Invalid oppilaitos oid ${invalidOid.get}")
    }
    oppilaitosOids
  }

  val columnSettings: Seq[(String, Column)] = Seq(
    "oppilaitosNimi" -> Column("Oppilaitos"),
    "opetuskieli" -> Column("Opetuskieli"),
    "oppilaidenMääräVOS" -> Column("Oppilaiden VOS määrä", comment = Some("\"Läsnä\"-tilaiset aikuisten perusopetuksen opiskeluoikeudet raportin tulostusparametreissa määriteltynä päivänä.")),
    "oppilaidenMääräMuuKuinVOS" -> Column("Oppilaiden ei-VOS määrä", comment = Some("\"Läsnä\"-tilaiset aikuisten perusopetuksen opiskeluoikeudet raportin tulostusparametreissa määriteltynä päivänä.")),
    "oppimääränSuorittajiaVOS" -> Column("Oppimäärän suorittajien VOS määrä", comment = Some("\"Läsnä\"-tilaiset aikuisten perusopetuksen opiskeluoikeudet raportin tulostusparametreissa määriteltynä päivänä.")),
    "oppimääränSuorittajiaMuuKuinVOS" -> Column("Oppimäärän suorittajien ei-VOS määrä", comment = Some("\"Läsnä\"-tilaiset aikuisten perusopetuksen opiskeluoikeudet raportin tulostusparametreissa määriteltynä päivänä.")),
    "aineopiskelijoitaVOS" -> Column("Aineopiskelijoita VOS määrä", comment = Some("\"Läsnä\"-tilaiset aikuisten perusopetuksen opiskeluoikeudet raportin tulostusparametreissa määriteltynä päivänä.")),
    "aineopiskelijoitaMuuKuinVOS" -> Column("Aineopiskelijoita ei-VOS määrä", comment = Some("\"Läsnä\"-tilaiset aikuisten perusopetuksen opiskeluoikeudet raportin tulostusparametreissa määriteltynä päivänä.")),
    "vieraskielisiäVOS" -> Column("Oppilaista VOS-vieraskielisiä"),
    "vieraskielisiäMuuKuinVOS" -> Column("Oppilaista ei-VOS-vieraskielisiä"),
  )
}

case class AikuistenPerusopetuksenOppijamäärätRaporttiRow(
  oppilaitosNimi: String,
  opetuskieli: String,
  oppilaidenMääräVOS: Int,
  oppilaidenMääräMuuKuinVOS: Int,
  oppimääränSuorittajiaVOS: Int,
  oppimääränSuorittajiaMuuKuinVOS: Int,
  aineopiskelijoitaVOS: Int,
  aineopiskelijoitaMuuKuinVOS: Int,
  vieraskielisiäVOS: Int,
  vieraskielisiäMuuKuinVOS: Int
)
