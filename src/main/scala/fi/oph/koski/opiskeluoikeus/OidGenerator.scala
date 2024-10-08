package fi.oph.koski.opiskeluoikeus

import com.typesafe.config.Config
import fi.oph.koski.config.Environment
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.vm.sade.oidgenerator.OIDGenerator.{generateOID, makeOID}

import scala.util.Random

object OidGenerator {
  private val random: Random = new Random()

  def apply(config: Config): OidGenerator = if (Environment.isUnitTestEnvironment(config)) {
    new MockOidGenerator
  } else {
    new OidGenerator
  }
}

class OidGenerator {
  // Generates oids of format 1.2.246.562.15.*
  def generateKoskiOid(oppijaOid: String): String = generateOID(15)

  private val ytrMin = 1000000000L;
  private val ytrMax = 4000000000L;

  // Generates oids of format 1.2.246.562.51.* , with * in range [10000000000, 40000000000)
  def generateYtrOid(oppijaOid: String): String = {
    val node = 51

    val number: Long = ytrMin + (OidGenerator.random.nextDouble() * (ytrMax - ytrMin).toDouble).toLong
    makeOID(node, number)
  }
}


// Gives twice the same oid for MockOppijat.opiskeluoikeudenOidKonflikti
class MockOidGenerator extends OidGenerator {
  private var previousOid: String = ""
  private var ytrCount = 0

  override def generateKoskiOid(oppijaOid: String): String = this.synchronized {
    if (oppijaOid != KoskiSpecificMockOppijat.opiskeluoikeudenOidKonflikti.oid) {
      super.generateKoskiOid("")
    } else if (previousOid.isEmpty) {
      getAndStoreKoski
    } else {
      getAndEmpty
    }
  }

  override def generateYtrOid(oppijaOid: String): String = {
    if (oppijaOid != KoskiSpecificMockOppijat.opiskeluoikeudenOidKonflikti.oid &&
        oppijaOid != KoskiSpecificMockOppijat.opiskeluoikeudenOidKonflikti2.oid) {
      super.generateYtrOid(oppijaOid)
    } else if (ytrCount == 0) {
      ytrCount = 1
      getAndStoreYtr
    } else if (ytrCount == 1) {
      ytrCount = 2
      previousOid
    } else {
      ytrCount = 0
      getAndEmpty
    }
  }

  private def getAndStoreKoski: String = {
    previousOid = super.generateKoskiOid("")
    previousOid
  }

  private def getAndStoreYtr: String = {
    previousOid = super.generateYtrOid("")
    previousOid
  }

  private def getAndEmpty: String = {
    val oid = previousOid
    previousOid = ""
    oid
  }
}
