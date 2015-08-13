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

abstract class StringEqualsFilter(key: String, value: String) extends SuoritusFilter {
  def whereClauseFraction = key + "=?"
  def apply(p: PositionedParameters) = SetParameter[String].apply(value, p)
}

case class HenkilönSuoritukset(personOid: String) extends StringEqualsFilter("person_oid", personOid)

case class OrganisaationSuoritukset(organisaatioOid: String) extends StringEqualsFilter("organisaatio_oid", organisaatioOid)

case class SuorituksetStatuksella(status: String) extends StringEqualsFilter("status", status) {
  override def recursive = true
}

case class KoulutusModuulinSuoritukset(komoOid: String) extends StringEqualsFilter("komo_oid", komoOid) {
  override def recursive = true
}

case class PäivämääränJälkeisetSuoritukset(päivämäärä: Date) extends SuoritusFilter {
  def whereClauseFraction = "suorituspaiva>?"
  def apply(p: PositionedParameters) = SetParameter[java.sql.Timestamp].apply(new Timestamp(päivämäärä.getTime), p)
  override def recursive = true
}