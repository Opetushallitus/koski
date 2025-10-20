package fi.oph.koski.todistus.swisscomclient

object SignatureResult extends Enumeration {
  type SignatureResult = Value
  val

  /**
   * The signature finished successfully. The signatures are already embedded in the PDF documents.
   */
  SUCCESS,

  /**
   * The user cancelled the signature.
   */
  USER_CANCEL,

  /**
   * The user did not respond in a timely manner.
   */
  USER_TIMEOUT,

  /**
   * The provided user serial number (part of the StepUp process) does not match the one on the server side.
   */
  SERIAL_NUMBER_MISMATCH,

  /**
   * The user failed to properly authenticate for the signature.
   */
  USER_AUTHENTICATION_FAILED,

  /**
   * The request is missing the required MSISDN parameter. This can happen sometimes in the context of the on-demand flow,
   * depending on the user's server configuration (e.g. the enforceStepUpAuthentication flag is true). As an alternative,
   * the on-demand with step-up flow can be used instead.
   */
  INSUFFICIENT_DATA_WITH_ABSENT_MSISDN = Value
}
