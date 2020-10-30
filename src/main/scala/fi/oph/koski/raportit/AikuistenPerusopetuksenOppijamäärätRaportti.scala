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
      oppilaitosOid = r.<<,
      oppilaitosNimi = r.<<,
      opetuskieli = r.<<,
      oppilaidenMääräYhteensä = r.<<,
      oppilaidenMääräVOS = r.<<,
      oppilaidenMääräMuuKuinVOS = r.<<,
      oppimääränSuorittajiaYhteensä = r.<<,
      oppimääränSuorittajiaVOS = r.<<,
      oppimääränSuorittajiaMuuKuinVOS = r.<<,
      aineopiskelijoitaYhteensä = r.<<,
      aineopiskelijoitaVOS = r.<<,
      aineopiskelijoitaMuuKuinVOS = r.<<,
      vieraskielisiäYhteensä = r.<<,
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
      r_opiskeluoikeus.oppilaitos_oid,
      r_opiskeluoikeus.oppilaitos_nimi,
      r_koodisto_koodi.nimi,
      count(*) as oppilaidenMääräYhteensä,
      count(case when opintojen_rahoitus = '1' then 1 end) as oppilaidenMääräVOS,
      count(case when opintojen_rahoitus != '1' then 1 end) as oppilaidenMääräMuuKuinVOS,
      count(case when oppimaaran_suorittaja then 1 end) oppimääränSuorittajiaYhteensä,
      count(case when opintojen_rahoitus = '1' and oppimaaran_suorittaja then 1 end) as oppimääränSuorittajiaVOS,
      count(case when opintojen_rahoitus != '1' and oppimaaran_suorittaja then 1 end) as oppimääränSuorittajiaMuuKuinVOS,
      count(case when not oppimaaran_suorittaja then 1 end) as aineopiskelijoitaYhteensä,
      count(case when opintojen_rahoitus = '1' and not oppimaaran_suorittaja then 1 end) as aineopiskelijoitaVOS,
      count(case when opintojen_rahoitus != '1' and not oppimaaran_suorittaja then 1 end) as aineopiskelijoitaMuuKuinVOS,
      count(case when aidinkieli not in('fi', 'sv', 'se', 'ri', 'vk') then 1 end) as vieraskielisiäYhteensä,
      count(case when aidinkieli not in('fi', 'sv', 'se', 'ri', 'vk') and opintojen_rahoitus = '1' then 1 end) as vieraskielisiäVOS,
      count(case when aidinkieli not in('fi', 'sv', 'se', 'ri', 'vk') and opintojen_rahoitus != '1' then 1 end) as vieraskielisiäMuuKuinVOS
    from r_opiskeluoikeus
    join r_henkilo on r_henkilo.oppija_oid = r_opiskeluoikeus.oppija_oid
    join r_opiskeluoikeus_aikajakso aikajakso on aikajakso.opiskeluoikeus_oid = r_opiskeluoikeus.opiskeluoikeus_oid
    join r_organisaatio_kieli on r_organisaatio_kieli.organisaatio_oid = oppilaitos_oid
    join r_koodisto_koodi
      on r_koodisto_koodi.koodisto_uri = split_part(r_organisaatio_kieli.kielikoodi, '_', 1)
      and r_koodisto_koodi.koodiarvo = split_part(split_part(r_organisaatio_kieli.kielikoodi, '_', 2), '#', 1)
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
    group by r_opiskeluoikeus.oppilaitos_oid, r_opiskeluoikeus.oppilaitos_nimi, r_koodisto_koodi.nimi
  """
  }

  private def käyttäjänOrganisaatioOidit(implicit u: KoskiSession) = u.organisationOids(AccessType.read)

  private def käyttäjänKoulutustoimijaOidit(implicit u: KoskiSession) = u.varhaiskasvatusKäyttöoikeudet
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
