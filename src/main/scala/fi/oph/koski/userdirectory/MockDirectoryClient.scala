package fi.oph.koski.userdirectory

import fi.oph.koski.koskiuser._

object MockDirectoryClient extends DirectoryClient {
  def findUser(username: String) =
    MockUsers.users.find(_.username == username).map(_.ldapUser)

  def authenticate(userid: String, wrappedPassword: Password) = findUser(userid).isDefined && userid == wrappedPassword.password

  override def organisaationSähköpostit(organisaatioOid: String, ryhmä: String): List[String] = {
    MockUsers.users.filter { u =>
      u.käyttöoikeudet.exists {
        case k: KäyttöoikeusOrg => k.organisaatio.oid == organisaatioOid && u.käyttöoikeusRyhmät.contains(ryhmä)
        case _ => false
      }
    }.map(_.username + "@example.com")
  }
}
