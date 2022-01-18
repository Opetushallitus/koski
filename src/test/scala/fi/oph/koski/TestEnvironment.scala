package fi.oph.koski

import fi.oph.koski.log.LogConfiguration

object TestEnvironment {
  LogConfiguration.configureLogging(isTestEnvironment = true)
}

trait TestEnvironment {
  TestEnvironment // Make sure the test environment is set up
}
