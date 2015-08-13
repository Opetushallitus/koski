package fi.oph.tor

import java.sql.Timestamp
import java.util.Date
import slick.jdbc.{PositionedParameters, SetParameter}
import SetParameter._

trait SuoritusFilter {
  def whereClauseFraction: String
  def apply(positionedParams: PositionedParameters)
  /**
   *  Whether or not we should include parents of matching rows
   */
  def recursive: Boolean = false
}

trait StringEqualsFilter extends SuoritusFilter {
  def key: String
  def whereClauseFraction = key + "=?"
}

case class HenkilönSuoritukset(personOid: String) extends StringEqualsFilter {
  def key = "person_oid"
  def apply(p: PositionedParameters) = SetParameter[String].apply(personOid, p)
}

case class OrganisaationSuoritukset(organisaatioOid: String) extends StringEqualsFilter {
  def key = "organisaatio_oid"
  def apply(p: PositionedParameters) = SetParameter[String].apply(organisaatioOid, p)
}

case class KoulutusModuulinSuoritukset(komoOid: String) extends StringEqualsFilter {
  def key = "komo_oid"
  def apply(p: PositionedParameters) = SetParameter[String].apply(komoOid, p)
  override def recursive = true
}

case class PäivämääränJälkeisetSuoritukset(päivämäärä: Date) extends SuoritusFilter {
  def whereClauseFraction = "suorituspaiva>?"
  def apply(p: PositionedParameters) = SetParameter[java.sql.Timestamp].apply(new Timestamp(päivämäärä.getTime), p)
  override def recursive = true
}