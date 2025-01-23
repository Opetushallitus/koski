package fi.oph.koski.massaluovutus.luokallejaaneet

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.massaluovutus.MassaluovutusUtils.defaultOrganisaatio
import fi.oph.koski.massaluovutus.{QueryFormat, QueryResultWriter}
import fi.oph.koski.schema.annotation.EnumValues

case class MassaluovutusQueryLuokalleJaaneetJson(
  `type`: String = "luokallejaaneet",
  @EnumValues(Set(QueryFormat.json))
  format: String = QueryFormat.json,
  organisaatioOid: Option[String],
) extends MassaluovutusQueryLuokalleJaaneet {
  override def run(application: KoskiApplication, writer: QueryResultWriter)(implicit user: KoskiSpecificSession): Either[String, Unit] = {
    forEachResult(application) { result =>
      writer.putJson(s"${result.opiskeluoikeus.oid.getOrElse("")}_luokka_${result.luokka}", result)
    }
  }

  override def fillAndValidate(implicit user: KoskiSpecificSession): Either[HttpStatus, MassaluovutusQueryLuokalleJaaneet] =
    if (organisaatioOid.isEmpty) {
      defaultOrganisaatio.map(o => copy(organisaatioOid = Some(o)))
    } else {
      Right(this)
    }
}
