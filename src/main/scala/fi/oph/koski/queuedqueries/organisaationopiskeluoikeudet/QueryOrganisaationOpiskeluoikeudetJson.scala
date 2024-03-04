package fi.oph.koski.queuedqueries.organisaationopiskeluoikeudet

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.{KoskiOpiskeluoikeusRow, KoskiTables}
import fi.oph.koski.queuedqueries.QueryUtils.QueryResourceManager
import fi.oph.koski.queuedqueries.{QueryFormat, QueryResultWriter}
import fi.oph.koski.schema.{KoskeenTallennettavaOpiskeluoikeus, KoskiSchema, Oppija, Organisaatio}
import fi.oph.scalaschema.annotation.EnumValue

import java.time.LocalDate

case class QueryOrganisaationOpiskeluoikeudetJson(
  @EnumValue("organisaationOpiskeluoikeudet")
  `type`: String = "organisaationOpiskeluoikeudet",
  @EnumValue(QueryFormat.json)
  format: String = QueryFormat.json,
  organisaatioOid: Option[Organisaatio.Oid],
  alkamispaiva: LocalDate,
  tila: Option[String],
  koulutusmuoto: Option[String],
  suoritustyyppi: Option[String],
) extends QueryOrganisaationOpiskeluoikeudet {
  def fetchData(
    application: KoskiApplication,
    writer: QueryResultWriter,
    oppilaitosOids: List[Organisaatio.Oid],
  ): Either[String, Unit] = QueryResourceManager(logger) { _ =>
    val db = getDb(application)
    val filters = defaultBaseFilter(oppilaitosOids)
    val oppijaOids = getOppijaOids(db, filters)

    forEachOpiskeluoikeusAndHenkilö(application, filters, oppijaOids) { (henkilö, opiskeluoikeudet) =>
      writer.putJson(henkilö.oid, Oppija(
        henkilö = application.henkilöRepository.oppijaHenkilöToTäydellisetHenkilötiedot(henkilö),
        opiskeluoikeudet = opiskeluoikeudet.flatMap(toKoskeenTallennettavaOpiskeluoikeus(application)),
      ))
    }
  }

  private def toKoskeenTallennettavaOpiskeluoikeus(application: KoskiApplication)(row: KoskiOpiskeluoikeusRow): Option[KoskeenTallennettavaOpiskeluoikeus] = {
    val json = KoskiTables.KoskiOpiskeluoikeusTable.readAsJValue(row.data, row.oid, row.versionumero, row.aikaleima)
    application.validatingAndResolvingExtractor.extract[KoskeenTallennettavaOpiskeluoikeus](KoskiSchema.strictDeserialization)(json) match {
      case Right(oo: KoskeenTallennettavaOpiskeluoikeus) => Some(oo)
      case Left(errors) =>
        logger.warn(s"Error deserializing opiskeluoikeus: ${errors}")
        None
    }
  }
}

