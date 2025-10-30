package fi.oph.koski.todistus.swisscomclient

object SwisscomAISSignRequest {
  def apply(id: String, base64HashToSign: String, config: SwisscomConfig): SwisscomAISSignRequest = {
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

    SwisscomAISSignRequest(
      SignRequest(
        `@RequestID` = id,
        InputDocuments = inputDocuments,
        OptionalInputs = optionalInputs
      )
    )
  }
}

case class SwisscomAISSignRequest(
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
