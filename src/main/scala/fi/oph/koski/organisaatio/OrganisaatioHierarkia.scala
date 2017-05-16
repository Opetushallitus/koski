package fi.oph.koski.organisaatio

import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.schema._

case class OrganisaatioHierarkia(oid: String, oppilaitosnumero: Option[Koodistokoodiviite], nimi: LocalizedString, yTunnus: Option[String], kotipaikka: Option[Koodistokoodiviite], organisaatiotyypit: List[String], oppilaitostyyppi: Option[String], aktiivinen: Boolean, children: List[OrganisaatioHierarkia]) {
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
      Oppilaitos(oid, oppilaitosnumero, Some(nimi), kotipaikka)
    } else if (organisaatiotyypit.contains("KOULUTUSTOIMIJA")) {
      Koulutustoimija(oid, Some(nimi), yTunnus, kotipaikka)
    } else if (organisaatiotyypit.contains("TOIMIPISTE")) {
      Toimipiste(oid, Some(nimi), kotipaikka)
    } else {
      OidOrganisaatio(oid, Some(nimi), kotipaikka)
    }

  def toKoulutustoimija: Option[Koulutustoimija] = toOrganisaatio match {
    case k: Koulutustoimija => Some(k)
    case _ => None
  }

  def toOppilaitos: Option[Oppilaitos] = toOrganisaatio match {
    case o: Oppilaitos => Some(o)
    case _ => None
  }

  def flatten: List[OrganisaatioWithOid] = OrganisaatioHierarkia.flatten(List(this)).map(_.toOrganisaatio)
}

object OrganisaatioHierarkia {
  def flatten(orgs: List[OrganisaatioHierarkia]): List[OrganisaatioHierarkia] = {
    orgs ++ orgs.flatMap { org => org :: flatten(org.children) }
  }
}