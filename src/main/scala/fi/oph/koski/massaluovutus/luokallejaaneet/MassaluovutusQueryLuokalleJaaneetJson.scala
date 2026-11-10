package fi.oph.koski.massaluovutus.luokallejaaneet

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.json.SensitiveDataAllowed
import fi.oph.koski.koskiuser.Session
import fi.oph.koski.massaluovutus.MassaluovutusUtils.defaultOrganisaatio
import fi.oph.koski.massaluovutus.{QueryFormat, QueryResultWriter}
import fi.oph.koski.schema.annotation.EnumValues

case class MassaluovutusQueryLuokalleJaaneetJson(
  `type`: String = "luokallejaaneet",
  @EnumValues(Set(QueryFormat.json))
  format: String = QueryFormat.json,
  organisaatioOid: Option[String],
) extends MassaluovutusQueryLuokalleJaaneet {
  override def run(application: KoskiApplication, writer: QueryResultWriter)
    (implicit user: Session with SensitiveDataAllowed): Either[String, Unit] = withKoskiSpecificSession { implicit koskiUser =>
    forEachResult(application, writer.queryId.toString, result => writer.jsonFileName(tiedostonNimi(result))) { result =>
      writer.putJson(tiedostonNimi(result), result)
    }
  }

  private def tiedostonNimi(result: MassaluovutusQueryLuokalleJaaneetResult): String =
    s"${result.opiskeluoikeus.oid.getOrElse("")}_luokka_${result.luokka}"

  override def fillAndValidate(implicit user: Session): Either[HttpStatus, MassaluovutusQueryLuokalleJaaneet] =
    if (organisaatioOid.isEmpty) {
      defaultOrganisaatio.map(o => copy(organisaatioOid = Some(o)))
    } else {
      Right(this)
    }
}
