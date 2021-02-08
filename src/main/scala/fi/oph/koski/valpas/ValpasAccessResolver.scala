package fi.oph.koski.valpas

import fi.oph.koski.koskiuser.{KäyttöoikeusOrg, Palvelurooli}
import fi.oph.koski.schema.OrganisaatioWithOid
import fi.oph.koski.valpas.valpasuser.ValpasSession

object ValpasAccessResolver {
  def valpasOrganisaatioOids(implicit session: ValpasSession): Set[String] =
    valpasOrganisaatiot.map(_.oid)

  def valpasOrganisaatiot(implicit session: ValpasSession): Set[OrganisaatioWithOid] =
    session.orgKäyttöoikeudet.flatMap(asValpasOrgKäyttöoikeus).map(_.organisaatio)

  def asValpasOrgKäyttöoikeus(orgKäyttöoikeus: KäyttöoikeusOrg): Option[KäyttöoikeusOrg] = {
    orgKäyttöoikeus.organisaatiokohtaisetPalveluroolit.exists(isValpasPalvelurooli) match {
      case true => Some(orgKäyttöoikeus)
      case false => None
    }
  }

  def isValpasPalvelurooli(palvelurooli: Palvelurooli): Boolean = {
    palvelurooli match {
      case Palvelurooli ("VALPAS", _) => true
      case _ => false
    }
  }
}
