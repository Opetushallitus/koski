package fi.oph.koski.integrationtest

import fi.oph.koski.config.KoskiApplication

object AuthenticationServiceTester extends App {
  println(KoskiApplication().oppijaRepository.findByOid("1.2.246.562.24.51633620848"))
}
