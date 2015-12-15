package fi.oph.tor.organisaatio

// TODO: eliminoi tämä pois
case class OrganisaatioPuu(roots: List[OrganisaatioHierarkia]) {
  def findById(id: String): Option[OrganisaatioHierarkia] = findOrganisaatiot(_.oid == id).headOption

  def findOrganisaatiot(f: (OrganisaatioHierarkia => Boolean)): List[OrganisaatioHierarkia] = {
    flatten(roots).filter(f)
  }

  def flatten(orgs: List[OrganisaatioHierarkia] = roots): List[OrganisaatioHierarkia] = {
    orgs.flatMap { org =>
      org :: flatten(org.children)
    }
  }
}
