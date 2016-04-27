package fi.oph.tor.oppilaitos

import fi.oph.tor.organisaatio.{OrganisaatioRepository, OrganisaatioHierarkia}
import fi.oph.tor.schema.{Koodistokoodiviite, Oppilaitos, OidOrganisaatio}
import fi.oph.tor.toruser.TorUser
import fi.oph.tor.localization.LocalizedStringImplicits.LocalizedStringFinnishOrdering

class OppilaitosRepository(organisatioRepository: OrganisaatioRepository) {
  def oppilaitokset(implicit context: TorUser): Iterable[OidOrganisaatio] = {
    findOppilaitokset("")
  }

  def findOppilaitokset(query: String)(implicit context: TorUser): Iterable[OidOrganisaatio] = {
    context.organisationOids
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

  private def toOppilaitos(org: OrganisaatioHierarkia) = OidOrganisaatio(org.oid, Some(org.nimi))
}