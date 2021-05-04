package fi.oph.koski.userdirectory

import fi.oph.koski.koskiuser._
import fi.oph.koski.valpas.valpasuser.ValpasMockUsers

class MockDirectoryClient() extends DirectoryClient {
  def mockUsers: Seq[MockUser] = MockUsers.users ++ ValpasMockUsers.users

  def findUser(username: String): Option[DirectoryUser] =
    mockUsers.find(_.username == username).map(_.ldapUser)

  def authenticate(userid: String, wrappedPassword: Password): Boolean = findUser(userid).isDefined && userid == wrappedPassword.password
}
