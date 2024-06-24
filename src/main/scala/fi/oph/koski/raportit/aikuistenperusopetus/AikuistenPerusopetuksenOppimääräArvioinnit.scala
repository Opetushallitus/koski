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
      ensimmainenArviointi = r.<<,
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
      SELECT DISTINCT
        oo.opiskeluoikeus_oid AS oo_opiskeluoikeus_oid,
        oo.alkamispaiva AS oo_alkamispaiva,
        oo.paattymispaiva AS oo_paattymispaiva,
        oo.viimeisin_tila,
        ps.suorituksen_tyyppi,
        os.koulutusmoduuli_koodiarvo,
        os.koulutusmoduuli_nimi,
        os.koulutusmoduuli_paikallinen,
        jsonb_array_length(os.data->'arviointi') AS arviointien_lkm,
        os.ensimmainen_arviointi_paiva,
        os.arviointi_paiva AS paras_arviointi_pvm,
        os.arviointi_arvosana_koodiarvo AS paras_arviointi_arvosana,
        ae.arviointi_pvm,
        ae.arviointi_arvosana,
        (ae.arviointi_pvm)::date = os.ensimmainen_arviointi_paiva AS ensimmainen_arviointi,
        CASE
          WHEN EXISTS (
            SELECT 1
            FROM (
              SELECT elem -> 'arvosana' ->> 'koodiarvo' AS arvosana
              FROM jsonb_array_elements(os.data->'arviointi') AS elem
              WHERE (elem ->> 'päivä')::date < (ae.arviointi_pvm)::date
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
              FROM jsonb_array_elements(os.data->'arviointi') AS elem
              WHERE (elem ->> 'päivä')::date < (ae.arviointi_pvm)::date
              ORDER BY (elem ->> 'päivä')::date DESC
              LIMIT 1
            ) subquery
            WHERE arvosana not in ('4', 'H')
          ) THEN true
          ELSE false
        END AS hyvaksytyn_korotus
      FROM r_opiskeluoikeus oo
      JOIN r_paatason_suoritus ps
        ON oo.opiskeluoikeus_oid = ps.opiskeluoikeus_oid
        AND oo.sisaltyy_opiskeluoikeuteen_oid IS NULL
      JOIN r_opiskeluoikeus_aikajakso oa
        ON oo.opiskeluoikeus_oid = oa.opiskeluoikeus_oid
      JOIN r_osasuoritus os
        ON ps.paatason_suoritus_id = os.paatason_suoritus_id
        OR oo.opiskeluoikeus_oid = os.sisaltyy_opiskeluoikeuteen_oid
      JOIN LATERAL (
        SELECT
          (elem ->> 'päivä') AS arviointi_pvm,
          (elem -> 'arvosana' ->> 'koodiarvo') AS arviointi_arvosana
        FROM jsonb_array_elements(os.data->'arviointi') AS elem
      ) ae ON true
      WHERE (oppilaitos_oid = ANY($oppilaitosOidit) OR koulutustoimija_oid = ANY($oppilaitosOidit))
        AND (ps.suorituksen_tyyppi = 'aikuistenperusopetuksenoppimaara'
          OR ps.suorituksen_tyyppi = 'aikuistenperusopetuksenoppimaaranalkuvaihe')
        AND (ae.arviointi_pvm)::date BETWEEN $aikaisintaan AND $viimeistaan;
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
    "ensimmainenArviointi" -> Column(t.get("raportti-excel-kolumni-ensimmainenArviointi"), comment = Some(t.get("raportti-excel-kolumni-ensimmainenArviointi-comment"))),
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
  paikallinenModuuli: Boolean,
  arviointienlkm: Int,
  ensimmainenArviointiPvm: Option[LocalDate],
  parasArviointiPvm: Option[LocalDate],
  parasArviointiArvosana: String,
  arviointiPvm: Option[LocalDate],
  arviointiArvosana: String,
  ensimmainenArviointi: Boolean,
  hylatynKorotus: Option[Boolean],
  hyvaksytynKorotus: Option[Boolean],
)
