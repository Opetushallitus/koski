package fi.oph.tor.organisaatio

case class OrganisaatioPuu(roots: List[Organisaatio]) {
  def findById(id: String): Option[Organisaatio] = findOrganisaatiot(_.oid == id).headOption

  def findOrganisaatiot(f: (Organisaatio => Boolean)): List[Organisaatio] = {
    flatten(roots).filter(f)
  }

  def flatten(orgs: List[Organisaatio]): List[Organisaatio] = {
    orgs.flatMap { org =>
      org :: flatten(org.children)
    }
  }
}
