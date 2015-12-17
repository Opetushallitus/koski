package fi.oph.tor.organisaatio

import fi.oph.tor.schema.OidOrganisaatio

object InMemoryOrganisaatioRepository {
  val empty = new InMemoryOrganisaatioRepository(Nil)

  private def flatten(orgs: List[OrganisaatioHierarkia]): List[OrganisaatioHierarkia] = {
    orgs.flatMap { org => org :: flatten(org.children) }
  }
}

class InMemoryOrganisaatioRepository(roots: List[OrganisaatioHierarkia]) extends OrganisaatioRepository {
  private val orgs: Map[String, OrganisaatioHierarkia] = InMemoryOrganisaatioRepository.flatten(roots).map(org => (org.oid, org)).toMap

  def getOrganisaatio(id: String): Option[OrganisaatioHierarkia] = orgs.get(id)

  def findOrganisaatiot(f: (OrganisaatioHierarkia => Boolean)): Iterable[OrganisaatioHierarkia] = {
    orgs.values.filter(f)
  }

  def hasReadAccess(organisaatio: OidOrganisaatio) = {
    getOrganisaatio(organisaatio.oid).isDefined
  }

  def oids = orgs.keys
}
