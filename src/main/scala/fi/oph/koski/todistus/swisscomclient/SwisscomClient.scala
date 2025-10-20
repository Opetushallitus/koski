package fi.oph.koski.todistus.swisscomclient

import com.typesafe.config.Config
import fi.oph.koski.json.Json4sHttp4s.json4sEncoderOf
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.log.Logging
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

object SwisscomClient extends Logging {
  def apply(config: Config): SwisscomClient = {
    // TODO: Mock etc.
    logger.info(s"Using Swisscom integration endpoint ${config.getString("todistus.swisscom.server.rest.signUrl")}")

    new RemoteSwisscomClient(config)
  }
}

trait SwisscomClient {
  def config: Config

  protected def requestSignature(req: AISSignRequest): Option[AISSignResponse]

  def signWithStaticCertificate(contentIn: InputStream, contentOut: OutputStream): SignatureResult = {
    val conf = config.getConfig("todistus.swisscom")

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
    pdSignature.setName(conf.getString("signature.name"))
    pdSignature.setReason(conf.getString("signature.reason"))
    pdSignature.setLocation(conf.getString("signature.location"))
    pdSignature.setContactInfo(conf.getString("signature.contactInfo"))

    val options = new SignatureOptions
    options.setPreferredSignatureSize(conf.getInt("signature.preferredSize"))

    pdDocument.addSignature(pdSignature, options)
    // Set this signature's access permissions level to 0, to ensure we just sign the PDF, not certify it
    // for more details: https://wwwimages2.adobe.com/content/dam/acom/en/devnet/pdf/pdfs/PDF32000_2008.pdf see section 12.7.4.5
    setPermissionsForSignatureOnly(pdDocument)

    val inMemoryStream: ByteArrayOutputStream = new ByteArrayOutputStream()

    val pbSigningSupport: ExternalSigningSupport = pdDocument.saveIncrementalForExternalSigning(inMemoryStream)

    val digest = MessageDigest.getInstance(conf.getString("digestAlgorithm"))

    val contentToSign = IOUtils.toByteArray(pbSigningSupport.getContent)
    val hashToSign = digest.digest(contentToSign)
    options.close()
    val base64HashToSign: String = Base64.getEncoder.encodeToString(hashToSign)

    val id = "DOC-" + UUID.randomUUID().toString
    val requestId = "ID-" + UUID.randomUUID().toString

    // buildAisSignRequest
    val documentHashes: List[DocumentHash] = List(DocumentHash(
      id,
      DsigDigestMethod(conf.getString("digestUri")),
      base64HashToSign
    ))

    val inputDocuments = InputDocuments(documentHashes)

    val addTimestamp = AddTimestamp("urn:ietf:rfc:3161")
    val claimedIdentity = ClaimedIdentity(s"${conf.getString("signature.claimedIdentityName")}:${conf.getString("signature.claimedIdentityKey")}")
    val addRevocationInformation = ScAddRevocationInformation(conf.getString("signature.revocationInformation"))
    val signatureStandard = conf.getString("signature.standard")

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
  myConfig: Config
) extends SwisscomClient with Logging {
  val config = myConfig

  protected def requestSignature(req: AISSignRequest): Option[AISSignResponse] = {
    // TODO: TOR-2400

    logger.info(s"TODO: Request: ${req}")

    None
  }
}
