package fi.oph.koski.koskiuser

import fi.oph.koski.koodisto.{KoodistoViitePalvelu, MockKoodistoPalvelu}
import fi.oph.koski.koskiuser.Käyttöoikeusryhmät.{ophPääkäyttäjä, oppilaitosKatselija, oppilaitosPalvelukäyttäjä, viranomaisKatselija}
import fi.oph.koski.organisaatio.MockOrganisaatiot.{lehtikuusentienToimipiste, omnomnia, oppilaitokset}
import fi.oph.koski.organisaatio.{MockOrganisaatioRepository, Opetushallitus}
import fi.oph.koski.schema.Organisaatio
import fi.vm.sade.security.ldap.{DirectoryClient, LdapUser}
import rx.lang.scala.Observable

object MockUsers extends DirectoryClient {
  val mockOrganisaatioRepository = new MockOrganisaatioRepository(KoodistoViitePalvelu(MockKoodistoPalvelu))

  val kalle = new MockUser(LdapUser(List(), "käyttäjä", "kalle", "12345"), (lehtikuusentienToimipiste :: oppilaitokset).toSet.map(palvelukäyttäjä))
  val localkoski = new MockUser(LdapUser(List(), "käyttäjä", "localkoski", "1.2.246.562.24.91698845204"), oppilaitokset.toSet.map(palvelukäyttäjä))
  val hiiri = new MockUser(LdapUser(List(), "käyttäjä", "hiiri", "11111"), Set((omnomnia, oppilaitosPalvelukäyttäjä)))
  val hiiriKatselija = new MockUser(LdapUser(List(), "käyttäjä", "hiirikatselija", "11112"), Set((omnomnia, oppilaitosKatselija)))
  val paakayttaja = new MockUser(LdapUser(List(), "käyttäjä", "pää", "00001"), Set((Opetushallitus.organisaatioOid, ophPääkäyttäjä)))
  val viranomainen = new MockUser(LdapUser(List(), "käyttäjä", "viranomais", "00002"), Set((Opetushallitus.organisaatioOid, viranomaisKatselija)))

  private def palvelukäyttäjä(org: String) = (org, oppilaitosPalvelukäyttäjä)

  val users = List(kalle, hiiri, hiiriKatselija, localkoski, paakayttaja, viranomainen)

  // UserOrganisationsRepository methods
  def getUserOrganisations(oid: String) = {
    Observable.just(users.map(user => (user.oid, user.käyttöoikeudet)).toMap.getOrElse(oid, Set.empty))
  }

  // DirectoryClient methods
  def findUser(userid: String) = users.find(_.username == userid).map(_.ldapUser)
  def authenticate(userid: String, password: String) = findUser(userid).isDefined && userid == password
}

case class MockUser(ldapUser: LdapUser, käyttöoikeudet: Set[(Organisaatio.Oid, Käyttöoikeusryhmä)]) extends UserWithPassword {
  def toKoskiUser(käyttöoikeuspalvelu: UserOrganisationsRepository) = new KoskiUser(ldapUser.oid, "192.168.0.10", "fi", käyttöoikeuspalvelu.käyttäjänKäyttöoikeudet(oid))
  def oid = ldapUser.oid
  def username = ldapUser.givenNames
  def password = username
}

