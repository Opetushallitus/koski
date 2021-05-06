package fi.oph.koski.valpas

import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koskiuser.{KäyttöoikeusOrg, Palvelurooli}
import fi.oph.koski.organisaatio.OrganisaatioRepository
import fi.oph.koski.schema.{Organisaatio, OrganisaatioWithOid}
import fi.oph.koski.valpas.opiskeluoikeusrepository.ValpasOppijaLaajatTiedot
import fi.oph.koski.valpas.valpasuser.{ValpasRooli, ValpasSession}

class ValpasAccessResolver(organisaatioRepository: OrganisaatioRepository) {
  def organisaatiohierarkiaOids(organisaatioOids: Set[Organisaatio.Oid])(implicit session: ValpasSession): Either[HttpStatus, Set[Organisaatio.Oid]] = {
    withOrgAccess(accessToAllOrgs(organisaatioOids), { () =>
      val childOids = organisaatioOids.flatMap(organisaatioRepository.getChildOids).flatten
      organisaatioOids ++ childOids
    })
  }

  def withOppijaAccess[T <: ValpasOppijaLaajatTiedot](oppija: T)(implicit session: ValpasSession): Either[HttpStatus, T] = {
    Either.cond(accessToSomeOrgs(oppija.oikeutetutOppilaitokset), oppija, ValpasErrorCategory.forbidden.oppija())
  }

  def withOppijaAccessAsOrganisaatio[T <: ValpasOppijaLaajatTiedot](organisaatioOid: Organisaatio.Oid)(oppija: T): Either[HttpStatus, T] = {
    Either.cond(oppija.oikeutetutOppilaitokset.contains(organisaatioOid), oppija, ValpasErrorCategory.forbidden.oppija())
  }

  def filterByOikeudet(organisaatioOidit: Set[Organisaatio.Oid])(implicit session: ValpasSession): Set[Organisaatio.Oid] = {
    organisaatioOidit.filter(oid => accessToAllOrgs(Set(oid)))
  }

  private def withOrgAccess[T](access: Boolean, fn: () => T): Either[HttpStatus, T] = {
    Either.cond(access, fn(), ValpasErrorCategory.forbidden.organisaatio())
  }

  private def accessToAllOrgs(organisaatioOids: Set[Organisaatio.Oid])(implicit session: ValpasSession): Boolean =
    onGlobaaliOppilaitosHakeutuminenOikeus || organisaatioOids.diff(oppilaitosHakeutuminenOrganisaatioOids).isEmpty

  private def accessToSomeOrgs(organisaatioOids: Set[Organisaatio.Oid])(implicit session: ValpasSession): Boolean =
    onGlobaaliOppilaitosHakeutuminenOikeus || organisaatioOids.intersect(oppilaitosHakeutuminenOrganisaatioOids).nonEmpty

  private def oppilaitosHakeutuminenOrganisaatioOids(implicit session: ValpasSession): Set[Organisaatio.Oid] =
    valpasOrganisaatiot(ValpasRooli.OPPILAITOS_HAKEUTUMINEN).map(_.oid)

  private def valpasOrganisaatiot(rooli: String)(implicit session: ValpasSession): Set[OrganisaatioWithOid] =
    session.orgKäyttöoikeudet
      .flatMap(asValpasOrgKäyttöoikeus(rooli))
      .map(_.organisaatio)

  private def asValpasOrgKäyttöoikeus(rooli: String)(orgKäyttöoikeus: KäyttöoikeusOrg): Option[KäyttöoikeusOrg] =
    if (orgKäyttöoikeus.organisaatiokohtaisetPalveluroolit.contains(Palvelurooli("VALPAS", rooli))) {
      Some(orgKäyttöoikeus)
    } else {
      None
    }

  private def onGlobaaliOppilaitosHakeutuminenOikeus(implicit session: ValpasSession): Boolean =
    session.hasGlobalValpasOikeus(Set(ValpasRooli.OPPILAITOS_HAKEUTUMINEN))
}
