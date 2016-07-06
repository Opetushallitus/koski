package fi.oph.koski.integrationtest

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.KoskiUser

object AuthenticationServiceTester extends App {
  println(KoskiApplication().oppijaRepository.findByOid("1.2.246.562.24.51633620848")(KoskiUser.systemUser))
}
