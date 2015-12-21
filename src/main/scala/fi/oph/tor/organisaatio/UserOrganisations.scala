package fi.oph.tor.organisaatio

import fi.oph.tor.schema.Organisaatio

object UserOrganisations {
  def apply(roots: List[OrganisaatioHierarkia]) = new UserOrganisations(flatten(roots).map(org => (org.oid, org)).toMap)
  val empty = new UserOrganisations(Map.empty)

  private def flatten(orgs: List[OrganisaatioHierarkia]): List[OrganisaatioHierarkia] = {
    orgs.flatMap { org =>
      org :: flatten(org.children)
    }
  }
}
class UserOrganisations(orgs: Map[String, OrganisaatioHierarkia]) {
  def findById(id: String): Option[OrganisaatioHierarkia] = orgs.get(id)

  def findOrganisaatiot(f: (OrganisaatioHierarkia => Boolean)): Iterable[OrganisaatioHierarkia] = {
    orgs.values.filter(f)
  }

  def hasReadAccess(organisaatio: Organisaatio) = {
    findById(organisaatio.oid).isDefined
  }

  def oids = orgs.keys
}
