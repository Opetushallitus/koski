package fi.oph.koski.api

import fi.oph.koski.koskiuser.{KoskiMockUser, MockUsers}

trait KoskiHttpSpec extends LocalJettyHttpSpecification {
  def defaultUser: KoskiMockUser = MockUsers.kalle
}
