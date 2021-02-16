package fi.oph.koski.raportit.aikuistenperusopetus

import java.time.LocalDate

import fi.oph.koski.db.KoskiDatabaseMethods
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.organisaatio.OrganisaatioService
import fi.oph.koski.raportit.{Column, DataSheet}
import fi.oph.koski.raportointikanta.RaportointiDatabase.DB
import fi.oph.koski.schema.Organisaatio.isValidOrganisaatioOid
import slick.jdbc.GetResult

import scala.concurrent.duration._

case class AikuistenPerusopetuksenMuutaKauttaRahoitetutKurssit(db: DB) extends KoskiDatabaseMethods {
  implicit private val getResult: GetResult[AikuistenPerusopetuksenMuutaKauttaRahoitetutKurssitRow] = GetResult(r =>
    AikuistenPerusopetuksenMuutaKauttaRahoitetutKurssitRow(
      opiskeluoikeudenOid = r.rs.getString("opiskeluoikeuden_oid"),
      oppilaitos =  r.rs.getString("oppilaitos_nimi"),
      kurssikoodi = r.rs.getString("kurssikoodi"),
      kurssinNimi = r.rs.getString("kurssin_nimi"),
      kurssinSuorituksenTyyppi = r.rs.getString("kurssin_suorituksen_tyyppi"),
      päätasonSuorituksenTyyppi = r.rs.getString("paatason_suorituksen_tyyppi"),
      oppijaOid = r.rs.getString("oppija_oid"),
    )
  )

  def build(oppilaitosOids: List[String], aikaisintaan: LocalDate, viimeistaan: LocalDate)(implicit u: KoskiSession): DataSheet = {
    val raporttiQuery = query(oppilaitosOids, aikaisintaan, viimeistaan).as[AikuistenPerusopetuksenMuutaKauttaRahoitetutKurssitRow]
    val rows = runDbSync(raporttiQuery, timeout = 5.minutes)
    DataSheet(
      title = "Muuta kautta rah.",
      rows = rows,
      columnSettings = columnSettings
    )
  }

  private def query(oppilaitosOidit: List[String], aikaisintaan: LocalDate, viimeistaan: LocalDate)(implicit u: KoskiSession) = {
    sql"""
          with paatason_suoritus as (
            select
              r_opiskeluoikeus.oppija_oid,
              r_opiskeluoikeus.oppilaitos_oid,
              r_opiskeluoikeus.oppilaitos_nimi,
              r_paatason_suoritus.paatason_suoritus_id,
              r_opiskeluoikeus.opiskeluoikeus_oid oo_opiskeluoikeus_oid,
              r_opiskeluoikeus.sisaltyy_opiskeluoikeuteen_oid,
              r_opiskeluoikeus.viimeisin_tila,
              r_paatason_suoritus.suorituksen_tyyppi
            from r_opiskeluoikeus
            join r_paatason_suoritus on r_opiskeluoikeus.opiskeluoikeus_oid = r_paatason_suoritus.opiskeluoikeus_oid
              and r_opiskeluoikeus.sisaltyy_opiskeluoikeuteen_oid is null
            where (oppilaitos_oid = any($oppilaitosOidit) or koulutustoimija_oid = any($oppilaitosOidit))
              and (r_paatason_suoritus.suorituksen_tyyppi = 'aikuistenperusopetuksenoppimaara'
                or r_paatason_suoritus.suorituksen_tyyppi = 'aikuistenperusopetuksenoppimaaranalkuvaihe'
                or r_paatason_suoritus.suorituksen_tyyppi = 'perusopetuksenoppiaineenoppimaara')
            )
            select distinct on (r_osasuoritus.osasuoritus_id)
              oo_opiskeluoikeus_oid opiskeluoikeuden_oid,
              paatason_suoritus.oppija_oid,
              oppilaitos_nimi oppilaitos_nimi,
              r_osasuoritus.koulutusmoduuli_koodiarvo kurssikoodi,
              r_osasuoritus.koulutusmoduuli_nimi kurssin_nimi,
              paatason_suoritus.suorituksen_tyyppi as paatason_suorituksen_tyyppi,
              r_osasuoritus.suorituksen_tyyppi as kurssin_suorituksen_tyyppi
            from paatason_suoritus
            join r_osasuoritus on (paatason_suoritus.paatason_suoritus_id = r_osasuoritus.paatason_suoritus_id or oo_opiskeluoikeus_oid = r_osasuoritus.sisaltyy_opiskeluoikeuteen_oid)
            join r_opiskeluoikeus_aikajakso on oo_opiskeluoikeus_oid = r_opiskeluoikeus_aikajakso.opiskeluoikeus_oid
              and r_opiskeluoikeus_aikajakso.alku <= r_osasuoritus.arviointi_paiva
              --- tämän tarkoitus on saada eronnut-tilan alkamisen kanssa samana päivänä arvioidut kurssit edelliselle aikajaksolle
              and ((case when viimeisin_tila = 'eronnut' then r_opiskeluoikeus_aikajakso.loppu - interval '1 day' else r_opiskeluoikeus_aikajakso.loppu end) >= r_osasuoritus.arviointi_paiva or r_opiskeluoikeus_aikajakso.loppu = '9999-12-30')
                where (r_osasuoritus.suorituksen_tyyppi = 'aikuistenperusopetuksenkurssi'
                      or r_osasuoritus.suorituksen_tyyppi = 'aikuistenperusopetuksenalkuvaiheenkurssi')
                  and r_osasuoritus.arviointi_paiva >= $aikaisintaan
                  and r_osasuoritus.arviointi_paiva <= $viimeistaan
                  and (tunnustettu = false or tunnustettu_rahoituksen_piirissa = true)
                  and r_opiskeluoikeus_aikajakso.opintojen_rahoitus = '6'
                  and r_osasuoritus.arviointi_arvosana_koodiarvo != 'O'
  """
  }

  val columnSettings: Seq[(String, Column)] = Seq(
    "opiskeluoikeudenOid" -> Column("Opiskeluoikeuden oid"),
    "oppijaOid" -> Column("Oppijanumero"),
    "oppilaitos" -> Column("Oppilaitos"),
    "kurssikoodi" -> Column("Kurssikoodi"),
    "kurssinNimi" -> Column("Kurssin nimi"),
    "päätasonSuorituksenTyyppi" -> Column("Päätason suorituksen tyyppi"),
    "kurssinSuorituksenTyyppi" -> Column("Kurssin suorituksen tyyppi"),
  )
}

case class AikuistenPerusopetuksenMuutaKauttaRahoitetutKurssitRow(
  opiskeluoikeudenOid: String,
  oppijaOid: String,
  oppilaitos: String,
  kurssikoodi: String,
  kurssinNimi: String,
  päätasonSuorituksenTyyppi: String,
  kurssinSuorituksenTyyppi: String,
)
