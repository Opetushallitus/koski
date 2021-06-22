package fi.oph.koski.organisaatio

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.{KoskiSpecificSession, KäyttöoikeusOrg, Session}
import fi.oph.koski.perustiedot.VarhaiskasvatusToimipistePerustiedot
import fi.oph.koski.schema.Organisaatio.Oid
import fi.oph.koski.schema.OrganisaatioWithOid

class OrganisaatioService(application: KoskiApplication) {
  val ostopalveluRootOid = "OSTOPALVELUTAIPALVELUSETELI"
  val organisaatioRepository: OrganisaatioRepository = application.organisaatioRepository
  private val perustiedot = VarhaiskasvatusToimipistePerustiedot(application.perustiedotIndexer)
  private val localizationRepository = application.koskiLocalizationRepository

  def searchInAllOrganizations(query: Option[String])(implicit user: Session): Iterable[OrganisaatioHierarkia] = {
    query match {
      case Some(qry) if qry.length >= 3 =>
        organisaatioRepository.findHierarkia(qry).sortBy(organisaatioNimi)
      case _ => Nil
    }
  }

  def searchInEntitledOrganizations(query: Option[String], orgTypes: OrgTypesToShow)(implicit user: Session): Iterable[OrganisaatioHierarkia] = {
    val orgs = getOrganisaatiot(orgTypes)
    query match {
      case Some(qry) => OrganisaatioHierarkiaFilter(qry, user.lang).filter(orgs)
      case None => orgs
    }
  }

  def kaikkiKäyttöoikeudellisetOrganisaatiot(implicit user: Session): Iterable[OrganisaatioHierarkia] = {
    if (user.hasGlobalReadAccess) {
      organisaatioRepository.findAllHierarkiat
    } else {
      searchInEntitledOrganizations(None, Kaikki)
    }
  }

  def organisaationAlaisetOrganisaatiot(organisaatioOid: Oid)(implicit user: KoskiSpecificSession): List[Oid] = {
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

  def omatOstopalveluOrganisaatiot(implicit user: Session): List[OrganisaatioHierarkia] =
    koulutustoimijoidenOstopalveluOrganisaatiot(user.varhaiskasvatusKoulutustoimijat)

  def omatOrganisaatiotJaKayttooikeusroolit(implicit user: Session): List[OrganisaatioHierarkiaJaKayttooikeusrooli] =
    user.orgKäyttöoikeudet.filter(_.juuri).toList.flatMap {
      ko: KäyttöoikeusOrg => {
        val hierarkia = organisaatioRepository.getOrganisaatioHierarkia(ko.organisaatio.oid)
        if (hierarkia.isDefined) {
          ko.organisaatiokohtaisetPalveluroolit.map(palvelurooli => OrganisaatioHierarkiaJaKayttooikeusrooli(hierarkia.get, palvelurooli.rooli))
        } else {
          List.empty
        }
      }
    }.sortBy(r => (r.organisaatioHierarkia.nimi.get(user.lang), r.kayttooikeusrooli))

  def kunnat: List[OrganisaatioWithOid] = organisaatioRepository.findKunnat.flatMap(_.toKunta)

  private def koulutustoimijoidenOstopalveluOrganisaatiot(koulutustoimijat: Set[Oid])(implicit user: Session): List[OrganisaatioHierarkia] =
    perustiedot.haeVarhaiskasvatustoimipisteet(koulutustoimijat) match {
      case päiväkoditJoihinTallennettuOpiskeluoikeuksia if päiväkoditJoihinTallennettuOpiskeluoikeuksia.nonEmpty =>
        val ostopalveluHierarkiat = kaikkiOstopalveluOrganisaatiohierarkiat(excludedKoulutustoimijaOidit = koulutustoimijat)
        OrganisaatioHierarkia.flatten(ostopalveluHierarkiat).filter(o => päiväkoditJoihinTallennettuOpiskeluoikeuksia.contains(o.oid))
      case _ => Nil
    }

  private def getOrganisaatiot(orgTypes: OrgTypesToShow)(implicit user: Session) = orgTypes match {
    case OmatOrganisaatiot => omatOrganisaatioHierarkiat
    case VarhaiskasvatusToimipisteet => kaikkiOstopalveluOrganisaatiohierarkiat(excludedKoulutustoimijaOidit = user.varhaiskasvatusKoulutustoimijat)
    case Kaikki => omatOrganisaatioHierarkiat ++ omatOstopalveluOrganisaatioHierarkiat
  }

  private def omatOrganisaatioHierarkiat(implicit user: Session): List[OrganisaatioHierarkia] =
    user.orgKäyttöoikeudet.filter(_.juuri).toList.flatMap { ko: KäyttöoikeusOrg =>
      organisaatioRepository.getOrganisaatioHierarkia(ko.organisaatio.oid)
    }.sortBy(organisaatioNimi)

  private def kaikkiOstopalveluOrganisaatiohierarkiat(excludedKoulutustoimijaOidit: Set[Oid])(implicit user: Session) = if (user.hasGlobalReadAccess || user.hasKoulutustoimijaVarhaiskasvatuksenJärjestäjäAccess) {
    organisaatioRepository.findVarhaiskasvatusHierarkiat
      .filterNot(o => excludedKoulutustoimijaOidit.contains(o.oid))
      .sortBy(organisaatioNimi)
  } else {
    Nil
  }

  private def omatOstopalveluOrganisaatioHierarkiat(implicit user: Session) = omatOstopalveluOrganisaatiot match {
    case Nil => Nil
    case children => List(OrganisaatioHierarkia(
      oid = ostopalveluRootOid,
      nimi = localizationRepository.get("Ostopalvelu/palveluseteli"),
      children = children,
      organisaatiotyypit = List(ostopalveluRootOid)
    ))
  }

  private def organisaatioNimi(implicit user: Session): OrganisaatioHierarkia => String = _.nimi.get(user.lang)
}
