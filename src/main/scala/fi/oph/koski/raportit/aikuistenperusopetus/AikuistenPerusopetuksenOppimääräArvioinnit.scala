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
      arviointienlkm = r.<<,
      ensimmainenArviointiPvm = r.nextDateOption.map(_.toLocalDate),
      parasArviointiPvm = r.nextDateOption.map(_.toLocalDate),
      parasArviointiArvosana = r.<<,
      arviointiPvm = r.nextDateOption.map(_.toLocalDate),
      arviointiArvosana = r.<<,
      hylatynKorotus = r.nextBooleanOption,
      hyvaksytynKorotus = r.nextBooleanOption,
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
      SELECT
        r_opiskeluoikeus.opiskeluoikeus_oid AS oo_opiskeluoikeus_oid,
        r_opiskeluoikeus.alkamispaiva AS oo_alkamispaiva,
        r_opiskeluoikeus.paattymispaiva AS oo_paattymispaiva,
        r_opiskeluoikeus.viimeisin_tila,
        r_paatason_suoritus.suorituksen_tyyppi,
        r_osasuoritus.koulutusmoduuli_koodiarvo,
        r_osasuoritus.koulutusmoduuli_nimi,
        r_osasuoritus.koulutusmoduuli_paikallinen,
        jsonb_array_length(r_osasuoritus.data->'arviointi') AS arviointien_lkm,
        r_osasuoritus.ensimmainen_arviointi_paiva,
        r_osasuoritus.arviointi_paiva AS paras_arviointi_pvm,
        r_osasuoritus.arviointi_arvosana_koodiarvo AS paras_arviointi_arvosana,
        arviointi_element ->> 'päivä' AS arviointi_pvm,
        arviointi_element -> 'arvosana' ->> 'koodiarvo' AS arviointi_arvosana,
        CASE
          WHEN EXISTS (
            SELECT 1
            FROM (
              SELECT elem -> 'arvosana' ->> 'koodiarvo' AS arvosana
              FROM jsonb_array_elements(r_osasuoritus.data->'arviointi') AS elem
              WHERE (elem ->> 'päivä')::date < (arviointi_element ->> 'päivä')::date
              ORDER BY (elem ->> 'päivä')::date DESC
              LIMIT 1
            ) subquery
            WHERE arvosana in ('4', 'H')
          ) THEN true
          ELSE false
        END AS hylatyn_korotus,
        CASE
          WHEN EXISTS (
            SELECT 1
            FROM (
              SELECT elem -> 'arvosana' ->> 'koodiarvo' AS arvosana
              FROM jsonb_array_elements(r_osasuoritus.data->'arviointi') AS elem
              WHERE (elem ->> 'päivä')::date < (arviointi_element ->> 'päivä')::date
              ORDER BY (elem ->> 'päivä')::date DESC
              LIMIT 1
            ) subquery
            WHERE arvosana not in ('4', 'H')
          ) THEN true
          ELSE false
        END AS hyvaksytyn_korotus
      FROM r_opiskeluoikeus
      JOIN r_paatason_suoritus
        ON r_opiskeluoikeus.opiskeluoikeus_oid = r_paatason_suoritus.opiskeluoikeus_oid
        AND r_opiskeluoikeus.sisaltyy_opiskeluoikeuteen_oid IS NULL
      JOIN r_opiskeluoikeus_aikajakso
        ON r_opiskeluoikeus.opiskeluoikeus_oid = r_opiskeluoikeus_aikajakso.opiskeluoikeus_oid
      JOIN r_osasuoritus
        ON r_paatason_suoritus.paatason_suoritus_id = r_osasuoritus.paatason_suoritus_id
        OR r_opiskeluoikeus.opiskeluoikeus_oid = r_osasuoritus.sisaltyy_opiskeluoikeuteen_oid,
      jsonb_array_elements(r_osasuoritus.data->'arviointi') AS arviointi_element
      WHERE (oppilaitos_oid = ANY($oppilaitosOidit) OR koulutustoimija_oid = ANY($oppilaitosOidit))
        AND (r_paatason_suoritus.suorituksen_tyyppi = 'aikuistenperusopetuksenoppimaara'
          OR r_paatason_suoritus.suorituksen_tyyppi = 'aikuistenperusopetuksenoppimaaranalkuvaihe')
        AND (arviointi_element ->> 'päivä')::date BETWEEN $aikaisintaan AND $viimeistaan;
  """
  }

  def columnSettings(t: LocalizationReader): Seq[(String, Column)] = Seq(
    "opiskeluoikeusOid" -> Column(t.get("raportti-excel-kolumni-opiskeluoikeusOid")),
    "alkamispaiva" -> Column(t.get("raportti-excel-kolumni-alkamispaiva")),
    "paattymispaiva" -> Column(t.get("raportti-excel-kolumni-paattymispaiva"), comment = Some(t.get("raportti-excel-kolumni-paattymispaiva-comment"))),
    "viimeisinTila" -> Column(t.get("raportti-excel-kolumni-viimeisinTila"), comment = Some(t.get("raportti-excel-kolumni-viimeisinTila-comment"))),
    "suorituksenTyyppi" -> Column(t.get("raportti-excel-kolumni-suorituksenTyyppi"), comment = Some(t.get("raportti-excel-kolumni-suorituksenTyyppi-comment"))),
    "kurssinKoodi" -> Column(t.get("raportti-excel-kolumni-kurssinKoodi"), comment = Some(t.get("raportti-excel-kolumni-kurssinKoodi-comment"))),
    "kurssinNimi" -> Column(t.get("raportti-excel-kolumni-kurssinNimi"), comment = Some(t.get("raportti-excel-kolumni-kurssinNimi-comment"))),
    "paikallinenModuuli" -> Column(t.get("raportti-excel-kolumni-paikallinenModuuli"), comment = Some(t.get("raportti-excel-kolumni-paikallinenModuuli-comment"))),
    "arviointienlkm" -> Column(t.get("raportti-excel-kolumni-arviointienlkm"), comment = Some(t.get("raportti-excel-kolumni-arviointienlkm-comment"))),
    "ensimmainenArviointiPvm" -> Column(t.get("raportti-excel-kolumni-ensimmainenArviointiPvm"), comment = Some(t.get("raportti-excel-kolumni-ensimmainenArviointiPvm-comment"))),
    "parasArviointiPvm" -> Column(t.get("raportti-excel-kolumni-parasArviointiPvm"), comment = Some(t.get("raportti-excel-kolumni-parasArviointiPvm-comment"))),
    "parasArviointiArvosana" -> Column(t.get("raportti-excel-kolumni-parasArviointiArvosana"), comment = Some(t.get("raportti-excel-kolumni-parasArviointiArvosana-comment"))),
    "arviointiPvm" -> Column(t.get("raportti-excel-kolumni-arviointiPvm"), comment = Some(t.get("raportti-excel-kolumni-arviointiPvm-comment"))),
    "arviointiArvosana" -> Column(t.get("raportti-excel-kolumni-arviointiArvosana"), comment = Some(t.get("raportti-excel-kolumni-arviointiArvosana-comment"))),
    "hylatynKorotus" -> Column(t.get("raportti-excel-kolumni-hylatynKorotus"), comment = Some(t.get("raportti-excel-kolumni-hylatynKorotus-comment"))),
    "hyvaksytynKorotus" -> Column(t.get("raportti-excel-kolumni-hyvaksytynKorotus"), comment = Some(t.get("raportti-excel-kolumni-hyvaksytynKorotus-comment"))),
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
  arviointienlkm: Int,
  ensimmainenArviointiPvm: Option[LocalDate],
  parasArviointiPvm: Option[LocalDate],
  parasArviointiArvosana: String,
  arviointiPvm: Option[LocalDate],
  arviointiArvosana: String,
  hylatynKorotus: Option[Boolean],
  hyvaksytynKorotus: Option[Boolean],
)
