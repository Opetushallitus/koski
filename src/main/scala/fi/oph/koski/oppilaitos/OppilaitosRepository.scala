package fi.oph.koski.oppilaitos

import fi.oph.koski.koskiuser.{AccessType, KoskiSession}
import fi.oph.koski.localization.LocalizedStringImplicits.LocalizedStringFinnishOrdering
import fi.oph.koski.organisaatio.{MockOrganisaatioRepository, OrganisaatioHierarkia, OrganisaatioRepository}
import fi.oph.koski.schema.{OidOrganisaatio, Oppilaitos}

case class OppilaitosRepository(organisatioRepository: OrganisaatioRepository) {
  def oppilaitokset(implicit context: KoskiSession): Iterable[OidOrganisaatio] = {
    context.organisationOids(AccessType.read)
      .flatMap(oid => organisatioRepository.getOrganisaatioHierarkia(oid))
      .filter(org => org.organisaatiotyypit.contains("OPPILAITOS"))
      .map(toOppilaitos)
      .toList
      .sortBy(_.nimi)
  }

  // Haetaan 5-numeroisella oppilaitosnumerolla (TK-koodi)
  def findByOppilaitosnumero(numero: String): Option[Oppilaitos] = {
    organisatioRepository.findByOppilaitosnumero(numero)
  }

  def findByOid(oid: String): Option[Oppilaitos] = {
    organisatioRepository.getOrganisaatio(oid).flatMap(_.toOppilaitos)
  }

  private def toOppilaitos(org: OrganisaatioHierarkia) = OidOrganisaatio(org.oid, Some(org.nimi))
}

object MockOppilaitosRepository extends OppilaitosRepository(MockOrganisaatioRepository)