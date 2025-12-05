package fi.oph.koski.massaluovutus.luokallejaaneet

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.json.SensitiveDataAllowed
import fi.oph.koski.koskiuser.Session
import fi.oph.koski.massaluovutus.MassaluovutusUtils.{QueryResourceManager, defaultOrganisaatio}
import fi.oph.koski.massaluovutus.{QueryFormat, QueryResultWriter}
import fi.oph.koski.schema.annotation.EnumValues

import scala.util.Using

case class MassaluovutusQueryLuokalleJaaneetCsv(
  `type`: String = "luokallejaaneet",
  @EnumValues(Set(QueryFormat.csv))
  format: String = QueryFormat.csv,
  organisaatioOid: Option[String],
) extends MassaluovutusQueryLuokalleJaaneet {
  override def run(application: KoskiApplication, writer: QueryResultWriter)
    (implicit user: Session with SensitiveDataAllowed): Either[String, Unit] = withKoskiSpecificSession { implicit koskiUser =>
    QueryResourceManager(logger) { mgr =>
      implicit val manager: Using.Manager = mgr
      val csvFile = writer.createCsv[CsvFields](s"luokalle_jaaneet_${organisaatioOid.get}", None)
      forEachResult(application) { result => csvFile.put(CsvFields(result)) }
      csvFile.save()
    }
  }

  override def fillAndValidate(implicit user: Session): Either[HttpStatus, MassaluovutusQueryLuokalleJaaneet] =
    if (organisaatioOid.isEmpty) {
      defaultOrganisaatio.map(o => copy(organisaatioOid = Some(o)))
    } else {
      Right(this)
    }
}

case class CsvFields(
  opiskeluoikeudenOid: Option[String],
  oppijanumero: String,
  luokka: String,
  versionumero: Option[Int],
  viimeisinVersionumero: Option[Int],
)

object CsvFields {
  def apply(r: MassaluovutusQueryLuokalleJaaneetResult): CsvFields = CsvFields(
    r.opiskeluoikeus.oid,
    r.oppijaOid,
    r.luokka,
    r.opiskeluoikeus.versionumero,
    r.viimeisinVersionumero,
  )
}
