package fi.oph.koski.valpas.valpasuser

import fi.oph.koski.koskiuser.KäyttöoikeusOrg
import fi.oph.koski.schema.OidOrganisaatio

object ValpasMockKäyttöoikeusryhmät {
  def oppilaitoskäyttäjä(organisaatioOid: String) = KäyttöoikeusOrg(
    OidOrganisaatio(organisaatioOid),
    List(ValpasPalvelurooli(ValpasRooli.OPPILAITOS)),
    true,
    None)

  def kuntakäyttäjä(organisaatioOid: String) = KäyttöoikeusOrg(
    OidOrganisaatio(organisaatioOid),
    List(ValpasPalvelurooli(ValpasRooli.KUNTA)),
    true,
    None)
}
