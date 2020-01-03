package fi.oph.koski.organisaatio

object OrganisaatioHierarkiaFilter {
  def prune(h: OrganisaatioHierarkia, filterF: OrganisaatioHierarkia => Boolean): Option[OrganisaatioHierarkia] = {
    if (filterF(h)) {
      Some(h)
    } else {
      h.children.flatMap(c => prune(c, filterF)) match {
        case Nil => None
        case children => Some(h.copy(children = children))
      }
    }
  }
}

case class OrganisaatioHierarkiaFilter(query: String, lang: String) {
  private def matchQuery(h: OrganisaatioHierarkia) = h.nimi.get(lang).toLowerCase.contains(query.toLowerCase)

  def prune(h: OrganisaatioHierarkia): Option[OrganisaatioHierarkia] =
    OrganisaatioHierarkiaFilter.prune(h, matchQuery)

  def filter(roots: Iterable[OrganisaatioHierarkia]): Iterable[OrganisaatioHierarkia] = roots.flatMap(prune)
}
