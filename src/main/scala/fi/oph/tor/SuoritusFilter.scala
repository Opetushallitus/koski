package fi.oph.tor

import slick.jdbc.{PositionedParameters, SetParameter}
import SetParameter._

trait SuoritusFilter {
  def key: String
  def apply(positionedParams: PositionedParameters)
  /**
   *  Whether or not we should include parents of matching rows
   */
  def recursive: Boolean = false
}

case class HenkilönSuoritukset(personOid: String) extends SuoritusFilter {
  def key = "person_oid"
  def apply(p: PositionedParameters) = SetParameter[String].apply(personOid, p)
}

case class OrganisaationSuoritukset(organisaatioOid: String) extends SuoritusFilter {
  def key = "organisaatio_oid"
  def apply(p: PositionedParameters) = SetParameter[String].apply(organisaatioOid, p)
}

case class KoulutusModuulinSuoritukset(komoOid: String) extends SuoritusFilter {
  def key = "komo_oid"
  def apply(p: PositionedParameters) = SetParameter[String].apply(komoOid, p)
  override def recursive = true
}