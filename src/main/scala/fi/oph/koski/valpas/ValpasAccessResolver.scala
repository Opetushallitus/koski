package fi.oph.koski.valpas

import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koskiuser.{KäyttöoikeusOrg, Palvelurooli}
import fi.oph.koski.organisaatio.OrganisaatioRepository
import fi.oph.koski.schema.{Opiskeluoikeus, Organisaatio, OrganisaatioWithOid}
import fi.oph.koski.valpas.opiskeluoikeusrepository.ValpasOppijaLaajatTiedot
import fi.oph.koski.valpas.valpasuser.{ValpasRooli, ValpasSession}

class ValpasAccessResolver(organisaatioRepository: OrganisaatioRepository) {
  def assertAccessToOrg(
    rooli: ValpasRooli.Role
  )(
    organisaatioOid: Organisaatio.Oid
  )(
    implicit session: ValpasSession
  ): Either[HttpStatus, Unit] = {
    withAccessToAllOrgs(rooli)(Set(organisaatioOid), () => ())
  }

  def withOppijaAccess[T <: ValpasOppijaLaajatTiedot](
    rooli: ValpasRooli.Role
  )(
    oppija: T
  )(
    implicit session: ValpasSession
  ): Either[HttpStatus, T] = {
    Either.cond(accessToSomeOrgs(rooli)(oppija.oikeutetutOppilaitokset), oppija, ValpasErrorCategory.forbidden.oppija())
  }

  def withOppijaAccessAsOrganisaatio[T <: ValpasOppijaLaajatTiedot]
    (organisaatioOid: Organisaatio.Oid)(oppija: T)
  : Either[HttpStatus, T] = {
    Either.cond(oppija.oikeutetutOppilaitokset.contains(organisaatioOid), oppija, ValpasErrorCategory.forbidden.oppija())
  }

  def withOpiskeluoikeusAccess[T <: ValpasOppijaLaajatTiedot]
    (opiskeluoikeusOid: Opiskeluoikeus.Oid)(oppija: T)
  : Either[HttpStatus, T] = {
    val valvottavat = oppija.opiskeluoikeudet.filter(_.onValvottava).map(_.oid)
    Either.cond(valvottavat.contains(opiskeluoikeusOid), oppija, ValpasErrorCategory.forbidden.opiskeluoikeus())
  }

  def filterByOikeudet(
    rooli: ValpasRooli.Role
  )(
    organisaatioOidit: Set[Organisaatio.Oid]
  )(
    implicit session: ValpasSession
  ): Set[Organisaatio.Oid] = {
    organisaatioOidit.filter(oid => accessToAllOrgs(rooli)(Set(oid)))
  }

  private def withAccessToAllOrgs[T](
    roolit: ValpasRooli.Role
  )(
    organisaatioOids: Set[Organisaatio.Oid],
    fn: () => T
  )(implicit session: ValpasSession)
  : Either[HttpStatus, T] = {
    Either.cond(accessToAllOrgs(roolit)(organisaatioOids), fn(), ValpasErrorCategory.forbidden.organisaatio())
  }

  private def accessToAllOrgs(
    rooli: ValpasRooli.Role
  )(
    organisaatioOids: Set[Organisaatio.Oid],
  )(
    implicit session: ValpasSession
  ): Boolean =
    onGlobaaliOikeus(rooli) || organisaatioOids.diff(oppilaitosOrganisaatioOids(rooli)).isEmpty

  private def accessToSomeOrgs(
    rooli: ValpasRooli.Role
  )(
    organisaatioOids: Set[Organisaatio.Oid],
  )(
    implicit session: ValpasSession
  ): Boolean =
    onGlobaaliOikeus(rooli) || organisaatioOids.intersect(oppilaitosOrganisaatioOids(rooli)).nonEmpty

  private def oppilaitosOrganisaatioOids(
    rooli: ValpasRooli.Role
  )(implicit session: ValpasSession): Set[Organisaatio.Oid] =
    valpasOrganisaatiot(rooli).map(_.oid)

  private def valpasOrganisaatiot(
    rooli: ValpasRooli.Role
  )(implicit session: ValpasSession): Set[OrganisaatioWithOid] =
    session.orgKäyttöoikeudet
      .flatMap(asValpasOrgKäyttöoikeus(rooli))
      .map(_.organisaatio)

  private def asValpasOrgKäyttöoikeus(
    rooli: ValpasRooli.Role
  )(
    orgKäyttöoikeus: KäyttöoikeusOrg
  ): Option[KäyttöoikeusOrg] =
    if (orgKäyttöoikeus.organisaatiokohtaisetPalveluroolit.contains(Palvelurooli("VALPAS", rooli))) {
      Some(orgKäyttöoikeus)
    } else {
      None
    }

  private def onGlobaaliOikeus(rooli: ValpasRooli.Role)(implicit session: ValpasSession): Boolean =
    session.hasGlobalValpasOikeus(Set(rooli))
}
