package fi.oph.koski.todistus.swisscomclient

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.scalaschema.annotation.DeserializeSingleValueAsArray

case class SwisscomAISSignResponse(
  SignResponse: SignResponse
) {
  def isSuccess: Boolean = SignResponse.Result.isSuccess
}

case class SignResponse(
  `@RequestID`: String,
  `@Profile`: String,
  Result: SignResponseResult,
  OptionalOutputs: Option[OptionalOutputs] = None,
  SignatureObject: Option[SignatureObject] = None
)

case class SignResponseResult(
  ResultMajor: Option[String] = None,
  ResultMinor: Option[String] = None,
  ResultMessage: Option[ResultMessage] = None
) {
  def isSuccess: Boolean = ResultMajor == Some("urn:oasis:names:tc:dss:1.0:resultmajor:Success")

  def toHttpStatus: HttpStatus =
    if (isSuccess) {
      HttpStatus.ok
    } else {
      KoskiErrorCategory
        .internalError(s"AIS signing error response: ${ResultMajor.getOrElse("")} ${ResultMinor.getOrElse("")} ${ResultMessage.getOrElse("")}")
    }
}

case class ResultMessage(
  `@xml.lang`: Option[String] = None,
  `$`: Option[String] = None
)

case class OptionalOutputs(
  `async.ResponseID`: Option[String] = None,
  `sc.APTransID`: Option[String] = None,
  `sc.RevocationInformation`: Option[ScRevocationInformation] = None
)

case class ScRevocationInformation(
  `sc.CRLs`: Option[ScCRLs] = None,
  `sc.OCSPs`: Option[ScOCSPs] = None
)

case class ScCRLs(
  @DeserializeSingleValueAsArray
  `sc.CRL`: List[String] = List.empty
)

case class ScOCSPs(
   @DeserializeSingleValueAsArray
  `sc.OCSP`: List[String] = List.empty
)

case class SignatureObject(
  Base64Signature: Option[Base64Signature] = None,
  Timestamp: Option[Timestamp] = None,
  Other: Option[Other] = None
)

case class Base64Signature(
  `$`: Option[String] = None,
  `@Type`: Option[String] = None
)

case class Timestamp(
  RFC3161TimeStampToken: Option[String] = None
)

case class Other(
  `sc.SignatureObjects`: Option[ScSignatureObjects] = None
)

case class ScSignatureObjects(
  `sc.ExtendedSignatureObject`: Option[List[ScExtendedSignatureObject]] = None
)

case class ScExtendedSignatureObject(
  `@WhichDocument`: Option[String] = None,
  Base64Signature: Option[Base64Signature__1] = None,
  Timestamp: Option[Timestamp__1] = None
)

case class Base64Signature__1(
  `@Type`: Option[String] = None,
  `$`: Option[String] = None
)

case class Timestamp__1(
  RFC3161TimeStampToken: Option[String] = None
)
