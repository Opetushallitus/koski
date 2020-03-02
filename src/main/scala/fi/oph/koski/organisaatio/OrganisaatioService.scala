package fi.oph.koski.organisaatio

import fi.oph.koski.koskiuser.{KoskiSession, KäyttöoikeusOrg}

class OrganisaatioService(organisaatioRepository: OrganisaatioRepository) {
  def searchInAllOrganizations(query: Option[String])(implicit u: KoskiSession): List[OrganisaatioHierarkia] = {
    query match {
      case Some(qry) if qry.length >= 3 =>
        organisaatioRepository.findHierarkia(qry).sortBy(organisaatioNimi)
      case _ => Nil
    }
  }

  def searchInEntitledOrganizations(query: Option[String], orgTypes: OrgTypesToShow)(implicit u: KoskiSession): Iterable[OrganisaatioHierarkia] = {
    val orgs = getOrganisaatiot(orgTypes)

    query match {
      case Some(qry) => OrganisaatioHierarkiaFilter(qry, u.lang).filter(orgs)
      case None => orgs
    }
  }

  private def getOrganisaatiot(orgTypes: OrgTypesToShow)(implicit u: KoskiSession) = orgTypes match {
    case VarhaiskasvatusToimipisteet => ostopalveluOrganisaatiot
    case OmatOrganisaatiot => omatOrganisaatiot
    case Kaikki => omatOrganisaatiot ++ ostopalveluOrganisaatiot
  }

  def omatOrganisaatiot(implicit u: KoskiSession): List[OrganisaatioHierarkia] =
    u.orgKäyttöoikeudet.filter(_.juuri).toList.flatMap { ko: KäyttöoikeusOrg =>
      organisaatioRepository.getOrganisaatioHierarkia(ko.organisaatio.oid)
    }.sortBy(organisaatioNimi)

  def ostopalveluOrganisaatiot(implicit u: KoskiSession): List[OrganisaatioHierarkia] =
    if (u.hasKoulutustoimijaVarhaiskasvatuksenJärjestäjäAccess) {
      organisaatioRepository.findVarhaiskasvatusHierarkiat
        .filterNot(h => u.varhaiskasvatusKoulutustoimijat.contains(h.oid)) // karsi oman organisaation päiväkodit pois
        .sortBy(organisaatioNimi)
    } else {
      Nil
    }

  private def organisaatioNimi(implicit u: KoskiSession): OrganisaatioHierarkia => String = _.nimi.get(u.lang)
}
