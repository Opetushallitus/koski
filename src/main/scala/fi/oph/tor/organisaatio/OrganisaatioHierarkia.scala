package fi.oph.tor.organisaatio

import fi.oph.tor.schema.{KoodistoKoodiViite, OidOrganisaatio, Oppilaitos, OrganisaatioWithOid}

case class OrganisaatioHierarkia(oid: String, oppilaitosnumero: Option[KoodistoKoodiViite], nimi: String, organisaatiotyypit: List[String], children: List[OrganisaatioHierarkia]) {
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

  def toOrganisaatio: OrganisaatioWithOid = oppilaitosnumero match {
    case Some(_) => Oppilaitos(oid, oppilaitosnumero, Some(nimi))
    case _ => OidOrganisaatio(oid, Some(nimi))
  }

  def toOppilaitos: Option[Oppilaitos] = oppilaitosnumero match {
    case Some(_) => Some(Oppilaitos(oid, oppilaitosnumero, Some(nimi)))
    case _ => None
  }
}
