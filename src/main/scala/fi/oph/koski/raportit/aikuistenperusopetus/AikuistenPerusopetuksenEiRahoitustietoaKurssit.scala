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

case class AikuistenPerusopetuksenEiRahoitustietoaKurssit(db: DB, organisaatioService: OrganisaatioService) extends KoskiDatabaseMethods {
  implicit private val getResult: GetResult[AikuistenPerusopetuksenEiRahoitustietoaKurssitRow] = GetResult(r =>
    AikuistenPerusopetuksenEiRahoitustietoaKurssitRow(
      opiskeluoikeudenOid = r.rs.getString("opiskeluoikeuden_oid"),
      oppilaitos =  r.rs.getString("oppilaitos_nimi"),
      kurssikoodi = r.rs.getString("kurssikoodi"),
      kurssinNimi = r.rs.getString("kurssin_nimi"),
      suorituksenTyyppi = r.rs.getString("suorituksen_tyyppi")
    )
  )

  def build(oppilaitosOids: List[String], aikaisintaan: LocalDate, viimeistaan: LocalDate)(implicit u: KoskiSession): DataSheet = {
    val raporttiQuery = query(validateOids(oppilaitosOids), aikaisintaan, viimeistaan).as[AikuistenPerusopetuksenEiRahoitustietoaKurssitRow]
    val rows = runDbSync(raporttiQuery, timeout = 5.minutes)
    DataSheet(
      title = "Ei rahoitustietoa",
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
              and (r_paatason_suoritus.suorituksen_tyyppi = 'aikuistenperusopetuksenoppimaara' or r_paatason_suoritus.suorituksen_tyyppi = 'aikuistenperusopetuksenoppimaaranalkuvaihe')
              and exists (
                select 1
                from r_opiskeluoikeus_aikajakso
                where r_opiskeluoikeus.opiskeluoikeus_oid = r_opiskeluoikeus_aikajakso.opiskeluoikeus_oid
              )
            )
            select distinct on (r_osasuoritus.osasuoritus_id)
              oo_opiskeluoikeus_oid opiskeluoikeuden_oid,
              oppilaitos_nimi oppilaitos_nimi,
              r_osasuoritus.koulutusmoduuli_koodiarvo kurssikoodi,
              r_osasuoritus.koulutusmoduuli_nimi kurssin_nimi,
              coalesce(r_osasuoritus.koulutusmoduuli_kurssin_tyyppi, '') as suorituksen_tyyppi
            from paatason_suoritus
            join r_opiskeluoikeus_aikajakso aikajakso on aikajakso.opiskeluoikeus_oid = oo_opiskeluoikeus_oid
            join r_opiskeluoikeus_aikajakso on oo_opiskeluoikeus_oid = r_opiskeluoikeus_aikajakso.opiskeluoikeus_oid
            join r_osasuoritus on (paatason_suoritus.paatason_suoritus_id = r_osasuoritus.paatason_suoritus_id or oo_opiskeluoikeus_oid = r_osasuoritus.sisaltyy_opiskeluoikeuteen_oid)
              and r_opiskeluoikeus_aikajakso.alku <= $viimeistaan
              and r_opiskeluoikeus_aikajakso.loppu >= $aikaisintaan
            where (r_osasuoritus.suorituksen_tyyppi = 'aikuistenperusopetuksenkurssi'
                  or r_osasuoritus.suorituksen_tyyppi = 'aikuistenperusopetuksenalkuvaiheenkurssi'
                  or r_osasuoritus.suorituksen_tyyppi = 'aikuistenperusopetuksenoppiaine'
                  or r_osasuoritus.suorituksen_tyyppi = 'aikuistenperusopetuksenalkuvaiheenoppiaine')              and r_osasuoritus.arviointi_paiva >= $aikaisintaan
              and r_osasuoritus.arviointi_paiva <= $viimeistaan
              and (tunnustettu = false or tunnustettu_rahoituksen_piirissa = true)
                and r_opiskeluoikeus_aikajakso.opintojen_rahoitus is null
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
    "opiskeluoikeudenOid" -> Column("Opiskeluoikeuden oid"),
    "oppilaitos" -> Column("Oppilaitos"),
    "kurssikoodi" -> Column("Kurssikoodi"),
    "kurssinNimi" -> Column("Kurssin nimi"),
    "suorituksenTyyppi" -> Column("Suorituksen tyyppi"),
  )
}

case class AikuistenPerusopetuksenEiRahoitustietoaKurssitRow(
   opiskeluoikeudenOid: String,
   oppilaitos: String,
   kurssikoodi: String,
   kurssinNimi: String,
   suorituksenTyyppi: String,
)
