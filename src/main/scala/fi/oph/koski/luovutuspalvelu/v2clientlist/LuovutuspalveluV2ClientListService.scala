package fi.oph.koski.luovutuspalvelu.v2clientlist

import com.typesafe.config.Config
import fi.oph.koski.config.{Environment, SecretsManager}
import fi.oph.koski.json.JsonSerializer

trait LuovutuspalveluV2ClientListService {
  def getClientList: List[LuovutuspalveluClient]
}

class MockLuovutuspalveluV2ClientListService extends LuovutuspalveluV2ClientListService {
  private val mockClientListJson =
    """[
      |{"subjectDn": "CN=oa2-dev.koski-luovutuspalvelu-test-certs.testiopintopolku.fi", "ips": ["0.0.0.0"],  "user": "koskioauth2sampledevpk"},
      |{"subjectDn":"CN=migri", "ips":["0.0.0.0"], "user": "Migri"},
      |{"subjectDn":"CN=kela", "ips":["0.0.0.0"], "user": "Laaja"},
      |{"subjectDn":"CN=omadataoauth2sample", "ips":["0.0.0.0"], "user": "omadataoauth2sample"},
      |{"subjectDn":"CN=oauth2client", "ips":["0.0.0.0"], "user": "oauth2client"}
      |]""".stripMargin
  private val mockClientList = JsonSerializer.parse[List[LuovutuspalveluClient]](mockClientListJson)
  override def getClientList: List[LuovutuspalveluClient] = {
    mockClientList
  }
}

class RemoteLuovutuspalveluV2ClientListService extends LuovutuspalveluV2ClientListService {
  private val secretsManagerClient = new SecretsManager

  private val secretId = secretsManagerClient.getSecretId("Luovutuspalvelu V2 client list", "LUOVUTUSPALVELU_V2_CLIENT_LIST_SECRET_ID")
  private val clientList = secretsManagerClient.getStructuredSecret[List[LuovutuspalveluClient]](secretId)

  override def getClientList: List[LuovutuspalveluClient] = {
    clientList
  }

}

object LuovutuspalveluV2ClientListService {
  def apply(config: Config): LuovutuspalveluV2ClientListService = {
    if (Environment.isServerEnvironment(config) && Environment.usesAwsSecretsManager) {
      new RemoteLuovutuspalveluV2ClientListService
    } else {
      new MockLuovutuspalveluV2ClientListService
    }
  }
}

case class LuovutuspalveluClient(subjectDn: String, ips: List[String], user: String)
