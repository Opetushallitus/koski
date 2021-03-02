package fi.oph.koski.raportit.aikuistenperusopetus

import java.time.LocalDate

import fi.oph.koski.db.KoskiDatabaseMethods
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.raportit.{Column, DataSheet}
import fi.oph.koski.raportointikanta.RaportointiDatabase.DB

import slick.jdbc.GetResult
import scala.concurrent.duration.DurationInt

case class AikuistenPerusopetuksenAineopiskelijoidenKurssikertymät(db: DB) extends KoskiDatabaseMethods {
  implicit private val getResult: GetResult[AikuistenPerusopetuksenAineopiskelijoidenKurssikertymätRow] = GetResult(r =>
    AikuistenPerusopetuksenAineopiskelijoidenKurssikertymätRow(
      oppilaitosOid = r.rs.getString("oppilaitos_oid"),
      oppilaitos =  r.rs.getString("oppilaitos_nimi"),
      yhteensäSuorituksia = r.rs.getInt("yhteensä_suorituksia"),
      yhteensäSuoritettujaSuorituksia = r.rs.getInt("yhteensä_suoritettuja_suorituksia"),
      yhteensäTunnistettujaSuorituksia = r.rs.getInt("yhteensä_tunnistettuja_suorituksia"),
      yhteensäTunnistettujaSuorituksiaRahoituksenPiirissä = r.rs.getInt("yhteensä_tunnistettuja_suorituksia_rahoituksen_piirissä"),
      päättövaiheenSuorituksia = r.rs.getInt("päättövaiheen_suorituksia"),
      päättövaiheenSuoritettujaSuorituksia = r.rs.getInt("päättövaiheen_suoritettuja_suorituksia"),
      päättövaiheenTunnistettujaSuorituksia = r.rs.getInt("päättövaiheen_tunnistettuja_suorituksia"),
      päättövaiheenTunnistettujaSuorituksiaRahoituksenPiirissä = r.rs.getInt("päättövaiheen_tunnistettuja_suorituksia_rahoituksen_piiriss"),
      alkuvaiheenSuorituksia = r.rs.getInt("alkuvaiheen_suorituksia"),
      alkuvaiheenSuoritettujaSuorituksia = r.rs.getInt("alkuvaiheen_suoritettuja_suorituksia"),
      alkuvaiheenTunnistettujaSuorituksia = r.rs.getInt("alkuvaiheen_tunnistettuja_suorituksia"),
      alkuvaiheenTunnistettujaSuorituksiaRahoituksenPiirissä = r.rs.getInt("alkuvaiheen_tunnistettuja_suorituksia_rahoituksen_piirissä"),
      suoritetutTaiRahoituksenPiirissäTunnustetutMuutaKauttaRahoitetut = r.rs.getInt("muuta_kautta_rahoitetut"),
      suoritetutTaiRahoituksenPiirissäTunnustetutEiRahoitusTietoa = r.rs.getInt("ei_rahoitustietoa"),
      suoritetutTaiRahoituksenPiirissäTunnustetutArviointipäiväEiTiedossa = r.rs.getInt("arviointipäivä_ei_opiskeluoikeuden_sisällä"),
    )
  )

  def build(oppilaitosOids: List[String], aikaisintaan: LocalDate, viimeistaan: LocalDate)(implicit u: KoskiSpecificSession): DataSheet = {
    val raporttiQuery = query(oppilaitosOids, aikaisintaan, viimeistaan).as[AikuistenPerusopetuksenAineopiskelijoidenKurssikertymätRow]
    val rows = runDbSync(raporttiQuery, timeout = 5.minutes)
    DataSheet(
      title = "Aineopiskelijat",
      rows = rows,
      columnSettings = columnSettings
    )
  }

  private def query(oppilaitosOidit: List[String], aikaisintaan: LocalDate, viimeistaan: LocalDate)(implicit u: KoskiSpecificSession) = {
    sql"""
      with paatason_suoritus as (
        select
          r_opiskeluoikeus.oppilaitos_oid,
          r_opiskeluoikeus.oppilaitos_nimi,
          r_paatason_suoritus.paatason_suoritus_id,
          r_opiskeluoikeus.opiskeluoikeus_oid oo_opiskeluoikeus_oid,
          r_opiskeluoikeus.sisaltyy_opiskeluoikeuteen_oid,
          r_opiskeluoikeus.viimeisin_tila,
          r_opiskeluoikeus.alkamispaiva oo_alkamisaiva,
          r_opiskeluoikeus.paattymispaiva oo_paattymispaiva
        from r_opiskeluoikeus
        join r_paatason_suoritus on r_opiskeluoikeus.opiskeluoikeus_oid = r_paatason_suoritus.opiskeluoikeus_oid
          and r_opiskeluoikeus.sisaltyy_opiskeluoikeuteen_oid is null
        where (oppilaitos_oid = any($oppilaitosOidit) or koulutustoimija_oid = any($oppilaitosOidit))
          and (r_paatason_suoritus.suorituksen_tyyppi = 'perusopetuksenoppiaineenoppimaara')
      )
      select kurssikertymat.*,
      coalesce(opiskeluoikeuden_ulkopuoliset.arviointipäivä_ei_opiskeluoikeuden_sisällä, 0) as arviointipäivä_ei_opiskeluoikeuden_sisällä
      from (
        select
          oppilaitos_oid oppilaitos_oid,
          oppilaitos_nimi oppilaitos_nimi,
          count(distinct r_osasuoritus.osasuoritus_id) yhteensä_suorituksia,
          count(distinct (case when tunnustettu = false then r_osasuoritus.osasuoritus_id end)) yhteensä_suoritettuja_suorituksia,
          count(distinct (case when tunnustettu then r_osasuoritus.osasuoritus_id end)) yhteensä_tunnistettuja_suorituksia,
          count(distinct (case when tunnustettu_rahoituksen_piirissa then r_osasuoritus.osasuoritus_id end)) yhteensä_tunnistettuja_suorituksia_rahoituksen_piirissä,
          count(distinct (case when suorituksen_tyyppi = 'aikuistenperusopetuksenkurssi' then r_osasuoritus.osasuoritus_id end)) päättövaiheen_suorituksia,
          count(distinct (case when tunnustettu = false and suorituksen_tyyppi = 'aikuistenperusopetuksenkurssi' then r_osasuoritus.osasuoritus_id end)) päättövaiheen_suoritettuja_suorituksia,
          count(distinct (case when tunnustettu and suorituksen_tyyppi = 'aikuistenperusopetuksenkurssi' then r_osasuoritus.osasuoritus_id end)) päättövaiheen_tunnistettuja_suorituksia,
          count(distinct (case when tunnustettu_rahoituksen_piirissa and suorituksen_tyyppi = 'aikuistenperusopetuksenkurssi' then r_osasuoritus.osasuoritus_id end)) päättövaiheen_tunnistettuja_suorituksia_rahoituksen_piiriss,
          count(distinct (case when suorituksen_tyyppi = 'aikuistenperusopetuksenalkuvaiheenkurssi' then r_osasuoritus.osasuoritus_id end)) alkuvaiheen_suorituksia,
          count(distinct (case when tunnustettu = false and suorituksen_tyyppi = 'aikuistenperusopetuksenalkuvaiheenkurssi' then r_osasuoritus.osasuoritus_id end)) alkuvaiheen_suoritettuja_suorituksia,
          count(distinct (case when tunnustettu and suorituksen_tyyppi = 'aikuistenperusopetuksenalkuvaiheenkurssi' then r_osasuoritus.osasuoritus_id end)) alkuvaiheen_tunnistettuja_suorituksia,
          count(distinct (case when tunnustettu_rahoituksen_piirissa and suorituksen_tyyppi = 'aikuistenperusopetuksenalkuvaiheenkurssi' then r_osasuoritus.osasuoritus_id end)) alkuvaiheen_tunnistettuja_suorituksia_rahoituksen_piirissä,
          count(distinct (case when (tunnustettu = false or tunnustettu_rahoituksen_piirissa = true) and r_opiskeluoikeus_aikajakso.opiskeluoikeus_oid is not null and r_opiskeluoikeus_aikajakso.opintojen_rahoitus = '6' then r_osasuoritus.osasuoritus_id end)) muuta_kautta_rahoitetut,
          count(distinct (case when (tunnustettu = false or tunnustettu_rahoituksen_piirissa = true) and r_opiskeluoikeus_aikajakso.opiskeluoikeus_oid is not null and r_opiskeluoikeus_aikajakso.opintojen_rahoitus is null then r_osasuoritus.osasuoritus_id end)) ei_rahoitustietoa
        from paatason_suoritus
        left join r_opiskeluoikeus_aikajakso on oo_opiskeluoikeus_oid = r_opiskeluoikeus_aikajakso.opiskeluoikeus_oid
        join r_osasuoritus on (paatason_suoritus.paatason_suoritus_id = r_osasuoritus.paatason_suoritus_id or oo_opiskeluoikeus_oid = r_osasuoritus.sisaltyy_opiskeluoikeuteen_oid)
          and r_opiskeluoikeus_aikajakso.alku <= r_osasuoritus.arviointi_paiva
          --- tämän tarkoitus on saada eronnut-tilan alkamisen kanssa samana päivänä arvioidut kurssit edelliselle aikajaksolle
          and ((case when viimeisin_tila = 'eronnut' then r_opiskeluoikeus_aikajakso.loppu - interval '1 day' else r_opiskeluoikeus_aikajakso.loppu end) >= r_osasuoritus.arviointi_paiva or r_opiskeluoikeus_aikajakso.loppu = '9999-12-30')
        where (r_osasuoritus.suorituksen_tyyppi = 'aikuistenperusopetuksenkurssi' or r_osasuoritus.suorituksen_tyyppi = 'aikuistenperusopetuksenalkuvaiheenkurssi')
          and r_osasuoritus.arviointi_paiva >= $aikaisintaan
          and r_osasuoritus.arviointi_paiva <= $viimeistaan
          and r_osasuoritus.arviointi_arvosana_koodiarvo != 'O'
        group by paatason_suoritus.oppilaitos_nimi, paatason_suoritus.oppilaitos_oid
      ) kurssikertymat
      --- aikajaksojen ulkopuoliset suoritukset
      left join (
        select
          oppilaitos_oid oppilaitos_oid,
          oppilaitos_nimi oppilaitos_nimi,
          count(distinct (case when (tunnustettu = false or tunnustettu_rahoituksen_piirissa = true) then r_osasuoritus.osasuoritus_id end)) arviointipäivä_ei_opiskeluoikeuden_sisällä
        from paatason_suoritus
        join r_opiskeluoikeus_aikajakso on oo_opiskeluoikeus_oid = r_opiskeluoikeus_aikajakso.opiskeluoikeus_oid
        join r_osasuoritus on paatason_suoritus.paatason_suoritus_id = r_osasuoritus.paatason_suoritus_id or oo_opiskeluoikeus_oid = r_osasuoritus.sisaltyy_opiskeluoikeuteen_oid
        where (r_osasuoritus.suorituksen_tyyppi = 'aikuistenperusopetuksenkurssi' or r_osasuoritus.suorituksen_tyyppi = 'aikuistenperusopetuksenalkuvaiheenkurssi')
          and r_osasuoritus.arviointi_paiva >= $aikaisintaan
          and r_osasuoritus.arviointi_paiva <= $viimeistaan
          and r_osasuoritus.arviointi_arvosana_koodiarvo != 'O'
          and (oo_alkamisaiva > r_osasuoritus.arviointi_paiva
            or (oo_paattymispaiva < r_osasuoritus.arviointi_paiva and viimeisin_tila = 'valmistunut'))
        group by paatason_suoritus.oppilaitos_nimi, paatason_suoritus.oppilaitos_oid
      ) opiskeluoikeuden_ulkopuoliset
      on opiskeluoikeuden_ulkopuoliset.oppilaitos_oid = kurssikertymat.oppilaitos_oid;
  """
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
