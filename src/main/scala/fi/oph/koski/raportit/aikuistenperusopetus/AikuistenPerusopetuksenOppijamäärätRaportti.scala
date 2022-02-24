package fi.oph.koski.raportit.aikuistenperusopetus

import java.time.LocalDate
import fi.oph.koski.db.QueryMethods
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.koskiuser.{AccessType, KoskiSpecificSession}
import fi.oph.koski.organisaatio.OrganisaatioService
import fi.oph.koski.raportit.{Column, DataSheet}
import fi.oph.koski.db.DB
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.schema.Organisaatio.isValidOrganisaatioOid
import slick.jdbc.GetResult

import scala.concurrent.duration.DurationInt

case class AikuistenPerusopetuksenOppijamäärätRaportti(db: DB, organisaatioService: OrganisaatioService) extends QueryMethods {
  implicit private val getResult: GetResult[AikuistenPerusopetuksenOppijamäärätRaporttiRow] = GetResult(r =>
    AikuistenPerusopetuksenOppijamäärätRaporttiRow(
      oppilaitosOid = r.rs.getString("oppilaitos_oid"),
      oppilaitosNimi = r.rs.getString("oppilaitos_nimi"),
      opetuskieli = r.rs.getString("nimi"),
      oppilaidenMääräYhteensä = r.rs.getInt("oppilaidenMääräYhteensä"),
      oppilaidenMääräVOS = r.rs.getInt("oppilaidenMääräVOS"),
      oppilaidenMääräMuuKuinVOS = r.rs.getInt("oppilaidenMääräMuuKuinVOS"),
      oppimääränSuorittajiaYhteensä = r.rs.getInt("oppimääränSuorittajiaYhteensä"),
      oppimääränSuorittajiaVOS = r.rs.getInt("oppimääränSuorittajiaVOS"),
      oppimääränSuorittajiaMuuKuinVOS = r.rs.getInt("oppimääränSuorittajiaMuuKuinVOS"),
      aineopiskelijoitaYhteensä = r.rs.getInt("aineopiskelijoitaYhteensä"),
      aineopiskelijoitaVOS = r.rs.getInt("aineopiskelijoitaVOS"),
      aineopiskelijoitaMuuKuinVOS = r.rs.getInt("aineopiskelijoitaMuuKuinVOS"),
      vieraskielisiäYhteensä = r.rs.getInt("vieraskielisiäYhteensä"),
      vieraskielisiäVOS = r.rs.getInt("vieraskielisiäVOS"),
      vieraskielisiäMuuKuinVOS = r.rs.getInt("vieraskielisiäMuuKuinVOS")
    )
  )

  def build(oppilaitosOids: List[String], päivä: LocalDate, t: LocalizationReader)(implicit u: KoskiSpecificSession): DataSheet = {
    val raporttiQuery = query(validateOids(oppilaitosOids), päivä).as[AikuistenPerusopetuksenOppijamäärätRaporttiRow]
    val rows = runDbSync(raporttiQuery, timeout = 5.minutes)
    DataSheet(
      title = t.get("raportti-excel-oppimäärä-sheet-name"),
      rows = rows,
      columnSettings = columnSettings(t)
    )
  }

  private def query(oppilaitosOidit: List[String], päivä: LocalDate)(implicit u: KoskiSpecificSession) = {
    sql"""
    select
      r_opiskeluoikeus.oppilaitos_oid,
      r_opiskeluoikeus.oppilaitos_nimi,
      r_koodisto_koodi.nimi,
      count(distinct r_opiskeluoikeus.opiskeluoikeus_oid) as oppilaidenMääräYhteensä,
      count(distinct (case when opintojen_rahoitus = '1' then r_opiskeluoikeus.opiskeluoikeus_oid end)) as oppilaidenMääräVOS,
      count(distinct (case when opintojen_rahoitus = '6' then r_opiskeluoikeus.opiskeluoikeus_oid end)) as oppilaidenMääräMuuKuinVOS,
      count(distinct (case when r_paatason_suoritus.suorituksen_tyyppi in ('aikuistenperusopetuksenoppimaara', 'aikuistenperusopetuksenoppimaaranalkuvaihe') then r_opiskeluoikeus.opiskeluoikeus_oid end)) oppimääränSuorittajiaYhteensä,
      count(distinct (case when opintojen_rahoitus = '1' and r_paatason_suoritus.suorituksen_tyyppi in ('aikuistenperusopetuksenoppimaara', 'aikuistenperusopetuksenoppimaaranalkuvaihe') then r_opiskeluoikeus.opiskeluoikeus_oid end)) as oppimääränSuorittajiaVOS,
      count(distinct (case when opintojen_rahoitus = '6' and r_paatason_suoritus.suorituksen_tyyppi in ('aikuistenperusopetuksenoppimaara', 'aikuistenperusopetuksenoppimaaranalkuvaihe') then r_opiskeluoikeus.opiskeluoikeus_oid end)) as oppimääränSuorittajiaMuuKuinVOS,
      count(distinct (case when r_paatason_suoritus.suorituksen_tyyppi = 'perusopetuksenoppiaineenoppimaara' then r_opiskeluoikeus.opiskeluoikeus_oid end)) as aineopiskelijoitaYhteensä,
      count(distinct (case when opintojen_rahoitus = '1' and r_paatason_suoritus.suorituksen_tyyppi = 'perusopetuksenoppiaineenoppimaara' then r_opiskeluoikeus.opiskeluoikeus_oid end)) as aineopiskelijoitaVOS,
      count(distinct (case when opintojen_rahoitus = '6' and r_paatason_suoritus.suorituksen_tyyppi = 'perusopetuksenoppiaineenoppimaara' then r_opiskeluoikeus.opiskeluoikeus_oid end)) as aineopiskelijoitaMuuKuinVOS,
      count(distinct (case when aidinkieli not in('fi', 'sv', 'se', 'ri', 'vk') then r_opiskeluoikeus.opiskeluoikeus_oid end)) as vieraskielisiäYhteensä,
      count(distinct (case when aidinkieli not in('fi', 'sv', 'se', 'ri', 'vk') and opintojen_rahoitus = '1' then r_opiskeluoikeus.opiskeluoikeus_oid end)) as vieraskielisiäVOS,
      count(distinct (case when aidinkieli not in('fi', 'sv', 'se', 'ri', 'vk') and opintojen_rahoitus = '6' then r_opiskeluoikeus.opiskeluoikeus_oid end)) as vieraskielisiäMuuKuinVOS
    from r_opiskeluoikeus
    join r_henkilo on r_henkilo.oppija_oid = r_opiskeluoikeus.oppija_oid
    join r_opiskeluoikeus_aikajakso aikajakso on aikajakso.opiskeluoikeus_oid = r_opiskeluoikeus.opiskeluoikeus_oid
    join r_organisaatio_kieli on r_organisaatio_kieli.organisaatio_oid = oppilaitos_oid
    join r_paatason_suoritus on r_paatason_suoritus.opiskeluoikeus_oid = r_opiskeluoikeus.opiskeluoikeus_oid
    join r_koodisto_koodi
      on r_koodisto_koodi.koodisto_uri = split_part(r_organisaatio_kieli.kielikoodi, '_', 1)
      and r_koodisto_koodi.koodiarvo = split_part(split_part(r_organisaatio_kieli.kielikoodi, '_', 2), '#', 1)
    join r_organisaatio on r_organisaatio.organisaatio_oid = oppilaitos_oid
    where r_opiskeluoikeus.oppilaitos_oid = any($oppilaitosOidit)
      and r_opiskeluoikeus.koulutusmuoto = 'aikuistenperusopetus'
      and aikajakso.alku <= $päivä
      and aikajakso.loppu >= $päivä
      and aikajakso.tila = 'lasna'
      and r_opiskeluoikeus.sisaltyy_opiskeluoikeuteen_oid is null
    -- access check
      and (
        #${(if (u.hasGlobalReadAccess) "true" else "false")}
        or
        r_opiskeluoikeus.oppilaitos_oid = any($käyttäjänOrganisaatioOidit)
        or
        r_opiskeluoikeus.koulutustoimija_oid = any($käyttäjänKoulutustoimijaOidit)
      )
    group by r_opiskeluoikeus.oppilaitos_oid, r_opiskeluoikeus.oppilaitos_nimi, r_koodisto_koodi.nimi
  """
  }

  private def käyttäjänOrganisaatioOidit(implicit u: KoskiSpecificSession) = u.organisationOids(AccessType.read).toSeq

  private def käyttäjänKoulutustoimijaOidit(implicit u: KoskiSpecificSession) = u.varhaiskasvatusKäyttöoikeudet.toSeq
    .filter(_.organisaatioAccessType.contains(AccessType.read))
    .map(_.koulutustoimija.oid)

  private def validateOids(oppilaitosOids: List[String]) = {
    val invalidOid = oppilaitosOids.find(oid => !isValidOrganisaatioOid(oid))
    if (invalidOid.isDefined) {
      throw new IllegalArgumentException(s"Invalid oppilaitos oid ${invalidOid.get}")
    }
    oppilaitosOids
  }

  def columnSettings(t: LocalizationReader): Seq[(String, Column)] = Seq(
    "oppilaitosOid" -> Column(t.get("raportti-excel-kolumni-oppilaitosOid")),
    "oppilaitosNimi" -> Column(t.get("raportti-excel-kolumni-oppilaitoksenNimi")),
    "opetuskieli" -> Column(t.get("raportti-excel-kolumni-opetuskieli"), comment = Some(t.get("raportti-excel-kolumni-opetuskieli-comment"))),
    "oppilaidenMääräYhteensä" -> Column(t.get("raportti-excel-kolumni-opiskelijoita"), comment = Some(t.get("raportti-excel-kolumni-opiskelijoita-AikuistenPerusopetus-comment"))),
    "oppilaidenMääräVOS" -> Column(t.get("raportti-excel-kolumni-oppilaidenMääräVOS"), comment = Some(t.get("raportti-excel-kolumni-oppilaidenMääräVOS-comment"))),
    "oppilaidenMääräMuuKuinVOS" -> Column(t.get("raportti-excel-kolumni-oppilaidenMääräMuuKuinVOS"), comment = Some(t.get("raportti-excel-kolumni-oppilaidenMääräMuuKuinVOS-comment"))),
    "oppimääränSuorittajiaYhteensä" -> Column(t.get("raportti-excel-kolumni-oppimääränSuorittajiaYhteensä-AikuistenPerusopetus"), comment = Some(t.get("raportti-excel-kolumni-oppimääränSuorittajiaYhteensä-AikuistenPerusopetus-comment"))),
    "oppimääränSuorittajiaVOS" -> Column(t.get("raportti-excel-kolumni-oppimääränSuorittajiaVOS-AikuistenPerusopetus"), comment = Some(t.get("raportti-excel-kolumni-oppimääränSuorittajiaVOS-AikuistenPerusopetus-comment"))),
    "oppimääränSuorittajiaMuuKuinVOS" -> Column(t.get("raportti-excel-kolumni-oppimääränSuorittajiaMuuKuinVOS-AikuistenPerusopetus"), comment = Some(t.get("raportti-excel-kolumni-oppimääränSuorittajiaMuuKuinVOS-AikuistenPerusopetus-comment"))),
    "aineopiskelijoitaYhteensä" -> Column(t.get("raportti-excel-kolumni-aineopiskelijoitaYhteensä-AikuistenPerusopetus"), comment = Some(t.get("raportti-excel-kolumni-aineopiskelijoitaYhteensä-AikuistenPerusopetus-comment"))),
    "aineopiskelijoitaVOS" -> Column(t.get("raportti-excel-kolumni-aineopiskelijoitaVOS-AikuistenPerusopetus"), comment = Some(t.get("raportti-excel-kolumni-aineopiskelijoitaVOS-AikuistenPerusopetus-comment"))),
    "aineopiskelijoitaMuuKuinVOS" -> Column(t.get("raportti-excel-kolumni-aineopiskelijoitaMuuKuinVOS-AikuistenPerusopetus"), comment = Some(t.get("raportti-excel-kolumni-aineopiskelijoitaMuuKuinVOS-AikuistenPerusopetus-comment"))),
    "vieraskielisiäYhteensä" -> Column(t.get("raportti-excel-kolumni-vieraskielisiä-opiskelijoita"), comment = Some(t.get("raportti-excel-kolumni-vieraskielisiä-opiskelijoita-comment"))),
    "vieraskielisiäVOS" -> Column(t.get("raportti-excel-kolumni-vieraskielisiäVOS-opiskelijoita"), comment = Some(t.get("raportti-excel-kolumni-vieraskielisiäVOS-opiskelijoita-comment"))),
    "vieraskielisiäMuuKuinVOS" -> Column(t.get("raportti-excel-kolumni-vieraskielisiäMuuKuinVOS-opiskelijoita"), comment = Some(t.get("raportti-excel-kolumni-vieraskielisiäMuuKuinVOS-opiskelijoita-comment"))),
  )
}

case class AikuistenPerusopetuksenOppijamäärätRaporttiRow(
  oppilaitosOid: String,
  oppilaitosNimi: String,
  opetuskieli: String,
  oppilaidenMääräYhteensä: Int,
  oppilaidenMääräVOS: Int,
  oppilaidenMääräMuuKuinVOS: Int,
  oppimääränSuorittajiaYhteensä: Int,
  oppimääränSuorittajiaVOS: Int,
  oppimääränSuorittajiaMuuKuinVOS: Int,
  aineopiskelijoitaYhteensä: Int,
  aineopiskelijoitaVOS: Int,
  aineopiskelijoitaMuuKuinVOS: Int,
  vieraskielisiäYhteensä: Int,
  vieraskielisiäVOS: Int,
  vieraskielisiäMuuKuinVOS: Int
)
