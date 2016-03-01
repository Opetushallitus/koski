package fi.oph.tor.toruser

import fi.oph.tor.koodisto.{KoodistoViitePalvelu, MockKoodistoPalvelu}
import fi.oph.tor.organisaatio.{MockOrganisaatioRepository, MockOrganisaatiot}
import fi.vm.sade.security.ldap.{DirectoryClient, LdapUser}
import rx.lang.scala.Observable

object MockUsers extends UserOrganisationsRepository with DirectoryClient {
  case class MockUser(ldapUser: LdapUser, organisaatiot: Set[String]) {
    def asTorUser = new TorUser(ldapUser.oid, Observable.just(organisaatiot))
    def oid = ldapUser.oid
    def username = ldapUser.givenNames
  }

  val mockOrganisaatioRepository = new MockOrganisaatioRepository(KoodistoViitePalvelu(MockKoodistoPalvelu))

  val kalle = MockUser(LdapUser(List(), "käyttäjä", "kalle", "12345"), MockOrganisaatiot.oppilaitokset.toSet)
  val localthor = MockUser(LdapUser(List(), "käyttäjä", "localthor", "1.2.246.562.24.91698845204"), MockOrganisaatiot.oppilaitokset.toSet)
  val hiiri = MockUser(LdapUser(List(), "käyttäjä", "hiiri", "11111"), Set(MockOrganisaatiot.omnomnia))

  val users = List(kalle, hiiri, localthor)

  // UserOrganisationsRepository methods
  def getUserOrganisations(oid: String): Observable[Set[String]] = Observable.just(users.map(user => (user.oid, user.organisaatiot)).toMap.getOrElse(oid, Set.empty))

  // DirectoryClient methods
  def findUser(userid: String) = users.find(_.username == userid).map(_.ldapUser)
  def authenticate(userid: String, password: String) = findUser(userid).isDefined && userid == password
}
