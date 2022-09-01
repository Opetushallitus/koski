package fi.oph.koski.raportit

import fi.oph.koski.executors.GlobalExecutionContext
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.raportit.YleissivistäväUtils.{lengthInDaysInDateRange, opetettavatOppiaineetJaNiidenKurssit, oppiaineidentiedot, removeContinuousSameTila}
import fi.oph.koski.raportointikanta.ROsasuoritusRow
import fi.oph.koski.schema.Organisaatio.Oid
import fi.oph.koski.schema.{Aikajakso, LähdejärjestelmäId}

import java.time.LocalDate

case class PerusopetukseenValmistavanRaportti(repository: PerusopetukseenValmistavanRaportitRepository, t: LocalizationReader) extends GlobalExecutionContext {
  def buildRaportti(
    oppilaitosOids: Seq[Oid],
    alku: LocalDate,
    loppu: LocalDate,
    osasuoritustenAikarajaus: Boolean,
    t: LocalizationReader
  ): DynamicDataSheet = {
    val rows = repository.perusopetukseenValmistavanRaporttiRows(oppilaitosOids, alku, loppu, osasuoritustenAikarajaus)

    DynamicDataSheet(
      title = t.get("raportti-excel-oppiaineet-sheet-name"),
      rows = buildDataSheetRows(rows, alku, loppu, t),
      columnSettings = buildDataSheetColumns(rows, t)
    )
  }

  def buildDataSheetRows(
    rows: Seq[PerusopetukseenValmistavanRaporttiRows],
    alku: LocalDate,
    loppu: LocalDate,
    t: LocalizationReader
  ): Seq[List[Any]] = {
    val stableRowParts = rows.map(buildStableFields(_, alku, loppu, t))
    val dynamicRowParts = rows.map(row => oppiaineidentiedot(row.päätasonSuoritus, row.osasuoritukset, oppiaineet(rows), _ => false, t))

    combineStableAndDynamicRowParts(stableRowParts, dynamicRowParts)
  }

  private def buildDataSheetColumns(
    rows: Seq[PerusopetukseenValmistavanRaporttiRows],
    t: LocalizationReader
  ) = {
    perusopetukseenValmistavanRaporttiStableColumnSettings ++ perusopetukseenValmistavanRaporttiDynamicColumnSettings(oppiaineet(rows), t)
  }

  private def isOppiaine(osasuoritus: ROsasuoritusRow): Boolean =
    osasuoritus.suorituksenTyyppi == "perusopetuksenoppiaineperusopetukseenvalmistavassaopetuksessa" ||
      osasuoritus.suorituksenTyyppi == "perusopetukseenvalmistavanopetuksenoppiaine"

  private def oppiaineet(rows: Seq[PerusopetukseenValmistavanRaporttiRows]) = opetettavatOppiaineetJaNiidenKurssit(_ => false, isOppiaine, rows, t)

  private def combineStableAndDynamicRowParts(staticRowParts: Seq[PerusopetukseenValmistavanRaporttiStableFields], dynamicRowParts: Seq[Seq[Any]]): Seq[List[Any]] =
    staticRowParts
      .zip(dynamicRowParts)
      .toList
      .map { case (a, b) => a.productIterator.toList ++ b }

  private def buildStableFields(row: PerusopetukseenValmistavanRaporttiRows, alku: LocalDate, loppu: LocalDate, t: LocalizationReader): PerusopetukseenValmistavanRaporttiStableFields = {
    val lahdejarjestelmaId = JsonSerializer.extract[Option[LähdejärjestelmäId]](row.opiskeluoikeus.data \ "lähdejärjestelmänId")

    PerusopetukseenValmistavanRaporttiStableFields(
      opiskeluoikeusOid = row.opiskeluoikeus.opiskeluoikeusOid,
      lähdejärjestelmä = lahdejarjestelmaId.map(_.lähdejärjestelmä.koodiarvo),
      lähdejärjestelmänTunniste = lahdejarjestelmaId.flatMap(_.id),
      koulutustoimijaNimi = if (t.language == "sv") row.opiskeluoikeus.koulutustoimijaNimiSv else row.opiskeluoikeus.koulutustoimijaNimi,
      oppilaitosNimi = if (t.language == "sv") row.opiskeluoikeus.oppilaitosNimiSv else row.opiskeluoikeus.oppilaitosNimi,
      toimipisteenNimi = if (t.language == "sv") row.päätasonSuoritus.toimipisteNimiSv else row.päätasonSuoritus.toimipisteNimi,
      aikaleima = row.opiskeluoikeus.aikaleima.toLocalDateTime.toLocalDate,
      yksiloity = row.henkilo.yksiloity,
      oppijaOid = row.opiskeluoikeus.oppijaOid,
      hetu = row.henkilo.hetu,
      sukunimi = row.henkilo.sukunimi,
      etunimet = row.henkilo.etunimet,
      kansalaisuus = row.henkilo.kansalaisuus,
      opiskeluoikeudenAlkamispäivä = row.opiskeluoikeus.alkamispäivä.map(_.toLocalDate),
      opiskeluoikeudenTila = row.opiskeluoikeus.viimeisinTila,
      opiskeluoikeudenTilatAikajaksonAikana = removeContinuousSameTila(row.aikajaksot).map(_.tila).mkString(","),
      suoritustyyppi = row.päätasonSuoritus.suorituksenTyyppi,
      suorituksenTila = if (row.päätasonSuoritus.vahvistusPäivä.isDefined) t.get("raportti-excel-default-value-valmis") else t.get("raportti-excel-default-value-kesken"),
      suorituksenVahvistuspaiva = row.päätasonSuoritus.vahvistusPäivä.getOrElse("").toString,
      läsnäolopäiviäAikajaksonAikana = row.aikajaksot.filter(_.tila == "lasna").map(j => Aikajakso(j.alku.toLocalDate, Some(j.loppu.toLocalDate))).map(lengthInDaysInDateRange(_, alku, loppu)).sum,
      rahoitukset = row.aikajaksot.flatMap(_.opintojenRahoitus).mkString(",")
    )
  }

  private val perusopetukseenValmistavanRaporttiStableColumnSettings = Seq(
    CompactColumn(t.get("raportti-excel-kolumni-opiskeluoikeusOid")),
    CompactColumn(t.get("raportti-excel-kolumni-lähdejärjestelmä")),
    CompactColumn(t.get("raportti-excel-kolumni-lähdejärjestelmänId")),
    CompactColumn(t.get("raportti-excel-kolumni-koulutustoimijaNimi")),
    CompactColumn(t.get("raportti-excel-kolumni-oppilaitoksenNimi")),
    CompactColumn(t.get("raportti-excel-kolumni-toimipisteNimi")),
    CompactColumn(t.get("raportti-excel-kolumni-päivitetty"), comment = Some(t.get("raportti-excel-kolumni-päivitetty-comment"))),
    CompactColumn(t.get("raportti-excel-kolumni-yksiloity"), comment = Some(t.get("raportti-excel-kolumni-yksiloity-comment"))),
    Column(t.get("raportti-excel-kolumni-oppijaOid")),
    Column(t.get("raportti-excel-kolumni-hetu")),
    Column(t.get("raportti-excel-kolumni-sukunimi")),
    Column(t.get("raportti-excel-kolumni-etunimet")),
    Column(t.get("raportti-excel-kolumni-kansalaisuus")),
    CompactColumn(t.get("raportti-excel-kolumni-opiskeluoikeudenAlkamispäivä")),
    CompactColumn(t.get("raportti-excel-kolumni-viimeisinTila"), comment = Some(t.get("raportti-excel-kolumni-viimeisinTila-comment"))),
    CompactColumn(t.get("raportti-excel-kolumni-kaikkiTilat"), comment = Some(t.get("raportti-excel-kolumni-kaikkiTilat-comment"))),
    CompactColumn(t.get("raportti-excel-kolumni-suorituksenTyyppi"), comment = Some(t.get("raportti-excel-kolumni-suorituksenTyyppi-lukio-comment"))),
    CompactColumn(t.get("raportti-excel-kolumni-suorituksenTila"), comment = Some(t.get("raportti-excel-kolumni-suorituksenTila-lukio-comment"))),
    CompactColumn(t.get("raportti-excel-kolumni-suorituksenVahvistuspaiva"), comment = Some(t.get("raportti-excel-kolumni-suorituksenVahvistuspaiva-lukio-comment"))),
    CompactColumn(t.get("raportti-excel-kolumni-läsnäolopäiviäAikajaksolla"), comment = Some(t.get("raportti-excel-kolumni-läsnäolopäiviäAikajaksolla-comment"))),
    CompactColumn(t.get("raportti-excel-kolumni-rahoitukset"), comment = Some(t.get("raportti-excel-kolumni-rahoitukset-lukio-comment"))),
  )

  private def perusopetukseenValmistavanRaporttiDynamicColumnSettings(oppiaineet: Seq[YleissivistäväRaporttiOppiaineJaKurssit], t: LocalizationReader) = {
    oppiaineet.map(x =>
      Column(title = x.oppiaine.toColumnTitle(t), comment = Some(t.get("raportti-excel-kolumni-oppiaineSarake-comment")))
    )
  }
}

private[raportit] case class PerusopetukseenValmistavanRaporttiStableFields(
  opiskeluoikeusOid: String,
  lähdejärjestelmä: Option[String],
  lähdejärjestelmänTunniste: Option[String],
  koulutustoimijaNimi: String,
  oppilaitosNimi: String,
  toimipisteenNimi: String,
  aikaleima: LocalDate,
  yksiloity: Boolean,
  oppijaOid: String,
  hetu: Option[String],
  sukunimi: String,
  etunimet: String,
  kansalaisuus: Option[String],
  opiskeluoikeudenAlkamispäivä: Option[LocalDate],
  opiskeluoikeudenTila: Option[String],
  opiskeluoikeudenTilatAikajaksonAikana: String,
  suoritustyyppi: String,
  suorituksenTila: String,
  suorituksenVahvistuspaiva: String,
  läsnäolopäiviäAikajaksonAikana: Int,
  rahoitukset: String
)
