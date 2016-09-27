package fi.oph.koski.organisaatio

import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.schema._

case class OrganisaatioHierarkia(oid: String, oppilaitosnumero: Option[Koodistokoodiviite], nimi: LocalizedString, yTunnus: Option[String], organisaatiotyypit: List[String], oppilaitostyyppi: Option[String], children: List[OrganisaatioHierarkia]) {
  def find(oid: String): Option[OrganisaatioHierarkia] = {
    if (oid == this.oid) {
      Some(this)
    } else {
      children.foldLeft(None: Option[OrganisaatioHierarkia]) {
        case (Some(found), _) => Some(found)
        case (_, child) => child.find(oid)
      }
    }
  }

  def toOrganisaatio: OrganisaatioWithOid = oppilaitosnumero match {
    case Some(_) => Oppilaitos(oid, oppilaitosnumero, Some(nimi))
    case _ => OidOrganisaatio(oid, Some(nimi), yTunnus)
  }

  def toOppilaitos: Option[Oppilaitos] = oppilaitosnumero match {
    case Some(_) => Some(Oppilaitos(oid, oppilaitosnumero, Some(nimi)))
    case _ => None
  }
}
