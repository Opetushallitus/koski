package fi.oph.koski.userdirectory

import fi.oph.koski.koskiuser._
import fi.oph.koski.valpas.valpasuser.ValpasMockUsers

import scala.collection.concurrent.TrieMap

class MockDirectoryClient() extends DirectoryClient {
  def mockUsers: Seq[MockUser] = MockUsers.users ++ ValpasMockUsers.users

  def findUser(username: String): Option[DirectoryUser] =
    mockUsers.find(_.username == username).map(_.ldapUser).map { käyttäjä =>
      MockDirectoryClient.asiointikieliOverride(username)
        .map(kieli => käyttäjä.copy(asiointikieli = Some(kieli)))
        .getOrElse(käyttäjä)
    }

  def authenticate(userid: String, wrappedPassword: Password): Boolean = findUser(userid).isDefined && userid == wrappedPassword.password
}

/**
 * Virkailijan kieli tulee asiointikielestä, joten sitä ei voi vaihtaa käyttöliittymästä. Jotta
 * paikallisessa kehitysympäristössä pääsee silti näkemään käyttöliittymän muilla kielillä,
 * mock-käyttäjän asiointikielen voi ylikirjoittaa: ks. FixtureServlet /koski/fixtures/asiointikieli.
 * Ylikirjoitus elää vain tässä mock-toteutuksessa, joten varsinainen kielenratkaisu ei tiedä siitä.
 */
object MockDirectoryClient {
  private val asiointikieliOverrides = TrieMap.empty[String, String]

  def setAsiointikieli(username: String, kieli: String): Unit = asiointikieliOverrides.put(username, kieli)

  def clearAsiointikieliOverrides(): Unit = asiointikieliOverrides.clear()

  def asiointikieliOverride(username: String): Option[String] = asiointikieliOverrides.get(username)
}
