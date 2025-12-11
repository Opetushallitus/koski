package fi.oph.koski.valpas.valpasuser

import fi.oph.koski.koskiuser.Rooli
import fi.oph.koski.organisaatio.{MockOrganisaatiot, Opetushallitus}
import fi.oph.koski.userdirectory.{OrganisaatioJaKäyttöoikeudet, PalveluJaOikeus}

object ValpasMockKäyttöoikeusryhmät {

  def peruskoulunKäyttäjä(organisaatioOid: String): Seq[OrganisaatioJaKäyttöoikeudet] = Seq(oppilaitosHakeutuminenKäyttäjä(organisaatioOid))

  def peruskoulunJossa10LuokkaKäyttäjä(organisaatioOid: String): Seq[OrganisaatioJaKäyttöoikeudet] = nivelvaiheenKäyttäjä(organisaatioOid)

  def nivelvaiheenKäyttäjä(organisaatioOid: String): Seq[OrganisaatioJaKäyttöoikeudet] = oppilaitosKäyttäjäKaikkiOikeudet(organisaatioOid)

  def toisenAsteenKäyttäjä(organisaatioOid: String): Seq[OrganisaatioJaKäyttöoikeudet] = Seq(oppilaitosSuorittaminenKäyttäjä _, oppilaitosMaksuttomuusKäyttäjä _).map(_(organisaatioOid))

  def oppilaitosKäyttäjäPelkkäSuorittaminen(organisaatioOid: String): Seq[OrganisaatioJaKäyttöoikeudet] = Seq(oppilaitosSuorittaminenKäyttäjä(organisaatioOid))

  def oppilaitosKäyttäjäKaikkiOikeudet(organisaatioOid: String): Seq[OrganisaatioJaKäyttöoikeudet] = Seq(oppilaitosHakeutuminenKäyttäjä _, oppilaitosSuorittaminenKäyttäjä _, oppilaitosMaksuttomuusKäyttäjä _).map(_(organisaatioOid))

  def oppilaitosKäyttäjäPelkkäMaksuttomuus(organisaatioOid: String): Seq[OrganisaatioJaKäyttöoikeudet] = Seq(oppilaitosMaksuttomuusKäyttäjä(organisaatioOid))

  def kuntakäyttäjä(organisaatioOid: String): Seq[OrganisaatioJaKäyttöoikeudet] = Seq(organisaatioKäyttäjä(organisaatioOid, ValpasRooli.KUNTA))

  def massaluovutuskäyttäjä(organisaatioOid: String): Seq[OrganisaatioJaKäyttöoikeudet] = Seq(organisaatioKäyttäjä(organisaatioOid, ValpasRooli.KUNTA_MASSALUOVUTUS))

  def pääkäyttäjä: Seq[OrganisaatioJaKäyttöoikeudet] =
    Seq(OrganisaatioJaKäyttöoikeudet(
      Opetushallitus.organisaatioOid,
      List(
        ValpasRooli.KUNTA,
        ValpasRooli.OPPILAITOS_HAKEUTUMINEN,
        ValpasRooli.OPPILAITOS_SUORITTAMINEN,
        ValpasRooli.OPPILAITOS_MAKSUTTOMUUS,
        ValpasRooli.KUNTA_MASSALUOVUTUS
      )
        .map(rooli => PalveluJaOikeus("VALPAS", rooli))))

  def hakeutuminenPääkäyttäjä: Seq[OrganisaatioJaKäyttöoikeudet] =
    Seq(OrganisaatioJaKäyttöoikeudet(
      Opetushallitus.organisaatioOid,
      List(PalveluJaOikeus("VALPAS", ValpasRooli.OPPILAITOS_HAKEUTUMINEN))
    ))
  def kelaLuovutuspalveluKäyttäjä: Seq[OrganisaatioJaKäyttöoikeudet] = Seq(OrganisaatioJaKäyttöoikeudet(MockOrganisaatiot.kela, List(PalveluJaOikeus("VALPAS", ValpasRooli.KELA))))
  def ytlLuovutuspalveluKäyttäjä: Seq[OrganisaatioJaKäyttöoikeudet] = Seq(OrganisaatioJaKäyttöoikeudet(MockOrganisaatiot.ytl, List(PalveluJaOikeus("VALPAS", ValpasRooli.YTL))))

  private def oppilaitosHakeutuminenKäyttäjä(organisaatioOid: String): OrganisaatioJaKäyttöoikeudet = organisaatioKäyttäjä(organisaatioOid, ValpasRooli.OPPILAITOS_HAKEUTUMINEN)

  private def oppilaitosSuorittaminenKäyttäjä(organisaatioOid: String): OrganisaatioJaKäyttöoikeudet = organisaatioKäyttäjä(organisaatioOid, ValpasRooli.OPPILAITOS_SUORITTAMINEN)

  private def oppilaitosMaksuttomuusKäyttäjä(organisaatioOid: String): OrganisaatioJaKäyttöoikeudet = organisaatioKäyttäjä(organisaatioOid, ValpasRooli.OPPILAITOS_MAKSUTTOMUUS)

  private def organisaatioKäyttäjä(organisaatioOid: String, rooli: String): OrganisaatioJaKäyttöoikeudet =
    OrganisaatioJaKäyttöoikeudet(organisaatioOid,
      List(PalveluJaOikeus("VALPAS", rooli))
    )
}
