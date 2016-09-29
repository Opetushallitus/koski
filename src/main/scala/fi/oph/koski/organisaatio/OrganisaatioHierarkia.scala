package fi.oph.koski.organisaatio

import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.schema._

case class OrganisaatioHierarkia(oid: String, oppilaitosnumero: Option[Koodistokoodiviite], nimi: LocalizedString, yTunnus: Option[String], organisaatiotyypit: List[String], oppilaitostyyppi: Option[String], children: List[OrganisaatioHierarkia]) {
  def find(oid: String): Option[OrganisaatioHierarkia] = {
    if (oid == this.oid) {
      Some(this)
    } else {
      children.foldLeft(None: Option[OrganisaatioHierarkia]) {
        case (Some(found), _) => Some(found)
        case (_, child) => child.find(oid)
      }
    }
  }

  def toOrganisaatio: OrganisaatioWithOid =
    if (organisaatiotyypit.contains("OPPILAITOS")) {
      Oppilaitos(oid, oppilaitosnumero, Some(nimi))
    } else if (organisaatiotyypit.contains("KOULUTUSTOIMIJA")) {
      Koulutustoimija(oid, Some(nimi), yTunnus)
    } else {
      OidOrganisaatio(oid, Some(nimi))
    }

  def toKoulutustoimija: Option[Koulutustoimija] = toOrganisaatio match {
    case k: Koulutustoimija => Some(k)
    case _ => None
  }

  def toOppilaitos: Option[Oppilaitos] = toOrganisaatio match {
    case o: Oppilaitos => Some(o)
    case _ => None
  }
}
