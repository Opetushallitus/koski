package fi.oph.koski.massaluovutus.organisaationopiskeluoikeudet

import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.db.SQLHelpers
import fi.oph.koski.massaluovutus.MassaluovutusUtils
import fi.oph.koski.organisaatio.OrganisaationAlaisetOrganisaatiot
import fi.oph.koski.schema.Organisaatio
import fi.oph.koski.schema.annotation.EnumValues
import fi.oph.scalaschema.annotation.{Description, Title}
import slick.jdbc.SQLActionBuilder

@Title("Organisaation opiskeluoikeudet")
@Description("Palauttaa hakuehtojen mukaiset organisaation ja sen alaorganisaatioiden opiskeluoikeudet.")
trait MassaluovutusQueryOrganisaationOpiskeluoikeudet extends MassaluovutusQueryOrganisaationOpiskeluoikeudetBase {
  @EnumValues(Set("organisaationOpiskeluoikeudet"))
  def `type`: String

  def withOrganisaatioOid(organisaatioOid: Organisaatio.Oid): MassaluovutusQueryOrganisaationOpiskeluoikeudet

  protected def organisaatioFilter(organisaatiot: OrganisaationAlaisetOrganisaatiot): SQLActionBuilder =
    SQLHelpers.concat(sql" AND ", MassaluovutusUtils.organisaatioFilter(organisaatiot))
}

object MassaluovutusQueryOrganisaationOpiskeluoikeudet {
  def allowedKoulutusmuodot: Set[String] = Set(
    "aikuistenperusopetus",
    "ammatillinenkoulutus",
    "diatutkinto",
    "ebtutkinto",
    "esiopetus",
    "europeanschoolofhelsinki",
    "ibtutkinto",
    "internationalschool",
    "korkeakoulutus",
    "lukiokoulutus",
    "luva",
    "muukuinsaanneltykoulutus",
    "perusopetukseenvalmistavaopetus",
    "perusopetuksenlisaopetus",
    "perusopetus",
    "taiteenperusopetus",
    "tuva",
    "vapaansivistystyonkoulutus",
  )
}
