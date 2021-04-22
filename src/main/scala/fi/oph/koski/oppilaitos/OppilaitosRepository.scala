package fi.oph.koski.oppilaitos

import com.typesafe.config.Config
import fi.oph.koski.config.Environment
import fi.oph.koski.koskiuser.{AccessType, KoskiSpecificSession}
import fi.oph.koski.localization.LocalizedStringImplicits.localizedStringOptionFinnishOrdering
import fi.oph.koski.organisaatio.{MockOrganisaatioRepository, OrganisaatioHierarkia, OrganisaatioRepository}
import fi.oph.koski.schema.{OidOrganisaatio, Oppilaitos}

object OppilaitosRepository {
  def apply(config: Config, organisatioRepository: OrganisaatioRepository): OppilaitosRepository = {
    if (Environment.isServerEnvironment(config)) {
      new OppilaitosRepository(organisatioRepository)
    } else {
      new MockOppilaitosRepository
    }
  }
}

class OppilaitosRepository(organisatioRepository: OrganisaatioRepository) {
  def oppilaitokset(implicit context: KoskiSpecificSession): Iterable[OidOrganisaatio] = {
    context.organisationOids(AccessType.read)
      .flatMap(oid => organisatioRepository.getOrganisaatioHierarkia(oid))
      .filter(org => org.organisaatiotyypit.contains("OPPILAITOS"))
      .map(toOppilaitos)
      .toList
      .sortBy(_.nimi)(localizedStringOptionFinnishOrdering)
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

class MockOppilaitosRepository extends OppilaitosRepository(organisatioRepository = MockOrganisaatioRepository) {
  // Jos ajetaan paikallista Koskea Virta-testiympäristön kanssa, useimpia oppilaitoksia ei löydy
  // MockOppilaitosRepositorystä. Käytetään Aalto-yliopistoa, jotta pystytään näyttämään edes jotain.
  // Testejä varten oppilaitoskoodilla "kuraa" saa aiheutettua virheen puuttuvasta oppilaitoksesta
  override def findByOppilaitosnumero(numero: String): Option[Oppilaitos] = {
    if (numero == "kuraa") {
      super.findByOppilaitosnumero("kuraa")
    } else {
      super.findByOppilaitosnumero(numero).orElse(super.findByOppilaitosnumero("10076"))
    }
  }
}
