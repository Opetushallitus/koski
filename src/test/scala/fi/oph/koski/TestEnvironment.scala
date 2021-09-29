package fi.oph.koski

import fi.oph.koski.log.LogConfiguration

object TestEnvironment {
  LogConfiguration.configureLoggingWithFileWatch()
}

trait TestEnvironment {
  TestEnvironment // Make sure the test environment is set up
}
