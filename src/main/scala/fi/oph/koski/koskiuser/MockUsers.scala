package fi.oph.koski.koskiuser

import fi.oph.koski.koskiuser.Käyttöoikeusryhmät._
import fi.oph.koski.organisaatio.MockOrganisaatiot.{lehtikuusentienToimipiste, omnia, oppilaitokset}
import fi.oph.koski.organisaatio.{MockOrganisaatiot, Opetushallitus}
import fi.oph.koski.schema.Organisaatio
import fi.vm.sade.security.ldap.LdapUser

object MockUsers {
  val kalle = MockUser(LdapUser(List(), "käyttäjä", "kalle", "1.2.246.562.24.99999999987"), (lehtikuusentienToimipiste :: oppilaitokset).toSet.map((_: String, oppilaitosTallentaja)))
  val localkoski = MockUser(LdapUser(List(), "käyttäjä", "localkoski", "1.2.246.562.24.99999999988"), oppilaitokset.toSet.map((_: String, oppilaitosTallentaja)))
  val omniaPalvelukäyttäjä = MockUser(LdapUser(List(), "käyttäjä", "omnia-palvelukäyttäjä", "1.2.246.562.24.99999999989"), Set((omnia, oppilaitosPalvelukäyttäjä)))
  val omniaKatselija = MockUser(LdapUser(List(), "käyttäjä", "omnia-katselija", "1.2.246.562.24.99999999990"), Set((omnia, oppilaitosKatselija)))
  val omniaTallentaja = MockUser(LdapUser(List(), "käyttäjä", "omnia-tallentaja", "1.2.246.562.24.99999999991"), Set((omnia, oppilaitosTallentaja)))
  val paakayttaja = MockUser(LdapUser(List(), "käyttäjä", "pää", "1.2.246.562.24.99999999992"), Set((Opetushallitus.organisaatioOid, ophPääkäyttäjä)))
  val viranomainen = MockUser(LdapUser(List(), "käyttäjä", "viranomais", "1.2.246.562.24.99999999993"), Set((Opetushallitus.organisaatioOid, viranomaisKatselija)))
  val stadinAmmattiopistoPalvelukäyttäjä = MockUser(LdapUser(List(), "stadin-palvelu", "stadin-palvelu", "1.2.246.562.24.99999999994"), Set((MockOrganisaatiot.stadinAmmattiopisto, oppilaitosPalvelukäyttäjä)))
  val stadinAmmattiopistoTallentaja = MockUser(LdapUser(List(), "tallentaja", "tallentaja", "1.2.246.562.24.99999999995"), Set((MockOrganisaatiot.stadinAmmattiopisto, oppilaitosTallentaja)))
  val stadinVastuukäyttäjä = MockUser(LdapUser(List(), "stadin-vastuu", "stadin-vastuu", "1.2.246.562.24.99999999996"), Set((MockOrganisaatiot.stadinAmmattiopisto, vastuukäyttäjä)))
  val helsinkiPalvelukäyttäjä = MockUser(LdapUser(List(), "helsinki", "helsinki", "1.2.246.562.24.99999999997"), Set((MockOrganisaatiot.helsinginKaupunki, oppilaitosPalvelukäyttäjä)))
  val kahdenOrganisaatioPalvelukäyttäjä = MockUser(LdapUser(List(), "palvelu2", "palvelu2", "1.2.246.562.24.99999999998"), Set((MockOrganisaatiot.helsinginKaupunki, oppilaitosPalvelukäyttäjä), (MockOrganisaatiot.omnia, oppilaitosPalvelukäyttäjä)))
  val omattiedot = MockUser(LdapUser(List(), "oppija", "oili", "1.2.246.562.24.99999999999"), Set((omnia, oppilaitosTallentaja)))

  val users = List(
    kalle,
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
  def toKoskiUser(käyttöoikeudet: KäyttöoikeusRepository) = new KoskiUser(ldapUser.oid, "192.168.0.10", "fi", käyttöoikeudet.käyttäjänKäyttöoikeudet(oid))
  def oid = ldapUser.oid
  def username = ldapUser.givenNames
  def password = username
}

