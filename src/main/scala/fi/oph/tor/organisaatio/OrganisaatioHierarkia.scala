package fi.oph.tor.organisaatio

import fi.oph.tor.schema.OidOrganisaatio

case class OrganisaatioHierarkia(oid: String, nimi: String, organisaatiotyypit: List[String], children: List[OrganisaatioHierarkia]) {
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
  def toOrganisaatio: OidOrganisaatio = OidOrganisaatio(oid, Some(nimi))
}
