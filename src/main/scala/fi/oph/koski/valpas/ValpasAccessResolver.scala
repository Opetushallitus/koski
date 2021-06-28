package fi.oph.koski.valpas

import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koskiuser.{KäyttöoikeusOrg, Palvelurooli}
import fi.oph.koski.schema.{Opiskeluoikeus, Organisaatio, OrganisaatioWithOid}
import fi.oph.koski.valpas.opiskeluoikeusrepository.ValpasOppijaLaajatTiedot
import fi.oph.koski.valpas.valpasuser.{ValpasRooli, ValpasSession}

class ValpasAccessResolver {
  def assertAccessToOrg
    (rooli: ValpasRooli.Role, organisaatioOid: Organisaatio.Oid)
    (implicit session: ValpasSession)
  : Either[HttpStatus, Unit] =
    Either.cond(
      accessToOrg(rooli, organisaatioOid),
      Unit,
      ValpasErrorCategory.forbidden.organisaatio()
    )

  def assertAccessToAnyOrg(rooli: ValpasRooli.Role)(implicit session: ValpasSession)
  : Either[HttpStatus, Unit] =
    Either.cond(
      accessToAnyOrg(rooli),
      Unit,
      ValpasErrorCategory.forbidden.toiminto()
    )

  def withOppijaAccess[T <: ValpasOppijaLaajatTiedot](oppija: T)(implicit session: ValpasSession)
  : Either[HttpStatus, T] =
    withOppijaAccess(
      Seq(
        ValpasRooli.OPPILAITOS_HAKEUTUMINEN,
        ValpasRooli.OPPILAITOS_MAKSUTTOMUUS,
        ValpasRooli.OPPILAITOS_SUORITTAMINEN,
        ValpasRooli.KUNTA
      ),
      oppija
    )

  def withOppijaAccess[T <: ValpasOppijaLaajatTiedot]
    (roolit: Seq[ValpasRooli.Role], oppija: T)(implicit session: ValpasSession)
  : Either[HttpStatus, T] = HttpStatus.any(roolit.map(withOppijaAccessAsRole(_)(oppija)))

  def withOppijaAccessAsRole[T <: ValpasOppijaLaajatTiedot]
    (rooli: ValpasRooli.Role)(oppija: T)(implicit session: ValpasSession)
  : Either[HttpStatus, T] = {
    val hasAccess = rooli match {
      case ValpasRooli.OPPILAITOS_HAKEUTUMINEN =>
        oppija.hakeutumisvalvovatOppilaitokset.nonEmpty &&
          accessToSomeOrgs(rooli, oppija.hakeutumisvalvovatOppilaitokset)
      case ValpasRooli.OPPILAITOS_MAKSUTTOMUUS =>
        accessToAnyOrg(rooli) && oppija.onOikeusValvoaMaksuttomuutta
      case ValpasRooli.KUNTA =>
        accessToAnyOrg(rooli) && oppija.onOikeusValvoaKunnalla
      case ValpasRooli.OPPILAITOS_SUORITTAMINEN =>
        oppija.suorittamisvalvovatOppilaitokset.nonEmpty &&
          accessToSomeOrgs(rooli, oppija.suorittamisvalvovatOppilaitokset)
      case _ => false
    }
    Either.cond(hasAccess, oppija, ValpasErrorCategory.forbidden.oppija())
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
            accessToSomeOrgs(rooli, oppija.hakeutumisvalvovatOppilaitokset)
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
            accessToSomeOrgs(rooli, oppija.suorittamisvalvovatOppilaitokset)
        )
      case _ =>
        throw new InternalError(s"Tuntematon rooli ${rooli}")
    }
  }

  def withOppijaAccessAsOrganisaatio[T <: ValpasOppijaLaajatTiedot]
    (rooli: ValpasRooli.Role, organisaatioOid: Organisaatio.Oid)
    (oppija: T)
    (implicit session: ValpasSession)
  : Either[HttpStatus, T] = {
    val hasAccess = rooli match {
      case ValpasRooli.OPPILAITOS_HAKEUTUMINEN =>
        accessToOrg(rooli, organisaatioOid) &&
          oppija.hakeutumisvalvovatOppilaitokset.contains(organisaatioOid)
      case ValpasRooli.OPPILAITOS_MAKSUTTOMUUS =>
        accessToAnyOrg(rooli) && oppija.onOikeusValvoaMaksuttomuutta
      case ValpasRooli.KUNTA =>
        accessToAnyOrg(rooli) && oppija.onOikeusValvoaKunnalla
      case ValpasRooli.OPPILAITOS_SUORITTAMINEN =>
        accessToOrg(rooli, organisaatioOid) &&
          oppija.suorittamisvalvovatOppilaitokset.contains(organisaatioOid)
      case _ => false
    }
    Either.cond(hasAccess, oppija, ValpasErrorCategory.forbidden.oppija())
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
            accessToOrg(rooli, oo.oppilaitos.oid))
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
      case ValpasRooli.OPPILAITOS_SUORITTAMINEN =>
        oppija.opiskeluoikeudet
          .find(oo => oo.oid == opiskeluoikeusOid &&
            oo.onSuorittamisValvottava &&
            accessToOrg(rooli, oo.oppilaitos.oid))
          .toRight(ValpasErrorCategory.forbidden.opiskeluoikeus())
          .map(_ => oppija)
      case _ =>
        Left(ValpasErrorCategory.internalError(s"Tuntematon rooli ${rooli}"))
    }
  }

  def accessToOrg
    (rooli: ValpasRooli.Role, organisaatioOid: Organisaatio.Oid)
    (implicit session: ValpasSession)
  : Boolean = accessToSomeOrgs(rooli, Set(organisaatioOid))

  def accessToSomeOrgs
    (rooli: ValpasRooli.Role, organisaatioOids: Set[Organisaatio.Oid])
    (implicit session: ValpasSession)
  : Boolean = onGlobaaliOikeus(rooli) || organisaatioOids.intersect(oppilaitosOrganisaatioOids(rooli)).nonEmpty

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
