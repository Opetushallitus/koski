package fi.oph.koski.todistus

import fi.oph.koski.log.Logging
import org.apache.pdfbox.Loader
import org.apache.pdfbox.cos.{COSArray, COSDictionary, COSName, COSStream}
import org.apache.pdfbox.io.RandomAccessReadBufferedFile
import org.apache.pdfbox.pdmodel.PDDocument
import org.bouncycastle.asn1.cms.ContentInfo
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.cert.ocsp.{BasicOCSPResp, OCSPResp}
import org.bouncycastle.cert.X509CRLHolder
import org.bouncycastle.cms.{CMSSignedData, SignerInformation}
import org.bouncycastle.tsp.TimeStampToken

import java.io.File
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success, Try, Using}

object PdfSignatureAnalyzer extends Logging {

  case class ValidationConfig(
    maxAllekirjoittamatonProsentti: Double,
    minVarmenneketjunKoko: Int,
    sallitutTiivistealgoritmit: Set[String],
    hashValidointi: Boolean,
    odotettuAllekirjoittajanNimi: String
  )

  object ValidationConfig {
    def fromConfig(config: com.typesafe.config.Config): ValidationConfig = {
      import scala.jdk.CollectionConverters._
      ValidationConfig(
        maxAllekirjoittamatonProsentti = config.getDouble("todistus.allekirjoitusvalidointi.maxAllekirjoittamatonProsentti"),
        minVarmenneketjunKoko = config.getInt("todistus.allekirjoitusvalidointi.minVarmenneketjunKoko"),
        sallitutTiivistealgoritmit = config.getStringList("todistus.allekirjoitusvalidointi.sallitutTiivistealgoritmit").asScala.toSet,
        hashValidointi = config.getBoolean("todistus.allekirjoitusvalidointi.hashValidointi"),
        odotettuAllekirjoittajanNimi = config.getString("todistus.allekirjoitusvalidointi.odotettuAllekirjoittajanNimi")
      )
    }
  }

  case class ByteRangeInfo(
    signedRange1Start: Int,
    signedRange1Length: Int,
    signatureStart: Int,
    signatureEnd: Int,
    signatureLength: Int,
    signedRange2Start: Int,
    signedRange2Length: Int,
    totalSignedLength: Int,
    unsignedAfterSignatureStart: Int,
    unsignedAfterSignatureLength: Int,
    pdfTotalLength: Int,
    validationErrors: List[String]
  ) {
    def unsignedPercentage: Double = (unsignedAfterSignatureLength.toDouble / pdfTotalLength.toDouble) * 100.0
    def isValid: Boolean = validationErrors.isEmpty
  }

  object ByteRangeInfo {
    def invalid(pdfTotalLength: Int, validationErrors: List[String]): ByteRangeInfo =
      ByteRangeInfo(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, pdfTotalLength, validationErrors)
  }

  case class SignatureContentsInfo(
    hexContent: String,
    pkcs7Bytes: Array[Byte],
    isValid: Boolean,
    validationErrors: List[String]
  )

  object SignatureContentsInfo {
    def invalid(validationErrors: List[String]): SignatureContentsInfo =
      SignatureContentsInfo("", Array.empty[Byte], false, validationErrors)
  }

  case class PKCS7Info(
    signerCount: Int,
    signers: List[SignerInfo],
    certificates: List[CertificateInfo],
    signatureValid: Option[Boolean],
    signatureValidationError: Option[String],
    isValid: Boolean,
    validationErrors: List[String]
  )

  case class SignerInfo(
    digestAlgorithm: String,
    encryptionAlgorithm: String,
    certificates: List[CertificateInfo],
    hasSignatureTimestamp: Boolean,
    signatureTimestampTime: Option[String]
  )

  case class CertificateInfo(
    subject: String,
    issuer: String,
    notBefore: String,
    notAfter: String,
    serialNumber: String
  )

  case class OCSPInfo(
    producedAt: String
  )

  case class CRLInfo(
    issuer: String,
    thisUpdate: String,
    nextUpdate: String,
    revokedCertCount: Int
  )

  case class DSSInfo(
    hasOCSPs: Boolean,
    ocspCount: Int,
    ocsps: List[OCSPInfo],
    hasCRLs: Boolean,
    crlCount: Int,
    crls: List[CRLInfo],
    hasVRI: Boolean,
    vriCount: Int,
    vriKeys: List[String],
    hasCerts: Boolean,
    certsCount: Int,
    isValid: Boolean,
    validationErrors: List[String]
  )

  object DSSInfo {
    def invalid(validationErrors: List[String]): DSSInfo =
      DSSInfo(
        hasOCSPs = false,
        ocspCount = 0,
        ocsps = List.empty,
        hasCRLs = false,
        crlCount = 0,
        crls = List.empty,
        hasVRI = false,
        vriCount = 0,
        vriKeys = List.empty,
        hasCerts = false,
        certsCount = 0,
        isValid = false,
        validationErrors = validationErrors
      )
  }

  case class AnalysisReport(
    byteRange: ByteRangeInfo,
    signatureContents: SignatureContentsInfo,
    pkcs7: Option[PKCS7Info],
    dss: DSSInfo,
    overallValid: Boolean,
    summary: String
  ) {
    def allValidationErrors: List[String] =
      byteRange.validationErrors ++
        signatureContents.validationErrors ++
        pkcs7.toList.flatMap(_.validationErrors) ++
        dss.validationErrors
  }

  def analyzePdfFile(pdfFile: File, validationConfig: ValidationConfig): Try[AnalysisReport] = {
    Using.Manager { use =>
      val document = use(Loader.loadPDF(new RandomAccessReadBufferedFile(pdfFile)))
      val pdfBytes = java.nio.file.Files.readAllBytes(pdfFile.toPath)
      analyzePdfDocument(pdfBytes, document, validationConfig)
    }
  }

  def analyzePdfDocument(pdfBytes: Array[Byte], document: PDDocument, validationConfig: ValidationConfig): AnalysisReport = {
    val signature = document.getLastSignatureDictionary

    if (signature == null) {
      val error = List("PDF does not contain a signature")
      return AnalysisReport(
        byteRange = ByteRangeInfo.invalid(pdfBytes.length, error),
        signatureContents = SignatureContentsInfo.invalid(error),
        pkcs7 = None,
        dss = DSSInfo.invalid(error),
        overallValid = false,
        summary = "=== PDF SIGNATURE ANALYSIS ===\n\nPDF does not contain a digital signature.\n"
      )
    }

    val byteRange = signature.getByteRange

    val byteRangeInfo = analyzeByteRange(byteRange, pdfBytes, validationConfig)

    val signatureContentsInfo = analyzeSignatureContents(
      pdfBytes,
      byteRangeInfo.signatureStart,
      byteRangeInfo.signatureEnd
    )

    // Extract signed content for cryptographic verification
    val signedContent = if (byteRangeInfo.isValid) {
      Some(getSignedContent(pdfBytes, byteRangeInfo))
    } else {
      None
    }

    val pkcs7Info = if (signatureContentsInfo.isValid) {
      analyzePKCS7(signatureContentsInfo.pkcs7Bytes, signedContent, validationConfig)
    } else {
      None
    }

    val dssInfo = analyzeDSS(document, pdfBytes, byteRangeInfo)

    val overallValid = byteRangeInfo.isValid &&
      signatureContentsInfo.isValid &&
      pkcs7Info.forall(_.isValid) &&
      dssInfo.isValid

    val summary = generateSummary(byteRangeInfo, signatureContentsInfo, pkcs7Info, dssInfo, overallValid)

    AnalysisReport(
      byteRange = byteRangeInfo,
      signatureContents = signatureContentsInfo,
      pkcs7 = pkcs7Info,
      dss = dssInfo,
      overallValid = overallValid,
      summary = summary
    )
  }

  private def analyzeByteRange(byteRange: Array[Int], pdfBytes: Array[Byte], validationConfig: ValidationConfig): ByteRangeInfo = {
    val signedRange1Start = byteRange(0)
    val signedRange1Length = byteRange(1)
    val signedRange2Start = byteRange(2)
    val signedRange2Length = byteRange(3)

    val signatureStart = signedRange1Start + signedRange1Length
    val signatureEnd = signedRange2Start
    val signatureLength = signatureEnd - signatureStart

    val totalSignedLength = signedRange1Length + signedRange2Length
    val unsignedAfterSignatureStart = signedRange2Start + signedRange2Length
    val unsignedAfterSignatureLength = pdfBytes.length - unsignedAfterSignatureStart

    val validationErrors = scala.collection.mutable.ListBuffer[String]()
    val unsignedPercentage = (unsignedAfterSignatureLength.toDouble / pdfBytes.length.toDouble) * 100.0

    if (unsignedPercentage > validationConfig.maxAllekirjoittamatonProsentti) {
      validationErrors += f"Unsigned portion is too large: $unsignedPercentage%.2f%% (max ${validationConfig.maxAllekirjoittamatonProsentti}%.2f%%)"
    }

    ByteRangeInfo(
      signedRange1Start = signedRange1Start,
      signedRange1Length = signedRange1Length,
      signatureStart = signatureStart,
      signatureEnd = signatureEnd,
      signatureLength = signatureLength,
      signedRange2Start = signedRange2Start,
      signedRange2Length = signedRange2Length,
      totalSignedLength = totalSignedLength,
      unsignedAfterSignatureStart = unsignedAfterSignatureStart,
      unsignedAfterSignatureLength = unsignedAfterSignatureLength,
      pdfTotalLength = pdfBytes.length,
      validationErrors = validationErrors.toList
    )
  }

  private def analyzeSignatureContents(
    pdfBytes: Array[Byte],
    signatureStart: Int,
    signatureEnd: Int
  ): SignatureContentsInfo = {
    val validationErrors = scala.collection.mutable.ListBuffer[String]()

    val signatureBytes = pdfBytes.slice(signatureStart, signatureEnd)
    val signatureBytesStr = new String(signatureBytes, StandardCharsets.ISO_8859_1)

    if (!signatureBytesStr.startsWith("<")) {
      validationErrors += "Signature value does not start with '<'"
    }
    if (!signatureBytesStr.endsWith(">")) {
      validationErrors += "Signature value does not end with '>'"
    }

    val hexContent = if (signatureBytesStr.length >= 2) {
      signatureBytesStr.substring(1, signatureBytesStr.length - 1)
    } else {
      ""
    }

    val hexPattern = "^[0-9A-Fa-f\\s]*$".r
    if (hexPattern.findFirstIn(hexContent).isEmpty) {
      validationErrors += "Signature hex data contains invalid characters"
    }

    val nonWhitespaceCount = hexContent.count(c => !c.isWhitespace)
    val whitespaceCount = hexContent.length - nonWhitespaceCount

    if (whitespaceCount > 0) {
      validationErrors += s"Signature contains whitespace characters ($whitespaceCount)"
    }

    if (nonWhitespaceCount % 2 != 0) {
      validationErrors += s"Number of hex characters ($nonWhitespaceCount) is not even"
    }

    val pkcs7BytesResult = Try {
      val hexAsciiBytes = signatureBytes.drop(1).dropRight(1)
      val hexString = new String(hexAsciiBytes, StandardCharsets.ISO_8859_1).filterNot(_.isWhitespace)
      hexString.grouped(2).map(Integer.parseInt(_, 16).toByte).toArray
    }

    val pkcs7Bytes = pkcs7BytesResult match {
      case Success(bytes) => bytes
      case Failure(e) =>
        validationErrors += s"Hex string decoding failed: ${e.getMessage}"
        Array.empty[Byte]
    }

    SignatureContentsInfo(
      hexContent = hexContent,
      pkcs7Bytes = pkcs7Bytes,
      isValid = validationErrors.isEmpty,
      validationErrors = validationErrors.toList
    )
  }

  private def analyzePKCS7(
    signatureBytes: Array[Byte],
    signedContent: Option[Array[Byte]],
    validationConfig: ValidationConfig
  ): Option[PKCS7Info] = {
    if (signatureBytes.length < 10) {
      logger.warn(s"PKCS#7 data on liian lyhyt (${signatureBytes.length} tavua)")
      return None
    }

    val validationErrors = scala.collection.mutable.ListBuffer[String]()

    // Parse ASN.1 length to find actual PKCS#7 structure size (may have padding at end)
    val actualLength = parseASN1Length(signatureBytes)
    val trimmedBytes = if (actualLength > 0 && actualLength < signatureBytes.length) {
      signatureBytes.take(actualLength)
    } else {
      signatureBytes
    }

    Try {
      val contentInfo = ContentInfo.getInstance(trimmedBytes)
      val cmsSignedData = new CMSSignedData(contentInfo)

      val signers = cmsSignedData.getSignerInfos.getSigners.asScala.toList

      val signerInfos = signers.map { signer: SignerInformation =>
        val digestAlg = signer.getDigestAlgOID
        val encryptionAlg = signer.getEncryptionAlgOID

        // Extract timestamp from signature timestamp token (TST)
        val (hasTimestamp, timestampTime) = Try {
          val unsignedAttrs = signer.getUnsignedAttributes
          if (unsignedAttrs != null) {
            // id-aa-signatureTimeStampToken OID: 1.2.840.113549.1.9.16.2.14
            val timestampOID = new ASN1ObjectIdentifier("1.2.840.113549.1.9.16.2.14")
            val timestampAttr = unsignedAttrs.get(timestampOID)
            if (timestampAttr != null) {
              Try {
                val timestampToken = timestampAttr.getAttrValues.getObjectAt(0)
                val tstInfo = ContentInfo.getInstance(timestampToken)
                val tstData = new TimeStampToken(tstInfo)
                val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                (true, Some(dateFormat.format(tstData.getTimeStampInfo.getGenTime)))
              }.getOrElse((true, None))
            } else {
              (false, None)
            }
          } else {
            (false, None)
          }
        }.getOrElse((false, None))

        val signerId = signer.getSID
        val signerCerts = cmsSignedData.getCertificates.getMatches(new org.bouncycastle.util.Selector[X509CertificateHolder] {
          override def `match`(cert: X509CertificateHolder): Boolean = {
            signerId.getSerialNumber == cert.getSerialNumber &&
              signerId.getIssuer.toString == cert.getIssuer.toString
          }
          override def clone(): Object = this
        }).asScala.toList

        val certInfos = signerCerts.map { cert =>
          val x509Cert = cert
          CertificateInfo(
            subject = x509Cert.getSubject.toString,
            issuer = x509Cert.getIssuer.toString,
            notBefore = x509Cert.getNotBefore.toString,
            notAfter = x509Cert.getNotAfter.toString,
            serialNumber = x509Cert.getSerialNumber.toString
          )
        }

        SignerInfo(
          digestAlgorithm = digestAlg,
          encryptionAlgorithm = encryptionAlg,
          certificates = certInfos,
          hasSignatureTimestamp = hasTimestamp,
          signatureTimestampTime = timestampTime
        )
      }

      val allCerts = cmsSignedData.getCertificates.getMatches(new org.bouncycastle.util.Selector[X509CertificateHolder] {
        override def `match`(cert: X509CertificateHolder): Boolean = true
        override def clone(): Object = this
      }).asScala.toList

      val allCertInfos = allCerts.map { cert =>
        val x509Cert = cert
        CertificateInfo(
          subject = x509Cert.getSubject.toString,
          issuer = x509Cert.getIssuer.toString,
          notBefore = x509Cert.getNotBefore.toString,
          notAfter = x509Cert.getNotAfter.toString,
          serialNumber = x509Cert.getSerialNumber.toString
        )
      }

      if (signers.isEmpty) {
        validationErrors += "PKCS#7 does not contain any signers"
      }

      if (allCerts.size < validationConfig.minVarmenneketjunKoko) {
        validationErrors += s"PKCS#7 does not contain a certificate chain (only ${allCerts.size} certificates, should be at least ${validationConfig.minVarmenneketjunKoko})"
      }

      // Validate first certificate's subject contains expected signer name
      allCertInfos.headOption match {
        case Some(firstCert) =>
          if (!firstCert.subject.contains(validationConfig.odotettuAllekirjoittajanNimi)) {
            validationErrors += s"First certificate's subject does not contain expected signer name '${validationConfig.odotettuAllekirjoittajanNimi}' (found: ${firstCert.subject})"
          }
        case None =>
          validationErrors += "No certificates found in PKCS#7"
      }

      signerInfos.foreach { signer =>
        if (!validationConfig.sallitutTiivistealgoritmit.contains(signer.digestAlgorithm)) {
          validationErrors += s"PKCS#7 uses a weak digest algorithm: ${signer.digestAlgorithm}"
        }
      }

      val (signatureValid, signatureValidationError) = signedContent match {
        case Some(content) if validationConfig.hashValidointi =>
          verifyCryptographicSignature(trimmedBytes, content) match {
            case Right(valid) => (Some(valid), None)
            case Left(error) =>
              validationErrors += error
              (Some(false), Some(error))
          }
        case _ =>
          (None, None)
      }

      PKCS7Info(
        signerCount = signers.size,
        signers = signerInfos,
        certificates = allCertInfos,
        signatureValid = signatureValid,
        signatureValidationError = signatureValidationError,
        isValid = validationErrors.isEmpty,
        validationErrors = validationErrors.toList
      )
    } match {
      case Success(info) =>
        Some(info)
      case Failure(e) =>
        logger.warn(s"PKCS#7 SignedData -rakenteen parserointi epäonnistui: ${e.getMessage}")
        None
    }
  }

  /**
   * Parsii ASN.1 DER-enkoodatun SEQUENCE-rakenteen todellisen pituuden.
   */
  private def parseASN1Length(bytes: Array[Byte]): Int = {
    if (bytes.length < 2) return -1

    // Tarkista että alkaa SEQUENCE tag:lla (0x30)
    if (bytes(0) != 0x30) {
      logger.warn(f"ASN.1 ei ala SEQUENCE tag:lla (0x30), vaan: ${bytes(0) & 0xFF}%02X")
      return -1
    }

    val lengthByte = bytes(1) & 0xFF

    if (lengthByte < 0x80) {
      // Lyhyt muoto: pituus on suoraan ensimmäisessä tavussa
      val contentLength = lengthByte
      val totalLength = 1 + 1 + contentLength  // tag + length-byte + content
      totalLength
    } else {
      // Pitkä muoto: pituustiedon tavujen määrä on 0x7F:n takana
      val numLengthBytes = lengthByte & 0x7F

      if (bytes.length < 2 + numLengthBytes) {
        logger.warn(f"ASN.1 length-tieto katkeaa: tarvitaan ${2 + numLengthBytes} tavua, saatavilla ${bytes.length}")
        return -1
      }

      // Lue pituus big-endian -muodossa
      var contentLength = 0
      for (i <- 0 until numLengthBytes) {
        contentLength = (contentLength << 8) | (bytes(2 + i) & 0xFF)
      }

      val totalLength = 1 + 1 + numLengthBytes + contentLength  // tag + length-indicator + length-bytes + content
      totalLength
    }
  }

  private def getSignedContent(pdfBytes: Array[Byte], byteRange: ByteRangeInfo): Array[Byte] = {
    val part1 = pdfBytes.slice(
      byteRange.signedRange1Start,
      byteRange.signedRange1Start + byteRange.signedRange1Length
    )
    val part2 = pdfBytes.slice(
      byteRange.signedRange2Start,
      byteRange.signedRange2Start + byteRange.signedRange2Length
    )
    part1 ++ part2
  }

  private def verifyCryptographicSignature(
    signatureBytes: Array[Byte],
    signedContent: Array[Byte]
  ): Either[String, Boolean] = Try {
    import org.bouncycastle.cms.{CMSProcessableByteArray, CMSSignedData}
    import org.bouncycastle.jce.provider.BouncyCastleProvider
    import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder

    val cms = new CMSSignedData(
      new CMSProcessableByteArray(signedContent),
      signatureBytes
    )

    val signers = cms.getSignerInfos.getSigners.asScala.toList

    if (signers.isEmpty) {
      throw new Exception("No signers found in signature")
    }

    signers.forall { signer =>
      val signerId = signer.getSID
      val certMatches = cms.getCertificates.getMatches(
        new org.bouncycastle.util.Selector[X509CertificateHolder] {
          override def `match`(cert: X509CertificateHolder): Boolean = {
            signerId.getSerialNumber == cert.getSerialNumber &&
              signerId.getIssuer.toString == cert.getIssuer.toString
          }
          override def clone(): Object = this
        }
      ).asScala.toList

      if (certMatches.isEmpty) {
        throw new Exception(s"No matching certificate found for signer with serial ${signerId.getSerialNumber}")
      }

      val cert = certMatches.head

      val verifier = new JcaSimpleSignerInfoVerifierBuilder()
        .setProvider(new BouncyCastleProvider())
        .build(cert)

      signer.verify(verifier)
    }
  }.toEither.left.map(ex => s"Signature verification failed: ${ex.getMessage}")

  private def analyzeDSS(document: PDDocument, pdfBytes: Array[Byte], byteRange: ByteRangeInfo): DSSInfo = {
    Using.Manager  { use =>
      val validationErrors = scala.collection.mutable.ListBuffer[String]()

      val catalog = document.getDocumentCatalog
      val catalogDict = catalog.getCOSObject

      val dssDictObj = catalogDict.getDictionaryObject(COSName.getPDFName("DSS"))

      if (dssDictObj == null) {
        validationErrors += "DSS structure not found in PDF"
        return DSSInfo.invalid(validationErrors.toList)
      }

      val dssDict = dssDictObj.asInstanceOf[COSDictionary]

      val ocspsObj = dssDict.getDictionaryObject(COSName.getPDFName("OCSPs"))
      val hasOCSPs = ocspsObj != null
      val (ocspCount, ocsps) = if (hasOCSPs) {
        val ocspArray = ocspsObj.asInstanceOf[COSArray]
        val ocspList = (0 until ocspArray.size()).flatMap { i =>
          Try {
            val ocspStream = ocspArray.getObject(i).asInstanceOf[COSStream]
            val inputStream = use(ocspStream.createInputStream())
            val ocspBytes = inputStream.readAllBytes()
            val ocspResp = new OCSPResp(ocspBytes)
            val basicResp = ocspResp.getResponseObject.asInstanceOf[BasicOCSPResp]

            val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val producedAt = dateFormat.format(basicResp.getProducedAt)

            OCSPInfo(producedAt)
          }.toOption
        }.toList
        (ocspArray.size(), ocspList)
      } else {
        (0, List.empty)
      }

      val crlsObj = dssDict.getDictionaryObject(COSName.getPDFName("CRLs"))
      val hasCRLs = crlsObj != null
      val (crlCount, crls) = if (hasCRLs) {
        val crlArray = crlsObj.asInstanceOf[COSArray]
        val crlList = (0 until crlArray.size()).flatMap { i =>
          Try {
            val crlStream = crlArray.getObject(i).asInstanceOf[COSStream]
            val inputStream = crlStream.createInputStream()
            val crlBytes = inputStream.readAllBytes()
            inputStream.close()
            val crlHolder = new X509CRLHolder(crlBytes)

            val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val issuer = crlHolder.getIssuer.toString
            val thisUpdate = dateFormat.format(crlHolder.getThisUpdate)
            val nextUpdate = Option(crlHolder.getNextUpdate)
              .map(dateFormat.format)
              .getOrElse("N/A")

            val revokedCertCount = crlHolder.getRevokedCertificates.size()

            CRLInfo(issuer, thisUpdate, nextUpdate, revokedCertCount)
          }.toOption
        }.toList
        (crlArray.size(), crlList)
      } else {
        (0, List.empty)
      }

      val vriObj = dssDict.getDictionaryObject(COSName.getPDFName("VRI"))
      val hasVRI = vriObj != null
      val (vriCount, vriKeys) = if (hasVRI) {
        val vriDict = vriObj.asInstanceOf[COSDictionary]
        val keys = vriDict.keySet().asScala.map(_.getName).toList
        (vriDict.size(), keys)
      } else {
        (0, List.empty[String])
      }

      val certsObj = dssDict.getDictionaryObject(COSName.getPDFName("Certs"))
      val hasCerts = certsObj != null
      val certsCount = if (hasCerts) {
        certsObj.asInstanceOf[COSArray].size()
      } else {
        0
      }

      if (!hasOCSPs && !hasCRLs) {
        validationErrors += "DSS does not contain OCSP or CRL data"
      }

      if (!hasVRI) {
        validationErrors += "DSS does not contain VRI structure"
      }

      val unsignedContent = pdfBytes.slice(byteRange.unsignedAfterSignatureStart, pdfBytes.length)
      val unsignedContentStr = new String(unsignedContent, StandardCharsets.ISO_8859_1)

      if (!unsignedContentStr.contains("/DSS")) {
        validationErrors += "Unsigned portion does not contain /DSS reference"
      }

      DSSInfo(
        hasOCSPs = hasOCSPs,
        ocspCount = ocspCount,
        ocsps = ocsps,
        hasCRLs = hasCRLs,
        crlCount = crlCount,
        crls = crls,
        hasVRI = hasVRI,
        vriCount = vriCount,
        vriKeys = vriKeys,
        hasCerts = hasCerts,
        certsCount = certsCount,
        isValid = validationErrors.isEmpty,
        validationErrors = validationErrors.toList
      )
    }.getOrElse(DSSInfo.invalid(List("Internal error generating DSS info")))
  }

  private def generateSummary(
    byteRange: ByteRangeInfo,
    signatureContents: SignatureContentsInfo,
    pkcs7: Option[PKCS7Info],
    dss: DSSInfo,
    overallValid: Boolean
  ): String = {
    val sb = new StringBuilder

    sb.append("=== PDF SIGNATURE ANALYSIS ===\n\n")

    // ByteRange-info
    sb.append(s"ByteRange: [${byteRange.signedRange1Start}, ${byteRange.signedRange1Length}, ${byteRange.signedRange2Start}, ${byteRange.signedRange2Length}]\n")
    sb.append(f"  Signed: ${byteRange.totalSignedLength}%,d bytes (${(byteRange.totalSignedLength.toDouble / byteRange.pdfTotalLength * 100)}%.1f%%)\n")
    sb.append(f"  Unsigned: ${byteRange.unsignedAfterSignatureLength}%,d bytes (${byteRange.unsignedPercentage}%.1f%%)\n")
    sb.append(f"  Signature: ${byteRange.signatureLength}%,d bytes\n")
    sb.append(f"  PDF total: ${byteRange.pdfTotalLength}%,d bytes\n\n")

    // Signature contents
    sb.append(s"Signature contents:\n")
    if (signatureContents.isValid) {
      sb.append("  ✓ Signature hex data is valid\n")
      sb.append(f"  Hex characters: ${signatureContents.hexContent.length}%,d (${signatureContents.pkcs7Bytes.length}%,d bytes binary)\n")
    } else {
      sb.append("  ✗ Signature hex data contains errors:\n")
      signatureContents.validationErrors.foreach { err =>
        sb.append(s"    - $err\n")
      }
    }
    sb.append("\n")

    // PKCS#7
    pkcs7 match {
      case Some(info) =>
        sb.append(s"PKCS#7 SignedData:\n")
        sb.append(s"  Signers: ${info.signerCount}\n")
        sb.append(s"  Certificates: ${info.certificates.size}\n")

        info.signers.zipWithIndex.foreach { case (signer, idx) =>
          sb.append(f"  Signer ${idx + 1}:\n")
          sb.append(s"    Digest algorithm: ${signer.digestAlgorithm}\n")
          sb.append(s"    Encryption algorithm: ${signer.encryptionAlgorithm}\n")
          if (signer.hasSignatureTimestamp) {
            signer.signatureTimestampTime.foreach { time =>
              sb.append(s"    Signature timestamp: $time\n")
            }
          }
        }

        // Show all certificates in chain with details
        sb.append(f"\n  Certificate chain (${info.certificates.size}):\n")
        info.certificates.zipWithIndex.foreach { case (cert, idx) =>
          val cn = cert.subject.split(",").find(_.trim.startsWith("CN=")).map(_.drop(3)).getOrElse("?")
          sb.append(f"\n  Certificate ${idx + 1}: $cn\n")
          sb.append(s"    Subject: ${cert.subject}\n")
          sb.append(s"    Issuer: ${cert.issuer}\n")
          sb.append(s"    Valid: ${cert.notBefore} - ${cert.notAfter}\n")
          sb.append(s"    Serial: ${cert.serialNumber}\n")
        }

        sb.append("\n  Cryptographic signature verification:\n")
        info.signatureValid match {
          case Some(true) =>
            sb.append("    ✓ Signature is cryptographically valid\n")
          case Some(false) =>
            sb.append("    ✗ Signature verification failed\n")
            info.signatureValidationError.foreach { err =>
              sb.append(s"       Error: $err\n")
            }
          case None =>
            sb.append("    - Not verified (signed content not available)\n")
        }

        if (!info.isValid) {
          sb.append("  ✗ PKCS#7 validation errors:\n")
          info.validationErrors.foreach { err =>
            sb.append(s"    - $err\n")
          }
        }
        sb.append("\n")

      case None =>
        sb.append("PKCS#7 SignedData: Could not parse\n\n")
    }

    // DSS
    sb.append(s"DSS (Document Security Store):\n")
    if (dss.hasOCSPs) {
      sb.append(f"  ✓ OCSP responses: ${dss.ocspCount}\n")
      dss.ocsps.zipWithIndex.foreach { case (ocsp, idx) =>
        sb.append(f"    OCSP ${idx + 1}:\n")
        sb.append(f"      Produced at: ${ocsp.producedAt}\n")
      }
    } else {
      sb.append("  - No OCSP responses\n")
    }

    if (dss.hasCRLs) {
      sb.append(f"  ✓ CRLs: ${dss.crlCount}\n")
      dss.crls.zipWithIndex.foreach { case (crl, idx) =>
        sb.append(f"    CRL ${idx + 1}:\n")
        sb.append(f"      Issuer: ${crl.issuer}\n")
        sb.append(f"      This update: ${crl.thisUpdate}\n")
        sb.append(f"      Next update: ${crl.nextUpdate}\n")
        sb.append(f"      Revoked certificates: ${crl.revokedCertCount}\n")
      }
    } else {
      sb.append("  - No CRLs\n")
    }

    if (dss.hasVRI) {
      sb.append(f"  ✓ VRI entries: ${dss.vriCount}\n")
      if (dss.vriKeys.nonEmpty) {
        sb.append("    VRI keys (signature hashes):\n")
        dss.vriKeys.foreach { key =>
          // Show first 16 characters of hash for readability
          val displayKey = if (key.length > 16) key.take(16) + "..." else key
          sb.append(s"      - $displayKey\n")
        }
      }
    } else {
      sb.append("  - No VRI entries\n")
    }

    if (dss.hasCerts) {
      sb.append(f"  ✓ Certificates in DSS: ${dss.certsCount}\n")
    } else {
      sb.append("  - No certificates in DSS\n")
    }

    if (!dss.isValid) {
      sb.append("  ✗ DSS validation errors:\n")
      dss.validationErrors.foreach { err =>
        sb.append(s"    - $err\n")
      }
    }
    sb.append("\n")

    // Summary
    sb.append("=== SUMMARY ===\n")
    if (overallValid) {
      sb.append("✓ PDF signature and structure appear valid\n")
    } else {
      sb.append("✗ PDF signature or structure has issues\n")
    }

    sb.toString()
  }
}
