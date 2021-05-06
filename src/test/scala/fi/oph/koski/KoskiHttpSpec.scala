package fi.oph.koski

import fi.oph.koski.koskiuser.{KoskiMockUser, MockUsers}

trait KoskiHttpSpec extends LocalJettyHttpSpec {
  def defaultUser: KoskiMockUser = MockUsers.kalle
}
