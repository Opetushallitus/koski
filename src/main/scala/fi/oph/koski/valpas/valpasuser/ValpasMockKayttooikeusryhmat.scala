package fi.oph.koski.valpas.valpasuser

import fi.oph.koski.koskiuser.{Käyttöoikeus, KäyttöoikeusOrg}
import fi.oph.koski.schema.OidOrganisaatio

object ValpasMockKäyttöoikeusryhmät {
  def peruskoulunKäyttäjä(organisaatioOid: String): Set[Käyttöoikeus] = Set(oppilaitosHakeutuminenKäyttäjä(organisaatioOid))

  def peruskoulunJossa10LuokkaKäyttäjä(organisaatioOid: String): Set[Käyttöoikeus] = nivelvaiheenKäyttäjä(organisaatioOid)

  def nivelvaiheenKäyttäjä(organisaatioOid: String): Set[Käyttöoikeus] = oppilaitosKäyttäjäKaikkiOikeudet(organisaatioOid)

  def toisenAsteenKäyttäjä(organisaatioOid: String): Set[Käyttöoikeus] = Set(oppilaitosSuorittaminenKäyttäjä _, oppilaitosMaksuttomuusKäyttäjä _).map(_(organisaatioOid))

  def oppilaitosKäyttäjäKaikkiOikeudet(organisaatioOid: String): Set[Käyttöoikeus] = Set(oppilaitosHakeutuminenKäyttäjä _, oppilaitosSuorittaminenKäyttäjä _, oppilaitosMaksuttomuusKäyttäjä _).map(_(organisaatioOid))

  def kuntakäyttäjä(organisaatioOid: String): Set[Käyttöoikeus] = Set(organisaatioKäyttäjä(organisaatioOid, ValpasRooli.KUNTA))

  private def oppilaitosHakeutuminenKäyttäjä(organisaatioOid: String) = organisaatioKäyttäjä(organisaatioOid, ValpasRooli.OPPILAITOS_HAKEUTUMINEN)

  private def oppilaitosSuorittaminenKäyttäjä(organisaatioOid: String) = organisaatioKäyttäjä(organisaatioOid, ValpasRooli.OPPILAITOS_SUORITTAMINEN)

  private def oppilaitosMaksuttomuusKäyttäjä(organisaatioOid: String) = organisaatioKäyttäjä(organisaatioOid, ValpasRooli.OPPILAITOS_MAKSUTTOMUUS)

  private def organisaatioKäyttäjä(organisaatioOid: String, rooli: String) = KäyttöoikeusOrg(
    OidOrganisaatio(organisaatioOid),
    List(ValpasPalvelurooli(rooli)),
    true,
    None)
}
