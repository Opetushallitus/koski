package fi.oph.koski.queuedqueries.organisaationopiskeluoikeudet

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.queuedqueries.QueryResultWriter
import fi.oph.koski.schema.{Oppija, Organisaatio}
import fi.oph.scalaschema.annotation.EnumValue

import java.time.LocalDate
import scala.util.Try

case class QueryOrganisaationOpiskeluoikeudetJson(
  @EnumValue("organisaationOpiskeluoikeudet")
  `type`: String = "organisaationOpiskeluoikeudet",
  @EnumValue("application/json")
  format: String = "application/json",
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
  ): Try[Unit] = Try {
    val db = getDb(application)
    val filters = defaultBaseFilter(oppilaitosOids)
    val oppijaOids = getOppijaOids(db, filters)

    forEachOpiskeluoikeus(application, filters, oppijaOids) { (henkilö, opiskeluoikeudet) =>
      writer.putJson(henkilö.oid, Oppija(
        henkilö = application.henkilöRepository.oppijaHenkilöToTäydellisetHenkilötiedot(henkilö),
        opiskeluoikeudet = opiskeluoikeudet,
      ))
    }
  }
}

