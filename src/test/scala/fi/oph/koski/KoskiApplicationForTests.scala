package fi.oph.koski

import com.typesafe.config.ConfigValueFactory.fromAnyRef
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.config.KoskiApplication.defaultConfig
import fi.oph.koski.jettylauncher.TestConfig

object KoskiApplicationForTests extends
  KoskiApplication(TestConfig.overrides.toList.foldLeft(defaultConfig)({ case (config, (key, value)) => config.withValue(key, fromAnyRef(value)) }))
