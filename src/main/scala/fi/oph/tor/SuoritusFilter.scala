package fi.oph.tor

import fi.oph.tor.db.Tables
import slick.driver.PostgresDriver.api._

trait SuoritusFilter {
  type SuoritusQuery = Query[Tables.Suoritus, Tables.SuoritusRow, Seq]

  def apply(query: SuoritusQuery): SuoritusQuery

  def and(filter: SuoritusFilter) = AndFilter(this, filter)
}

object KaikkiSuoritukset extends SuoritusFilter {
  def apply(query: SuoritusQuery) = query
}

case class HenkilönSuoritukset(personOid: String) extends SuoritusFilter {
  def apply(query: SuoritusQuery) = query.filter(_.personOid === personOid)
}

case class OrganisaationSuoritukset(organisaatioOid: String) extends SuoritusFilter {
  def apply(query: SuoritusQuery) = query.filter(_.organisaatioOid === organisaatioOid)
}

case class AndFilter(left: SuoritusFilter, right: SuoritusFilter) extends SuoritusFilter {
  def apply(query: SuoritusQuery) = right(left(query))
}