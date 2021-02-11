package fi.oph.koski.valpas.userdirectory

import fi.oph.koski.koskiuser.{KäyttöoikeusOrg, Palvelurooli}
import fi.oph.koski.schema.OidOrganisaatio
import fi.oph.koski.userdirectory.{OrganisaatioJaKäyttöoikeudet, PalveluJaOikeus}

object ValpasKäyttöoikeusRoolit {
  def resolveKäyttäjänKäyttöoikeudet(organisaatiot: List[OrganisaatioJaKäyttöoikeudet]) =
    organisaatiot.flatMap {
      case OrganisaatioJaKäyttöoikeudet(organisaatioOid, käyttöoikeudet) =>
        val roolit = käyttöoikeudet.collect { case PalveluJaOikeus(palvelu, oikeus) => Palvelurooli(palvelu, oikeus) }
        if (!roolit.map(_.palveluName).contains("VALPAS")) {
          Nil
        } else {
          List(KäyttöoikeusOrg(OidOrganisaatio(organisaatioOid), roolit, juuri = true, oppilaitostyyppi = None))
        }
    }
}
