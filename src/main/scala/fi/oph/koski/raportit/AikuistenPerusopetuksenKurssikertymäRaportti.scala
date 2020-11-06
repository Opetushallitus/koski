package fi.oph.koski.raportit

import java.time.LocalDate

import fi.oph.koski.db.KoskiDatabaseMethods
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.organisaatio.OrganisaatioService
import fi.oph.koski.raportointikanta.RaportointiDatabase.DB
import fi.oph.koski.schema.Organisaatio.isValidOrganisaatioOid
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

  def build(oppilaitosOids: List[String], aikaisintaan: LocalDate, viimeistaan: LocalDate)(implicit u: KoskiSession): DataSheet = {
    val raporttiQuery = query(validateOids(oppilaitosOids), aikaisintaan, viimeistaan).as[AikuistenPerusopetuksenKurssikertymäRaporttiRow]
    val rows = runDbSync(raporttiQuery, timeout = 5.minutes)
    DataSheet(
      title = "Suoritukset",
      rows = rows,
      columnSettings = columnSettings
    )
  }

  private def query(oppilaitosOidit: List[String], aikaisintaan: LocalDate, viimeistaan: LocalDate)(implicit u: KoskiSession) = {
    sql"""
      with paatason_suoritus as (
            select
              r_opiskeluoikeus.oppilaitos_nimi,
              r_paatason_suoritus.paatason_suoritus_id,
              r_opiskeluoikeus.sisaltyy_opiskeluoikeuteen_oid
            from r_opiskeluoikeus
            join r_paatason_suoritus on r_opiskeluoikeus.opiskeluoikeus_oid = r_paatason_suoritus.opiskeluoikeus_oid
            where
              oppilaitos_oid = any($oppilaitosOidit)
              and (r_paatason_suoritus.suorituksen_tyyppi = 'aikuistenperusopetuksenoppimaara' or r_paatason_suoritus.suorituksen_tyyppi = 'aikuistenperusopetuksenoppimaaranalkuvaihe')
              and exists (
                select 1 from r_opiskeluoikeus_aikajakso
                where r_opiskeluoikeus.opiskeluoikeus_oid = r_opiskeluoikeus_aikajakso.opiskeluoikeus_oid
                  and r_opiskeluoikeus_aikajakso.tila = 'lasna'
                  and r_opiskeluoikeus_aikajakso.alku <= $viimeistaan
                  and r_opiskeluoikeus_aikajakso.loppu >= $aikaisintaan
              )
      ) select
          oppilaitos_nimi oppilaitos,
          count(*) filter (where tunnustettu = false) suoritettuja,
          count(*) filter (where tunnustettu) tunnustettuja,
          count(*) filter (where tunnustettu_rahoituksen_piirissa) tunnustettuja_rahoituksen_piirissa,
          count(*) filter (where tunnustettu = false and suorituksen_tyyppi = 'aikuistenperusopetuksenkurssi') suoritettuja,
          count(*) filter (where tunnustettu and suorituksen_tyyppi = 'aikuistenperusopetuksenkurssi') tunnustettuja,
          count(*) filter (where tunnustettu_rahoituksen_piirissa and suorituksen_tyyppi = 'aikuistenperusopetuksenkurssi') tunnustettuja_rahoituksen_piirissa,
          count(*) filter (where tunnustettu = false and suorituksen_tyyppi = 'aikuistenperusopetuksenalkuvaiheenkurssi') suoritettuja,
          count(*) filter (where tunnustettu and suorituksen_tyyppi = 'aikuistenperusopetuksenalkuvaiheenkurssi') tunnustettuja,
          count(*) filter (where tunnustettu_rahoituksen_piirissa and suorituksen_tyyppi = 'aikuistenperusopetuksenalkuvaiheenkurssi') tunnustettuja_rahoituksen_piirissa
        from paatason_suoritus
        join r_osasuoritus on (paatason_suoritus.paatason_suoritus_id = r_osasuoritus.paatason_suoritus_id or opiskeluoikeus_oid = r_osasuoritus.sisaltyy_opiskeluoikeuteen_oid)
        where (r_osasuoritus.suorituksen_tyyppi = 'aikuistenperusopetuksenkurssi' or r_osasuoritus.suorituksen_tyyppi = 'aikuistenperusopetuksenalkuvaiheenkurssi')
          and r_osasuoritus.arviointi_paiva >= $aikaisintaan
          and r_osasuoritus.arviointi_paiva <= $viimeistaan
        group by paatason_suoritus.oppilaitos_nimi;
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
