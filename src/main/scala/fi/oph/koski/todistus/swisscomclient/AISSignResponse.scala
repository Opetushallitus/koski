package fi.oph.koski.todistus.swisscomclient

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
  `sc.RevocationInformation`: Option[ScRevocationInformation]
)

case class ScRevocationInformation(
  `sc.CRLs`: Option[ScCRLs],
  `sc.OCSPs`: Option[ScOCSPs]
)

case class ScCRLs(
  `sc.CRL`: Option[List[String]]
)

case class ScOCSPs(
  `sc.OCSP`: Option[List[String]]
)

case class SignatureObject(
  Base64Signature: Option[Base64Signature],
  Timestamp: Option[Timestamp],
  Other: Option[Other]
)

case class Base64Signature(
  `$`: Option[String],
  `@Type`: Option[String]
)

case class Timestamp(
  RFC3161TimeStampToken: Option[String]
)

case class Other(
  `sc.SignatureObjects`: Option[ScSignatureObjects]
)

case class ScSignatureObjects(
  `sc.ExtendedSignatureObject`: Option[List[ScExtendedSignatureObject]]
)

case class ScExtendedSignatureObject(
  `@WhichDocument`: Option[String],
  Base64Signature: Option[Base64Signature__1],
  Timestamp: Option[Timestamp__1]
)

case class Base64Signature__1(
  `@Type`: Option[String],
  `$`: Option[String]
)

case class Timestamp__1(
  RFC3161TimeStampToken: Option[String]
)
