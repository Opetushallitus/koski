package fi.oph.koski.todistus.swisscomclient

case class AISSignRequest(
  SignRequest: SignRequest
)

case class SignRequest(
  `@Profile`: String = "http://ais.swisscom.ch/1.1",
  `@RequestID`: String,
  InputDocuments: InputDocuments,
  OptionalInputs: OptionalInputs
)

case class InputDocuments(
  DocumentHash: List[DocumentHash]
)

case class DocumentHash(
  `@ID`: String,
  `dsig.DigestMethod`: DsigDigestMethod,
  `dsig.DigestValue`: String
)


case class DsigDigestMethod(
  `@Algorithm`: String
)

case class OptionalInputs(
  AddTimestamp: AddTimestamp,
  ClaimedIdentity: ClaimedIdentity,
  SignatureType: String,
  `sc.AddRevocationInformation`: ScAddRevocationInformation,
  `sc.SignatureStandard`: String
)

case class AddTimestamp(
  `@Type`: String = "urn:ietf:rfc:3161"
)

case class ClaimedIdentity(
  Name: String
)

case class ScAddRevocationInformation(
  `@Type`: String
)

case class AISSignResponse(
  SignResponse: SignResponse
)

case class SignResponse(
  `@RequestID`: String,
  `@Profile`: String,
  Result: Result,
  OptionalOutputs: Option[OptionalOutputs],
  SignatureObject: Option[SignatureObject]
)

case class Result(
  ResultMajor: Option[String],
  ResultMinor: Option[String],
  ResultMessage: Option[ResultMessage]
)

case class ResultMessage(
  `@xml.lang`: Option[String],
  `$`: Option[String]
)

case class OptionalOutputs(
  `async.ResponseID`: Option[String],
  `sc.APTransID`: Option[String],
  `sc.RevocationInformation`: ScRevocationInformation
)

case class ScRevocationInformation(
  `sc.CRLs`: ScCRLs,
  `sc.OCSPs`: ScOCSPs
)

case class ScCRLs(
  `sc.CRL`: List[String]
)

case class ScOCSPs(
  `sc.OCSP`: List[String]
)

case class SignatureObject(
  Base64Signature: Base64Signature,
  Timestamp: Timestamp,
  Other: Other
)

case class Base64Signature(
  `$`: String,
  `@Type`: String
)

case class Timestamp(
  RFC3161TimeStampToken: String
)

case class Other(
  `sc.SignatureObjects`: ScSignatureObjects,
)

case class ScSignatureObjects(
  `sc.ExtendedSignatureObject`: List[ScExtendedSignatureObject]
)

case class ScExtendedSignatureObject(
  `@WhichDocument`: String,
  Base64Signature: Base64Signature__1,
  Timestamp: Timestamp__1
)

case class Base64Signature__1(
  `@Type`: String,
  `$`: String
)

case class Timestamp__1(
  RFC3161TimeStampToken: String
)
