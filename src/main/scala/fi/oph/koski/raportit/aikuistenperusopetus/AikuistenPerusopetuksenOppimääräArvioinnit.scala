package fi.oph.koski.raportit.aikuistenperusopetus

import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.db.{DB, QueryMethods}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.raportit.{Column, DataSheet}
import slick.jdbc.GetResult

import java.sql.Date
import java.time.LocalDate
import scala.concurrent.duration.DurationInt

case class AikuistenPerusopetuksenOppimääräArvioinnit(db: DB) extends QueryMethods  {
  implicit private val getResult: GetResult[AikuistenPerusopetuksenOppimääränArvioinnitRow] = GetResult(r =>
    AikuistenPerusopetuksenOppimääränArvioinnitRow(
      opiskeluoikeusOid = r.<<,
      alkamispaiva = r.nextDateOption.map(_.toLocalDate),
      paattymispaiva = r.nextDateOption.map(_.toLocalDate),
      viimeisinTila = r.<<,
      suorituksenTyyppi = r.<<,
      kurssinKoodi = r.<<,
      kurssinNimi = r.<<,
      paikallinenModuuli = r.<<,
      arviointiPvm = r.nextDateOption.map(_.toLocalDate),
      arviointiKoodiarvo = r.<<,
      ensimmainenArviointiPvm = r.nextDateOption.map(_.toLocalDate),
      hylatynKorotus = r.nextBooleanOption,
      hyvaksytynKorotus = r.nextBooleanOption,
      arviointienlkm = r.<<
    )
  )

  def build(oppilaitosOids: List[String], aikaisintaan: LocalDate, viimeistaan: LocalDate, t: LocalizationReader)(implicit u: KoskiSpecificSession): DataSheet = {
    val raporttiQuery = query(oppilaitosOids, aikaisintaan, viimeistaan, t.language).as[AikuistenPerusopetuksenOppimääränArvioinnitRow]
    val rows = runDbSync(raporttiQuery, timeout = 5.minutes)
    DataSheet(
      title = t.get("raportti-excel-arvioinnit-sheet-name"),
      rows = rows,
      columnSettings = columnSettings(t)
    )
  }


  private def query(oppilaitosOidit: List[String], aikaisintaan: LocalDate, viimeistaan: LocalDate, lang: String)(implicit u: KoskiSpecificSession) = {
    sql"""
      select
        r_opiskeluoikeus.opiskeluoikeus_oid as oo_opiskeluoikeus_oid,
        r_opiskeluoikeus.alkamispaiva as oo_alkamisaiva,
        r_opiskeluoikeus.paattymispaiva as oo_paattymispaiva,
        r_opiskeluoikeus.viimeisin_tila, -- viimeisin tila
        r_paatason_suoritus.suorituksen_tyyppi, -- päätason suorituksen tyyppi
        r_osasuoritus.koulutusmoduuli_koodiarvo, -- kurssin koodi
        r_osasuoritus.koulutusmoduuli_nimi, -- kurssin nimi
        r_osasuoritus.koulutusmoduuli_paikallinen, -- valtakunnallinen vai paikallinen
        r_osasuoritus.arviointi_paiva, -- arviointipaiva
        r_osasuoritus.arviointi_arvosana_koodiarvo, -- arvosana
        r_osasuoritus.ensimmainen_arviointi_paiva, -- ensimmäinen arviointi vai ei
        false,
        false,
        array_length(r_osasuoritus.arviointi_paivat, 1) as arviointipaivienlkm -- useampi kuin yksi arviointi vai ei,
      from r_opiskeluoikeus
        join r_paatason_suoritus on r_opiskeluoikeus.opiskeluoikeus_oid = r_paatason_suoritus.opiskeluoikeus_oid
          and r_opiskeluoikeus.sisaltyy_opiskeluoikeuteen_oid is null
        join r_opiskeluoikeus_aikajakso on r_opiskeluoikeus.opiskeluoikeus_oid = r_opiskeluoikeus_aikajakso.opiskeluoikeus_oid
        join r_osasuoritus on r_paatason_suoritus.paatason_suoritus_id = r_osasuoritus.paatason_suoritus_id or r_opiskeluoikeus.opiskeluoikeus_oid = r_osasuoritus.sisaltyy_opiskeluoikeuteen_oid
        left join lateral (
          select count(1) as arviointipvm_count
          from unnest(r_osasuoritus.arviointi_paivat) as arviointipvm
          where arviointipvm between $aikaisintaan and $viimeistaan
        ) as subquery on true
      where (oppilaitos_oid = any($oppilaitosOidit) or koulutustoimija_oid = any($oppilaitosOidit))
        and (r_paatason_suoritus.suorituksen_tyyppi = 'aikuistenperusopetuksenoppimaara' or r_paatason_suoritus.suorituksen_tyyppi = 'aikuistenperusopetuksenoppimaaranalkuvaihe')
  """
  }

  def columnSettings(t: LocalizationReader): Seq[(String, Column)] = Seq(
    "opiskeluoikeusOid" -> Column(t.get("raportti-excel-kolumni-opiskeluoikeusOid")),
    "alkamispaiva" -> Column(t.get("raportti-excel-kolumni-alkamispaiva")),
    "paattymispaiva" -> Column(t.get("raportti-excelalkamispaivakolumni-paattymispaiva"), comment = Some(t.get("raportti-excel-kolumni-paattymispaiva-comment"))),
    "viimeisinTila" -> Column(t.get("raportti-excel-kolumni-viimeisinTila"), comment = Some(t.get("raportti-excel-kolumni-viimeisinTila-comment"))),
    "suorituksenTyyppi" -> Column(t.get("raportti-excel-kolumni-suorituksenTyyppi"), comment = Some(t.get("raportti-excel-kolumni-suorituksenTyyppi-comment"))),
    "kurssinKoodi" -> Column(t.get("raportti-excel-kolumni-kurssinKoodi"), comment = Some(t.get("raportti-excel-kolumni-kurssinKoodi-comment"))),
    "kurssinNimi" -> Column(t.get("raportti-excel-kolumni-kurssinNimi"), comment = Some(t.get("raportti-excel-kolumni-kurssinNimi-comment"))),
    "paikallinenModuuli" -> Column(t.get("raportti-excel-kolumni-paikallinenModuuli"), comment = Some(t.get("raportti-excel-kolumni-paikallinenModuuli-comment"))),
    "arviointiPvm" -> Column(t.get("raportti-excel-kolumni-arviointiPvm"), comment = Some(t.get("raportti-excel-kolumni-arviointiPvm-comment"))),
    "arviointiKoodiarvo" -> Column(t.get("raportti-excel-kolumni-arviointiKoodiarvo"), comment = Some(t.get("raportti-excel-kolumni-arviointiKoodiarvo-comment"))),
    "ensimmainenArviointiPvm" -> Column(t.get("raportti-excel-kolumni-ensimmainenArviointiPvm"), comment = Some(t.get("raportti-excel-kolumni-ensimmainenArviointiPvm-comment"))),
    "hylatynKorotus" -> Column(t.get("raportti-excel-kolumni-hylatynKorotus"), comment = Some(t.get("raportti-excel-kolumni-hylatynKorotus-comment"))),
    "hyvaksytynKorotus" -> Column(t.get("raportti-excel-kolumni-hyvaksytynKorotus"), comment = Some(t.get("raportti-excel-kolumni-hyvaksytynKorotus-comment"))),
    "arviointienlkm" -> Column(t.get("raportti-excel-kolumni-arviointienlkm"), comment = Some(t.get("raportti-excel-kolumni-arviointienlkm-comment"))),
  )
}

case class AikuistenPerusopetuksenOppimääränArvioinnitRow(
  opiskeluoikeusOid: String,
  alkamispaiva: Option[LocalDate],
  paattymispaiva: Option[LocalDate],
  viimeisinTila: String,
  suorituksenTyyppi: String,
  kurssinKoodi: String,
  kurssinNimi: String,
  paikallinenModuuli: String,
  arviointiPvm: Option[LocalDate],
  arviointiKoodiarvo: String,
  ensimmainenArviointiPvm: Option[LocalDate],
  hylatynKorotus: Option[Boolean],
  hyvaksytynKorotus: Option[Boolean],
  arviointienlkm: Int
)
