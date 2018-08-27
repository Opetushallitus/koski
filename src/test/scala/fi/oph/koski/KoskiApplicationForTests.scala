package fi.oph.koski

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.config.KoskiApplication.defaultConfig
import fi.oph.koski.jettylauncher.TestConfig

object KoskiApplicationForTests extends
  KoskiApplication(TestConfig.overrides.withFallback(defaultConfig))
