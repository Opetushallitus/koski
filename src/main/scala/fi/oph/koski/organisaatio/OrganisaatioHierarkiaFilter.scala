package fi.oph.koski.organisaatio

object OrganisaatioHierarkiaFilter {
  def filter(h: OrganisaatioHierarkia, condition: OrganisaatioHierarkia => Boolean): Option[OrganisaatioHierarkia] = {
    if (condition(h)) {
      Some(h)
    } else {
      val filtered = h.children.view.flatMap(c => filter(c, condition))
      if (filtered.isEmpty) {
        None
      } else {
        Some(h.copy(children = filtered.toList))
      }
    }
  }
}

case class OrganisaatioHierarkiaFilter(query: String, lang: String) {
  private def matchQuery(h: OrganisaatioHierarkia) = h.nimi.get(lang).toLowerCase.contains(query.toLowerCase)

  def prune(h: OrganisaatioHierarkia): Option[OrganisaatioHierarkia] =
    OrganisaatioHierarkiaFilter.filter(h, matchQuery)

  def filter(roots: Iterable[OrganisaatioHierarkia]): Iterable[OrganisaatioHierarkia] = roots.flatMap(prune)
}
