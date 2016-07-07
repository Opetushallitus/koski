package fi.oph.koski.koskiuser

import fi.oph.koski.schema.OrganisaatioWithOid

sealed trait Käyttöoikeus {
  def ryhmä: Käyttöoikeusryhmä
  def oppilaitostyyppi: Option[String]
}

case class OrganisaatioKäyttöoikeus(organisaatio: OrganisaatioWithOid, oppilaitostyyppi: Option[String], ryhmä: OrganisaationKäyttöoikeusryhmä) extends Käyttöoikeus

case class GlobaaliKäyttöoikeus(ryhmä: GlobaaliKäyttöoikeusryhmä) extends Käyttöoikeus {
  def oppilaitostyyppi = None
}