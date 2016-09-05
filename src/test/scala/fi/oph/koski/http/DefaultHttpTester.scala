package fi.oph.koski.http

import fi.oph.koski.koskiuser.{MockUsers, UserWithPassword}

trait DefaultHttpTester extends HttpTester {
  override def baseUrl = sys.env.getOrElse("KOSKI_BASE_URL", "http://localhost:7021/koski")

  def defaultUser = new UserWithPassword {
    override def username = sys.env.getOrElse("KOSKI_USER", MockUsers.kalle.username)
    override def password = sys.env.getOrElse("KOSKI_PASS", MockUsers.kalle.password)
  }
}
