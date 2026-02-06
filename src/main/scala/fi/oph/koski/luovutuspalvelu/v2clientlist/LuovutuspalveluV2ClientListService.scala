package fi.oph.koski.luovutuspalvelu.v2clientlist

import com.typesafe.config.Config
import fi.oph.koski.config.{Environment, SecretsManager}
import fi.oph.koski.json.JsonSerializer

trait LuovutuspalveluV2ClientListService {
  def getClientList: List[LuovutuspalveluClient]
}

class MockLuovutuspalveluV2ClientListService extends LuovutuspalveluV2ClientListService {
  // Huom: omadataoauth2sample-käyttäjällä on kaksi sertifikaattia:
  // - CN=omadataoauth2sample: yksikkötestien käyttämä
  // - CN=client.example.com,O=Testi,C=FI: Java-esimerkkiclientin käyttämä (omadata-oauth2-sample/server/testca/generate-certs.sh)
  private val mockClientListJson =
    """[
      |{"subjectDn": "CN=oa2-dev.koski-luovutuspalvelu-test-certs.testiopintopolku.fi", "ips": ["0.0.0.0"],  "user": "koskioauth2sampledevpk"},
      |{"subjectDn":"CN=migri", "ips":["0.0.0.0"], "user": "Migri"},
      |{"subjectDn":"CN=kela", "ips":["0.0.0.0"], "user": "Laaja"},
      |{"subjectDn":"CN=kela-suppeat", "ips":["0.0.0.0"], "user": "Suppea"},
      |{"subjectDn":"CN=omadataoauth2sample", "ips":["0.0.0.0"], "user": "omadataoauth2sample"},
      |{"subjectDn":"CN=client.example.com,O=Testi,C=FI", "ips":["0.0.0.0"], "user": "omadataoauth2sample"},
      |{"subjectDn":"CN=oauth2client", "ips":["0.0.0.0"], "user": "oauth2client"},
      |{"subjectDn":"CN=oauth2kaikkiclient", "ips":["0.0.0.0"], "user": "oauth2kaikkiclient"},
      |{"subjectDn":"CN=oauth2clienteirek", "ips":["0.0.0.0"], "user": "oauth2clienteirek"},
      |{"subjectDn":"CN=oauth2oph", "ips":["0.0.0.0"], "user": "oauth2oph"},
      |{"subjectDn":"CN=oauth2hsl", "ips":["0.0.0.0"], "user": "oauth2hsl"},
      |{"subjectDn":"CN=tilastokeskus", "ips":["0.0.0.0"], "user": "Teppo"},
      |{"subjectDn":"CN=valpas-kela", "ips":["0.0.0.0"], "user": "valpas-kela"},
      |{"subjectDn":"CN=valpas-helsinki", "ips":["0.0.0.0"], "user": "valpas-helsinki"},
      |{"subjectDn":"CN=valvira", "ips":["0.0.0.0"], "user": "Ville"},
      |{"subjectDn":"CN=ytl", "ips":["0.0.0.0"], "user": "ylermi"},
      |{"subjectDn":"CN=sdg", "ips":["0.0.0.0"], "user": "Saleria"},
      |{"subjectDn":"CN=valpas-ytl", "ips":["0.0.0.0"], "user": "valpas-ytl"},
      |{"subjectDn":"CN=valpas-monta", "ips":["0.0.0.0"], "user": "valpas-monta"},
      |{"subjectDn":"CN=kalle", "ips":["0.0.0.0"], "user": "kalle"}
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
