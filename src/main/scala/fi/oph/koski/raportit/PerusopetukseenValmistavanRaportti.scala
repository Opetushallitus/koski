package fi.oph.koski.raportit

import fi.oph.koski.executors.GlobalExecutionContext
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.raportit.YleissivistäväUtils.{lengthInDaysInDateRange, removeContinuousSameTila}
import fi.oph.koski.schema.Organisaatio.Oid
import fi.oph.koski.schema.{Aikajakso, LähdejärjestelmäId}

import java.time.LocalDate

case class PerusopetukseenValmistavanRaportti(repository: PerusopetukseenValmistavanRaportitRepository, t: LocalizationReader) extends GlobalExecutionContext {

  def buildRows(oppilaitosOids: Seq[Oid],
    alku: LocalDate,
    loppu: LocalDate,
    t: LocalizationReader
   ): Seq[PerusopetukseenValmistavanRaporttiRow] = {
    val rows = repository.perusopetukseenValmistavanRaporttiRows(oppilaitosOids, alku, loppu)
    rows.map(buildRow(_, alku, loppu, t))
  }

  def buildDataSheet(rows: Seq[PerusopetukseenValmistavanRaporttiRow]): DynamicDataSheet = {
    DynamicDataSheet(
      title = t.get("raportti-excel-oppiaineet-sheet-name"),
      rows = rows.map(_.productIterator.toList),
      columnSettings = perusopetukseenValmistavanRaporttiColumnSettings(rows)
    )
  }

  private def perusopetukseenValmistavanRaporttiColumnSettings(rows: Seq[PerusopetukseenValmistavanRaporttiRow]) = {
    Seq(
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
  }

  private def buildRow(row: PerusopetukseenValmistavanRaporttiRows, alku: LocalDate, loppu: LocalDate, t: LocalizationReader): PerusopetukseenValmistavanRaporttiRow = {
    val lähdejärjestelmänId = JsonSerializer.extract[Option[LähdejärjestelmäId]](row.opiskeluoikeus.data \ "lähdejärjestelmänId")

    PerusopetukseenValmistavanRaporttiRow(
      opiskeluoikeusOid = row.opiskeluoikeus.opiskeluoikeusOid,
      lähdejärjestelmä = lähdejärjestelmänId.map(_.lähdejärjestelmä.koodiarvo),
      lähdejärjestelmänTunniste = lähdejärjestelmänId.flatMap(_.id),
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
      rahoitukset = row.aikajaksot.flatMap(_.opintojenRahoitus).mkString(","),
    )
  }
}

private[raportit] case class PerusopetukseenValmistavanRaporttiRow(
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
