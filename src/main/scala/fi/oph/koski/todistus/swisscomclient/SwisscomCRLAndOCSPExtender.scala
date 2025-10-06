package fi.oph.koski.todistus.swisscomclient

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.log.Logging
import org.apache.pdfbox.cos.{COSArray, COSBase, COSDictionary, COSName, COSStream, COSUpdateInfo}
import org.apache.pdfbox.pdmodel.{PDDocument, PDDocumentCatalog}
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature
import org.bouncycastle.asn1.{ASN1EncodableVector, ASN1Enumerated, ASN1InputStream, DEROctetString, DERSequence, DERTaggedObject}
import org.bouncycastle.asn1.ocsp.OCSPObjectIdentifiers
import org.bouncycastle.cert.ocsp.{BasicOCSPResp, OCSPResp}

import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.cert.{CertificateFactory, X509CRL}

import scala.collection.JavaConverters._

import fi.oph.koski.util.ChainingSyntax._

object SwisscomCRLAndOCSPExtender extends Logging {

  def extendPdfWithCrlAndOcsp(
    pdDocument: PDDocument,
    documentBytes: Array[Byte],
    crlEntries: List[Array[Byte]],
    ocspEntries: List[Array[Byte]]
  ): Either[HttpStatus, Unit] = {
    try {
      val pdDocumentCatalog: PDDocumentCatalog = pdDocument.getDocumentCatalog
      val cosDocumentCatalog: COSDictionary = pdDocumentCatalog.getCOSObject
      cosDocumentCatalog.setNeedToBeUpdated(true)

      for {
        _ <- addExtensions(pdDocumentCatalog)
        validationDataCrls <- getCrlEncodedForm(crlEntries)
        encodedOcspEntries <- getOcspEncodedForm(ocspEntries)
        lastSignature <- getLastRelevantSignature(pdDocument)
        validationDataOcsps <- HttpStatus.foldEithers(encodedOcspEntries.map(buildOCSPResponse))
        validationDataKey <- getSignatureHashKey(lastSignature, documentBytes).map(COSName.getPDFName)

        pdDssDict <- getOrCreateDictionaryEntry(classOf[COSDictionary], cosDocumentCatalog, COSName.getPDFName("DSS"))
        pdVriMapDict <- getOrCreateDictionaryEntry(classOf[COSDictionary], pdDssDict, COSName.getPDFName("VRI"))
        ocsps <- getOrCreateDictionaryEntry(classOf[COSArray], pdDssDict, COSName.getPDFName("OCSPs"))
        crls <- getOrCreateDictionaryEntry(classOf[COSArray], pdDssDict, COSName.getPDFName("CRLs"))
        certs <- getOrCreateDictionaryEntry(classOf[COSArray], pdDssDict, COSName.getPDFName("Certs"))

        vriDict = new COSDictionary
        vriOcsps = new COSArray
        vriCrls = new COSArray

        ocspsStreams <- HttpStatus.foldEithers(validationDataOcsps.map(o => createStream(pdDocument, o)))
          .tap(_.foreach(s => {
            ocsps.add(s)
            vriOcsps.add(s)
          }))
          .tap(ocspsStreams => if (ocspsStreams.nonEmpty) {
            vriDict.setItem(COSName.getPDFName("OCSP"), vriOcsps)
          })

        crlsStreams <- HttpStatus.foldEithers(validationDataCrls.map(o => createStream(pdDocument, o)))
          .tap(_.foreach(s => {
            crls.add(s)
            vriCrls.add(s)
          }))
          .tap(crlsStreams => if (crlsStreams.nonEmpty) {
            vriDict.setItem(COSName.getPDFName("CRL"), vriCrls)
          })

        _ = pdVriMapDict.setItem(validationDataKey, vriDict)

        _ = if (ocsps.size() > 0) {
          pdDssDict.setItem(COSName.getPDFName("OCSPs"), ocsps)
        } else {
          pdDssDict.removeItem(COSName.getPDFName("OCSPs"))
        }

        _ = if (crls.size() > 0) {
          pdDssDict.setItem(COSName.getPDFName("CRLs"), crls)
        } else {
          pdDssDict.removeItem(COSName.getPDFName("CRLs"))
        }

        _ = if (certs.size() > 0) {
          pdDssDict.setItem(COSName.getPDFName("Certs"), certs)
        } else {
          pdDssDict.removeItem(COSName.getPDFName("Certs"))
        }

        _ = pdDssDict.setItem(COSName.getPDFName("VRI"), pdVriMapDict)
        _ = cosDocumentCatalog.setItem(COSName.getPDFName("DSS"), pdDssDict)
      } yield Unit
    } catch {
      case e: Exception =>
        logger.warn(e)("Failed to extend PDF with CRLs and OCSPs")
        Left(KoskiErrorCategory.internalError("Failed to extend PDF with CRLs and OCSPs: " + e.toString))
    }
  }

  private def addExtensions(catalog: PDDocumentCatalog): Either[HttpStatus, Unit] = {
    try {
      val dssExtensions = new COSDictionary
      dssExtensions.setDirect(true)
      catalog.getCOSObject.setItem("Extensions", dssExtensions)
      val adbeExtension = new COSDictionary
      adbeExtension.setDirect(true)
      dssExtensions.setItem("ADBE", adbeExtension)
      adbeExtension.setName("BaseVersion", "1.7")
      adbeExtension.setInt("ExtensionLevel", 5)
      catalog.setVersion("1.7")
      Right(Unit)
    } catch {
      case e: Exception =>
        logger.warn(e)("Failed to add extensions")
        Left(KoskiErrorCategory.internalError("Failed to add extensions: " + e.toString))
    }
  }

  private def getCrlEncodedForm(crlEntries: List[Array[Byte]]): Either[HttpStatus, Seq[Array[Byte]]] = {
    HttpStatus.foldEithers(
      crlEntries.map(crl => {
        try {
          val x509crl: X509CRL = CertificateFactory.getInstance("X.509").generateCRL(new ByteArrayInputStream(crl)).asInstanceOf[X509CRL]
          Right(x509crl.getEncoded)
        } catch {
          case e: Exception =>
            logger.warn(e)("Failed to generate X509CRL from CRL content received from AIS")
            Left(KoskiErrorCategory.internalError("Failed to generate X509CRL from CRL content received from AIS: " + e.toString))
        }
      })
    )
  }

  private def getOcspEncodedForm(ocspEntries: List[Array[Byte]]): Either[HttpStatus, Seq[Array[Byte]]] = {
    HttpStatus.foldEithers(
      ocspEntries.map(ocsp => {
        try {
          val ocspResp = new OCSPResp(new ByteArrayInputStream(ocsp))
          val basicResp = ocspResp.getResponseObject.asInstanceOf[BasicOCSPResp]
          Right(basicResp.getEncoded) // Add Basic OCSP Response to Collection (ASN.1 encoded representation of this object)
        } catch {
          case e: Exception =>
            logger.warn(e)("Failed to generate X509CRL from CRL content received from AIS")
            Left(KoskiErrorCategory.internalError("Failed to generate X509CRL from OCSP content received from AIS: " + e.toString))
        }
      })
    )
  }

  private def getLastRelevantSignature(document: PDDocument): Either[HttpStatus, PDSignature] = {
    try {
      document.getSignatureDictionaries.asScala.map(signature => {
          val sigOffset = signature.getByteRange()(1)
          (sigOffset, signature)
        })
        .sortBy(_._1)
        .lastOption
        .map(_._2)
        .filter(s => {
          val t = s.getCOSObject.getItem(COSName.TYPE)
          t.equals(COSName.SIG) || t.equals(COSName.DOC_TIME_STAMP)
        }).toRight({
          logger.warn("Cannot extend PDF with CRL and OCSP data. No signature was found in the PDF")
          KoskiErrorCategory.internalError("Cannot extend PDF with CRL and OCSP data. No signature was found in the PDF")
        })
    } catch {
      case e: Exception =>
        logger.warn(e)("Failed to get last relevant signature content received from AIS")
        Left(KoskiErrorCategory.internalError("Failed to get last relevant signature content received from AIS: " + e.toString))
    }
  }

  private def buildOCSPResponse(content: Array[Byte]): Either[HttpStatus, Array[Byte]] = {
    try {
      val derOctet = new DEROctetString(content)
      val v2 = new ASN1EncodableVector
      v2.add(OCSPObjectIdentifiers.id_pkix_ocsp_basic)
      v2.add(derOctet)
      val den = new ASN1Enumerated(0)
      val v3 = new ASN1EncodableVector
      v3.add(den)
      v3.add(new DERTaggedObject(true, 0, new DERSequence(v2)))
      val seq = new DERSequence(v3)
      Right(seq.getEncoded)
    } catch {
      case e: Exception =>
        logger.warn(e)("Failed to build OCSP response")
        Left(KoskiErrorCategory.internalError("Failed to build OCSP response: " + e.toString))
    }
  }

  private def getSignatureHashKey(signature: PDSignature, documentBytes: Array[Byte]): Either[HttpStatus, String] = {
    try {
      var contentToConvert = signature.getContents(documentBytes)
      if (signature.getSubFilter == "urn:ietf:rfc:3161") {
        val din = new ASN1InputStream(new ByteArrayInputStream(contentToConvert))
        val pkcs = din.readObject
        contentToConvert = pkcs.getEncoded
      }
      Right(convertToHexString(hashBytesWithSha1(contentToConvert)))
    } catch {
      case e: Exception =>
        logger.warn(e)("Failed to get signature hash key")
        Left(KoskiErrorCategory.internalError("Failed to get signature hash key: " + e.toString))
    }
  }

  private def getOrCreateDictionaryEntry[T <: COSBase with COSUpdateInfo](clazz: Class[T], parent: COSDictionary, name: COSName): Either[HttpStatus, T] = {
    val element = parent.getDictionaryObject(name)
    if (clazz.isInstance(element)) {
      val result = clazz.cast(element)
      result.setNeedToBeUpdated(true)
      Right(result)
    }
    else if (element != null) {
      val msg = "Element " + name + " from dictionary is not of type " + clazz.getCanonicalName

      logger.warn(msg)
      Left(KoskiErrorCategory.internalError(msg))
    }
    else {
      try {
        val result = clazz.getDeclaredConstructor().newInstance()
        result.setDirect(false)
        parent.setItem(name, result)
        Right(result)
      }
      catch {
        case e: Exception => {
          val msg = "Failed to create new instance of " + clazz.getCanonicalName
          logger.warn(e)(msg)
          Left(KoskiErrorCategory.internalError(msg))
        }
      }
    }
  }

  private def createStream(pdDocument: PDDocument, data: Array[Byte]): Either[HttpStatus, COSStream] = {
    try {
      val stream = pdDocument.getDocument.createCOSStream

      val unfilteredStream = stream.createOutputStream(COSName.FLATE_DECODE)
      try {
        unfilteredStream.write(data)
      }
      finally {
        if (unfilteredStream != null) unfilteredStream.close()
      }
      Right(stream)
    } catch {
      case e: Exception =>
        logger.warn(e)("Failed to create stream")
        Left(KoskiErrorCategory.internalError("Failed to create stream"))
    }
  }

  private val HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII)

  private def convertToHexString(bytes: Array[Byte]): String = {
    val hexChars = new Array[Byte](bytes.length * 2)
    for (j <- bytes.indices) {
      val v = bytes(j) & 0xFF
      hexChars(j * 2) = HEX_ARRAY(v >>> 4)
      hexChars(j * 2 + 1) = HEX_ARRAY(v & 0x0F)
    }
    new String(hexChars, StandardCharsets.UTF_8)
  }

  private def hashBytesWithSha1(b: Array[Byte]): Array[Byte] = {
    val sh = MessageDigest.getInstance("SHA1")
    sh.digest(b)
  }
}
