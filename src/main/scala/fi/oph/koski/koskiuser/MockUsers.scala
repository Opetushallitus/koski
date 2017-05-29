package fi.oph.koski.koskiuser

import fi.oph.koski.koskiuser.AuthenticationUser.fromLdapUser
import fi.oph.koski.koskiuser.Käyttöoikeusryhmät._
import fi.oph.koski.organisaatio.MockOrganisaatiot.{lehtikuusentienToimipiste, omnia, oppilaitokset}
import fi.oph.koski.organisaatio.{MockOrganisaatiot, Opetushallitus}
import fi.oph.koski.schema.Organisaatio
import fi.vm.sade.security.ldap.LdapUser

object MockUsers {
  val kalle = MockUser("käyttäjä", "kalle", "1.2.246.562.24.99999999987", (lehtikuusentienToimipiste :: oppilaitokset).toSet.map((_: String, oppilaitosTallentaja)))
  val pärre = MockUser("käyttäjä", "pärre", "1.2.246.562.24.99999999901", (lehtikuusentienToimipiste :: oppilaitokset).toSet.map((_: String, oppilaitosTallentaja)), "sv")
  val localkoski = MockUser("käyttäjä", "localkoski", "1.2.246.562.24.99999999988", oppilaitokset.toSet.map((_: String, oppilaitosTallentaja)))
  val omniaPalvelukäyttäjä = MockUser("käyttäjä", "omnia-palvelukäyttäjä", "1.2.246.562.24.99999999989", Set((omnia, oppilaitosPalvelukäyttäjä)))
  val omniaKatselija = MockUser("käyttäjä", "omnia-katselija", "1.2.246.562.24.99999999990", Set((omnia, oppilaitosKatselija)))
  val omniaTallentaja = MockUser("käyttäjä", "omnia-tallentaja", "1.2.246.562.24.99999999991", Set((omnia, oppilaitosTallentaja)))
  val paakayttaja = MockUser("käyttäjä", "pää", "1.2.246.562.24.99999999992", Set((Opetushallitus.organisaatioOid, ophPääkäyttäjä)))
  val viranomainen = MockUser("käyttäjä", "viranomais", "1.2.246.562.24.99999999993", Set((Opetushallitus.organisaatioOid, viranomaisKatselija)))
  val stadinAmmattiopistoPalvelukäyttäjä = MockUser("stadin-palvelu", "stadin-palvelu", "1.2.246.562.24.99999999994", Set((MockOrganisaatiot.stadinAmmattiopisto, oppilaitosPalvelukäyttäjä)))
  val stadinAmmattiopistoTallentaja = MockUser("tallentaja", "tallentaja", "1.2.246.562.24.99999999995", Set((MockOrganisaatiot.stadinAmmattiopisto, oppilaitosTallentaja)))
  val stadinVastuukäyttäjä = MockUser("stadin-vastuu", "stadin-vastuu", "1.2.246.562.24.99999999996", Set((MockOrganisaatiot.stadinAmmattiopisto, vastuukäyttäjä)))
  val helsinkiPalvelukäyttäjä = MockUser("helsinki", "helsinki", "1.2.246.562.24.99999999997", Set((MockOrganisaatiot.helsinginKaupunki, oppilaitosPalvelukäyttäjä)))
  val kahdenOrganisaatioPalvelukäyttäjä = MockUser("palvelu2", "palvelu2", "1.2.246.562.24.99999999998", Set((MockOrganisaatiot.helsinginKaupunki, oppilaitosPalvelukäyttäjä), (MockOrganisaatiot.omnia, oppilaitosPalvelukäyttäjä)))
  val omattiedot = MockUser("Oppija", "Oili", "1.2.246.562.24.99999999999", Set((omnia, oppilaitosTallentaja)))

  val users = List(
    kalle,
    pärre,
    omniaPalvelukäyttäjä,
    omniaKatselija,
    omniaTallentaja,
    localkoski,
    paakayttaja,
    viranomainen,
    stadinAmmattiopistoPalvelukäyttäjä,
    stadinAmmattiopistoTallentaja,
    helsinkiPalvelukäyttäjä,
    kahdenOrganisaatioPalvelukäyttäjä,
    omattiedot,
    stadinVastuukäyttäjä
  )
}

case class MockUser(ldapUser: LdapUser, käyttöoikeudet: Set[(Organisaatio.Oid, Käyttöoikeusryhmä)]) extends UserWithPassword {
  def toKoskiUser(käyttöoikeudet: KäyttöoikeusRepository) = {
    val authUser: AuthenticationUser = fromLdapUser(ldapUser.oid, ldapUser)
    new KoskiSession(authUser, "fi", "192.168.0.10", käyttöoikeudet.käyttäjänKäyttöoikeudet(authUser))
  }
  def oid = ldapUser.oid
  def username = ldapUser.givenNames
  def password = username
}

object MockUser {
  def apply(lastname: String, firstname: String, oid: String, käyttöoikeusryhmät: Set[(Organisaatio.Oid, Käyttöoikeusryhmä)], lang: String = "fi"): MockUser = {
    val roolit = s"LANG_$lang" :: käyttöoikeusryhmät.flatMap { case (oid, ryhmä) =>
      ryhmä.palveluroolit.map { palveluRooli =>
        LdapKayttooikeudet.roleString(palveluRooli.palveluName, palveluRooli.rooli, oid)
      }
    }.toList
    MockUser(LdapUser(roolit, lastname, firstname, oid), käyttöoikeusryhmät)
  }
}

