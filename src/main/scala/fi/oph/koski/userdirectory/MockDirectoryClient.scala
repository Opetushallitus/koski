package fi.oph.koski.userdirectory

import com.typesafe.config.Config
import fi.oph.koski.config.Features
import fi.oph.koski.koskiuser._
import fi.oph.koski.valpas.valpasuser.ValpasMockUsers

class MockDirectoryClient(config: Config) extends DirectoryClient {
  val features = Features(config)
  def mockUsers = MockUsers.users ++ (if (features.valpas) ValpasMockUsers.users else List())

  def findUser(username: String) =
    mockUsers.find(_.username == username).map(_.ldapUser)

  def authenticate(userid: String, wrappedPassword: Password) = findUser(userid).isDefined && userid == wrappedPassword.password
}
