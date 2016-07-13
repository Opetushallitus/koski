package fi.oph.koski.koskiuser

import fi.oph.koski.koskiuser.Käyttöoikeusryhmät.{ophPääkäyttäjä, oppilaitosKatselija, oppilaitosPalvelukäyttäjä, viranomaisKatselija}
import fi.oph.koski.organisaatio.MockOrganisaatiot.{lehtikuusentienToimipiste, omnia, oppilaitokset}
import fi.oph.koski.organisaatio.Opetushallitus
import fi.oph.koski.schema.Organisaatio
import fi.vm.sade.security.ldap.LdapUser

object MockUsers {
  val kalle = new MockUser(LdapUser(List(), "käyttäjä", "kalle", "12345"), (lehtikuusentienToimipiste :: oppilaitokset).toSet.map((_: String, oppilaitosPalvelukäyttäjä)))
  val localkoski = new MockUser(LdapUser(List(), "käyttäjä", "localkoski", "1.2.246.562.24.91698845204"), oppilaitokset.toSet.map((_: String, oppilaitosPalvelukäyttäjä)))
  val hiiri = new MockUser(LdapUser(List(), "käyttäjä", "hiiri", "11111"), Set((omnia, oppilaitosPalvelukäyttäjä)))
  val hiiriKatselija = new MockUser(LdapUser(List(), "käyttäjä", "hiirikatselija", "11112"), Set((omnia, oppilaitosKatselija)))
  val paakayttaja = new MockUser(LdapUser(List(), "käyttäjä", "pää", "00001"), Set((Opetushallitus.organisaatioOid, ophPääkäyttäjä)))
  val viranomainen = new MockUser(LdapUser(List(), "käyttäjä", "viranomais", "00002"), Set((Opetushallitus.organisaatioOid, viranomaisKatselija)))

  val users = List(kalle, hiiri, hiiriKatselija, localkoski, paakayttaja, viranomainen)
}

case class MockUser(ldapUser: LdapUser, käyttöoikeudet: Set[(Organisaatio.Oid, Käyttöoikeusryhmä)]) extends UserWithPassword {
  def toKoskiUser(käyttöoikeudet: KäyttöoikeusRepository) = new KoskiUser(ldapUser.oid, "192.168.0.10", "fi", käyttöoikeudet.käyttäjänKäyttöoikeudet(oid))
  def oid = ldapUser.oid
  def username = ldapUser.givenNames
  def password = username
}

