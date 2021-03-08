package fi.oph.koski.valpas

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.{KäyttöoikeusOrg, Palvelurooli}
import fi.oph.koski.schema.OrganisaatioWithOid
import fi.oph.koski.valpas.valpasuser.{ValpasRooli, ValpasSession}

class ValpasAccessResolver(application: KoskiApplication) {
  val organisaatioRepository = application.organisaatioRepository

  def oppilaitosHakeutuminenOrganisaatioOids(implicit session: ValpasSession): Set[String] =
    valpasOrganisaatiot(ValpasRooli.OPPILAITOS_HAKEUTUMINEN).map(_.oid)

  def organisaatiohierarkiaOids(organisaatioOids: Set[String])(implicit session: ValpasSession): Option[Set[String]] = {
    if (accessToAllOrgs(organisaatioOids)) {
      val childOids = organisaatioOids.flatMap(organisaatioRepository.getChildOids).flatten
      Some(organisaatioOids ++ childOids)
    } else {
      None
    }
  }

  def accessToAllOrgs(organisaatioOids: Set[String])(implicit session: ValpasSession): Boolean =
    onGlobaaliOppilaitosHakeutuminenOikeus || organisaatioOids.diff(oppilaitosHakeutuminenOrganisaatioOids).isEmpty

  def accessToSomeOrgs(organisaatioOids: Set[String])(implicit session: ValpasSession): Boolean =
    onGlobaaliOppilaitosHakeutuminenOikeus || organisaatioOids.intersect(oppilaitosHakeutuminenOrganisaatioOids).nonEmpty

  def valpasOrganisaatiot(rooli: String)(implicit session: ValpasSession): Set[OrganisaatioWithOid] =
    session.orgKäyttöoikeudet
      .flatMap(asValpasOrgKäyttöoikeus(rooli))
      .map(_.organisaatio)

  def asValpasOrgKäyttöoikeus(rooli: String)(orgKäyttöoikeus: KäyttöoikeusOrg): Option[KäyttöoikeusOrg] = {
    orgKäyttöoikeus.organisaatiokohtaisetPalveluroolit.contains(Palvelurooli("VALPAS", rooli)) match {
      case true => Some(orgKäyttöoikeus)
      case false => None
    }
  }

  private def onGlobaaliOppilaitosHakeutuminenOikeus(implicit session: ValpasSession): Boolean =
    session.hasGlobalValpasOikeus(Set(ValpasRooli.OPPILAITOS_HAKEUTUMINEN))
}
