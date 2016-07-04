package fi.oph.koski.oppilaitos

import fi.oph.koski.koskiuser.{AccessType, KoskiUser}
import fi.oph.koski.localization.LocalizedStringImplicits.LocalizedStringFinnishOrdering
import fi.oph.koski.organisaatio.{OrganisaatioHierarkia, OrganisaatioRepository}
import fi.oph.koski.schema.{Koodistokoodiviite, OidOrganisaatio, Oppilaitos}

case class OppilaitosRepository(organisatioRepository: OrganisaatioRepository) {
  def oppilaitokset(implicit context: KoskiUser): Iterable[OidOrganisaatio] = {
    findOppilaitokset("")
  }

  def findOppilaitokset(query: String)(implicit context: KoskiUser): Iterable[OidOrganisaatio] = {
    context.organisationOids(AccessType.read)
      .flatMap(oid => organisatioRepository.getOrganisaatioHierarkia(oid))
      .filter(org => org.organisaatiotyypit.contains("OPPILAITOS") && org.nimi.get("fi").toLowerCase.contains(query.toLowerCase))
      .map(toOppilaitos)
      .toList
      .sortBy(_.nimi)
  }

  // Haetaan 5-numeroisella oppilaitosnumerolla (TK-koodi)
  def findByOppilaitosnumero(numero: String): Option[Oppilaitos] = {
    organisatioRepository.search(numero).flatMap {
      case o@Oppilaitos(_, Some(Koodistokoodiviite(koodiarvo, _, _, _, _)), _) if koodiarvo == numero => Some(o)
      case _ => None
    }.headOption
  }

  def findByOid(oid: String): Option[Oppilaitos] = {
    organisatioRepository.getOrganisaatio(oid).flatMap(_.toOppilaitos)
  }

  private def toOppilaitos(org: OrganisaatioHierarkia) = OidOrganisaatio(org.oid, Some(org.nimi))
}