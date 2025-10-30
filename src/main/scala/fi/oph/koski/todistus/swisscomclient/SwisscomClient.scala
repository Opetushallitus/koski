package fi.oph.koski.todistus.swisscomclient

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.log.Logging
import fi.oph.koski.todistus.swisscomclient.SwisscomConfigSecretsSource.MOCK_FROM_CONFIG
import org.apache.pdfbox.cos.COSDictionary
import org.apache.pdfbox.io.IOUtils
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.{ExternalSigningSupport, PDSeedValue, PDSeedValueMDP, PDSignature, SignatureOptions}
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField

import java.io.{ByteArrayOutputStream, InputStream, OutputStream}
import java.security.MessageDigest
import java.util
import java.util.{Base64, Calendar}

import scala.util.{Failure, Success, Using}

object SwisscomClient extends Logging {
  def apply(config: SwisscomConfig): SwisscomClient = {
    if (config.configSource == MOCK_FROM_CONFIG) {
      logger.info(s"Using mock Swisscom client")
      new MockSwisscomClient(config)
    } else {
      logger.info(s"Using Swisscom integration endpoint ${config.signUrl}")
      new RemoteSwisscomClient(config)
    }
  }
}

trait SwisscomClient extends Logging {
  def config: SwisscomConfig

  protected def requestSignature(req: SwisscomAISSignRequest): Either[HttpStatus, SwisscomAISSignResponse]

  def signWithStaticCertificate(id: String, contentIn: InputStream, contentOut: OutputStream): Either[HttpStatus, SwisscomAISSignResponse] = {
    Using.Manager { use => {
      val pdDocument = use(PDDocument.load(contentIn))
      val inMemoryStream: ByteArrayOutputStream = new ByteArrayOutputStream()

      val (pbSigningSupport: ExternalSigningSupport, base64HashToSign: String) =
        prepareForSigning(pdDocument, inMemoryStream)

      val aisSignRequest = SwisscomAISSignRequest(id, base64HashToSign, config)

      for {
        signResponse <- requestSignatureWithLogging(aisSignRequest)
        signature <- signResponse.SignResponse.SignatureObject.flatMap(_.Base64Signature.flatMap(_.`$`))
          .toRight(KoskiErrorCategory.internalError("No signature found"))
          .map(base64Decode)

        _ = signContent(use, contentOut, inMemoryStream, pbSigningSupport, signResponse, signature)
      } yield signResponse
    }} match {
      case Success(value) =>
        value
      case Failure(ex) =>
        logger.warn(ex)("Signing with static certificate failed")
        Left(KoskiErrorCategory.internalError("Signing with static certificate failed: " + ex.getMessage))
    }
  }

  private def prepareForSigning(pdDocument: PDDocument, inMemoryStream: ByteArrayOutputStream) = {
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

    val pbSigningSupport: ExternalSigningSupport = pdDocument.saveIncrementalForExternalSigning(inMemoryStream)

    val digest = MessageDigest.getInstance(config.digestAlgorithm)

    val contentToSign = IOUtils.toByteArray(pbSigningSupport.getContent)
    val hashToSign = digest.digest(contentToSign)
    options.close()
    val base64HashToSign: String = Base64.getEncoder.encodeToString(hashToSign)

    (pbSigningSupport, base64HashToSign)
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

  def requestSignatureWithLogging(req: SwisscomAISSignRequest): Either[HttpStatus, SwisscomAISSignResponse] = {
    logger.info(s"SEND RequestId: ${req.SignRequest.`@RequestID`}")

    val result = requestSignature(req)

    result match {
      case Right(response) =>
        logger.info(s"SUCCESS Response for RequestId: ${req.SignRequest.`@RequestID`}: ${response.SignResponse.`@RequestID`}, ${response.SignResponse.Result}")
      case Left(error) =>
        logger.warn(s"SUCCESS Response for RequestId: ${req.SignRequest.`@RequestID`}: ${error.toString}")
    }

    result
  }

  private def signContent(
                           use: Using.Manager,
                           contentOut: OutputStream,
                           inMemoryStream: ByteArrayOutputStream,
                           pbSigningSupport: ExternalSigningSupport,
                           signResponse: SwisscomAISSignResponse,
                           signature: Array[Byte]
  ): Unit = {
    val crlEntries =
      signResponse.SignResponse.OptionalOutputs.flatMap(_.`sc.RevocationInformation`.flatMap(_.`sc.CRLs`.map(_.`sc.CRL`))).toList.flatten
        .map(base64Decode)

    val ocspEntries =
      signResponse.SignResponse.OptionalOutputs.flatMap(_.`sc.RevocationInformation`.flatMap(_.`sc.OCSPs`.map(_.`sc.OCSP`))).toList.flatten
        .map(base64Decode)

    pbSigningSupport.setSignature(signature)

    if (crlEntries.nonEmpty || ocspEntries.nonEmpty) {
      val documentBytes = inMemoryStream.toByteArray
      val pdDocument = use(PDDocument.load(documentBytes))
      SwisscomCRLAndOCSPExtender.extendPdfWithCrlAndOcsp(pdDocument, documentBytes, crlEntries, ocspEntries)
      pdDocument.saveIncremental(contentOut)
    }
    else {
      contentOut.write(inMemoryStream.toByteArray)
    }
  }

  private def base64Decode(str: String) = Base64.getDecoder().decode(str)
}
