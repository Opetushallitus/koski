package fi.oph.koski.integrationtest

import fi.oph.koski.KoskiApplicationForTests

object AuthenticationServiceTester extends App {
  println(KoskiApplicationForTests.henkilöRepository.findByOid("1.2.246.562.24.51633620848"))
}
