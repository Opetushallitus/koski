package fi.oph.koski.organisaatio

import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.schema.Koodistokoodiviite

/**
 * Selvittää oppilaitoksen tämänhetkisen oppilaitostyypin Organisaatiopalvelun hierarkiasta. Tietoa ei tallenneta
 * opiskeluoikeuden dataan, vaan se täydennetään lukuhetkellä (ks. käyttö KoskiOppijaFacade ja OmaData-paketit).
 */
class OppilaitostyyppiResolver(
  organisaatioRepository: OrganisaatioRepository,
  koodistoViitePalvelu: KoodistoViitePalvelu
) {
  def oppilaitostyyppi(oppilaitosOid: String): Option[Koodistokoodiviite] =
    organisaatioRepository.getOrganisaatioHierarkia(oppilaitosOid)
      .flatMap(_.oppilaitostyyppi)
      .flatMap(koodistoViitePalvelu.validate("oppilaitostyyppi", _))
}
