package fi.oph.koski.massaluovutus.oppivelvolliset

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.json.SensitiveDataAllowed
import fi.oph.koski.koodisto.Kunta
import fi.oph.koski.koskiuser.Session
import fi.oph.koski.log.Logging
import fi.oph.koski.massaluovutus.{QueryFormat, QueryResultWriter, ValpasMassaluovutusQueryParameters}
import fi.oph.koski.schema.annotation.EnumValues
import fi.oph.koski.valpas.log.ValpasAuditLog
import fi.oph.koski.valpas.massaluovutus.{ValpasMassaluovutusOppija, ValpasMassaluovutusResult}
import fi.oph.koski.valpas.oppija.ValpasAccessResolver
import fi.oph.koski.valpas.valpasuser.ValpasSession
import fi.oph.scalaschema.annotation.{Description, Title}

@Title("Kunnan oppivelvolliset oppijat")
@Description("Palauttaa kunnan oppijat, jotka ovat oppivelvollisuuden piirissä.")
case class ValpasOppivelvollisetQuery(
  @EnumValues(Set("oppivelvolliset"))
  `type`: String = "oppivelvolliset",
  @EnumValues(Set(QueryFormat.json))
  format: String = QueryFormat.json,
  @Description("Kunnan organisaatio-oid")
  kuntaOid: String
) extends ValpasMassaluovutusQueryParameters with Logging {

  override def run(application: KoskiApplication, writer: QueryResultWriter)(implicit user: Session with SensitiveDataAllowed): Either[String, Unit] = {
    // TODO: Implement in next step
    Left("Not implemented yet")
  }

  override def queryAllowed(application: KoskiApplication)(implicit user: Session): Boolean = {
    // TODO: Implement in later step
    false
  }

  private def getKuntaKoodiByKuntaOid(application: KoskiApplication, kuntaOid: String): Option[String] = {
    Kunta.validateAndGetKuntaKoodi(application.organisaatioService, application.koodistoPalvelu, kuntaOid) match {
      case Right(kuntaKoodi) => Some(kuntaKoodi)
      case Left(error) =>
        logger.warn(s"ValpasOppivelvollisetQuery getKuntaKoodiByKuntaOid: ${error.errors.map(_.toString).mkString(", ")}")
        None
    }
  }
}
