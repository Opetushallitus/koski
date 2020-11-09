package fi.oph.koski.raportit

import java.time.LocalDate

import fi.oph.koski.db.KoskiDatabaseMethods
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.koskiuser.{AccessType, KoskiSession}
import fi.oph.koski.organisaatio.OrganisaatioService
import fi.oph.koski.raportointikanta.RaportointiDatabase.DB
import fi.oph.koski.schema.Organisaatio.isValidOrganisaatioOid
import slick.jdbc.GetResult

import scala.concurrent.duration._

case class AikuistenPerusopetuksenOppijamäärätRaportti(db: DB, organisaatioService: OrganisaatioService) extends KoskiDatabaseMethods {
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

  def build(oppilaitosOids: List[String], päivä: LocalDate)(implicit u: KoskiSession): DataSheet = {
    val raporttiQuery = query(validateOids(oppilaitosOids), päivä).as[AikuistenPerusopetuksenOppijamäärätRaporttiRow]
    val rows = runDbSync(raporttiQuery, timeout = 5.minutes)
    DataSheet(
      title = "Suoritukset",
      rows = rows,
      columnSettings = columnSettings
    )
  }

  private def query(oppilaitosOidit: List[String], päivä: LocalDate)(implicit u: KoskiSession) = {
    sql"""
    select
      r_opiskeluoikeus.oppilaitos_oid,
      r_opiskeluoikeus.oppilaitos_nimi,
      r_koodisto_koodi.nimi,
      count(distinct r_opiskeluoikeus.opiskeluoikeus_oid) as oppilaidenMääräYhteensä,
      count(distinct (case when opintojen_rahoitus = '1' then r_opiskeluoikeus.opiskeluoikeus_oid end)) as oppilaidenMääräVOS,
      count(distinct (case when opintojen_rahoitus != '1' then r_opiskeluoikeus.opiskeluoikeus_oid end)) as oppilaidenMääräMuuKuinVOS,
      count(distinct (case when oppimaaran_suorittaja then r_opiskeluoikeus.opiskeluoikeus_oid end)) oppimääränSuorittajiaYhteensä,
      count(distinct (case when opintojen_rahoitus = '1' and oppimaaran_suorittaja then r_opiskeluoikeus.opiskeluoikeus_oid end)) as oppimääränSuorittajiaVOS,
      count(distinct (case when opintojen_rahoitus != '1' and oppimaaran_suorittaja then r_opiskeluoikeus.opiskeluoikeus_oid end)) as oppimääränSuorittajiaMuuKuinVOS,
      count(distinct (case when not oppimaaran_suorittaja then r_opiskeluoikeus.opiskeluoikeus_oid end)) as aineopiskelijoitaYhteensä,
      count(distinct (case when opintojen_rahoitus = '1' and not oppimaaran_suorittaja then r_opiskeluoikeus.opiskeluoikeus_oid end)) as aineopiskelijoitaVOS,
      count(distinct (case when opintojen_rahoitus != '1' and not oppimaaran_suorittaja then r_opiskeluoikeus.opiskeluoikeus_oid end)) as aineopiskelijoitaMuuKuinVOS,
      count(distinct (case when aidinkieli not in('fi', 'sv', 'se', 'ri', 'vk') then r_opiskeluoikeus.opiskeluoikeus_oid end)) as vieraskielisiäYhteensä,
      count(distinct (case when aidinkieli not in('fi', 'sv', 'se', 'ri', 'vk') and opintojen_rahoitus = '1' then r_opiskeluoikeus.opiskeluoikeus_oid end)) as vieraskielisiäVOS,
      count(distinct (case when aidinkieli not in('fi', 'sv', 'se', 'ri', 'vk') and opintojen_rahoitus != '1' then r_opiskeluoikeus.opiskeluoikeus_oid end)) as vieraskielisiäMuuKuinVOS
    from r_opiskeluoikeus
    join r_henkilo on r_henkilo.oppija_oid = r_opiskeluoikeus.oppija_oid
    join r_opiskeluoikeus_aikajakso aikajakso on aikajakso.opiskeluoikeus_oid = r_opiskeluoikeus.opiskeluoikeus_oid
    join r_organisaatio_kieli on r_organisaatio_kieli.organisaatio_oid = oppilaitos_oid
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

  private def käyttäjänOrganisaatioOidit(implicit u: KoskiSession) = u.organisationOids(AccessType.read).toSeq

  private def käyttäjänKoulutustoimijaOidit(implicit u: KoskiSession) = u.varhaiskasvatusKäyttöoikeudet.toSeq
    .filter(_.organisaatioAccessType.contains(AccessType.read))
    .map(_.koulutustoimija.oid)

  private def validateOids(oppilaitosOids: List[String]) = {
    val invalidOid = oppilaitosOids.find(oid => !isValidOrganisaatioOid(oid))
    if (invalidOid.isDefined) {
      throw new IllegalArgumentException(s"Invalid oppilaitos oid ${invalidOid.get}")
    }
    oppilaitosOids
  }

  val columnSettings: Seq[(String, Column)] = Seq(
    "oppilaitosOid" -> Column("Oppilaitoksen oid-tunniste"),
    "oppilaitosNimi" -> Column("Oppilaitos"),
    "opetuskieli" -> Column("Opetuskieli", comment = Some("Oppilaitokselle merkityt opetuskielet Opintopolun organisaatiopalvelussa.")),
    "oppilaidenMääräYhteensä" -> Column("Opiskelijoiden määrä yhteensä", comment = Some("\"Läsnä\"-tilaiset aikuisten perusopetuksen opiskeluoikeudet raportin tulostusparametreissa määriteltynä päivänä.")),
    "oppilaidenMääräVOS" -> Column("Opiskelijoista valtionosuusrahoitteisia", comment = Some("\"Läsnä\"-tilaisista opiskeluoikeuksista ne, joille on merkitty raportin tulostusparametreissa määritellylle päivälle osuvalle läsnäolojaksolle rahoitusmuodoksi \"Valtionosuusrahoitteinen koulutus\".")),
    "oppilaidenMääräMuuKuinVOS" -> Column("Opiskelijoista muuta kautta rahoitettuja", comment = Some("\"Läsnä\"-tilaisista opiskeluoikeuksista ne, joille on merkitty raportin tulostusparametreissa määritellylle päivälle osuvalle läsnäolojaksolle rahoitusmuodoksi \"Muuta kautta rahoitettu\".")),
    "oppimääränSuorittajiaYhteensä" -> Column("Opiskelijoista aikuisten perusopetuksen oppimäärän suorittajia", comment = Some("\"Läsnä\"-tilaisista opiskeluoikeuksista ne, joissa päätason suorituksena aikuisten perusopetuksen oppimäärä ja/tai alkuvaihe.")),
    "oppimääränSuorittajiaVOS" -> Column("Perusopetuksen oppimäärän suorittajista valtionosuusrahoitteisia", comment = Some("\"Läsnä\"-tilaisista opiskeluoikeuksista ne, joissa päätason suorituksena aikuisten perusopetuksen oppimäärä ja/tai alkuvaihe ja joille on merkitty raportin tulostusparametreissa määritellylle päivälle osuvalle läsnäolojaksolle rahoitusmuodoksi \"Valtionosuusrahoitteinen koulutus\".")),
    "oppimääränSuorittajiaMuuKuinVOS" -> Column("Perusopetuksen oppimäärän suorittajista muuta kautta rahoitettuja", comment = Some("\"Läsnä\"-tilaisista opiskeluoikeuksista ne, joissa päätason suorituksena aikuisten perusopetuksen oppimäärä ja/tai alkuvaihe ja joille on merkitty raportin tulostusparametreissa määritellylle päivälle osuvalle läsnäolojaksolle rahoitusmuodoksi \"Muuta kautta rahoitettu\".")),
    "aineopiskelijoitaYhteensä" -> Column("Opiskelijoista aineopiskelijoita", comment = Some("\"Läsnä\"-tilaisista opiskeluoikeuksista ne, joissa suoritetaan aikuisten perusopetuksen aineopintoja.")),
    "aineopiskelijoitaVOS" -> Column("Aineopiskelijoista valtionosuusrahoitteisia", comment = Some("\"Läsnä\"-tilaisista opiskeluoikeuksista ne, joissa suoritetaan aikuisten perusopetuksen aineopintoja ja joille on merkitty raportin tulostusparametreissa määritellylle päivälle osuvalle läsnäolojaksolle rahoitusmuodoksi \"Valtionosuusrahoitteinen koulutus\".")),
    "aineopiskelijoitaMuuKuinVOS" -> Column("Aineopiskelijoista muuta kautta rahoitettuja", comment = Some("\"Läsnä\"tilaisista opiskeluoikeuksista ne, joissa suoritetaan aikuisten perusopetuksen aineopintoja ja joille on merkitty raportin tulostusparametreissa määritellylle päivälle osuvalle läsnäolojaksolle rahoitusmuodoksi \"Muuta kautta rahoitettu\".")),
    "vieraskielisiäYhteensä" -> Column("Opiskelijoista vieraskielisiä", comment = Some("\"Läsnä\"-tilaisista opiskeluoikeuksista ne, joissa oppijan äidinkieli on jokin muu kuin suomi, ruotsi, saame, romani tai viittomakieli.")),
    "vieraskielisiäVOS" -> Column("Opiskelijoista vieraskielisiä - valtionosuusrahoitteiset", comment = Some("\"Läsnä\"-tilaisista opiskeluoikeuksista ne, joissa oppijan äidinkieli on jokin muu kuin suomi, ruotsi, saame, romani tai viittomakieli ja joille on merkitty raportin tulostusparametreissa määritellylle päivälle osuvalle läsnäolojaksolle rahoitusmuodoksi \"Valtionosuusrahoitteinen koulutus\".")),
    "vieraskielisiäMuuKuinVOS" -> Column("Opiskelijoista vieraskielisiä - muuta kautta rahoitetut", comment = Some("\"Läsnä\"-tilaisista opiskeluoikeuksista ne, joissa oppijan äidinkieli on jokin muu kuin suomi, ruotsi, saame, romani tai viittomakieli ja joille on merkitty raportin tulostusparametreissa määritellylle päivälle osuvalle läsnäolojaksolle rahoitusmuodoksi \"Muuta kautta rahoitettu\".")),
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
