package fi.oph.koski.integrationtest

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.koskiuser.KoskiUser

object AuthenticationServiceTester extends App {
  println(KoskiApplicationForTests.oppijaRepository.findByOid("1.2.246.562.24.51633620848"))
}
