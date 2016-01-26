package fi.oph.tor.toruser

import fi.oph.tor.koodisto.{KoodistoViitePalvelu, MockKoodistoPalvelu}
import fi.oph.tor.organisaatio.{InMemoryOrganisaatioRepository, MockOrganisaatioRepository, MockOrganisaatiot, OrganisaatioHierarkia}
import fi.vm.sade.security.ldap.{DirectoryClient, LdapUser}

object MockUsers extends UserOrganisationsRepository with DirectoryClient {
  case class MockUser(ldapUser: LdapUser, organisaatiot: List[OrganisaatioHierarkia]) {
    def organisaatioRepository = InMemoryOrganisaatioRepository(organisaatiot)
    def asTorUser = TorUser(ldapUser.oid, organisaatioRepository)
    def oid = ldapUser.oid
    def username = ldapUser.givenNames
  }

  val mockOrganisaatioRepository = new MockOrganisaatioRepository(KoodistoViitePalvelu(MockKoodistoPalvelu))

  val kalle = MockUser(LdapUser(List(), "käyttäjä", "kalle", "12345"), MockOrganisaatiot.oppilaitokset.flatMap(mockOrganisaatioRepository.getOrganisaatioHierarkia(_)))
  val hiiri = MockUser(LdapUser(List(), "käyttäjä", "hiiri", "11111"), List(MockOrganisaatiot.omnomnia).flatMap(mockOrganisaatioRepository.getOrganisaatioHierarkia(_)))

  val users = List(kalle, hiiri)

  // UserOrganisationsRepository methods
  def getUserOrganisations(oid: String): InMemoryOrganisaatioRepository = users.map(user => (user.oid, user.organisaatioRepository)).toMap.getOrElse(oid, InMemoryOrganisaatioRepository.empty)

  // DirectoryClient methods
  def findUser(userid: String) = users.find(_.username == userid).map(_.ldapUser)
  def authenticate(userid: String, password: String) = findUser(userid).isDefined && userid == password
}
