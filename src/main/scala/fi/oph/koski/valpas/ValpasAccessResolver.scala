package fi.oph.koski.valpas

import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koskiuser.{KäyttöoikeusOrg, Palvelurooli}
import fi.oph.koski.schema.{Opiskeluoikeus, Organisaatio, OrganisaatioWithOid}
import fi.oph.koski.valpas.opiskeluoikeusrepository.ValpasOppijaLaajatTiedot
import fi.oph.koski.valpas.valpasuser.{ValpasRooli, ValpasSession}

class ValpasAccessResolver {
  def assertAccessToOrg(
    rooli: ValpasRooli.Role
  )(
    organisaatioOid: Organisaatio.Oid
  )(
    implicit session: ValpasSession
  ): Either[HttpStatus, Unit] = {
    withAccessToAllOrgs(rooli)(Set(organisaatioOid), () => ())
  }

  def assertAccessToAnyOrg(
    rooli: ValpasRooli.Role
  )(
    implicit session: ValpasSession
  ): Either[HttpStatus, Unit] = {
    Either.cond(accessToAnyOrg(rooli), () => (), ValpasErrorCategory.forbidden.toiminto())
  }

  def withOppijaAccess[T <: ValpasOppijaLaajatTiedot](
    oppija: T
  )(
    implicit session: ValpasSession
  ): Either[HttpStatus, T] =
    withOppijaAccessAsAnyRole(
      Seq(
        ValpasRooli.OPPILAITOS_HAKEUTUMINEN,
        ValpasRooli.OPPILAITOS_MAKSUTTOMUUS,
        ValpasRooli.OPPILAITOS_SUORITTAMINEN,
        ValpasRooli.KUNTA
      )
    )(oppija)

  def withOppijaAccessAsAnyRole[T <: ValpasOppijaLaajatTiedot](
    roolit: Seq[ValpasRooli.Role]
  )(
    oppija: T
  )(
    implicit session: ValpasSession
  ): Either[HttpStatus, T] = {
    if (roolit.exists(withOppijaAccessAsRole(_)(oppija).isRight)) {
      Right(oppija)
    } else {
      Left(ValpasErrorCategory.forbidden.oppija())
    }
  }

  def withOppijaAccessAsRole[T <: ValpasOppijaLaajatTiedot](
    rooli: ValpasRooli.Role
  )(
    oppija: T
  )(
    implicit session: ValpasSession
  ): Either[HttpStatus, T] = {
    rooli match {
      case ValpasRooli.OPPILAITOS_HAKEUTUMINEN =>
        Either.cond(
          oppija.hakeutumisvalvovatOppilaitokset.nonEmpty &&
            accessToSomeOrgs(rooli)(oppija.hakeutumisvalvovatOppilaitokset),
          oppija,
          ValpasErrorCategory.forbidden.oppija()
        )
      case ValpasRooli.OPPILAITOS_MAKSUTTOMUUS =>
        Either.cond(
          accessToAnyOrg(rooli) && oppija.onOikeusValvoaMaksuttomuutta,
          oppija,
          ValpasErrorCategory.forbidden.oppija()
        )
      case ValpasRooli.KUNTA =>
        Either.cond(
          accessToAnyOrg(rooli) && oppija.onOikeusValvoaKunnalla,
          oppija,
          ValpasErrorCategory.forbidden.oppija()
        )
      case ValpasRooli.OPPILAITOS_SUORITTAMINEN =>
        Either.cond(
          oppija.suorittamisvalvovatOppilaitokset.nonEmpty &&
            accessToSomeOrgs(rooli)(oppija.suorittamisvalvovatOppilaitokset),
          oppija,
          ValpasErrorCategory.forbidden.oppija()
        )
      case _ =>
        Left(ValpasErrorCategory.internalError(s"Tuntematon rooli ${rooli}"))
    }
  }

  def filterByOppijaAccess[T <: ValpasOppijaLaajatTiedot](
    rooli: ValpasRooli.Role
  )(
    oppijat: Seq[T]
  )(
    implicit session: ValpasSession
  ): Seq[T] = {
    rooli match {
      case ValpasRooli.OPPILAITOS_HAKEUTUMINEN =>
        oppijat.filter(
          oppija => oppija.hakeutumisvalvovatOppilaitokset.nonEmpty &&
            accessToSomeOrgs(rooli)(oppija.hakeutumisvalvovatOppilaitokset)
        )
      case ValpasRooli.OPPILAITOS_MAKSUTTOMUUS if accessToAnyOrg(rooli) =>
        oppijat.filter(_.onOikeusValvoaMaksuttomuutta)
      case ValpasRooli.OPPILAITOS_MAKSUTTOMUUS => Seq.empty
      case ValpasRooli.KUNTA if accessToAnyOrg(rooli) =>
        oppijat.filter(_.onOikeusValvoaKunnalla)
      case ValpasRooli.KUNTA => Seq.empty
      case ValpasRooli.OPPILAITOS_SUORITTAMINEN =>
        oppijat.filter(
          oppija => oppija.suorittamisvalvovatOppilaitokset.nonEmpty &&
            accessToSomeOrgs(rooli)(oppija.suorittamisvalvovatOppilaitokset)
        )
      case _ =>
        throw new InternalError(s"Tuntematon rooli ${rooli}")
    }
  }

  def withOppijaAccessAsOrganisaatio[T <: ValpasOppijaLaajatTiedot](
    rooli: ValpasRooli.Role
  )(
    organisaatioOid: Organisaatio.Oid
  )(
    oppija: T
  )(
    implicit session: ValpasSession
  ) : Either[HttpStatus, T] = {
    rooli match {
      case ValpasRooli.OPPILAITOS_HAKEUTUMINEN =>
        Either.cond(
          accessToAllOrgs(rooli)(Set(organisaatioOid)) &&
            oppija.hakeutumisvalvovatOppilaitokset.contains(organisaatioOid),
          oppija,
          ValpasErrorCategory.forbidden.oppija()
        )
      case ValpasRooli.OPPILAITOS_MAKSUTTOMUUS =>
        Either.cond(
          accessToAnyOrg(rooli) && oppija.onOikeusValvoaMaksuttomuutta,
          oppija,
          ValpasErrorCategory.forbidden.oppija()
        )
      case ValpasRooli.KUNTA =>
        Either.cond(
          accessToAnyOrg(rooli) && oppija.onOikeusValvoaKunnalla,
          oppija,
          ValpasErrorCategory.forbidden.oppija()
        )
      case ValpasRooli.OPPILAITOS_SUORITTAMINEN =>
        Either.cond(
          accessToAllOrgs(rooli)(Set(organisaatioOid)) &&
            oppija.suorittamisvalvovatOppilaitokset.contains(organisaatioOid),
          oppija,
          ValpasErrorCategory.forbidden.oppija()
        )
      case _ =>
        Left(ValpasErrorCategory.internalError(s"Tuntematon rooli ${rooli}"))
    }
  }

  def withOpiskeluoikeusAccess[T <: ValpasOppijaLaajatTiedot](
    rooli: ValpasRooli.Role
  )(
    opiskeluoikeusOid: Opiskeluoikeus.Oid
  )(
    oppija: T
  )(
    implicit session: ValpasSession
  )
  : Either[HttpStatus, T] = {
    rooli match {
      case ValpasRooli.OPPILAITOS_HAKEUTUMINEN =>
        oppija.opiskeluoikeudet
          .find(oo => oo.oid == opiskeluoikeusOid &&
            oo.onHakeutumisValvottava &&
            accessToOrg(rooli)(oo.oppilaitos.oid))
          .toRight(ValpasErrorCategory.forbidden.opiskeluoikeus())
          .map(_ => oppija)
      case ValpasRooli.OPPILAITOS_MAKSUTTOMUUS =>
        Either.cond(
          accessToAnyOrg(rooli) && oppija.onOikeusValvoaMaksuttomuutta,
          oppija,
          ValpasErrorCategory.forbidden.oppija()
        )
      case ValpasRooli.KUNTA =>
        Either.cond(
          accessToAnyOrg(rooli) && oppija.onOikeusValvoaKunnalla,
          oppija,
          ValpasErrorCategory.forbidden.oppija()
        )
      case ValpasRooli.OPPILAITOS_SUORITTAMINEN => {
        oppija.opiskeluoikeudet
          .find(oo => oo.oid == opiskeluoikeusOid &&
            oo.onSuorittamisValvottava &&
            accessToOrg(rooli)(oo.oppilaitos.oid))
          .toRight(ValpasErrorCategory.forbidden.opiskeluoikeus())
          .map(_ => oppija)
      }
      case _ =>
        Left(ValpasErrorCategory.internalError(s"Tuntematon rooli ${rooli}"))
    }
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
    rooli: ValpasRooli.Role
  )(
    organisaatioOids: Set[Organisaatio.Oid],
    fn: () => T
  )(implicit session: ValpasSession)
  : Either[HttpStatus, T] = {
    Either.cond(accessToAllOrgs(rooli)(organisaatioOids), fn(), ValpasErrorCategory.forbidden.organisaatio())
  }

  private def accessToAllOrgs(
    rooli: ValpasRooli.Role
  )(
    organisaatioOids: Set[Organisaatio.Oid],
  )(
    implicit session: ValpasSession
  ): Boolean =
    onGlobaaliOikeus(rooli) || organisaatioOids.diff(oppilaitosOrganisaatioOids(rooli)).isEmpty

  private def accessToOrg(
    rooli: ValpasRooli.Role
  )(
    organisaatioOid: Organisaatio.Oid,
  )(
    implicit session: ValpasSession
  ): Boolean =
    accessToSomeOrgs(rooli)(Set(organisaatioOid))

  def accessToSomeOrgs(
    rooli: ValpasRooli.Role
  )(
    organisaatioOids: Set[Organisaatio.Oid],
  )(
    implicit session: ValpasSession
  ): Boolean =
    onGlobaaliOikeus(rooli) || organisaatioOids.intersect(oppilaitosOrganisaatioOids(rooli)).nonEmpty

  private def accessToAnyOrg(
    rooli: ValpasRooli.Role
  )(
    implicit session: ValpasSession
  ): Boolean =
    onGlobaaliOikeus(rooli) || oppilaitosOrganisaatioOids(rooli).nonEmpty

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
