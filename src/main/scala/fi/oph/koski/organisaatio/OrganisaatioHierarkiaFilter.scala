package fi.oph.koski.organisaatio

case class OrganisaatioHierarkiaFilter(query: String, lang: String) {
  private def matchQuery(h: OrganisaatioHierarkia) = h.nimi.get(lang).toLowerCase.contains(query.toLowerCase)

  def prune(h: OrganisaatioHierarkia): Option[OrganisaatioHierarkia] = {
    if (matchQuery(h)) {
      Some(h)
    } else {
      h.children.flatMap(prune) match {
        case Nil => None
        case children => Some(h.copy(children = children))
      }
    }
  }

  def filter(roots: Iterable[OrganisaatioHierarkia]) = roots.flatMap(prune)
}