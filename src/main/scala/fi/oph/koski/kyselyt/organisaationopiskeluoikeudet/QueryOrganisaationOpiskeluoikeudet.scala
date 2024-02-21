package fi.oph.koski.kyselyt.organisaationopiskeluoikeudet

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.{AccessType, KoskiSpecificSession}
import fi.oph.koski.kyselyt.{QueryParameters, ResultStream}
import fi.oph.koski.schema.Organisaatio
import fi.oph.scalaschema.annotation.EnumValue

import java.time.LocalDate

case class QueryOrganisaationOpiskeluoikeudet(
  @EnumValue("organisaationOpiskeluoikeudet")
  `type`: String = "organisaationOpiskeluoikeudet",
  @EnumValue("application/json")
  format: String = "application/json",
  organisaatioOid: Option[Organisaatio.Oid],
  alku: LocalDate,
  tila: Option[String], // TODO: Lisää sallitut arvot
  tyyppi: Option[String], // TODO: Lisää sallitut arvot
) extends QueryParameters {

  def run(application: KoskiApplication): Either[String, List[ResultStream]] = {
    val orgs = application.organisaatioService.organisaationAlaisetOrganisaatiot(organisaatioOid.get)
    Right(List(ResultStream("orgs.txt", orgs.mkString("\n"))))
  }

  def queryAllowed(application: KoskiApplication)(implicit user: KoskiSpecificSession): Boolean =
    user.hasGlobalReadAccess || (
      organisaatioOid.exists(user.organisationOids(AccessType.read).contains)
        && tyyppi.forall(user.allowedOpiskeluoikeusTyypit.contains)
      )

  override def withDefaults(implicit user: KoskiSpecificSession): Either[HttpStatus, QueryOrganisaationOpiskeluoikeudet] =
    if (organisaatioOid.isEmpty) {
      defaultOrganisaatio.map(oid => copy(organisaatioOid = Some(oid)))
    } else {
      Right(this)
    }

  private def defaultOrganisaatio(implicit user: KoskiSpecificSession) = {
    val organisaatiot = user.juuriOrganisaatiot
    if (organisaatiot.isEmpty) {
      Left(KoskiErrorCategory.unauthorized("Käyttäjäoikeuksissa ei ole määritelty eksplisiittisesti lukuoikeutta yhdenkään tietyn organisaation tietoihin.")) // Mahdollista esim. pääkäyttäjän tunnuksilla
    } else if (organisaatiot.size > 1) {
      Left(KoskiErrorCategory.unauthorized("Kenttää `organisaatioOid` ei ole annettu, eikä organisaatiota voi yksiselitteisesti päätellä käyttöoikeuksista."))
    } else {
      Right(user.juuriOrganisaatiot.head.oid)
    }
  }
}
