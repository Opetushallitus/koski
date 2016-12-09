package fi.oph.koski.opiskeluoikeus

trait OpiskeluoikeusSortOrder {
  def field: String
}

case class Ascending(field: String) extends OpiskeluoikeusSortOrder
case class Descending(field: String) extends OpiskeluoikeusSortOrder

object OpiskeluoikeusSortOrder {
  val oppijaOid = "oppijaOid"
}