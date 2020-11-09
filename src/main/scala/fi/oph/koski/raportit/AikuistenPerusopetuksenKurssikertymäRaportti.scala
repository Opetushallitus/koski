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
      oppilaitosOid = r.rs.getString("oppilaitos_oid"),
      oppilaitos =  r.rs.getString("oppilaitos_nimi"),
      yhteensäSuorituksia = r.rs.getInt("yhteensäSuorituksia"),
      yhteensäSuoritettujaSuorituksia = r.rs.getInt("yhteensäSuoritettujaSuorituksia"),
      yhteensäTunnistettujaSuorituksia = r.rs.getInt("yhteensäTunnistettujaSuorituksia"),
      yhteensäTunnistettujaSuorituksiaRahoituksenPiirissä = r.rs.getInt("yhteensäTunnistettujaSuorituksiaRahoituksenPiirissä"),
      päättövaiheenSuorituksia = r.rs.getInt("päättövaiheenSuorituksia"),
      päättövaiheenSuoritettujaSuorituksia = r.rs.getInt("päättövaiheenSuoritettujaSuorituksia"),
      päättövaiheenTunnistettujaSuorituksia = r.rs.getInt("päättövaiheenTunnistettujaSuorituksia"),
      päättövaiheenTunnistettujaSuorituksiaRahoituksenPiirissä = r.rs.getInt("päättövaiheenTunnistettujaSuorituksiaRahoituksenPiirissä"),
      alkuvaiheenSuorituksia = r.rs.getInt("alkuvaiheenSuorituksia"),
      alkuvaiheenSuoritettujaSuorituksia = r.rs.getInt("alkuvaiheenSuoritettujaSuorituksia"),
      alkuvaiheenTunnistettujaSuorituksia = r.rs.getInt("alkuvaiheenTunnistettujaSuorituksia"),
      alkuvaiheenTunnistettujaSuorituksiaRahoituksenPiirissä = r.rs.getInt("alkuvaiheenTunnistettujaSuorituksiaRahoituksenPiirissä")
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
              r_opiskeluoikeus.oppilaitos_oid,
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
          oppilaitos_oid oppilaitos_oid,
          oppilaitos_nimi oppilaitos_nimi,
          count(*) yhteensäSuorituksia,
          count(*) filter (where tunnustettu = false) yhteensäSuoritettujaSuorituksia,
          count(*) filter (where tunnustettu) yhteensäTunnistettujaSuorituksia,
          count(*) filter (where tunnustettu_rahoituksen_piirissa) yhteensäTunnistettujaSuorituksiaRahoituksenPiirissä,
          count(*) filter (where suorituksen_tyyppi = 'aikuistenperusopetuksenkurssi') päättövaiheenSuorituksia,
          count(*) filter (where tunnustettu = false and suorituksen_tyyppi = 'aikuistenperusopetuksenkurssi') päättövaiheenSuoritettujaSuorituksia,
          count(*) filter (where tunnustettu and suorituksen_tyyppi = 'aikuistenperusopetuksenkurssi') päättövaiheenTunnistettujaSuorituksia,
          count(*) filter (where tunnustettu_rahoituksen_piirissa and suorituksen_tyyppi = 'aikuistenperusopetuksenkurssi') päättövaiheenTunnistettujaSuorituksiaRahoituksenPiirissä,
          count(*) filter (where suorituksen_tyyppi = 'aikuistenperusopetuksenalkuvaiheenkurssi') alkuvaiheenSuorituksia,
          count(*) filter (where tunnustettu = false and suorituksen_tyyppi = 'aikuistenperusopetuksenalkuvaiheenkurssi') alkuvaiheenSuoritettujaSuorituksia,
          count(*) filter (where tunnustettu and suorituksen_tyyppi = 'aikuistenperusopetuksenalkuvaiheenkurssi') alkuvaiheenTunnistettujaSuorituksia,
          count(*) filter (where tunnustettu_rahoituksen_piirissa and suorituksen_tyyppi = 'aikuistenperusopetuksenalkuvaiheenkurssi') alkuvaiheenTunnistettujaSuorituksiaRahoituksenPiirissä
        from paatason_suoritus
        join r_osasuoritus on (paatason_suoritus.paatason_suoritus_id = r_osasuoritus.paatason_suoritus_id or opiskeluoikeus_oid = r_osasuoritus.sisaltyy_opiskeluoikeuteen_oid)
        where (r_osasuoritus.suorituksen_tyyppi = 'aikuistenperusopetuksenkurssi' or r_osasuoritus.suorituksen_tyyppi = 'aikuistenperusopetuksenalkuvaiheenkurssi')
          and r_osasuoritus.arviointi_paiva >= $aikaisintaan
          and r_osasuoritus.arviointi_paiva <= $viimeistaan
        group by paatason_suoritus.oppilaitos_nimi, paatason_suoritus.oppilaitos_oid;
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
    "oppilaitosOid" -> Column("Oppilaitoksen oid-tunniste"),
    "oppilaitos" -> Column("Oppilaitos"),
    "yhteensäSuorituksia" -> Column("Kurssikertymä yhteensä", comment = Some("Kaikki sellaiset aikuisten perusopetuksen oppimäärän tai alkuvaiheen suorituksen sisältä löytyvät kurssit, joiden arviointipäivämäärä osuu tulostusparametreissa määritellyn aikajakson sisään.")),
    "yhteensäSuoritettujaSuorituksia" -> Column("Suoritetut kurssit”", comment = Some("Kaikki sellaiset aikuisten perusopetuksen oppimäärän tai alkuvaiheen suorituksen sisältä löytyvät kurssit, joiden arviointipäivämäärä osuu tulostusparametreissa määritellyn aikajakson sisään ja joita ei ole merkitty tunnustetuiksi.")),
    "yhteensäTunnistettujaSuorituksia" -> Column("Tunnustetut kurssit", comment = Some("Kaikki sellaiset aikuisten perusopetuksen oppimäärän tai alkuvaiheen suorituksen sisältä löytyvät kurssit, joiden arviointipäivämäärä osuu tulostusparametreissa määritellyn aikajakson sisään ja jotka on merkitty tunnustetuiksi.")),
    "yhteensäTunnistettujaSuorituksiaRahoituksenPiirissä" -> Column("Tunnustetut kurssit - rahoituksen piirissä", comment = Some("Kaikki sellaiset aikuisten perusopetuksen oppimäärän tai alkuvaiheen suorituksen sisältä löytyvät kurssit, joiden arviointipäivämäärä osuu tulostusparametreissa määritellyn aikajakson sisään, jotka on merkitty tunnustetuiksi ja jotka on merkitty rahoituksen piirissä oleviksi.")),
    "päättövaiheenSuorituksia" -> Column("Kurssikertymä yhteensä - päättövaihe", comment = Some("Kaikki sellaiset aikuisten perusopetuksen oppimäärän suorituksen sisältä löytyvät kurssit, joiden arviointipäivämäärä osuu tulostusparametreissa määritellyn aikajakson sisään.")),
    "päättövaiheenSuoritettujaSuorituksia" -> Column("Suoritetut kurssit - päättövaihe”", comment = Some("Kaikki sellaiset aikuisten perusopetuksen oppimäärän suorituksen sisältä löytyvät kurssit, joiden arviointipäivämäärä osuu tulostusparametreissa määritellyn aikajakson sisään ja joita ei ole merkitty tunnustetuiksi.")),
    "päättövaiheenTunnistettujaSuorituksia" -> Column("Tunnustetut kurssit - päättövaihe", comment = Some("Kaikki sellaiset aikuisten perusopetuksen oppimäärän suorituksen sisältä löytyvät kurssit, joiden arviointipäivämäärä osuu tulostusparametreissa määritellyn aikajakson sisään ja jotka on merkitty tunnustetuiksi.")),
    "päättövaiheenTunnistettujaSuorituksiaRahoituksenPiirissä" -> Column("Tunnustetut kurssit - rahoituksen piirissä - päättövaihe", comment = Some("Kaikki sellaiset aikuisten perusopetuksen oppimäärän suorituksen sisältä löytyvät kurssit, joiden arviointipäivämäärä osuu tulostusparametreissa määritellyn aikajakson sisään, jotka on merkitty tunnustetuiksi ja jotka on merkitty rahoituksen piirissä oleviksi.")),
    "alkuvaiheenSuorituksia" -> Column("Kurssikertymä yhteensä - alkuvaihe", comment = Some("Kaikki sellaiset aikuisten perusopetuksen alkuvaiheen suorituksen sisältä löytyvät kurssit, joiden arviointipäivämäärä osuu tulostusparametreissa määritellyn aikajakson sisään.")),
    "alkuvaiheenSuoritettujaSuorituksia" -> Column("Suoritetut kurssit - alkuvaihe", comment = Some("Kaikki sellaiset aikuisten perusopetuksen alkuvaiheen suorituksen sisältä löytyvät kurssit, joiden arviointipäivämäärä osuu tulostusparametreissa määritellyn aikajakson sisään ja joita ei ole merkitty tunnustetuiksi.")),
    "alkuvaiheenTunnistettujaSuorituksia" -> Column("Tunnustetut kurssit - alkuvaihe", comment = Some("Kaikki sellaiset aikuisten perusopetuksen alkuvaiheen suorituksen sisältä löytyvät kurssit, joiden arviointipäivämäärä osuu tulostusparametreissa määritellyn aikajakson sisään ja jotka on merkitty tunnustetuiksi.")),
    "alkuvaiheenTunnistettujaSuorituksiaRahoituksenPiirissä" -> Column("Tunnustetut kurssit - rahoituksen piirissä - alkuvaihe", comment = Some("Kaikki sellaiset aikuisten perusopetuksen alkuvaiheen suorituksen sisältä löytyvät kurssit, joiden arviointipäivämäärä osuu tulostusparametreissa määritellyn aikajakson sisään, jotka on merkitty tunnustetuiksi ja jotka on merkitty rahoituksen piirissä oleviksi.")),
  )
}

case class AikuistenPerusopetuksenKurssikertymäRaporttiRow(
   oppilaitosOid: String,
   oppilaitos: String,
   yhteensäSuorituksia: Int,
   yhteensäSuoritettujaSuorituksia: Int,
   yhteensäTunnistettujaSuorituksia: Int,
   yhteensäTunnistettujaSuorituksiaRahoituksenPiirissä: Int,
   päättövaiheenSuorituksia: Int,
   päättövaiheenSuoritettujaSuorituksia: Int,
   päättövaiheenTunnistettujaSuorituksia: Int,
   päättövaiheenTunnistettujaSuorituksiaRahoituksenPiirissä: Int,
   alkuvaiheenSuorituksia: Int,
   alkuvaiheenSuoritettujaSuorituksia: Int,
   alkuvaiheenTunnistettujaSuorituksia: Int,
   alkuvaiheenTunnistettujaSuorituksiaRahoituksenPiirissä: Int
)
