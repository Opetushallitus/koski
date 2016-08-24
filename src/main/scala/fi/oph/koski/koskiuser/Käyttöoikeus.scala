package fi.oph.koski.koskiuser

import fi.oph.koski.schema.OrganisaatioWithOid

sealed trait Käyttöoikeus {
  def ryhmä: Käyttöoikeusryhmä
  def oppilaitostyyppi: Option[String]
}

/**
  * Organisation access rights, juuri is set to true when the organisaatio is at the root of an organisation hierarchy for this user
  */
case class OrganisaatioKäyttöoikeus(organisaatio: OrganisaatioWithOid, oppilaitostyyppi: Option[String], ryhmä: OrganisaationKäyttöoikeusryhmä, juuri: Boolean) extends Käyttöoikeus

case class GlobaaliKäyttöoikeus(ryhmä: GlobaaliKäyttöoikeusryhmä) extends Käyttöoikeus {
  def oppilaitostyyppi = None
}