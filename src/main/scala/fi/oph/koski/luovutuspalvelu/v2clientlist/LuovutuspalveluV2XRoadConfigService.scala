package fi.oph.koski.luovutuspalvelu.v2clientlist

import com.typesafe.config.Config
import fi.oph.koski.config.{Environment, SecretsManager}

trait LuovutuspalveluV2XRoadConfigService {
  def getSecurityServers: List[XRoadSecurityServer]
  def getXRoadClients: Map[String, String]
}

class MockLuovutuspalveluV2XRoadConfigService extends LuovutuspalveluV2XRoadConfigService {
  override def getSecurityServers: List[XRoadSecurityServer] = List(
    XRoadSecurityServer("CN=liityntapalvelin",List("0.0.0.0"))
  )

  override def getXRoadClients: Map[String, String] = Map(
    "SUBSYSTEM:FI-TEST/GOV/0245437-2/ServiceViewClient" -> "SuomiFi",
    "SUBSYSTEM:FI-TEST/GOV/000000-1/TestSystem" -> "HSL"
  )
}

class RemoteLuovutuspalveluV2XRoadConfigService extends LuovutuspalveluV2XRoadConfigService {
  private val secretsManagerClient = new SecretsManager

  private val securityServerSecretId = secretsManagerClient.getSecretId("Luovutuspalvelu V2 X-Road security server list", "LUOVUTUSPALVELU_V2_SECURITY_SEVER_LIST_SECRET_ID")
  private val securityServers = secretsManagerClient.getStructuredSecret[List[XRoadSecurityServer]](securityServerSecretId)

  private val xRoadClientsSecretId = secretsManagerClient.getSecretId("Luovutuspalvelu V2 X-Road clients", "LUOVUTUSPALVELU_V2_XROAD_CLIENTS_SECRET_ID")
  private val xRoadClients = secretsManagerClient.getStructuredSecret[Map[String, String]](xRoadClientsSecretId)

  override def getSecurityServers: List[XRoadSecurityServer] = securityServers
  override def getXRoadClients: Map[String, String] = xRoadClients

}

object LuovutuspalveluV2XRoadConfigService {
  def apply(config: Config): LuovutuspalveluV2XRoadConfigService = {
    if (Environment.isServerEnvironment(config) && Environment.usesAwsSecretsManager) {
      new RemoteLuovutuspalveluV2XRoadConfigService
    } else {
      new MockLuovutuspalveluV2XRoadConfigService
    }
  }
}

case class XRoadSecurityServer(subjectDn: String, ips: List[String])
