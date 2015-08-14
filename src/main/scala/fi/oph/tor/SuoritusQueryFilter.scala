package fi.oph.tor

import java.sql.Timestamp
import java.util.Date
import slick.jdbc.{PositionedParameters, SetParameter}
import SetParameter._

case class SuoritusQuery(filters: List[SuoritusQueryFilter] = List(), includeChildren: Boolean = false) {
  def withFilter(filter: SuoritusQueryFilter) = copy (filters = filter :: filters)
}

trait SuoritusQueryFilter {
  def whereClauseFraction: String
  def apply(positionedParams: PositionedParameters)
  /**
   *  Whether or not we need to recursively look for the parents of matching rows (needed if the match doesn't automatically include parents, like when searching by personOid)
   */
  def searchParentsRecursively: Boolean = false
}

abstract class StringEqualsFilter(key: String, value: String) extends SuoritusQueryFilter {
  def whereClauseFraction = key + "=?"
  def apply(p: PositionedParameters) = SetParameter[String].apply(value, p)
}

case class HenkilönSuoritukset(personOid: String) extends StringEqualsFilter("person_oid", personOid)

case class OrganisaationSuoritukset(organisaatioOid: String) extends StringEqualsFilter("organisaatio_oid", organisaatioOid)

case class SuorituksetStatuksella(status: String) extends StringEqualsFilter("status", status) {
  override def searchParentsRecursively = true
}

case class KoulutusModuulinSuoritukset(komoOid: String) extends StringEqualsFilter("komo_oid", komoOid) {
  override def searchParentsRecursively = true
}

case class PäivämääränJälkeisetSuoritukset(päivämäärä: Date) extends SuoritusQueryFilter {
  def whereClauseFraction = "suorituspaiva>?"
  def apply(p: PositionedParameters) = SetParameter[java.sql.Timestamp].apply(new Timestamp(päivämäärä.getTime), p)
  override def searchParentsRecursively = true
}