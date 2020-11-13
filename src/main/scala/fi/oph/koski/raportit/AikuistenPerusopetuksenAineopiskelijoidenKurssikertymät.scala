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

case class AikuistenPerusopetuksenAineopiskelijoidenKurssikertymät(db: DB, organisaatioService: OrganisaatioService) extends KoskiDatabaseMethods {
  implicit private val getResult: GetResult[AikuistenPerusopetuksenAineopiskelijoidenKurssikertymätRow] = GetResult(r =>
    AikuistenPerusopetuksenAineopiskelijoidenKurssikertymätRow(
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
      alkuvaiheenTunnistettujaSuorituksiaRahoituksenPiirissä = r.rs.getInt("alkuvaiheenTunnistettujaSuorituksiaRahoituksenPiirissä"),
      suoritetutTaiRahoituksenPiirissäTunnustetutMuutaKauttaRahoitetut = r.rs.getInt("suoritetutTaiRahoituksenPiirissäTunnustetutMuutaKauttaRahoitet"),
      suoritetutTaiRahoituksenPiirissäTunnustetutEiRahoitusTietoa = r.rs.getInt("suoritetutTaiRahoituksenPiirissäTunnustetutEiRahoitusTietoa"),
      suoritetutTaiRahoituksenPiirissäTunnustetutArviointipäiväEiTiedossa = r.rs.getInt("suoritetutTaiRahoituksenPiirissäTunnustetutArviointipäiväEiT"),
    )
  )

  def build(oppilaitosOids: List[String], aikaisintaan: LocalDate, viimeistaan: LocalDate)(implicit u: KoskiSession): DataSheet = {
    val raporttiQuery = query(validateOids(oppilaitosOids), aikaisintaan, viimeistaan).as[AikuistenPerusopetuksenAineopiskelijoidenKurssikertymätRow]
    val rows = runDbSync(raporttiQuery, timeout = 5.minutes)
    DataSheet(
      title = "Aineopiskelijat",
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
          r_opiskeluoikeus.opiskeluoikeus_oid oo_opiskeluoikeus_oid,
          r_opiskeluoikeus.sisaltyy_opiskeluoikeuteen_oid,
          r_opiskeluoikeus.viimeisin_tila
        from r_opiskeluoikeus
        join r_paatason_suoritus on r_opiskeluoikeus.opiskeluoikeus_oid = r_paatason_suoritus.opiskeluoikeus_oid
          and r_opiskeluoikeus.sisaltyy_opiskeluoikeuteen_oid is null
        where (oppilaitos_oid = any($oppilaitosOidit) or koulutustoimija_oid = any($oppilaitosOidit))
          and (r_paatason_suoritus.suorituksen_tyyppi = 'perusopetuksenoppiaineenoppimaara')
          and exists (
            select 1
            from r_opiskeluoikeus_aikajakso
            where r_opiskeluoikeus.opiskeluoikeus_oid = r_opiskeluoikeus_aikajakso.opiskeluoikeus_oid
          )
      )
      select kurssikertymat.*,
      coalesce(opiskeluoikeuden_ulkopuoliset.suoritetutTaiRahoituksenPiirissäTunnustetutArviointipäiväEiTiedossa, 0) as suoritetutTaiRahoituksenPiirissäTunnustetutArviointipäiväEiTiedossa
      from (
        select
          oppilaitos_oid oppilaitos_oid,
          oppilaitos_nimi oppilaitos_nimi,
          count(distinct r_osasuoritus.osasuoritus_id) yhteensäSuorituksia,
          count(distinct (case when tunnustettu = false then r_osasuoritus.osasuoritus_id end)) yhteensäSuoritettujaSuorituksia,
          count(distinct (case when tunnustettu then r_osasuoritus.osasuoritus_id end)) yhteensäTunnistettujaSuorituksia,
          count(distinct (case when tunnustettu_rahoituksen_piirissa then r_osasuoritus.osasuoritus_id end)) yhteensäTunnistettujaSuorituksiaRahoituksenPiirissä,
          count(distinct (case when suorituksen_tyyppi = 'aikuistenperusopetuksenkurssi' then r_osasuoritus.osasuoritus_id end)) päättövaiheenSuorituksia,
          count(distinct (case when tunnustettu = false and suorituksen_tyyppi = 'aikuistenperusopetuksenkurssi' then r_osasuoritus.osasuoritus_id end)) päättövaiheenSuoritettujaSuorituksia,
          count(distinct (case when tunnustettu and suorituksen_tyyppi = 'aikuistenperusopetuksenkurssi' then r_osasuoritus.osasuoritus_id end)) päättövaiheenTunnistettujaSuorituksia,
          count(distinct (case when tunnustettu_rahoituksen_piirissa and suorituksen_tyyppi = 'aikuistenperusopetuksenkurssi' then r_osasuoritus.osasuoritus_id end)) päättövaiheenTunnistettujaSuorituksiaRahoituksenPiirissä,
          count(distinct (case when suorituksen_tyyppi = 'aikuistenperusopetuksenalkuvaiheenkurssi' then r_osasuoritus.osasuoritus_id end)) alkuvaiheenSuorituksia,
          count(distinct (case when tunnustettu = false and suorituksen_tyyppi = 'aikuistenperusopetuksenalkuvaiheenkurssi' then r_osasuoritus.osasuoritus_id end)) alkuvaiheenSuoritettujaSuorituksia,
          count(distinct (case when tunnustettu and suorituksen_tyyppi = 'aikuistenperusopetuksenalkuvaiheenkurssi' then r_osasuoritus.osasuoritus_id end)) alkuvaiheenTunnistettujaSuorituksia,
          count(distinct (case when tunnustettu_rahoituksen_piirissa and suorituksen_tyyppi = 'aikuistenperusopetuksenalkuvaiheenkurssi' then r_osasuoritus.osasuoritus_id end)) alkuvaiheenTunnistettujaSuorituksiaRahoituksenPiirissä,
          count(distinct (case when (tunnustettu = false or tunnustettu_rahoituksen_piirissa = true) and r_opiskeluoikeus_aikajakso.opintojen_rahoitus = '6' then r_osasuoritus.osasuoritus_id end)) suoritetutTaiRahoituksenPiirissäTunnustetutMuutaKauttaRahoitetut,
          count(distinct (case when (tunnustettu = false or tunnustettu_rahoituksen_piirissa = true) and r_opiskeluoikeus_aikajakso.opintojen_rahoitus is null then r_osasuoritus.osasuoritus_id end)) suoritetutTaiRahoituksenPiirissäTunnustetutEiRahoitusTietoa
        from paatason_suoritus
        join r_opiskeluoikeus_aikajakso aikajakso on aikajakso.opiskeluoikeus_oid = oo_opiskeluoikeus_oid
        join r_opiskeluoikeus_aikajakso on oo_opiskeluoikeus_oid = r_opiskeluoikeus_aikajakso.opiskeluoikeus_oid
        join r_osasuoritus on (paatason_suoritus.paatason_suoritus_id = r_osasuoritus.paatason_suoritus_id or oo_opiskeluoikeus_oid = r_osasuoritus.sisaltyy_opiskeluoikeuteen_oid)
          and r_opiskeluoikeus_aikajakso.alku >= $aikaisintaan
          and r_opiskeluoikeus_aikajakso.loppu <= $viimeistaan
        where (r_osasuoritus.suorituksen_tyyppi = 'aikuistenperusopetuksenkurssi' or r_osasuoritus.suorituksen_tyyppi = 'aikuistenperusopetuksenalkuvaiheenkurssi')
          and r_osasuoritus.arviointi_paiva >= $aikaisintaan
          and r_osasuoritus.arviointi_paiva <= $viimeistaan
        group by paatason_suoritus.oppilaitos_nimi, paatason_suoritus.oppilaitos_oid
      ) kurssikertymat
      left join (
        select
          oppilaitos_oid oppilaitos_oid,
          oppilaitos_nimi oppilaitos_nimi,
          count(distinct (case when (tunnustettu = false or tunnustettu_rahoituksen_piirissa = true) and r_opiskeluoikeus_aikajakso.opintojen_rahoitus is null then r_osasuoritus.osasuoritus_id end)) suoritetutTaiRahoituksenPiirissäTunnustetutArviointipäiväEiTiedossa
        from paatason_suoritus
        join r_opiskeluoikeus_aikajakso aikajakso on aikajakso.opiskeluoikeus_oid = oo_opiskeluoikeus_oid
        join r_opiskeluoikeus_aikajakso on oo_opiskeluoikeus_oid = r_opiskeluoikeus_aikajakso.opiskeluoikeus_oid
        join r_osasuoritus on paatason_suoritus.paatason_suoritus_id = r_osasuoritus.paatason_suoritus_id or oo_opiskeluoikeus_oid = r_osasuoritus.sisaltyy_opiskeluoikeuteen_oid
        where (r_osasuoritus.suorituksen_tyyppi = 'aikuistenperusopetuksenkurssi' or r_osasuoritus.suorituksen_tyyppi = 'aikuistenperusopetuksenalkuvaiheenkurssi')
          and r_osasuoritus.arviointi_paiva >= $aikaisintaan
          and r_osasuoritus.arviointi_paiva <= $viimeistaan
          and viimeisin_tila = 'valmistunut'
          and not exists (
            select 1
            from r_opiskeluoikeus
            where oo_opiskeluoikeus_oid = r_opiskeluoikeus.opiskeluoikeus_oid
              and r_opiskeluoikeus.alkamispaiva >= $aikaisintaan
              and r_opiskeluoikeus.paattymispaiva <= $viimeistaan
          )
        group by paatason_suoritus.oppilaitos_nimi, paatason_suoritus.oppilaitos_oid
      ) opiskeluoikeuden_ulkopuoliset
      on opiskeluoikeuden_ulkopuoliset.oppilaitos_oid = kurssikertymat.oppilaitos_oid;

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
    "yhteensäSuoritettujaSuorituksia" -> Column("Suoritetut kurssit", comment = Some("Kaikki sellaiset aikuisten perusopetuksen oppimäärän tai alkuvaiheen suorituksen sisältä löytyvät kurssit, joiden arviointipäivämäärä osuu tulostusparametreissa määritellyn aikajakson sisään ja joita ei ole merkitty tunnustetuiksi.")),
    "yhteensäTunnistettujaSuorituksia" -> Column("Tunnustetut kurssit", comment = Some("Kaikki sellaiset aikuisten perusopetuksen oppimäärän tai alkuvaiheen suorituksen sisältä löytyvät kurssit, joiden arviointipäivämäärä osuu tulostusparametreissa määritellyn aikajakson sisään ja jotka on merkitty tunnustetuiksi.")),
    "yhteensäTunnistettujaSuorituksiaRahoituksenPiirissä" -> Column("Tunnustetut kurssit - rahoituksen piirissä", comment = Some("Kaikki sellaiset aikuisten perusopetuksen oppimäärän tai alkuvaiheen suorituksen sisältä löytyvät kurssit, joiden arviointipäivämäärä osuu tulostusparametreissa määritellyn aikajakson sisään, jotka on merkitty tunnustetuiksi ja jotka on merkitty rahoituksen piirissä oleviksi.")),
    "päättövaiheenSuorituksia" -> Column("Kurssikertymä yhteensä - päättövaihe", comment = Some("Kaikki sellaiset aikuisten perusopetuksen oppimäärän suorituksen sisältä löytyvät kurssit, joiden arviointipäivämäärä osuu tulostusparametreissa määritellyn aikajakson sisään.")),
    "päättövaiheenSuoritettujaSuorituksia" -> Column("Suoritetut kurssit - päättövaihe", comment = Some("Kaikki sellaiset aikuisten perusopetuksen oppimäärän suorituksen sisältä löytyvät kurssit, joiden arviointipäivämäärä osuu tulostusparametreissa määritellyn aikajakson sisään ja joita ei ole merkitty tunnustetuiksi.")),
    "päättövaiheenTunnistettujaSuorituksia" -> Column("Tunnustetut kurssit - päättövaihe", comment = Some("Kaikki sellaiset aikuisten perusopetuksen oppimäärän suorituksen sisältä löytyvät kurssit, joiden arviointipäivämäärä osuu tulostusparametreissa määritellyn aikajakson sisään ja jotka on merkitty tunnustetuiksi.")),
    "päättövaiheenTunnistettujaSuorituksiaRahoituksenPiirissä" -> Column("Tunnustetut kurssit - rahoituksen piirissä - päättövaihe", comment = Some("Kaikki sellaiset aikuisten perusopetuksen oppimäärän suorituksen sisältä löytyvät kurssit, joiden arviointipäivämäärä osuu tulostusparametreissa määritellyn aikajakson sisään, jotka on merkitty tunnustetuiksi ja jotka on merkitty rahoituksen piirissä oleviksi.")),
    "alkuvaiheenSuorituksia" -> Column("Kurssikertymä yhteensä - alkuvaihe", comment = Some("Kaikki sellaiset aikuisten perusopetuksen alkuvaiheen suorituksen sisältä löytyvät kurssit, joiden arviointipäivämäärä osuu tulostusparametreissa määritellyn aikajakson sisään.")),
    "alkuvaiheenSuoritettujaSuorituksia" -> Column("Suoritetut kurssit - alkuvaihe", comment = Some("Kaikki sellaiset aikuisten perusopetuksen alkuvaiheen suorituksen sisältä löytyvät kurssit, joiden arviointipäivämäärä osuu tulostusparametreissa määritellyn aikajakson sisään ja joita ei ole merkitty tunnustetuiksi.")),
    "alkuvaiheenTunnistettujaSuorituksia" -> Column("Tunnustetut kurssit - alkuvaihe", comment = Some("Kaikki sellaiset aikuisten perusopetuksen alkuvaiheen suorituksen sisältä löytyvät kurssit, joiden arviointipäivämäärä osuu tulostusparametreissa määritellyn aikajakson sisään ja jotka on merkitty tunnustetuiksi.")),
    "alkuvaiheenTunnistettujaSuorituksiaRahoituksenPiirissä" -> Column("Tunnustetut kurssit - rahoituksen piirissä - alkuvaihe", comment = Some("Kaikki sellaiset aikuisten perusopetuksen alkuvaiheen suorituksen sisältä löytyvät kurssit, joiden arviointipäivämäärä osuu tulostusparametreissa määritellyn aikajakson sisään, jotka on merkitty tunnustetuiksi ja jotka on merkitty rahoituksen piirissä oleviksi.")),
    "suoritetutTaiRahoituksenPiirissäTunnustetutMuutaKauttaRahoitetut" -> Column("Suoritetut tai rahoituksen piirissä oleviksi merkityt tunnustetut kurssit - muuta kautta rahoitetut", comment = Some("Sellaiset suoritetut tai rahoituksen piirissä oleviksi merkityt tunnustetut kurssit, joiden arviointipäivä osuu muuta kautta rahoitetun läsnäolojakson sisälle. Kurssien tunnistetiedot löytyvät välilehdeltä \"Muuta kautta rah.\"")),
    "suoritetutTaiRahoituksenPiirissäTunnustetutEiRahoitusTietoa" -> Column("Suoritetut tai rahoituksen piirissä oleviksi merkityt tunnustetut kurssit, joilla ei rahoitustietoa", comment = Some("Sellaiset suoritetut tai rahoituksen piirissä oleviksi merkityt tunnustetut pakolliset tai valtakunnalliset syventävät kurssit, joiden arviointipäivä osuus sellaiselle tilajaksolle, jolta ei löydy tietoa rahoitusmuodosta. Kurssien tunnistetiedot löytyvät välilehdeltä \"Ei rahoitusmuotoa\".")),
    "suoritetutTaiRahoituksenPiirissäTunnustetutArviointipäiväEiTiedossa" -> Column("Suoritetut tai rahoituksen piirissä oleviksi merkityt tunnustetut kurssit – arviointipäivä ei opiskeluoikeuden sisällä", comment = Some("Suoritetut tai rahoituksen piirissä oleviksi merkityt tunnustetut kurssit, joiden arviointipäivä on aikaisemmin kuin opiskeluoikeuden alkamispäivä tai joiden arviointipäivä on myöhemmin kuin \"Valmistunut\"-tilan päivä. Kurssien tunnistetiedot löytyvät välilehdeltä \"Opiskeluoikeuden ulkop.\".")),
  )
}

case class AikuistenPerusopetuksenAineopiskelijoidenKurssikertymätRow(
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
   alkuvaiheenTunnistettujaSuorituksiaRahoituksenPiirissä: Int,
   suoritetutTaiRahoituksenPiirissäTunnustetutMuutaKauttaRahoitetut: Int,
   suoritetutTaiRahoituksenPiirissäTunnustetutEiRahoitusTietoa: Int,
   suoritetutTaiRahoituksenPiirissäTunnustetutArviointipäiväEiTiedossa: Int
)
