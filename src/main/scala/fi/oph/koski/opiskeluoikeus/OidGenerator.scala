package fi.oph.koski.opiskeluoikeus

import com.typesafe.config.Config
import fi.oph.koski.config.Environment
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.vm.sade.oidgenerator.OIDGenerator.generateOID

object OidGenerator {
  def apply(config: Config): OidGenerator = if (Environment.isUnitTestEnvironment(config)) {
    new MockOidGenerator
  } else {
    new OidGenerator
  }
}

class OidGenerator {
  // Generates oids of format 1.2.246.562.15.*
  def generateOid(oppijaOid: String): String = generateOID(15)
}

// Gives twice the same oid for MockOppijat.opiskeluoikeudenOidKonflikti
class MockOidGenerator extends OidGenerator {
  private var previousOid: String = ""

  override def generateOid(oppijaOid: String): String = this.synchronized {
    if (oppijaOid != KoskiSpecificMockOppijat.opiskeluoikeudenOidKonflikti.oid) {
      super.generateOid("")
    } else if (previousOid.isEmpty) {
      getAndStore
    } else {
      getAndEmpty
    }
  }

  private def getAndStore: String = {
    previousOid = super.generateOid("")
    previousOid
  }

  private def getAndEmpty: String = {
    val oid = previousOid
    previousOid = ""
    oid
  }
}
