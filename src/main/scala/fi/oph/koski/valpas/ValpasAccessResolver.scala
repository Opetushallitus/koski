package fi.oph.koski.valpas

import fi.oph.koski.koskiuser.{KäyttöoikeusOrg, Palvelurooli}
import fi.oph.koski.schema.OrganisaatioWithOid
import fi.oph.koski.valpas.valpasuser.{ValpasRooli, ValpasSession}

object ValpasAccessResolver {
  def oppilaitosHakeutuminenOrganisaatioOids(implicit session: ValpasSession): Set[String] =
    valpasOrganisaatiot(ValpasRooli.OPPILAITOS_HAKEUTUMINEN).map(_.oid)

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
}
