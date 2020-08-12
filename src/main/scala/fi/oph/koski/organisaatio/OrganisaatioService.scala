package fi.oph.koski.organisaatio

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.{KoskiSession, KäyttöoikeusOrg}
import fi.oph.koski.perustiedot.VarhaiskasvatusToimipistePerustiedot
import fi.oph.koski.schema.Organisaatio.Oid

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

  def organisaationAlaisetOrganisaatiot(organisaatioOid: Oid)(implicit user: KoskiSession): List[Oid] = {
    organisaatioRepository.getOrganisaatio(organisaatioOid).toList.flatMap { org =>
      val children = organisaatioRepository.getChildOids(org.oid).toList.flatten
      if (org.toKoulutustoimija.isDefined) {
        val koulutustoimijat = user.varhaiskasvatusKoulutustoimijat + org.toKoulutustoimija.get.oid
        children ++ koulutustoimijoidenOstopalveluOrganisaatiot(koulutustoimijat).map(_.oid)
      } else {
        children
      }
    }
  }

  def omatOstopalveluOrganisaatiot(implicit user: KoskiSession): List[OrganisaatioHierarkia] =
    koulutustoimijoidenOstopalveluOrganisaatiot(user.varhaiskasvatusKoulutustoimijat)

  private def koulutustoimijoidenOstopalveluOrganisaatiot(koulutustoimijat: Set[Oid])(implicit user: KoskiSession): List[OrganisaatioHierarkia] =
    perustiedot.haeVarhaiskasvatustoimipisteet(koulutustoimijat) match {
      case päiväkoditJoihinTallennettuOpiskeluoikeuksia if päiväkoditJoihinTallennettuOpiskeluoikeuksia.nonEmpty =>
        val ostopalveluHierarkiat = kaikkiOstopalveluOrganisaatiohierarkiat(excludedKoulutustoimijaOidit = koulutustoimijat)
        OrganisaatioHierarkia.flatten(ostopalveluHierarkiat).filter(o => päiväkoditJoihinTallennettuOpiskeluoikeuksia.contains(o.oid))
      case _ => Nil
    }

  private def getOrganisaatiot(orgTypes: OrgTypesToShow)(implicit user: KoskiSession) = orgTypes match {
    case OmatOrganisaatiot => omatOrganisaatioHierarkiat
    case VarhaiskasvatusToimipisteet => kaikkiOstopalveluOrganisaatiohierarkiat(excludedKoulutustoimijaOidit = user.varhaiskasvatusKoulutustoimijat)
    case Kaikki => omatOrganisaatioHierarkiat ++ omatOstopalveluOrganisaatioHierarkiat
  }

  private def omatOrganisaatioHierarkiat(implicit user: KoskiSession): List[OrganisaatioHierarkia] =
    user.orgKäyttöoikeudet.filter(_.juuri).toList.flatMap { ko: KäyttöoikeusOrg =>
      organisaatioRepository.getOrganisaatioHierarkia(ko.organisaatio.oid)
    }.sortBy(organisaatioNimi)

  private def kaikkiOstopalveluOrganisaatiohierarkiat(excludedKoulutustoimijaOidit: Set[Oid])(implicit user: KoskiSession) = if (user.hasGlobalReadAccess || user.hasKoulutustoimijaVarhaiskasvatuksenJärjestäjäAccess) {
    organisaatioRepository.findVarhaiskasvatusHierarkiat
      .filterNot(o => excludedKoulutustoimijaOidit.contains(o.oid))
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
}
