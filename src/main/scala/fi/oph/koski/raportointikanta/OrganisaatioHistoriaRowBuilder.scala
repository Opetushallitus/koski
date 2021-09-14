package fi.oph.koski.raportointikanta

import java.time.LocalDate

import scalaz.syntax.std.list._

import fi.oph.koski.schema.{KoskeenTallennettavaOpiskeluoikeus, OpiskeluoikeudenOrganisaatiohistoria}
import fi.oph.koski.util.DateOrdering.localDateOrdering

object OrganisaatioHistoriaRowBuilder {
  private val EarlyDate = LocalDate.of(1900, 1, 1)
  private val LateDate = LocalDate.of(9999, 12, 31)

  def buildOrganisaatioHistoriaRows(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): Seq[ROrganisaatioHistoriaRow] = {
    val nykyinenOrganisaatio = OpiskeluoikeudenOrganisaatiohistoria(
      muutospäivä = LateDate,
      oppilaitos = opiskeluoikeus.oppilaitos,
      koulutustoimija = opiskeluoikeus.koulutustoimija
    )
    val kaikkiOrganisaatiot = opiskeluoikeus.organisaatiohistoria.toList.flatten ::: List(nykyinenOrganisaatio)
    val firstStartDate = opiskeluoikeus.alkamispäivä.getOrElse(EarlyDate)

    val organisaatiohistoriat = kaikkiOrganisaatiot.mapAccumLeft(
      firstStartDate,
      buildOrganisaatioHistoriaRow(opiskeluoikeus)
    )._2

    organisaatiohistoriat
      .groupBy(_.alku)
      .values
      .map(_.last)
      .toList
      .sortBy(_.alku)(localDateOrdering)
  }

  private def latterOf(a: LocalDate, b: LocalDate): LocalDate = if (b.isAfter(a)) b else a

  private def buildOrganisaatioHistoriaRow
    (opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus)
    (previousMuutospäivä: LocalDate, organisaatioHistoria: OpiskeluoikeudenOrganisaatiohistoria)
  : (LocalDate, ROrganisaatioHistoriaRow) = {
    val loppu = latterOf(organisaatioHistoria.muutospäivä.minusDays(1), previousMuutospäivä)
    val row = ROrganisaatioHistoriaRow(
      opiskeluoikeusOid = opiskeluoikeus.oid.get,
      alku = previousMuutospäivä,
      loppu = loppu,
      oppilaitosOid = organisaatioHistoria.oppilaitos.map(_.oid),
      koulutustoimijaOid = organisaatioHistoria.koulutustoimija.map(_.oid)
    )
    (organisaatioHistoria.muutospäivä, row)
  }
}
