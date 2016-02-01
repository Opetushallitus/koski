package fi.oph.tor.servlet

import fi.oph.tor.http.ErrorDetail
import fi.oph.tor.http.TorErrorCode.TorErrorCode

case class InvalidRequestException(errorDetail: ErrorDetail) extends Exception(errorDetail.toString) {
  def this(errorCode: TorErrorCode, message: String) = this(ErrorDetail(errorCode, message))
}
