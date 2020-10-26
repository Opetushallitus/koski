package fi.oph.koski.raportit

import java.sql.Date

import fi.oph.koski.db.KoskiDatabaseMethods
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.koskiuser.{AccessType, KoskiSession}
import fi.oph.koski.organisaatio.OrganisaatioService
import fi.oph.koski.raportointikanta.RaportointiDatabase.DB
import fi.oph.koski.schema.Organisaatio.isValidOrganisaatioOid
import fi.oph.koski.util.SQL
import fi.oph.koski.util.SQL.toSqlListUnsafe
import slick.jdbc.GetResult

import scala.concurrent.duration._

case class AikuistenPerusopetuksenKurssikertymäRaportti(db: DB, organisaatioService: OrganisaatioService) extends KoskiDatabaseMethods {
  implicit private val getResult: GetResult[AikuistenPerusopetuksenKurssikertymäRaporttiRow] = GetResult(r =>
    AikuistenPerusopetuksenKurssikertymäRaporttiRow(
      oppilaitos = r.<<,
      yhteensäSuorituksia = r.<<,
      yhteensäTunnistettujaSuorituksia = r.<<,
      yhteensäTunnistettujaSuorituksiaRahoituksenPiirissä = r.<<,
      päättövaiheenSuorituksia = r.<<,
      päättövaiheenTunnistettujaSuorituksia = r.<<,
      päättövaiheenTunnistettujaSuorituksiaRahoituksenPiirissä = r.<<,
      alkuvaiheenSuorituksia = r.<<,
      alkuvaiheenTunnistettujaSuorituksia = r.<<,
      alkuvaiheenTunnistettujaSuorituksiaRahoituksenPiirissä = r.<<
    )
  )

  def build(oppilaitosOids: List[String], aikaisintaan: Date, viimeistaan: Date)(implicit u: KoskiSession): DataSheet = {
    val raporttiQuery = query(validateOids(oppilaitosOids), aikaisintaan, viimeistaan).as[AikuistenPerusopetuksenKurssikertymäRaporttiRow]
    val rows = runDbSync(raporttiQuery, timeout = 5.minutes)
    DataSheet(
      title = "Suoritukset",
      rows = rows,
      columnSettings = columnSettings
    )
  }

  private def query(oppilaitosOidit: List[String], aikaisintaan: Date, viimeistaan: Date)(implicit u: KoskiSession) = {
    sql"""
SELECT oo.oppilaitos_nimi,
	count(*) filter(WHERE tunnustettu = false) yhteensä_suoritettuja,
	count(*) filter(WHERE tunnustettu) yhteensä_tunnustettuja,
	count(*) filter(WHERE tunnustettu_rahoituksen_piirissa) yhteensä_tunnustettuja_rahoituksen_piirissa,
	count(*) filter(WHERE tunnustettu = false
		AND osasuoritukset.suorituksen_tyyppi = 'aikuistenperusopetuksenkurssi') päättövaiheen_suoritettuja,
	count(*) filter(WHERE tunnustettu
		AND osasuoritukset.suorituksen_tyyppi = 'aikuistenperusopetuksenkurssi') päättövaiheen_tunnustettuja,
	count(*) filter(WHERE tunnustettu_rahoituksen_piirissa
		AND osasuoritukset.suorituksen_tyyppi = 'aikuistenperusopetuksenkurssi') päättövaiheen_tunnustettuja_rahoituksen_piirissa,
	count(*) filter(WHERE tunnustettu = false
		AND osasuoritukset.suorituksen_tyyppi = 'aikuistenperusopetuksenalkuvaiheenkurssi') alkuvaiheen_suoritettuja,
	count(*) filter(WHERE tunnustettu
		AND osasuoritukset.suorituksen_tyyppi = 'aikuistenperusopetuksenalkuvaiheenkurssi') alkuvaiheen_tunnustettuja,
	count(*) filter(WHERE tunnustettu_rahoituksen_piirissa
		AND osasuoritukset.suorituksen_tyyppi = 'aikuistenperusopetuksenalkuvaiheenkurssi') alkuvaiheen_tunnustettuja_rahoituksen_piirissa
FROM r_opiskeluoikeus AS oo
JOIN r_opiskeluoikeus_aikajakso AS ooa ON ooa.opiskeluoikeus_oid = oo.opiskeluoikeus_oid
JOIN r_paatason_suoritus AS pts ON oo.opiskeluoikeus_oid = pts.opiskeluoikeus_oid
LEFT JOIN lateral(SELECT DISTINCT * FROM (
		SELECT DISTINCT CASE
				WHEN os_oo.sisaltyy_opiskeluoikeuteen_oid IS NULL
					AND NOT EXISTS (
						SELECT DISTINCT b.sisaltyy_opiskeluoikeuteen_oid
						FROM r_opiskeluoikeus AS b
						WHERE b.sisaltyy_opiskeluoikeuteen_oid IS NOT NULL
							AND b.sisaltyy_opiskeluoikeuteen_oid = os_oo.opiskeluoikeus_oid
						)
					THEN os_oo.opiskeluoikeus_oid
				WHEN os_oo.sisaltyy_opiskeluoikeuteen_oid IS NOT NULL
					THEN os_oo.sisaltyy_opiskeluoikeuteen_oid
				END AS oo_oid,
			os_oo_os.osasuoritus_id,
			os_oo_os.suorituksen_tyyppi,
			os_oo_os.arviointi_paiva,
			os_oo_os.tunnustettu,
			os_oo_os.tunnustettu_rahoituksen_piirissa,
			os_oo_os.arviointi_hyvaksytty
		FROM r_opiskeluoikeus AS os_oo
		JOIN r_osasuoritus AS os_oo_os ON os_oo.opiskeluoikeus_oid = os_oo_os.opiskeluoikeus_oid
		WHERE os_oo.koulutusmuoto = 'aikuistenperusopetus'
		) AS osasuoritukset1 WHERE osasuoritukset1.oo_oid = oo.opiskeluoikeus_oid) AS osasuoritukset ON true
JOIN r_organisaatio AS org ON oo.koulutustoimija_oid = org.organisaatio_oid
WHERE oo.koulutusmuoto = 'aikuistenperusopetus'
	AND pts.suorituksen_tyyppi = 'aikuistenperusopetuksenoppimaara'
	AND (
		osasuoritukset.suorituksen_tyyppi = 'aikuistenperusopetuksenkurssi'
		OR osasuoritukset.suorituksen_tyyppi = 'aikuistenperusopetuksenalkuvaiheenkurssi'
		)
	AND osasuoritukset.arviointi_paiva BETWEEN $aikaisintaan
		AND $viimeistaan
	AND (
		oo.oppilaitos_oid IN (#${SQL.toSqlListUnsafe(oppilaitosOidit) })
		OR oo.koulutustoimija_oid IN (#${SQL.toSqlListUnsafe(oppilaitosOidit) })
		)
GROUP BY oo.oppilaitos_nimi
ORDER BY oo.oppilaitos_nimi
  """
  }

  private def validateOids(oppilaitosOids: List[String]) = {
    val invalidOid = oppilaitosOids.find(oid => !isValidOrganisaatioOid(oid))
    if (invalidOid.isDefined) {
      throw new IllegalArgumentException(s"Invalid oppilaitos oid ${invalidOid.get}")
    }
    oppilaitosOids
  }

  val columnSettings: Seq[(String, Column)] = Seq(
    "oppilaitos" -> Column("Oppilaitos"),
    "yhteensäSuorituksia" -> Column("yhteensäSuorituksia"),
    "yhteensäTunnistettujaSuorituksia" -> Column("yhteensäTunnistettujaSuorituksia"),
    "yhteensäTunnistettujaSuorituksiaRahoituksenPiirissä" -> Column("yhteensäTunnistettujaSuorituksiaRahoituksenPiirissä", comment = Some("\"Läsnä\"-tilaiset aikuisten perusopetuksen opiskeluoikeudet raportin tulostusparametreissa määriteltynä päivänä.")),
    "päättövaiheenSuorituksia" -> Column("päättövaiheenSuorituksia", comment = Some("\"Läsnä\"-tilaiset aikuisten perusopetuksen opiskeluoikeudet raportin tulostusparametreissa määriteltynä päivänä.")),
    "päättövaiheenTunnistettujaSuorituksia" -> Column("päättövaiheenTunnistettujaSuorituksia", comment = Some("\"Läsnä\"-tilaiset aikuisten perusopetuksen opiskeluoikeudet raportin tulostusparametreissa määriteltynä päivänä.")),
    "päättövaiheenTunnistettujaSuorituksiaRahoituksenPiirissä" -> Column("päättövaiheenTunnistettujaSuorituksiaRahoituksenPiirissä", comment = Some("\"Läsnä\"-tilaiset aikuisten perusopetuksen opiskeluoikeudet raportin tulostusparametreissa määriteltynä päivänä.")),
    "alkuvaiheenSuorituksia" -> Column("alkuvaiheenSuorituksia", comment = Some("\"Läsnä\"-tilaiset aikuisten perusopetuksen opiskeluoikeudet raportin tulostusparametreissa määriteltynä päivänä.")),
    "alkuvaiheenTunnistettujaSuorituksia" -> Column("alkuvaiheenTunnistettujaSuorituksia", comment = Some("\"Läsnä\"-tilaiset aikuisten perusopetuksen opiskeluoikeudet raportin tulostusparametreissa määriteltynä päivänä.")),
    "alkuvaiheenTunnistettujaSuorituksiaRahoituksenPiirissä" -> Column("alkuvaiheenTunnistettujaSuorituksiaRahoituksenPiirissä"),
  )
}

case class AikuistenPerusopetuksenKurssikertymäRaporttiRow(
   oppilaitos: String,
   yhteensäSuorituksia: Int,
   yhteensäTunnistettujaSuorituksia: Int,
   yhteensäTunnistettujaSuorituksiaRahoituksenPiirissä: Int,
   päättövaiheenSuorituksia: Int,
   päättövaiheenTunnistettujaSuorituksia: Int,
   päättövaiheenTunnistettujaSuorituksiaRahoituksenPiirissä: Int,
   alkuvaiheenSuorituksia: Int,
   alkuvaiheenTunnistettujaSuorituksia: Int,
   alkuvaiheenTunnistettujaSuorituksiaRahoituksenPiirissä: Int
)
