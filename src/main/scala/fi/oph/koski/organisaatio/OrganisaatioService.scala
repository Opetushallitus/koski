package fi.oph.koski.organisaatio

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.{KoskiSession, KäyttöoikeusOrg}
import fi.oph.koski.perustiedot.VarhaiskasvatusToimipistePerustiedot

class OrganisaatioService(application: KoskiApplication) {
  val ostopalveluRootOid = "OSTOPALVELUTAIPALVELUSETELI"
  val organisaatioRepository: OrganisaatioRepository = application.organisaatioRepository
  private val perustiedot = VarhaiskasvatusToimipistePerustiedot(application.perustiedotIndexer)
  private val localizationRepository = application.localizationRepository

  def searchInAllOrganizations(query: Option[String])(implicit user: KoskiSession): Iterable[OrganisaatioHierarkia] = {
    query match {
      case Some(qry) if qry.length >= 3 =>
        organisaatioRepository.findHierarkia(qry).sortBy(organisaatioNimi)
      case _ => Nil
    }
  }

  def searchInEntitledOrganizations(query: Option[String], orgTypes: OrgTypesToShow)(implicit user: KoskiSession): Iterable[OrganisaatioHierarkia] = {
    val orgs = getOrganisaatiot(orgTypes)
    query match {
      case Some(qry) => OrganisaatioHierarkiaFilter(qry, user.lang).filter(orgs)
      case None => orgs
    }
  }

  def omatOstopalveluOrganisaatiot(implicit user: KoskiSession): List[OrganisaatioHierarkia] =
    perustiedot.haeVarhaiskasvatustoimipisteet(user.varhaiskasvatusKoulutustoimijat) match {
      case päiväkoditJoihinTallennettuOpiskeluoikeuksia if päiväkoditJoihinTallennettuOpiskeluoikeuksia.nonEmpty =>
        OrganisaatioHierarkia.flatten(kaikkiOstopalveluOrganisaatioHierarkiat).filter(o => päiväkoditJoihinTallennettuOpiskeluoikeuksia.contains(o.oid))
      case _ => Nil
    }

  private def getOrganisaatiot(orgTypes: OrgTypesToShow)(implicit user: KoskiSession) = orgTypes match {
    case OmatOrganisaatiot => omatOrganisaatioHierarkiat
    case VarhaiskasvatusToimipisteet => kaikkiOstopalveluOrganisaatioHierarkiat
    case Kaikki => omatOrganisaatioHierarkiat ++ omatOstopalveluOrganisaatioHierarkiat
  }

  private def omatOrganisaatioHierarkiat(implicit user: KoskiSession): List[OrganisaatioHierarkia] =
    user.orgKäyttöoikeudet.filter(_.juuri).toList.flatMap { ko: KäyttöoikeusOrg =>
      organisaatioRepository.getOrganisaatioHierarkia(ko.organisaatio.oid)
    }.sortBy(organisaatioNimi)

  private def kaikkiOstopalveluOrganisaatioHierarkiat(implicit user: KoskiSession) = if (user.hasKoulutustoimijaVarhaiskasvatuksenJärjestäjäAccess) {
    organisaatioRepository.findVarhaiskasvatusHierarkiat
      .filterNot(isOmanOrganisaationPäiväkoti)
      .sortBy(organisaatioNimi)
  } else {
    Nil
  }

  private def omatOstopalveluOrganisaatioHierarkiat(implicit user: KoskiSession) = omatOstopalveluOrganisaatiot match {
    case Nil => Nil
    case children => List(OrganisaatioHierarkia(
      oid = ostopalveluRootOid,
      nimi = localizationRepository.get("Ostopalvelu/palveluseteli"),
      children = children,
      organisaatiotyypit = List(ostopalveluRootOid)
    ))
  }

  private def organisaatioNimi(implicit user: KoskiSession): OrganisaatioHierarkia => String = _.nimi.get(user.lang)

  private def isOmanOrganisaationPäiväkoti(org: OrganisaatioHierarkia)(implicit user: KoskiSession) =
    user.varhaiskasvatusKoulutustoimijat.contains(org.oid)
}
