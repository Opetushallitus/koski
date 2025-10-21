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
