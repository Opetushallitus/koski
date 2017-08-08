package fi.oph.koski.util

import com.typesafe.config.Config
import fi.oph.koski.db.KoskiDatabaseConfig
import fi.oph.koski.fixture.Fixtures
import fi.vm.sade.oidgenerator.OIDGenerator.generateOID

object OidGenerator {
  def apply(config: Config): OidGenerator = if (Fixtures.shouldUseFixtures(config)) {
    new MockOidGenerator
  } else {
    new OidGenerator
  }
}

class OidGenerator {
  def generateOid: String = generateOID(15)
}

// Gives twice the same oid
class MockOidGenerator extends OidGenerator {
  private var previousOid: String = ""
  override def generateOid: String = this.synchronized {
    if (previousOid.isEmpty) {
      getAndStore
    } else {
      getAndEmpty
    }
  }

  private def getAndStore: String = {
    previousOid = super.generateOid
    previousOid
  }

  private def getAndEmpty: String = {
    val oid = previousOid
    previousOid = ""
    oid
  }
}
