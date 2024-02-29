package fi.oph.koski.queuedqueries.organisaationopiskeluoikeudet

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.queuedqueries.QueryResultWriter
import fi.oph.koski.schema.Organisaatio
import fi.oph.scalaschema.annotation.EnumValue

import java.time.LocalDate
import scala.util.{Try, Using}

case class QueryOrganisaationOpiskeluoikeudetCsv(
  @EnumValue("organisaationOpiskeluoikeudet")
  `type`: String = "organisaationOpiskeluoikeudet",
  @EnumValue("text/csv")
  format: String = "text/csv",
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
  ): Try[Unit] = Using.Manager { use =>
    val db = getDb(application)
    val filters = defaultBaseFilter(oppilaitosOids)
    val oppijaOids = getOppijaOids(db, filters)

    val opiskeluoikeusCsv = use(writer.createCsv[OpiskeluoikeusEntry]("opiskeluoikeus"))

    forEachOpiskeluoikeus(application, filters, oppijaOids) { (henkilö, opiskeluoikeudet) =>
      opiskeluoikeudet.foreach { opiskeluoikeus =>
        opiskeluoikeusCsv.put(OpiskeluoikeusEntry(henkilö, opiskeluoikeus))
      }
    }

    opiskeluoikeusCsv.save()
  }
}
