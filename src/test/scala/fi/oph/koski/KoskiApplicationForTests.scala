package fi.oph.koski

import com.typesafe.config.ConfigValueFactory.fromAnyRef
import fi.oph.koski.config.KoskiApplication.defaultConfig
import fi.oph.koski.config.{Environment, KoskiApplication}

object KoskiApplicationForTests extends KoskiApplication(
  defaultConfig
    .withValue("env", fromAnyRef(Environment.UnitTest))
    .withValue("tiedote.checkInterval", fromAnyRef("5s"))
    .withValue("tiedote.batchSize", fromAnyRef(1))
)
