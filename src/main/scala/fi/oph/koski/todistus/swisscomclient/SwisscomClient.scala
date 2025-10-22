package fi.oph.koski.todistus.swisscomclient

import com.typesafe.config.Config
import fi.oph.koski.config.SecretsManager
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.log.{Logging, NotLoggable}
import fi.oph.koski.todistus.swisscomclient.SignatureResult.SignatureResult
import org.apache.pdfbox.cos.COSDictionary
import org.apache.pdfbox.io.IOUtils
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.{ExternalSigningSupport, PDSeedValue, PDSeedValueMDP, PDSignature, SignatureOptions}
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField

import java.io.{ByteArrayOutputStream, InputStream, OutputStream}
import java.security.MessageDigest
import java.util
import java.util.{Base64, Calendar, UUID}
import org.json4s.jackson.JsonMethods.parse
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest


object SwisscomClient extends Logging {
  def apply(config: SwisscomConfig): SwisscomClient = {
    // TODO: Mock etc.
    logger.info(s"Using Swisscom integration endpoint ${config.signUrl}")

    new RemoteSwisscomClient(config)
  }
}

case class SwisscomConfig(
  signUrl: String,
  keyStore: String,
  keyStorePassword: String,
  digestAlgorithm: String,
  digestUri: String,
  signaturePreferredSize: Int,
  signatureStandard: String,
  signatureRevocationInformation: String,
  signatureClaimedIdentityName: String,
  signatureClaimedIdentityKey: String,
  signatureDistinguishedName: String,
  signatureName: String,
  signatureReason: String,
  signatureLocation: String,
  signatureContactInfo: String
) extends NotLoggable

object SwisscomConfig extends Logging {
  def fromConfig(config: Config): SwisscomConfig = {
    val swisscomConfig = config.getConfig("todistus.swisscom")
    val signatureConfig = swisscomConfig.getConfig("signature")

    SwisscomConfig(
      signUrl = swisscomConfig.getString("signUrl"),
      keyStore = "",
      keyStorePassword = "",
      digestAlgorithm = swisscomConfig.getString("digestAlgorithm"),
      digestUri = swisscomConfig.getString("digestUri"),
      signaturePreferredSize = signatureConfig.getInt("preferredSize"),
      signatureStandard = signatureConfig.getString("standard"),
      signatureRevocationInformation = signatureConfig.getString("revocationInformation"),
      signatureClaimedIdentityName = "mock",
      signatureClaimedIdentityKey = "mock",
      signatureDistinguishedName = "mock",
      signatureName = signatureConfig.getString("name"),
      signatureReason = signatureConfig.getString("reason"),
      signatureLocation = signatureConfig.getString("location"),
      signatureContactInfo = signatureConfig.getString("contactInfo")
    )
  }

  def fromSecretsManager(config: Config): SwisscomConfig = {
    val swisscomConfig = config.getConfig("todistus.swisscom")
    val signatureConfig = swisscomConfig.getConfig("signature")

    val cachedSecretsClient = new SecretsManager
    val secretId = cachedSecretsClient.getSecretId("Swisscom Secrets", "SWISSCOM_SECRET_ID")
    val secrets = cachedSecretsClient.getStructuredSecret[SwisscomSecretsConfig](secretId)

    SwisscomConfig(
      signUrl = swisscomConfig.getString("signUrl"),
      keyStore = secrets.keyStore,
      keyStorePassword = secrets.keyStorePassword,
      digestAlgorithm = swisscomConfig.getString("digestAlgorithm"),
      digestUri = swisscomConfig.getString("digestUri"),
      signaturePreferredSize = signatureConfig.getInt("preferredSize"),
      signatureStandard = signatureConfig.getString("standard"),
      signatureRevocationInformation = signatureConfig.getString("revocationInformation"),
      signatureClaimedIdentityName = secrets.signatureClaimedIdentityName,
      signatureClaimedIdentityKey = secrets.signatureClaimedIdentityKey,
      signatureDistinguishedName = secrets.signatureDistinguishedName,
      signatureName = signatureConfig.getString("name"),
      signatureReason = signatureConfig.getString("reason"),
      signatureLocation = signatureConfig.getString("location"),
      signatureContactInfo = signatureConfig.getString("contactInfo")
    )
  }

  def fromSsoCredentialsSecretsManager(config: Config): SwisscomConfig = {
    val swisscomConfig = config.getConfig("todistus.swisscom")
    val signatureConfig = swisscomConfig.getConfig("signature")

    val secretId = sys.env.getOrElse("SWISSCOM_SECRET_ID", "swisscom-secrets")

    val ssoFromProfile =
      ProfileCredentialsProvider.builder()
        .profileName(sys.env.getOrElse("AWS_PROFILE", "oph-koski-dev"))
        .build()

    val smClient = SecretsManagerClient.builder()
      .region(Region.EU_WEST_1)
      .credentialsProvider(ssoFromProfile)
      .build()

    val req = GetSecretValueRequest.builder().secretId(secretId).build()
    val resp = smClient.getSecretValue(req)

    val secrets = JsonSerializer.extract[SwisscomSecretsConfig](parse(resp.secretString()), ignoreExtras = true)

    SwisscomConfig(
      signUrl = swisscomConfig.getString("signUrl"),
      keyStore = secrets.keyStore,
      keyStorePassword = secrets.keyStorePassword,
      digestAlgorithm = swisscomConfig.getString("digestAlgorithm"),
      digestUri = swisscomConfig.getString("digestUri"),
      signaturePreferredSize = signatureConfig.getInt("preferredSize"),
      signatureStandard = signatureConfig.getString("standard"),
      signatureRevocationInformation = signatureConfig.getString("revocationInformation"),
      signatureClaimedIdentityName = secrets.signatureClaimedIdentityName,
      signatureClaimedIdentityKey = secrets.signatureClaimedIdentityKey,
      signatureDistinguishedName = secrets.signatureDistinguishedName,
      signatureName = signatureConfig.getString("name"),
      signatureReason = signatureConfig.getString("reason"),
      signatureLocation = signatureConfig.getString("location"),
      signatureContactInfo = signatureConfig.getString("contactInfo")
    )
  }
}

case class SwisscomSecretsConfig(
  keyStore: String,
  keyStorePassword: String,
  signatureClaimedIdentityName: String,
  signatureClaimedIdentityKey: String,
  signatureDistinguishedName: String,
) extends NotLoggable

trait SwisscomClient {
  def config: SwisscomConfig

  protected def requestSignature(req: AISSignRequest): Option[AISSignResponse]

  def signWithStaticCertificate(contentIn: InputStream, contentOut: OutputStream): SignatureResult = {
    // prepareForSigning
    val pdDocument = PDDocument.load(contentIn)

    val pdSignature = new PDSignature

    pdSignature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE)
    pdSignature.setSubFilter(PDSignature.SUBFILTER_ETSI_CADES_DETACHED)
    // Add 3 Minutes to move signing time within the OnDemand Certificate Validity
    // This is only relevant _in case the signature does not include a timestamp_
    // See section 5.8.5.1 of the Reference Guide
    val signDate = Calendar.getInstance
    signDate.add(Calendar.MINUTE, 3)

    pdSignature.setSignDate(signDate)
    // TODO: TOR-2400: Tarkista, tarvitseeko näiden olla konffattavissa, vai jäävätkö vain placeholderiksi?
    // Jos vain placeholder, poista konffeista.
    pdSignature.setName(config.signatureName)
    pdSignature.setReason(config.signatureReason)
    pdSignature.setLocation(config.signatureLocation)
    pdSignature.setContactInfo(config.signatureContactInfo)

    val options = new SignatureOptions
    options.setPreferredSignatureSize(config.signaturePreferredSize)

    pdDocument.addSignature(pdSignature, options)
    // Set this signature's access permissions level to 0, to ensure we just sign the PDF, not certify it
    // for more details: https://wwwimages2.adobe.com/content/dam/acom/en/devnet/pdf/pdfs/PDF32000_2008.pdf see section 12.7.4.5
    setPermissionsForSignatureOnly(pdDocument)

    val inMemoryStream: ByteArrayOutputStream = new ByteArrayOutputStream()

    val pbSigningSupport: ExternalSigningSupport = pdDocument.saveIncrementalForExternalSigning(inMemoryStream)

    val digest = MessageDigest.getInstance(config.digestAlgorithm)

    val contentToSign = IOUtils.toByteArray(pbSigningSupport.getContent)
    val hashToSign = digest.digest(contentToSign)
    options.close()
    val base64HashToSign: String = Base64.getEncoder.encodeToString(hashToSign)

    val id = "DOC-" + UUID.randomUUID().toString
    val requestId = "ID-" + UUID.randomUUID().toString

    // buildAisSignRequest
    val documentHashes: List[DocumentHash] = List(DocumentHash(
      id,
      DsigDigestMethod(config.digestUri),
      base64HashToSign
    ))

    val inputDocuments = InputDocuments(documentHashes)

    val addTimestamp = AddTimestamp("urn:ietf:rfc:3161")
    val claimedIdentity = ClaimedIdentity(s"${config.signatureClaimedIdentityName}:${config.signatureClaimedIdentityKey}")
    val addRevocationInformation = ScAddRevocationInformation(config.signatureRevocationInformation)
    val signatureStandard = config.signatureStandard

    val optionalInputs = OptionalInputs(
      addTimestamp,
      claimedIdentity,
      "urn:ietf:rfc:3369",
      addRevocationInformation,
      signatureStandard
    )

    val aisSignRequest = AISSignRequest(
      SignRequest(
        `@RequestID` = requestId,
        InputDocuments = inputDocuments,
        OptionalInputs = optionalInputs
      )
    )

    // requestSignature
    val result = requestSignature(aisSignRequest)

    // TODO: finishDocumentsSigning

    SignatureResult.SUCCESS
  }

  private def setPermissionsForSignatureOnly(pdDocument: PDDocument): Unit = {
    val signatureFields: util.List[PDSignatureField] = pdDocument.getSignatureFields
    val pdSignatureField: PDSignatureField = signatureFields.get(signatureFields.size - 1)
    var pdSeedValue: PDSeedValue = pdSignatureField.getSeedValue
    if (pdSeedValue == null) {
      val newSeedValueDict: COSDictionary = new COSDictionary
      newSeedValueDict.setNeedToBeUpdated(true)
      pdSeedValue = new PDSeedValue(newSeedValueDict)
      pdSignatureField.setSeedValue(pdSeedValue)
    }
    var pdSeedValueMDP: PDSeedValueMDP = pdSeedValue.getMDP
    if (pdSeedValueMDP == null) {
      val newMDPDict: COSDictionary = new COSDictionary
      newMDPDict.setNeedToBeUpdated(true)
      pdSeedValueMDP = new PDSeedValueMDP(newMDPDict)
      pdSeedValue.setMPD(pdSeedValueMDP)
    }
    pdSeedValueMDP.setP(0) // identify this signature as an author signature, not document certification
  }
}

class RemoteSwisscomClient(
  myConfig: SwisscomConfig
) extends SwisscomClient with Logging {
  val config = myConfig

  protected def requestSignature(req: AISSignRequest): Option[AISSignResponse] = {
    // TODO: TOR-2400

    logger.info(s"Request: ${req}")

    val foo: String = JsonSerializer.writeWithRoot(req)

    logger.info("Request JSON: "+ foo)

    None
  }
}
